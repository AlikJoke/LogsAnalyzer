package org.analyzer.logs.service.management;

import lombok.NonNull;
import org.analyzer.logs.dao.LogRecordRepository;
import org.analyzer.logs.model.LogRecordEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ElasticLogsManagementService implements LogsManagementService {

    private final LogRecordRepository logsRepository;
    private final IndexOperations indexOps;

    @Autowired
    public ElasticLogsManagementService(
            @NonNull ElasticsearchTemplate template,
            @NonNull LogRecordRepository logsRepository) {
        this.logsRepository = logsRepository;
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
                    result.put("data-count", this.logsRepository.count());

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
