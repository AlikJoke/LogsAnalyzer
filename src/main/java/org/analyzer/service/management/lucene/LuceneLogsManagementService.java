package org.analyzer.service.management.lucene;

import lombok.NonNull;
import org.analyzer.config.lucene.LuceneConfiguration;
import org.analyzer.dao.LogsStorage;
import org.analyzer.service.management.LogsManagementService;
import org.apache.lucene.index.IndexWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LuceneLogsManagementService implements LogsManagementService {

    @Autowired
    private IndexWriter indexWriter;
    @Autowired
    private LuceneConfiguration luceneConfiguration;
    @Autowired
    private LogsStorage logsStorage;

    @Override
    public boolean createIndex() {
        throw new UnsupportedOperationException("Index will be created automatically if needed");
    }

    @Override
    public boolean existsIndex() {
        try {
            final var indexFiles = this.indexWriter.getDirectory().listAll();
            return indexFiles != null && indexFiles.length > 0;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void refreshIndex() {
        throw new UnsupportedOperationException("Refresh not supported by Lucene");
    }

    @Override
    public boolean dropIndex() {
        try {
            this.indexWriter.deleteAll();
            this.indexWriter.commit();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        return true;
    }

    @NonNull
    @Override
    public Map<String, Object> indexInfo() {

        try {
            final Map<String, Object> result = new HashMap<>(2, 1);

            result.put("index-files", this.indexWriter.getDirectory().listAll());
            result.put("index-type", this.luceneConfiguration.getType());
            result.put("index-name", this.luceneConfiguration.getStoragePath());
            result.put("data-count", this.logsStorage.allCount());

            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
