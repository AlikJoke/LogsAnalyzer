package org.analyzer.logs.dao;

import org.analyzer.logs.model.UserSearchQueryEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

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

    @Query
    List<UserSearchQueryEntity> findAllByUserKey(
            @Nonnull String userKey,
            @Nonnull Pageable pageable);
}
