package org.analyzer.service.logs.std;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import org.analyzer.dao.LogsStatisticsRepository;
import org.analyzer.dao.LogsStorage;
import org.analyzer.entities.LogRecordEntity;
import org.analyzer.entities.LogsStatisticsEntity;
import org.analyzer.entities.UserEntity;
import org.analyzer.service.logs.*;
import org.analyzer.service.logs.std.postfilters.PostFiltersSequenceBuilder;
import org.analyzer.service.queries.UserQueriesService;
import org.analyzer.service.users.CurrentUserAccessor;
import org.analyzer.service.util.JsonConverter;
import org.analyzer.service.util.LongRunningTaskExecutor;
import org.analyzer.service.util.UnzipperUtil;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@Service
public class StandardLogsService implements LogsService {

    private static final String STATISTICS_CACHE = "statistics";

    @Autowired
    private LogsStorage logsStorage;
    @Autowired
    private LogRecordsParser parser;
    @Autowired
    private UnzipperUtil zipUtil;
    @Autowired
    private PostFiltersSequenceBuilder postFiltersSequenceBuilder;
    @Autowired
    private LogsAnalyzer logsAnalyzer;
    @Autowired
    private MeterRegistry meterRegistry;
    @Autowired
    private LogsStatisticsRepository statisticsRepository;
    @Autowired
    private LongRunningTaskExecutor taskExecutor;
    @Autowired
    private CurrentUserAccessor userAccessor;
    @Autowired
    private LogKeysFactory logKeysFactory;
    @Autowired
    private JsonConverter jsonConverter;
    @Autowired
    private UserQueriesService currentUserQueryService;
    @Autowired
    private LogRecordsIndexer logRecordsIndexer;

    private Counter indexedFilesCounter;
    private Counter simpleSearchRequestsCounter;
    private Counter extendedSearchRequestsCounter;
    private Counter logsAnalyzeCounter;

    @PostConstruct
    private void init() {
        this.indexedFilesCounter = createMeterCounter("logs.indexed.files", "All indexed logs files count", null);
        this.simpleSearchRequestsCounter = createMeterCounter("logs.simple.search.requests", "All simple search requests count", "simple");
        this.extendedSearchRequestsCounter = createMeterCounter("logs.extended.search.requests", "All extended search requests count", "extended");
        this.logsAnalyzeCounter = createMeterCounter("logs.analyze.requests", "All logs analyze requests count", null);
    }

    @Override
    @NonNull
    public CompletableFuture<String> index(
            @NonNull File logFile,
            @Nullable LogRecordFormat recordFormat) {

        final var uuidKey = UUID.randomUUID().toString();
        final var userEntity = this.userAccessor.get();
        return this.taskExecutor.execute(
                () -> this.zipUtil.flat(logFile)
                                    .forEach(file -> processLogFile(userEntity, uuidKey, recordFormat, file))
                )
                .thenApply(v -> uuidKey)
                .whenComplete((result, ex) -> {
            if (ex != null) {
                final var userIndexingKey = this.logKeysFactory.createUserIndexingKey(userEntity.getHash(), uuidKey);
                this.logsStorage.deleteAllByIdRegex(userIndexingKey);
            }
        });
    }

    @Nonnull
    @Override
    public List<String> searchByQuery(@Nonnull SearchQuery searchQuery) {
        final var user = this.userAccessor.get();
        this.taskExecutor.execute(
                () -> {
                    try (final var userContext = this.userAccessor.as(user)) {
                        this.currentUserQueryService.create(searchQuery);
                    }
                }
        );

        return searchByFilterQuery(searchQuery)
                .stream()
                .map(LogRecordEntity::getSource)
                .toList();
    }

