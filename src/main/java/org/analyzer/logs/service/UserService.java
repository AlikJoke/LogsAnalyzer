package org.analyzer.logs.service;

import org.analyzer.logs.model.UserEntity;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserService {

    @Nonnull
    UserEntity create(@Nonnull UserEntity user);

    @Nonnull
    UserEntity disable(@Nonnull String username);

    @Nonnull
    UserEntity enable(@Nonnull String username);

    @Nonnull
    UserEntity update(@Nonnull UserEntity user);

    @Nonnull
    List<UserEntity> findAllWithClearingSettings();

    @Nonnull
    List<UserEntity> findAllWithScheduledIndexingSettings(@Nonnull LocalDateTime modifiedAfter);

    @Nonnull
    List<UserEntity> findAllWithTelegramId();

    @Nonnull
    UserEntity findById(@Nonnull String username);

    @Nonnull
    UserEntity findByUserHash(@Nonnull String userHash);

    @Nonnull
    Optional<UserEntity> findByTelegramId(@Nonnull Long telegramId);

    long findCount(boolean onlyActive);
}
