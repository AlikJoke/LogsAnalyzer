package org.parser.app.service.elastic;

import jakarta.annotation.PreDestroy;
import lombok.NonNull;
import org.parser.app.dao.LogRecordRepository;
import org.parser.app.model.LogRecord;
import org.parser.app.service.LogRecordParser;
import org.parser.app.service.LogRecordService;
import org.parser.app.service.SearchQueryParser;
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
        return logRecordRepository.deleteAll();
    }

    @Override
    public Mono<Long> getAllRecordsCount() {
        return this.logRecordRepository.count();
    }

    @Nonnull
    @Override
    public Flux<String> getRecordsByFilter(@Nonnull String filterQuery) {
        return this.queryParser.parse(filterQuery)
                                .flatMapMany(query -> template.search(query, LogRecord.class))
                                .map(SearchHit::getContent)
                                .map(LogRecord::getRecord);
    }

    @PreDestroy
    private void close() {
        dropIndex().block();
    }
}
