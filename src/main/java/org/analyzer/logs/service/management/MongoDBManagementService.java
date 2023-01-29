package org.analyzer.logs.service.management;

import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.Map;

public interface MongoDBManagementService<T> {

    @Nonnull
    Mono<Boolean> createCollection(@Nonnull Class<T> entity);

    @Nonnull
    Mono<Boolean> existsCollection(@Nonnull Class<T> entity);

    @Nonnull
    Mono<Boolean> dropCollection(@Nonnull Class<T> entity);

    @Nonnull
    Mono<Map<String, Object>> collectionInfo(@Nonnull Class<T> entity);


}
