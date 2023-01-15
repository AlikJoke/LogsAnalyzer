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
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ElasticLogRecordService implements LogRecordService {

    @Autowired
    private ElasticsearchTemplate template;
    @Autowired
    private LogRecordRepository logRecordRepository;
    @Autowired
    private LogRecordParser parser;
    @Autowired
    private ZipUtil zipUtil;
    @Autowired
    private SearchQueryParser<CriteriaQuery> queryParser;

    @Override
    public void index(@NonNull File logFile, @NonNull String originalLogFileName, @Nullable String logRecordPattern) throws IOException {

        final List<LogRecord> records;
        if (this.zipUtil.isArchive(logFile)) {
            records = this.zipUtil.unzip(logFile)
                                    .parallelStream()
                                    .map(file -> this.parser.parse(file, originalLogFileName, logRecordPattern))
                                    .flatMap(List::stream)
                                    .collect(Collectors.toList());
        } else {
            records = this.parser.parse(logFile, originalLogFileName, logRecordPattern);
        }

        this.logRecordRepository.saveAll(records);
    }

    @Override
    public void dropIndex() {
        logRecordRepository.deleteAll();
    }

    @Override
    public long getAllRecordsCount() {
        return this.logRecordRepository.count();
    }

    @Nonnull
    @Override
    public List<String> getRecordsByFilter(@Nonnull String filterQuery) {
        final Query searchQuery = this.queryParser.parse(filterQuery);
        final SearchHits<LogRecord> hits = template.search(searchQuery, LogRecord.class);
        return hits.getSearchHits()
                    .stream()
                    .map(SearchHit::getContent)
                    .map(LogRecord::getRecord)
                    .collect(Collectors.toList());
    }

    @PreDestroy
    private void close() {
        dropIndex();
    }
}
