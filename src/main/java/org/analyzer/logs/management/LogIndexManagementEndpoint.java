package org.analyzer.logs.management;

import org.analyzer.logs.service.management.LogsManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.*;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;

@Component
@Endpoint(id = "logs")
public class LogIndexManagementEndpoint {

    @Autowired
    private LogsManagementService managementService;

    @ReadOperation
    public Mono<Map<String, Object>> read(@Selector String operation) {
        return switch (operation) {
            case "exists" -> this.managementService
                                        .existsIndex()
                                        .map(exists -> Collections.singletonMap("index-exists", exists));
            case "information" -> readInformation();
            default -> Mono.error(() -> new UnsupportedOperationException(operation));
        };
    }

    @ReadOperation
    public Mono<Map<String, Object>> readInformation() {
        return this.managementService.indexInfo();
    }

    @WriteOperation
    public Mono<Boolean> write(@Selector String operation) {
        return switch (operation) {
            case "create" -> this.managementService.createIndex();
            case "refresh" -> this.managementService.refreshIndex().thenReturn(true);
            default -> Mono.error(() -> new UnsupportedOperationException(operation));
        };
    }

    @DeleteOperation
    public Mono<Boolean> delete(@Selector String operation) {
        if ("drop".equals(operation)) {
            return this.managementService.dropIndex();
        }

        return Mono.error(() -> new UnsupportedOperationException(operation));
    }
}
