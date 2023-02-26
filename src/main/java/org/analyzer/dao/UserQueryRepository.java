package org.analyzer.dao;

import org.analyzer.entities.UserSearchQueryEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.util.List;

public interface UserQueryRepository extends MongoRepository<UserSearchQueryEntity, String> {

    @Nonnull
    List<UserSearchQueryEntity> findAllByUserKeyAndCreatedBetween(
            @Nonnull String userKey,
            @Nonnull LocalDateTime from,
            @Nonnull LocalDateTime to,
            @Nonnull Sort sort);

    void deleteAllByUserKey(@Nonnull String userKey);

    @Nonnull
    List<UserSearchQueryEntity> findAllByUserKey(
            @Nonnull String userKey,
            @Nonnull Pageable pageable);
}
