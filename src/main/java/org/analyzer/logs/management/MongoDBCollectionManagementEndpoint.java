package org.analyzer.logs.management;

import lombok.NonNull;
import org.analyzer.logs.service.management.MongoDBManagementService;
import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;

abstract class MongoDBCollectionManagementEndpoint<T extends MongoDBManagementService> {

    protected final T managementService;

    protected MongoDBCollectionManagementEndpoint(@NonNull T managementService) {
        this.managementService = managementService;
    }

    @ReadOperation
    public Mono<Map<String, Object>> read(@Selector String operation) {
        return switch (operation) {
            case "exists" -> this.managementService
                                    .existsCollection()
                                    .map(exists -> Collections.singletonMap("collection-exists", exists));
            case "information" -> this.managementService.indexesInfo();
            default -> Mono.error(() -> new UnsupportedOperationException(operation));
        };
    }

    @WriteOperation
    public Mono<Boolean> write(@Selector String operation) {
        if ("create".equals(operation)) {
            return this.managementService.createCollection().thenReturn(true);
        }

        return Mono.error(() -> new UnsupportedOperationException(operation));
    }

    @DeleteOperation
    public Mono<Boolean> delete(@Selector(match = Selector.Match.ALL_REMAINING) String[] operation) {
        if (operation == null || operation.length == 0) {
            return Mono.error(() -> new UnsupportedOperationException("Operation not specified"));
        }

        if ("drop".equals(operation[0])) {
            return this.managementService.dropCollection().thenReturn(true);
        }

        return Mono.error(() -> new UnsupportedOperationException(StringUtils.arrayToDelimitedString(operation, "/")));
    }
}
