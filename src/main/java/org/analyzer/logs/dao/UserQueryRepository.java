package org.analyzer.logs.dao;

import org.analyzer.logs.model.UserSearchQueryEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;

public interface UserQueryRepository extends ReactiveMongoRepository<UserSearchQueryEntity, String> {

    @Nonnull
    Flux<UserSearchQueryEntity> findAllByUserKeyAndCreatedBetween(
            @Nonnull String userKey,
            @Nonnull LocalDateTime from,
            @Nonnull LocalDateTime to,
            @Nonnull Sort sort);

    @Nonnull
    Mono<Void> deleteAllByUserKey(@Nonnull String userKey);

    @Query(delete = true)
    Mono<Void> deleteAllByUserKey(
            @Nonnull String userKey,
            @Nonnull Sort sort,
            @Nonnull Pageable pageable);
}
