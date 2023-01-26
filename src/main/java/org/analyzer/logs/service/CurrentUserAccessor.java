package org.analyzer.logs.service;

import org.analyzer.logs.model.UserEntity;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

public interface CurrentUserAccessor {

    @Nonnull
    Mono<UserEntity> get();
}
