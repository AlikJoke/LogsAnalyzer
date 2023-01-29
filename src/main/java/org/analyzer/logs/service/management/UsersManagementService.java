package org.analyzer.logs.service.management;

import org.analyzer.logs.model.UserEntity;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

public interface UsersManagementService extends MongoDBManagementService<UserEntity> {

    @Nonnull
    Mono<Boolean> disableUser(@Nonnull String username);

    @Nonnull
    Mono<Long> count(boolean active);
}
