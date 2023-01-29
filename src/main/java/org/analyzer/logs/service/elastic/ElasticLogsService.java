package org.analyzer.logs.service.elastic;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import org.analyzer.logs.dao.LogRecordRepository;
import org.analyzer.logs.dao.LogsStatisticsRepository;
import org.analyzer.logs.model.LogRecordEntity;
import org.analyzer.logs.model.LogsStatisticsEntity;
import org.analyzer.logs.model.UserEntity;
import org.analyzer.logs.service.*;
import org.analyzer.logs.service.std.DefaultLogsAnalyzer;
import org.analyzer.logs.service.std.postfilters.PostFiltersSequenceBuilder;
import org.analyzer.logs.service.util.JsonConverter;
import org.analyzer.logs.service.util.LongRunningTaskExecutor;
import org.analyzer.logs.service.util.ZipUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.function.Tuple2;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
public class ElasticLogsService implements LogsService {

    private static final Logger logger = Loggers.getLogger(ElasticLogsService.class);

    @Autowired
    private ReactiveElasticsearchTemplate template;
    @Autowired
    private LogRecordRepository logRecordRepository;
    @Autowired
    private LogRecordsParser parser;
    @Autowired
    private ZipUtil zipUtil;
    @Autowired
    private SearchQueryParser<StringQuery> queryParser;
    @Autowired
    private PostFiltersSequenceBuilder postFiltersSequenceBuilder;
    @Autowired
    private DefaultLogsAnalyzer logsAnalyzer;
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

    @Value("${elasticsearch.default.indexing.buffer_size:2500}")
    private int elasticIndexBufferSize;

    private Counter indexedRecordsCounter;
    private Counter indexedFilesCounter;
    private Counter simpleSearchRequestsCounter;
    private Counter extendedSearchRequestsCounter;
    private Counter logsAnalyzeCounter;
    private Counter elasticIndexRequestsCounter;

    @PostConstruct
    private void init() {
        this.indexedRecordsCounter = createMeterCounter("logs.indexed.records", "All logs indexed records count", null);
        this.indexedFilesCounter = createMeterCounter("logs.indexed.files", "All indexed logs files count", null);
        this.simpleSearchRequestsCounter = createMeterCounter("logs.simple.search.requests", "All simple search requests count", "simple");
        this.extendedSearchRequestsCounter = createMeterCounter("logs.extended.search.requests", "All extended search requests count", "extended");
        this.logsAnalyzeCounter = createMeterCounter("logs.analyze.requests", "All logs analyze requests count", null);
        this.elasticIndexRequestsCounter = createMeterCounter("logs.index.requests", "All logs index requests to elastic count", null);
    }

    @Override
    @NonNull
    public Mono<String> index(
            @NonNull Mono<File> logFile,
            @Nullable LogRecordFormat recordFormat) {

        final var uuidKey = Mono.fromSupplier(UUID.randomUUID()::toString);
        final var userEntity = this.userAccessor.get();
        return userEntity
                .map(UserEntity::getHash)
                .zipWith(uuidKey)
                .flatMap(indexingKey ->
                        this.zipUtil
                                .flat(logFile)
                                .log(logger)
                                .doOnNext(file -> this.indexedFilesCounter.increment())
                                .parallel()
                                .runOn(Schedulers.parallel())
                                .map(file -> this.parser.parse(this.logKeysFactory.createIndexedLogFileKey(file.getName(), indexingKey.getT1(), indexingKey.getT2()), file, recordFormat))
                                .flatMap(records -> records
                                                        .cache()
                                                        .transform(recordsFlux -> sendToAnalyzeLogs(recordsFlux, indexingKey))
                                                        .buffer(this.elasticIndexBufferSize)
                                                        .doOnNext(buffer -> this.indexedRecordsCounter.increment(buffer.size()))
                                                        .doOnNext(buffer -> this.elasticIndexRequestsCounter.increment())
                                                        .map(this.logRecordRepository::saveAll)
                                                        .log(logger)
                                )
                                .flatMap(Flux::then)
                                .then()
                                .thenReturn(uuidKey)
                )
                .flatMap(Function.identity());
    }

    @Nonnull
    @Override
    public Flux<String> searchByQuery(@Nonnull SearchQuery searchQuery) {
        return searchByFilterQuery(searchQuery)
                    .map(LogRecordEntity::getSource);
    }

