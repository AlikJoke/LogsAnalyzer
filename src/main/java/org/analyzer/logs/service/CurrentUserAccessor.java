package org.analyzer.logs.service;

import org.analyzer.logs.model.UserEntity;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

public interface CurrentUserAccessor {

    @Nonnull
    UserEntity get();

    void set(@Nonnull String userKey);

    void set(@Nonnull Mono<UserEntity> user);
}
