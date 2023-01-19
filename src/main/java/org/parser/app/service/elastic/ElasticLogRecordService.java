package org.parser.app.service.elastic;

import jakarta.annotation.PreDestroy;
import lombok.NonNull;
import org.parser.app.dao.LogRecordRepository;
import org.parser.app.model.LogRecord;
import org.parser.app.service.*;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.function.Function;

@Service
public class ElasticLogRecordService implements LogRecordService {

    @Autowired
    private ReactiveElasticsearchTemplate template;
    @Autowired
    private LogRecordRepository logRecordRepository;
    @Autowired
    private LogRecordParser parser;
    @Autowired
    private ZipUtil zipUtil;
    @Autowired
    private SearchQueryParser<StringQuery> queryParser;
    @Autowired
    private PostFiltersSequenceBuilder postFiltersSequenceBuilder;

    @Override
    public Mono<Void> index(@NonNull Mono<File> logFile, @NonNull String originalLogFileName, @Nullable String logRecordPattern) {

        return this.zipUtil.flat(logFile)
                            .parallel()
                            .runOn(Schedulers.parallel())
                            .map(Mono::just)
                            .map(file -> this.parser.parse(file, originalLogFileName, logRecordPattern))
                            .flatMap(this.logRecordRepository::saveAll)
                            .then();
    }

    @Override
    public Mono<Void> dropIndex() {
        return template.indexOps(LogRecord.class)
                        .delete()
                        .then();
    }

    @Override
    public Mono<Long> getAllRecordsCount() {
        return this.logRecordRepository.count();
    }

    @Nonnull
    @Override
    public Flux<String> searchByQuery(@Nonnull SearchQuery searchQuery) {

        final Flux<LogRecord> records = this.queryParser.parse(searchQuery)
                                                        .flatMapMany(query -> template.search(query, LogRecord.class))
                                                        .map(SearchHit::getContent);

        final Flux<PostFilter<?>> postFilters = this.postFiltersSequenceBuilder.build(searchQuery.postFilters());
        return postFilters
                    .reduce(Function.<Flux<LogRecord>> identity(), Function::andThen)
                    .flatMapMany(f -> f.apply(records))
                    .map(LogRecord::getSource);
    }

    @PreDestroy
    private void close() {
        dropIndex().block();
    }
}
