package org.parser.app.service.elastic;

import lombok.NonNull;
import org.parser.app.dao.LogRecordRepository;
import org.parser.app.model.LogRecord;
import org.parser.app.service.*;
import org.parser.app.service.std.AggregatorFactory;
import org.parser.app.service.std.PostFiltersSequenceBuilder;
import org.parser.app.service.util.ZipUtil;
import org.springframework.beans.factory.annotation.Autowired;
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

    private static final int BUFFER_SIZE = 2_500;

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
    private AggregatorFactory aggregatorsFactory;

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
                                                    .buffer(BUFFER_SIZE)
                                                    .map(this.logRecordRepository::saveAll)
                                                    .log(logger))
                            .flatMap(Flux::then)
                            .then();
    }

    @Nonnull
    @Override
    public Flux<String> searchByQuery(@Nonnull SearchQuery searchQuery) {

        final Flux<LogRecord> records = this.queryParser.parse(searchQuery)
                                                        .flatMapMany(query -> template.search(query, LogRecord.class))
                                                        .map(SearchHit::getContent);

        final Flux<PostFilter> postFilters = this.postFiltersSequenceBuilder.build(searchQuery.postFilters());
        final Mono<Aggregator> aggregator = this.aggregatorsFactory.create(searchQuery.aggregator());

        final Flux<LogRecord> filteredRecords = postFilters
                                                    .reduce(Function.<Flux<LogRecord>> identity(), Function::andThen)
                                                    .flatMapMany(f -> f.apply(records));
        return aggregator
                .flatMapMany(a -> a.apply(filteredRecords));
    }
}
