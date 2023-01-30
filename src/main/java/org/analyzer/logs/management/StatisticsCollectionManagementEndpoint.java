package org.analyzer.logs.management;

import lombok.NonNull;
import org.analyzer.logs.service.management.StatisticsManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Component
@Endpoint(id = "statistics")
public class StatisticsCollectionManagementEndpoint extends MongoDBCollectionManagementEndpoint<StatisticsManagementService> {

    @Autowired
    protected StatisticsCollectionManagementEndpoint(@NonNull StatisticsManagementService managementService) {
        super(managementService);
    }

    @ReadOperation
    public Mono<Map<String, Object>> read(@Selector String operation) {
        if ("counters".equals(operation)) {
            return this.managementService.commonCount()
                    .zipWith(this.managementService.countRecordsByUsers().collectList())
                    .zipWith(this.managementService.countStatsByUsers().collectList())
                    .map(zip -> {
                        final Map<String, Object> result = new HashMap<>();
                        result.put("common", zip.getT1().getT1());
                        result.put("records-by-user", zip.getT1().getT2());
                        result.put("stats-by-user", zip.getT2());

                        return result;
                    });
        }

        return super.read(operation);
    }
}