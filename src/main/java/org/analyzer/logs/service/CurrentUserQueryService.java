package org.analyzer.logs.service;

import org.analyzer.logs.model.UserSearchQueryEntity;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.util.List;

public interface CurrentUserQueryService {

    @Nonnull
    UserSearchQueryEntity create(@Nonnull SearchQuery query);

    @Nonnull
    List<UserSearchQueryEntity> findAll(@Nonnull LocalDateTime from, @Nonnull LocalDateTime to);

    void deleteAll();

    void delete(@Nonnull String queryId);
}
