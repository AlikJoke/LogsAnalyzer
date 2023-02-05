package org.analyzer.logs.dao;

import org.analyzer.logs.model.LogsStatisticsEntity;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;

public interface LogsStatisticsRepository extends ReactiveMongoRepository<LogsStatisticsEntity, String> {

    @Nonnull
    Mono<LogsStatisticsEntity> findByDataQueryRegexOrId(@Nonnull String statisticsKey, @Nonnull String id);

    @Nonnull
    @Query("{ 'user_key' : '?0', 'created' : { $lte : ?1 } }")
    Flux<LogsStatisticsEntity> findAllByUserKeyAndCreationDateBefore(
            @Nonnull String userKey,
            @Nonnull LocalDateTime creationDate);

    @Nonnull
    @Query(value = "{ 'user_key' : '?0', 'created' : { $lte : ?1 } }", delete = true)
    Flux<LogsStatisticsEntity> deleteAllByUserKeyAndCreationDateBefore(
            @Nonnull String userKey,
            @Nonnull LocalDateTime creationDate);
}
