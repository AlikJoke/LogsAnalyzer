package org.analyzer.logs.service.management;

import org.analyzer.logs.model.LogsStatisticsEntity;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import javax.annotation.Nonnull;

public interface StatisticsManagementService extends MongoDBManagementService<LogsStatisticsEntity> {

    @Nonnull
    Mono<Long> commonCount();

    @Nonnull
    Mono<Tuple2<String, Long>> countStatsByUsers();

    @Nonnull
    Mono<Tuple2<String, Long>> countRecordsByUsers();
}
