package org.analyzer.logs.dao;

import org.analyzer.logs.model.HttpArchiveEntity;
import org.analyzer.logs.model.UserSearchQueryEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import javax.annotation.Nonnull;
import java.util.List;

public interface HttpArchiveRepository extends MongoRepository<HttpArchiveEntity, String> {

    void deleteAllByUserKey(@Nonnull String userKey);

    @Nonnull
    List<UserSearchQueryEntity> findAllByUserKey(
            @Nonnull String userKey,
            @Nonnull Pageable pageable);
}
