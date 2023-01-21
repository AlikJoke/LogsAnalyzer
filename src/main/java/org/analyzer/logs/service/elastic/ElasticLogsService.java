package org.analyzer.logs.service.elastic;

import lombok.NonNull;
import org.analyzer.logs.dao.LogRecordRepository;
import org.analyzer.logs.model.LogRecord;
import org.analyzer.logs.service.*;
import org.analyzer.logs.service.std.LogsAnalyzer;
import org.analyzer.logs.service.std.PostFiltersSequenceBuilder;
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
    private LogsAnalyzer logsAnalyzer;

    @Value("${elasticsearch.default.indexing.buffer_size:2500}")
    private int elasticIndexBufferSize;

    @Override
    public @NonNull Mono<Void> index(
            @NonNull Mono<File> logFile,
            @NonNull String originalLogFileName,
            @Nullable LogRecordFormat recordFormat) {

        return this.zipUtil.flat(logFile)
                            .log(logger)
                            .parallel()
                            .runOn(Schedulers.parallel())
                            .map(Mono::just)
                            .map(file -> this.parser.parse(file, originalLogFileName, recordFormat))
                            .flatMap(records -> records
                                                    .buffer(this.elasticIndexBufferSize)
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
        return this.logsAnalyzer.analyze(filteredRecords, searchQuery.aggregations());
    }

    private Flux<LogRecord> searchByFilterQuery(@Nonnull SearchQuery searchQuery) {

        final Flux<LogRecord> records = this.queryParser.parse(searchQuery)
                                                        .flatMapMany(query -> template.search(query, LogRecord.class))
                                                        .map(SearchHit::getContent);

        final Flux<PostFilter> postFilters = this.postFiltersSequenceBuilder.build(searchQuery.postFilters());

        return postFilters
                .reduce(Function.<Flux<LogRecord>> identity(), Function::andThen)
                .flatMapMany(f -> f.apply(records));
    }
}
