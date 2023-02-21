package org.analyzer.logs.management;

import lombok.NonNull;
import org.analyzer.logs.service.management.HttpArchivesManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Endpoint(id = "httpArchives")
public class HttpArchiveCollectionManagementEndpoint extends MongoDBCollectionManagementEndpoint<HttpArchivesManagementService> {

    @Autowired
    protected HttpArchiveCollectionManagementEndpoint(@NonNull HttpArchivesManagementService managementService) {
        super(managementService);
    }

    @ReadOperation
    public Map<String, Object> read(@Selector String operation) {
        if ("counters".equals(operation)) {
            final Map<String, Object> result = new HashMap<>();
            result.put("common", this.managementService.commonCount());
            result.put("http-archives-by-user", this.managementService.countByUsers());

            return result;
        }

        return super.read(operation);
    }
}