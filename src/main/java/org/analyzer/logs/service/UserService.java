package org.analyzer.logs.service;

import org.analyzer.logs.model.UserEntity;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.util.List;

public interface UserService {

    @Nonnull
    UserEntity create(@Nonnull UserEntity user);

    void disable(@Nonnull String username);

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

    long findCount(boolean onlyActive);
}
