package org.analyzer.dao.elastic;

import lombok.NonNull;
import org.analyzer.dao.ElasticLogRecordRepository;
import org.analyzer.dao.LogsStorage;
import org.analyzer.entities.LogRecordEntity;
import org.analyzer.service.logs.SearchQueryParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.StringQuery;

import java.util.List;

public class ElasticLogsStorage implements LogsStorage {

    @Autowired
    private ElasticLogRecordRepository repository;
    @Autowired
    private ElasticsearchTemplate template;
    @Autowired
    private SearchQueryParser<StringQuery> queryParser;

    @Override
    public void deleteAllByIdRegex(@NonNull String id) {
        this.repository.deleteAllByIdRegex(id);
    }

    @Override
    public void deleteAll(@NonNull List<LogRecordEntity> records) {
        this.repository.deleteAll(records);
    }

    @Override
    public void saveAll(@NonNull List<LogRecordEntity> records) {
        this.repository.saveAll(records);
    }

    @Override
    public long allCount() {
        return this.repository.count();
    }

    @NonNull
    @Override
    public List<LogRecordEntity> searchByQuery(@NonNull StorageQuery query) {
        final var searchQuery = this.queryParser.parse(query.query(), query.userKey());
        return this.template.search(searchQuery, LogRecordEntity.class)
                            .stream()
                            .map(SearchHit::getContent)
                            .toList();
    }
}
