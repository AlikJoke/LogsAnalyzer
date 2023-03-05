package org.analyzer.service.management.elastic;

import lombok.NonNull;
import org.analyzer.dao.LogsStorage;
import org.analyzer.entities.LogRecordEntity;
import org.analyzer.service.management.LogsManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;

import java.util.HashMap;
import java.util.Map;

public class ElasticLogsManagementService implements LogsManagementService {

    private final LogsStorage logsStorage;
    private final IndexOperations indexOps;

    @Autowired
    public ElasticLogsManagementService(
            @NonNull ElasticsearchTemplate template,
            @NonNull LogsStorage logsStorage) {
        this.logsStorage = logsStorage;
        this.indexOps = template.indexOps(LogRecordEntity.class);
    }

    @Override
    public boolean createIndex() {
        return this.indexOps.createWithMapping();
    }

    @Override
    public boolean existsIndex() {
        return this.indexOps.exists();
    }

    @Override
    public void refreshIndex() {
        this.indexOps.refresh();
    }

    @Override
    public boolean dropIndex() {
        return this.indexOps.delete();
    }

    @NonNull
    @Override
    public Map<String, Object> indexInfo() {
        final Map<String, Object> result = new HashMap<>();
        this.indexOps
                .getInformation()
                .forEach(inf -> {
                    result.put("index-name", inf.getName());
                    result.put("data-count", this.logsStorage.allCount());

                    if (inf.getMapping() != null) {
                        result.putAll(inf.getMapping());
                    }

                    if (inf.getSettings() != null) {
                        result.putAll(inf.getSettings());
                    }
                });

        return result;
    }
}
