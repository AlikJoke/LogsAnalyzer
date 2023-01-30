package org.analyzer.logs.service.management;

import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.Map;

public interface MongoDBManagementService {

    @Nonnull
    Mono<Void> createCollection();

    @Nonnull
    Mono<Boolean> existsCollection();

    @Nonnull
    Mono<Void> dropCollection();

    @Nonnull
    Mono<Map<String, Object>> indexesInfo();
}
