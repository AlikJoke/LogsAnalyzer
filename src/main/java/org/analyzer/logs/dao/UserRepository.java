package org.analyzer.logs.dao;

import org.analyzer.logs.model.UserEntity;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;

public interface UserRepository extends ReactiveMongoRepository<UserEntity, String> {

    @Query("{ 'settings.cleaning_interval' : { $gt : 0 } }")
    Flux<UserEntity> findAllWithClearingSettings();

    @Query("{ 'settings.scheduled_indexing_settings.notification_settings' : { $ne : null }, 'modified' : { $gt : '?0' } }")
    Flux<UserEntity> findAllWithScheduledIndexingSettings(@Nonnull final LocalDateTime modifiedAfter);

    @Nonnull
    Mono<UserEntity> findByHash(@Nonnull String hash);
}
