package org.analyzer.dao.lucene;

import lombok.NonNull;
import org.analyzer.dao.LogsStorage;
import org.analyzer.entities.LogRecordEntity;
import org.analyzer.service.logs.SearchQueryParser;
import org.apache.lucene.search.Query;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class LuceneLogsStorage implements LogsStorage {

    @Autowired
    private SearchQueryParser<Query> queryParser;

    @Override
    public void deleteAllByIdRegex(@NonNull String id) {
        // TODO
    }

    @Override
    public void deleteAll(@NonNull List<LogRecordEntity> records) {
        // TODO
    }

    @Override
    public void saveAll(@NonNull List<LogRecordEntity> records) {
        // TODO
    }

    @Override
    public long allCount() {
        // TODO
        return 0;
    }

    @NonNull
    @Override
    public List<LogRecordEntity> searchByQuery(@NonNull LogsStorage.StorageQuery query) {
        // TODO
        return null;
    }
}
