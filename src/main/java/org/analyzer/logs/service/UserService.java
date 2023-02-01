package org.analyzer.logs.service;

import org.analyzer.logs.model.UserEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;

public interface UserService {

    @Nonnull
    Mono<UserEntity> create(@Nonnull Mono<UserEntity> user);

    @Nonnull
    Mono<Void> disable(@Nonnull String username);

    @Nonnull
    Mono<UserEntity> update(@Nonnull Mono<UserEntity> user);

    @Nonnull
    Flux<UserEntity> findAllWithClearingSettings();

    @Nonnull
    Flux<UserEntity> findAllWithScheduledIndexingSettings(@Nonnull LocalDateTime modifiedAfter);

    @Nonnull
    Flux<UserEntity> findAllWithTelegramId();

    @Nonnull
    Mono<UserEntity> findById(@Nonnull String username);

    @Nonnull
    Mono<UserEntity> findByUserHash(@Nonnull String userHash);

    @Nonnull
    Mono<Long> findCount(boolean onlyActive);
}
