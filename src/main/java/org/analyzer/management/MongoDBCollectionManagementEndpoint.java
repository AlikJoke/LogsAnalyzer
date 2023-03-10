package org.analyzer.management;

import lombok.NonNull;
import org.analyzer.service.exceptions.UnsupportedApplicationOperationException;
import org.analyzer.service.management.MongoDBManagementService;
import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.util.StringUtils;

import java.util.Map;

abstract class MongoDBCollectionManagementEndpoint<T extends MongoDBManagementService> {

    protected final T managementService;

    protected MongoDBCollectionManagementEndpoint(@NonNull T managementService) {
        this.managementService = managementService;
    }

    @ReadOperation
    public Map<String, Object> read(@Selector String operation) {
        return switch (operation) {
            case "exists" -> Map.of("collection-exists", this.managementService.existsCollection());
            case "information" -> this.managementService.indexesInfo();
            default -> throw new UnsupportedApplicationOperationException(operation);
        };
    }

    @WriteOperation
    public void write(@Selector String operation) {
        if ("create".equals(operation)) {
            this.managementService.createCollection();
            return;
        }

        throw new UnsupportedApplicationOperationException(operation);
    }

    @DeleteOperation
    public boolean delete(@Selector(match = Selector.Match.ALL_REMAINING) String[] operation) {
        if (operation == null || operation.length == 0) {
            throw new UnsupportedApplicationOperationException("");
        }

        if ("drop".equals(operation[0])) {
            this.managementService.dropCollection();
            return true;
        }

        throw new UnsupportedApplicationOperationException(StringUtils.arrayToDelimitedString(operation, "/"));
    }
}
