package org.analyzer.management;

import lombok.NonNull;
import org.analyzer.service.management.UserQueriesManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Endpoint(id = "userQueries")
public class UserQueriesCollectionManagementEndpoint extends MongoDBCollectionManagementEndpoint<UserQueriesManagementService> {

    @Autowired
    protected UserQueriesCollectionManagementEndpoint(@NonNull UserQueriesManagementService managementService) {
        super(managementService);
    }

    @ReadOperation
    public Map<String, Object> read(@Selector String operation) {
        if ("counters".equals(operation)) {
            final Map<String, Object> result = new HashMap<>();
            result.put("common", this.managementService.commonCount());
            result.put("queries-by-user", this.managementService.countByUsers());

            return result;
        }

        return super.read(operation);
    }
}