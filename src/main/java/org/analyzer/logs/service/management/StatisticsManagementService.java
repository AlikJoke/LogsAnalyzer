package org.analyzer.logs.service.management;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

public interface StatisticsManagementService extends MongoDBManagementService {

    @Nonnull
    Mono<Long> commonCount();

    @Nonnull
    Flux<CountByUsers> countStatsByUsers();

    @Nonnull
    Flux<CountByUsers> countRecordsByUsers();

    interface CountByUsers {

        @Nonnull
        String getUserKey();

        long getCount();
    }
}
