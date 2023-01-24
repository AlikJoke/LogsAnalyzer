package org.analyzer.logs.dao;

import org.analyzer.logs.model.LogsStatisticsEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

public interface LogsStatisticsRepository extends ReactiveMongoRepository<LogsStatisticsEntity, String> {

    @Nonnull
    Mono<LogsStatisticsEntity> findByDataQueryLike(@Nonnull String statisticsKey);
}
