package org.analyzer.logs.service.management;

import reactor.core.publisher.Flux;

import javax.annotation.Nonnull;

public interface StatisticsManagementService extends MongoDBManagementServiceWithUserCounters {

    @Nonnull
    Flux<CountByUsers> countRecordsByUsers();
}
