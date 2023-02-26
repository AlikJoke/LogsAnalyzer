package org.analyzer.service.queries.std;

import lombok.NonNull;
import org.analyzer.dao.UserQueryRepository;
import org.analyzer.entities.UserSearchQueryEntity;
import org.analyzer.service.users.CurrentUserAccessor;
import org.analyzer.service.queries.UserQueriesService;
import org.analyzer.service.logs.SearchQuery;
import org.analyzer.service.util.JsonConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class DefaultUserQueriesService implements UserQueriesService {

    @Autowired
    private CurrentUserAccessor userAccessor;
    @Autowired
    private UserQueryRepository userQueryRepository;
    @Autowired
    private JsonConverter jsonConverter;
    @Value("${logs.analyzer.user-queries.max-count:100}")
    private int userQueriesMaxCount;

    @NonNull
    @Override
    public UserSearchQueryEntity create(@NonNull SearchQuery searchQuery) {
        final var user = this.userAccessor.get();
        deleteAllMoreThanLimit(user.getHash());

        final var userQuery = new UserSearchQueryEntity()
                                    .setCreated(LocalDateTime.now())
                                    .setQuery(searchQuery.toJson(this.jsonConverter))
                                    .setId(UUID.randomUUID().toString())
                                    .setUserKey(user.getHash());
        return this.userQueryRepository.save(userQuery);
    }

    @NonNull
    @Override
    public List<UserSearchQueryEntity> findAll(@NonNull LocalDateTime from, @NonNull LocalDateTime to) {
        final var user = this.userAccessor.get();
        return this.userQueryRepository.findAllByUserKeyAndCreatedBetween(user.getHash(), from, to, Sort.by(Sort.Direction.DESC, "created"));
    }

    @Override
    public void deleteAll() {
        final var user = this.userAccessor.get();
        this.userQueryRepository.deleteAllByUserKey(user.getHash());
    }

    @Override
    public void delete(@NonNull String queryId) {
        this.userQueryRepository.deleteById(queryId);
    }

    private void deleteAllMoreThanLimit(@NonNull String userHash) {
        final var sort = Sort.by(Sort.Direction.DESC, "created");
        final var pageable = PageRequest.of(1, this.userQueriesMaxCount, sort);
        this.userQueryRepository.deleteAll(
                this.userQueryRepository.findAllByUserKey(userHash, pageable)
        );
    }
}
