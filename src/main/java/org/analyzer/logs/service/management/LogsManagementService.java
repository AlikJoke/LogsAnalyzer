package org.analyzer.logs.service.management;

import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.Map;

public interface LogsManagementService {

    @Nonnull
    Mono<Boolean> createIndex();

    @Nonnull
    Mono<Boolean> existsIndex();

    @Nonnull
    Mono<Void> refreshIndex();

    @Nonnull
    Mono<Boolean> dropIndex();

    @Nonnull
    Mono<Map<String, Object>> indexInfo();
}
