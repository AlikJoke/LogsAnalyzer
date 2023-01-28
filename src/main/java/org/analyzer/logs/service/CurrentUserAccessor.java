package org.analyzer.logs.service;

import org.analyzer.logs.model.UserEntity;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import javax.annotation.Nonnull;

public interface CurrentUserAccessor {

    @Nonnull
    Mono<UserEntity> get();

    @Nonnull
    Context set(@Nonnull String userKey);

    @Nonnull
    Context set(@Nonnull Mono<UserEntity> user);
}
