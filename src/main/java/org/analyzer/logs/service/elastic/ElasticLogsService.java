package org.analyzer.logs.service.elastic;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import org.analyzer.logs.dao.LogRecordRepository;
import org.analyzer.logs.dao.LogsStatisticsRepository;
import org.analyzer.logs.model.LogRecordEntity;
import org.analyzer.logs.model.LogsStatisticsEntity;
import org.analyzer.logs.service.*;
import org.analyzer.logs.service.std.DefaultLogsAnalyzer;
import org.analyzer.logs.service.std.postfilters.PostFiltersSequenceBuilder;
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
    public Mono<Void> index(
            @NonNull Mono<File> logFile,
            @Nullable LogRecordFormat recordFormat,
            final boolean preAnalyze) {

        final String uuidKey = UUID.randomUUID().toString();

        return this.zipUtil.flat(logFile)
                            .log(logger)
                            .doOnNext(file -> this.indexedFilesCounter.increment())
                            .parallel()
                            .runOn(Schedulers.parallel())
                            .map(file -> this.parser.parse(composeSearchKey(file, uuidKey), file, recordFormat))
                            .flatMap(records -> records
                                                    .cache()
                                                    .transform(recordsFlux -> sendToAnalyzeLogsIfNeed(preAnalyze, recordsFlux, uuidKey))
                                                    .buffer(this.elasticIndexBufferSize)
                                                    .doOnNext(buffer -> this.indexedRecordsCounter.increment(buffer.size()))
                                                    .doOnNext(buffer -> this.elasticIndexRequestsCounter.increment())
                                                    .map(this.logRecordRepository::saveAll)
                                                    .log(logger)
                            )
                            .flatMap(Flux::then)
                            .then();
    }

    @Nonnull
    @Override
    public Flux<String> searchByQuery(@Nonnull SearchQuery searchQuery) {
        return searchByFilterQuery(searchQuery)
                    .map(LogRecordEntity::getSource);
    }

    @NonNull
    @Override
    public Mono<LogsStatistics> analyze(@NonNull AnalyzeQuery analyzeQuery) {
        final var filteredRecords = searchByFilterQuery(analyzeQuery).cache();
        return analyze(filteredRecords, analyzeQuery);
    }

    private Mono<LogsStatistics> analyze(
            final Flux<LogRecordEntity> recordFlux,
            final AnalyzeQuery analyzeQuery) {

        return this.logsAnalyzer.analyze(recordFlux, analyzeQuery)
                                .doOnNext(stats -> logsAnalyzeCounter.increment())
                                .flatMap(stats -> processStatsSaving(analyzeQuery, stats));
    }

    private Mono<LogsStatistics> processStatsSaving(
            final AnalyzeQuery analyzeQuery,
            final LogsStatistics stats) {

        if (!analyzeQuery.save()) {
            return Mono.just(stats);
        }

        return stats.toResultMap()
                    .flatMap(statsMap -> saveStatsEntity(analyzeQuery, statsMap))
                    .thenReturn(stats);
    }

    private Mono<LogsStatisticsEntity> saveStatsEntity(
            final AnalyzeQuery analyzeQuery,
            final Map<String, Object> stats) {

        final LogsStatisticsEntity entity =
                LogsStatisticsEntity.builder()
                                    .id(UUID.randomUUID().toString())
                                    .created(LocalDateTime.now())
                                    .title(analyzeQuery.analyzeResultName())
                                    .dataQuery(analyzeQuery.toSearchQuery().toJson())
                                    .stats(stats)
                                    .build();
        return this.statisticsRepository.save(entity);
    }

    private Flux<LogRecordEntity> searchByFilterQuery(@Nonnull SearchQuery searchQuery) {

        final var records = this.queryParser.parse(searchQuery)
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

    private Flux<LogRecordEntity> sendToAnalyzeLogsIfNeed(
            final boolean preAnalyze,
            final Flux<LogRecordEntity> records,
            final String searchKey) {
        if (preAnalyze) {
            final AnalyzeQuery analyzeQuery = new AnalyzeQueryOnIndexWrapper(searchKey);
            this.taskExecutor.execute(
                    () -> analyze(records, analyzeQuery).subscribe()
            );
        }

        return records;
    }

    private String composeSearchKey(final File file, final String key) {
        return file.getName() + "$" + key;
    }
}
