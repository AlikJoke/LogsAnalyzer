package org.analyzer.logs.service.management;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

public interface MongoDBManagementServiceWithUserCounters extends MongoDBManagementService {

    @Nonnull
    Mono<Long> commonCount();

    @Nonnull
    Flux<CountByUsers> countByUsers();

    interface CountByUsers {

        @Nonnull
        String getUserKey();

        long getCount();
    }
}
