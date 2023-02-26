package org.analyzer.management;

import lombok.NonNull;
import org.analyzer.service.management.StatisticsManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.stereotype.Component;

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
    public Map<String, Object> read(@Selector String operation) {
        if ("counters".equals(operation)) {
            final Map<String, Object> result = new HashMap<>();
            result.put("common", this.managementService.commonCount());
            result.put("records-by-user", this.managementService.countRecordsByUsers());
            result.put("stats-by-user", this.managementService.countByUsers());

            return result;
        }

        return super.read(operation);
    }
}