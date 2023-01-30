package org.analyzer.logs.service.management;

import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

public interface UsersManagementService extends MongoDBManagementService {

    @Nonnull
    Mono<Boolean> disableUser(@Nonnull String username);

    @Nonnull
    Mono<Long> count(boolean onlyActive);
}
