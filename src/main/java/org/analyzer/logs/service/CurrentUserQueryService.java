package org.analyzer.logs.service;

import org.analyzer.logs.model.UserSearchQueryEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;

public interface CurrentUserQueryService {

    @Nonnull
    Mono<UserSearchQueryEntity> create(@Nonnull SearchQuery query);

    @Nonnull
    Flux<UserSearchQueryEntity> findAll(@Nonnull LocalDateTime from, @Nonnull LocalDateTime to);

    @Nonnull
    Mono<Void> deleteAll();

    @Nonnull
    Mono<Boolean> delete(@Nonnull String queryId);
}
