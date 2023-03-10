package org.analyzer.dao;

import org.analyzer.entities.UserEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<UserEntity, String> {

    @Query("{ 'settings.cleaning_interval' : { $gt : 0 } }")
    List<UserEntity> findAllWithClearingSettings();

    @Query("{ 'settings.scheduled_indexing_settings' : { $ne : null }, 'active' : true, 'modified' : { $gt : ?0 } }")
    List<UserEntity> findAllWithScheduledIndexingSettings(@Nonnull final LocalDateTime modifiedAfter);

    @Query("{ 'settings.notification_settings.notify_telegram' : { $ne : null }, 'active' : true }")
    List<UserEntity> findAllWithTelegramId();

    @Nonnull
    Optional<UserEntity> findByHash(@Nonnull String hash);

    @Nonnull
    @Query("{ 'settings.notification_settings.notify_telegram' : ?0 }")
    Optional<UserEntity> findByTelegramId(@Nonnull Long telegramId);

    @Query
    long countByActive(boolean active);
}
