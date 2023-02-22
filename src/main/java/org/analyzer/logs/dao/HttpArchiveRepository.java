package org.analyzer.logs.dao;

import lombok.NonNull;
import org.analyzer.logs.model.HttpArchiveEntity;
import org.analyzer.logs.model.UserSearchQueryEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.util.List;

public interface HttpArchiveRepository extends MongoRepository<HttpArchiveEntity, String> {

    void deleteAllByUserKey(@Nonnull String userKey);

    @Nonnull
    List<UserSearchQueryEntity> findAllByUserKey(
            @Nonnull String userKey,
            @Nonnull Pageable pageable);

    @Query(value = "{ 'user_key' : '?0', 'created' : { $lte : ?1 } }", delete = true)
    void deleteAllByUserKeyAndCreationDate(
            @NonNull String userKey,
            @NonNull LocalDateTime beforeDate);
}