    @NonNull
    @Override
    public File searchAndExportByQuery(@NonNull SearchQuery query) {
        try {
            SearchQuery pageQuery = query;
            List<String> records;
            final File logsFile = File.createTempFile(UUID.randomUUID().toString(), null);
            logsFile.deleteOnExit();

            while (!(records = this.searchByQuery(pageQuery)).isEmpty()) {
                FileUtils.writeLines(logsFile, StandardCharsets.UTF_8.displayName(), records, true);
                pageQuery = pageQuery.toNextPageQuery();
            }

            return logsFile;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @NonNull
    @Override
    public MapLogsStatistics analyze(@NonNull AnalyzeQuery analyzeQuery) {

        MapLogsStatistics stats = null;
        var pageNumber = 0;
        SearchQuery searchQuery = analyzeQuery;
        do {
            final var recordsToAnalyzePart = searchByFilterQuery(searchQuery);

            final var partStats = this.logsAnalyzer.analyze(recordsToAnalyzePart, analyzeQuery);
            stats = stats == null ? partStats : stats.joinWith(partStats);

            searchQuery = searchQuery.toNextPageQuery();
            pageNumber = recordsToAnalyzePart.isEmpty() ? -1 : pageNumber + 1;
        } while (pageNumber != -1);

        this.logsAnalyzer.applyFinalQueryLimitations(stats, analyzeQuery);

        this.logsAnalyzeCounter.increment();

        processStatsSaving(analyzeQuery, stats, this.userAccessor.get().getHash());
        return stats;
    }

    @NonNull
    @Override
    @Cacheable(value = STATISTICS_CACHE, key = "#root.args[0]")
    public Optional<LogsStatisticsEntity> findStatisticsByKey(@NonNull String key) {
        return this.statisticsRepository.findByDataQueryRegexOrId(key, key);
    }

    @NonNull
    @Override
    public List<LogsStatisticsEntity> findAllStatisticsByCreationDate(@NonNull LocalDateTime beforeDate) {
        final var user = this.userAccessor.get();
        return this.statisticsRepository.findAllByUserKeyAndCreationDateBefore(user.getHash(), beforeDate);
    }

    @Override
    @CacheEvict(allEntries = true)
    public void deleteStatistics(@NonNull List<LogsStatisticsEntity> stats) {
        this.statisticsRepository.deleteAll(stats);
    }

    @NonNull
    @Override
    @CacheEvict(value = STATISTICS_CACHE, allEntries = true)
    public List<String> deleteAllStatisticsByCreationDate(@NonNull LocalDateTime beforeDate) {
        final var user = this.userAccessor.get();
        return this.statisticsRepository.deleteAllByUserKeyAndCreationDateBefore(user.getHash(), beforeDate)
                                        .stream()
                                        .map(LogsStatisticsEntity::getId)
                                        .toList();
    }

    @Override
    public void deleteByQuery(@NonNull SearchQuery deleteQuery) {
        final var user = this.userAccessor.get();
        this.logsStorage.deleteByQuery(new LogsStorage.StorageQuery(deleteQuery, user.getHash()));
    }

    private void processStatsSaving(
            final AnalyzeQuery analyzeQuery,
            final MapLogsStatistics stats,
            final String userKey) {

        if (analyzeQuery.save()) {
            saveStatsEntity(analyzeQuery, new HashMap<>(stats), userKey);
        }
    }

    private void saveStatsEntity(
            final AnalyzeQuery analyzeQuery,
            final Map<String, Object> stats,
            final String userKey) {

        final var entity =
                new LogsStatisticsEntity()
                        .setId(analyzeQuery.getId())
                        .setCreated(LocalDateTime.now())
                        .setTitle(analyzeQuery.analyzeResultName())
                        .setDataQuery(analyzeQuery.toSearchQuery(0).toJson(this.jsonConverter))
                        .setUserKey(userKey)
                        .setStats(stats);
        this.statisticsRepository.save(entity);
    }

    private List<LogRecordEntity> searchByFilterQuery(@Nonnull SearchQuery searchQuery) {

        final var user = this.userAccessor.get();
        (searchQuery.extendedFormat() ? extendedSearchRequestsCounter : simpleSearchRequestsCounter).increment();

        final var postFilters = this.postFiltersSequenceBuilder.build(searchQuery.postFilters());

        final var storageQuery = new LogsStorage.StorageQuery(searchQuery, user.getHash());
        final var logRecords = this.logsStorage.searchByQuery(storageQuery);

        return postFilters
                .stream()
                .reduce(Function.<List<LogRecordEntity>> identity(), Function::andThen, (pf1, pf2) -> pf2)
                .apply(logRecords);
    }

    private Counter createMeterCounter(
            final String metricName,
            final String description,
            final String type) {
        final var builder = Counter.builder(metricName)
                                    .description(description);
        if (type != null) {
            builder.tag("type", type);
        }

        return builder.register(this.meterRegistry);
    }

    private void processLogFile(final UserEntity user, final String indexingKey, final LogRecordFormat recordFormat, final File file) {

        final var userIndexingKey = this.logKeysFactory.createUserIndexingKey(user.getHash(), indexingKey);
        this.indexedFilesCounter.increment();

        try (final var userContext = this.userAccessor.as(user);
             final var packageIterator = this.parser.parse(this.logKeysFactory.createIndexedLogFileKey(userIndexingKey, file.getName()), file, recordFormat)) {

            final List<CompletableFuture<Void>> indexingFuture = new ArrayList<>();
            while (packageIterator.hasNext()) {
                final var recordsPackage = packageIterator.next();
                indexingFuture.add(this.logRecordsIndexer.index(recordsPackage));
            }

            CompletableFuture
                    .allOf(indexingFuture.toArray(new CompletableFuture[0]))
                    .join();

            this.logsStorage.flush();

            final var analyzeQuery = new AnalyzeQueryOnIndexWrapper(indexingKey);
            analyze(analyzeQuery);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
