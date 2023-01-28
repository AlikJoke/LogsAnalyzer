package org.analyzer.logs.service;

import org.analyzer.logs.model.UserEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

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
    Mono<UserEntity> findById(@Nonnull String username);

    @Nonnull
    Mono<UserEntity> findByUserHash(@Nonnull String userHash);
}
