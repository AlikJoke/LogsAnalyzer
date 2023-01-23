package org.analyzer.logs.service.management;

import lombok.NonNull;
import org.analyzer.logs.dao.LogRecordRepository;
import org.analyzer.logs.model.LogRecordEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ReactiveIndexOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.HashMap;
import java.util.Map;

@Service
public class ElasticLogsManagementService implements LogsManagementService {

    private static final Logger logger = Loggers.getLogger(ElasticLogsManagementService.class);

    private final LogRecordRepository logsRepository;

    private final ReactiveIndexOperations indexOps;

    @Autowired
    public ElasticLogsManagementService(
            @NonNull ReactiveElasticsearchTemplate template,
            @NonNull LogRecordRepository logsRepository) {
        this.logsRepository = logsRepository;
        this.indexOps = template.indexOps(LogRecordEntity.class);
    }

    @NonNull
    @Override
    public Mono<Boolean> createIndex() {
        return this.indexOps
                        .createWithMapping()
                        .log(logger);
    }

    @NonNull
    @Override
    public Mono<Boolean> existsIndex() {
        return this.indexOps.exists();
    }

    @NonNull
    @Override
    public Mono<Void> refreshIndex() {
        return this.indexOps
                        .refresh()
                        .log(logger);
    }

    @NonNull
    @Override
    public Mono<Boolean> dropIndex() {
        return this.indexOps
                        .delete()
                        .log(logger);
    }

    @NonNull
    @Override
    public Mono<Map<String, Object>> indexInfo() {
        return this.indexOps
                        .getInformation()
                        .zipWith(this.logsRepository.count())
                        .map(tuple -> {

                            final Map<String, Object> properties = new HashMap<>();
                            properties.put("index-name", tuple.getT1().getName());
                            properties.put("data-count", tuple.getT2());

                            if (tuple.getT1().getMapping() != null) {
                                properties.putAll(tuple.getT1().getMapping());
                            }

                            if (tuple.getT1().getSettings() != null) {
                                properties.putAll(tuple.getT1().getSettings());
                            }

                            return properties;
                        })
                        .singleOrEmpty();
    }
}
