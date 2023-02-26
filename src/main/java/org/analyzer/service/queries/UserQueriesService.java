package org.analyzer.service.queries;

import org.analyzer.entities.UserSearchQueryEntity;
import org.analyzer.service.logs.SearchQuery;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.util.List;

public interface UserQueriesService {

    @Nonnull
    UserSearchQueryEntity create(@Nonnull SearchQuery query);

    @Nonnull
    List<UserSearchQueryEntity> findAll(@Nonnull LocalDateTime from, @Nonnull LocalDateTime to);

    void deleteAll();

    void delete(@Nonnull String queryId);
}