    @NonNull
    @Override
    public Mono<MapLogsStatistics> analyze(@NonNull AnalyzeQuery analyzeQuery) {
        final var filteredRecords = searchByFilterQuery(analyzeQuery).cache();
        return analyze(filteredRecords, analyzeQuery);
    }

    @NonNull
    @Override
    public Mono<LogsStatisticsEntity> findStatisticsByKey(@NonNull String key) {
        return this.statisticsRepository.findByDataQueryRegexOrId(key);
    }

    @NonNull
    @Override
    public Flux<LogsStatisticsEntity> findAllStatisticsByUserKeyAndCreationDate(
            @NonNull String userKey,
            @NonNull LocalDateTime beforeDate) {
        return this.statisticsRepository.findAllByUserKeyAndCreationDateBefore(userKey, beforeDate);
    }

    @NonNull
    @Override
    public Mono<Void> deleteStatistics(@NonNull Flux<LogsStatisticsEntity> statsFlux) {
        return this.statisticsRepository.deleteAll(statsFlux);
    }

    @NonNull
    @Override
    public Flux<String> deleteAllStatisticsByUserKeyAndCreationDate(@NonNull String userKey, @NonNull LocalDateTime beforeDate) {
        return this.statisticsRepository.deleteAllByUserKeyAndCreationDateBefore(userKey, beforeDate);
    }

    @NonNull
    @Override
    public Mono<Void> deleteByQuery(@NonNull SearchQuery deleteQuery) {
        return this.logRecordRepository.deleteAll(searchByFilterQuery(deleteQuery));
    }

    private Mono<MapLogsStatistics> analyze(
            final Flux<LogRecordEntity> recordFlux,
            final AnalyzeQuery analyzeQuery) {

        return this.logsAnalyzer
                        .analyze(recordFlux, analyzeQuery)
                        .doOnNext(stats -> logsAnalyzeCounter.increment())
                        .flatMap(userStats ->
                                this.userAccessor.get()
                                                    .map(UserEntity::getHash)
                                                    .map(userKey -> processStatsSaving(analyzeQuery, userStats, userKey))
                                                    .flatMap(Mono::single)
                                                    .doOnError(ex -> logger.error("", ex))
                        );
    }

    private Mono<MapLogsStatistics> processStatsSaving(
            final AnalyzeQuery analyzeQuery,
            final MapLogsStatistics stats,
            final String userKey) {

        if (!analyzeQuery.save()) {
            return Mono.just(stats);
        }

        return stats.toResultMap()
                    .flatMap(statsMap -> saveStatsEntity(analyzeQuery, statsMap, userKey))
                    .thenReturn(stats);
    }

    private Mono<LogsStatisticsEntity> saveStatsEntity(
            final AnalyzeQuery analyzeQuery,
            final Map<String, Object> stats,
            final String userKey) {

        final LogsStatisticsEntity entity =
                LogsStatisticsEntity.builder()
                                        .id(analyzeQuery.getId())
                                        .created(LocalDateTime.now())
                                        .title(analyzeQuery.analyzeResultName())
                                        .dataQuery(analyzeQuery.toSearchQuery().toJson(this.jsonConverter))
                                        .userKey(userKey)
                                        .stats(stats)
                                    .build();
        return this.statisticsRepository.save(entity);
    }

    private Flux<LogRecordEntity> searchByFilterQuery(@Nonnull SearchQuery searchQuery) {

        final var records = this.userAccessor.get()
                                            .flatMap(user -> this.queryParser.parse(searchQuery, user.getHash()))
                                            .doOnNext(query -> (searchQuery.extendedFormat() ? extendedSearchRequestsCounter : simpleSearchRequestsCounter).increment())
                                            .flatMapMany(query -> template.search(query, LogRecordEntity.class))
                                            .map(SearchHit::getContent);

        final var postFilters = this.postFiltersSequenceBuilder.build(searchQuery.postFilters());

        return postFilters
                .reduce(Function.<Flux<LogRecordEntity>> identity(), Function::andThen)
                .flatMapMany(f -> f.apply(records));
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

    private Flux<LogRecordEntity> sendToAnalyzeLogs(
            final Flux<LogRecordEntity> records,
            final Tuple2<String, String> userIndexingKey) {
        final var analyzeQuery = new AnalyzeQueryOnIndexWrapper(userIndexingKey.getT2());
        this.taskExecutor.execute(
                () -> {
                    final String userKey = userIndexingKey.getT1();
                    analyze(records, analyzeQuery)
                            .contextWrite(this.userAccessor.set(userKey))
                            .subscribe();
                }
        );

        return records;
    }
}
