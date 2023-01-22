package org.analyzer.logs.service.elastic;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import org.analyzer.logs.dao.LogRecordRepository;
import org.analyzer.logs.model.LogRecord;
import org.analyzer.logs.service.*;
import org.analyzer.logs.service.std.DefaultLogsAnalyzer;
import org.analyzer.logs.service.std.postfilters.PostFiltersSequenceBuilder;
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
            @NonNull String originalLogFileName,
            @Nullable LogRecordFormat recordFormat) {

        return this.zipUtil.flat(logFile)
                            .log(logger)
                            .doOnNext(file -> this.indexedFilesCounter.increment())
                            .parallel()
                            .runOn(Schedulers.parallel())
                            .map(Mono::just)
                            .map(file -> this.parser.parse(file, originalLogFileName, recordFormat))
                            .flatMap(records -> records
                                                    .buffer(this.elasticIndexBufferSize)
                                                    .doOnNext(buffer -> this.indexedRecordsCounter.increment(buffer.size()))
                                                    .doOnNext(buffer -> this.elasticIndexRequestsCounter.increment())
                                                    .map(this.logRecordRepository::saveAll)
                                                    .log(logger))
                            .flatMap(Flux::then)
                            .then();
    }

    @Nonnull
    @Override
    public Flux<String> searchByQuery(@Nonnull SearchQuery searchQuery) {
        return searchByFilterQuery(searchQuery)
                    .map(LogRecord::getSource);
    }

    @NonNull
    @Override
    public Mono<LogsStatistics> analyze(@NonNull SearchQuery searchQuery) {
        final Flux<LogRecord> filteredRecords = searchByFilterQuery(searchQuery).cache();
        return this.logsAnalyzer.analyze(filteredRecords, searchQuery.aggregations())
                                .doOnNext(stat -> logsAnalyzeCounter.increment());
    }

    private Flux<LogRecord> searchByFilterQuery(@Nonnull SearchQuery searchQuery) {

        final Flux<LogRecord> records = this.queryParser.parse(searchQuery)
                                                        .doOnNext(query -> (searchQuery.extendedFormat() ? extendedSearchRequestsCounter : simpleSearchRequestsCounter).increment())
                                                        .flatMapMany(query -> template.search(query, LogRecord.class))
                                                        .map(SearchHit::getContent);

        final Flux<PostFilter> postFilters = this.postFiltersSequenceBuilder.build(searchQuery.postFilters());

        return postFilters
                .reduce(Function.<Flux<LogRecord>> identity(), Function::andThen)
                .flatMapMany(f -> f.apply(records));
    }

    private Counter createMeterCounter(
            final String metricName,
            final String description,
            final String type) {
        final Counter.Builder builder = Counter.builder(metricName)
                                                .description(description);
        if (type != null) {
            builder.tag("type", type);
        }

        return builder.register(this.meterRegistry);
    }
}
