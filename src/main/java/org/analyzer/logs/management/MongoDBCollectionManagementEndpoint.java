package org.analyzer.logs.management;

import lombok.NonNull;
import org.analyzer.logs.service.management.MongoDBManagementService;
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
            default -> throw new UnsupportedOperationException(operation);
        };
    }

    @WriteOperation
    public boolean write(@Selector String operation) {
        if ("create".equals(operation)) {
            this.managementService.createCollection();
            return true;
        }

        throw new UnsupportedOperationException(operation);
    }

    @DeleteOperation
    public boolean delete(@Selector(match = Selector.Match.ALL_REMAINING) String[] operation) {
        if (operation == null || operation.length == 0) {
            throw new UnsupportedOperationException("Operation not specified");
        }

        if ("drop".equals(operation[0])) {
            this.managementService.dropCollection();
            return true;
        }

        throw new UnsupportedOperationException(StringUtils.arrayToDelimitedString(operation, "/"));
    }
}
