package org.analyzer.dao;

import org.analyzer.entities.LogsStatisticsEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LogsStatisticsRepository extends MongoRepository<LogsStatisticsEntity, String> {

    @Nonnull
    Optional<LogsStatisticsEntity> findByDataQueryRegexOrId(@Nonnull String statisticsKey, @Nonnull String id);

    @Nonnull
    @Query("{ 'user_key' : '?0', 'created' : { $lte : ?1 } }")
    List<LogsStatisticsEntity> findAllByUserKeyAndCreationDateBefore(
            @Nonnull String userKey,
            @Nonnull LocalDateTime creationDate);

    @Nonnull
    @Query(value = "{ 'user_key' : '?0', 'created' : { $lte : ?1 } }", delete = true)
    List<LogsStatisticsEntity> deleteAllByUserKeyAndCreationDateBefore(
            @Nonnull String userKey,
            @Nonnull LocalDateTime creationDate);
}
