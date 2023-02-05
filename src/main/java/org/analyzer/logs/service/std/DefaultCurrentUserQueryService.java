package org.analyzer.logs.service.std;

import lombok.NonNull;
import org.analyzer.logs.dao.UserQueryRepository;
import org.analyzer.logs.model.UserEntity;
import org.analyzer.logs.model.UserSearchQueryEntity;
import org.analyzer.logs.service.CurrentUserAccessor;
import org.analyzer.logs.service.CurrentUserQueryService;
import org.analyzer.logs.service.SearchQuery;
import org.analyzer.logs.service.util.JsonConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class DefaultCurrentUserQueryService implements CurrentUserQueryService {

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
    public Mono<UserSearchQueryEntity> create(@NonNull SearchQuery searchQuery) {
        return this.userAccessor.get()
                .map(UserEntity::getHash)
                .flatMap(hash ->
                        deleteAllMoreThanLimit(hash)
                                .thenReturn(
                                    new UserSearchQueryEntity()
                                            .setCreated(LocalDateTime.now())
                                            .setQuery(searchQuery.toJson(this.jsonConverter))
                                            .setId(UUID.randomUUID().toString())
                                            .setUserKey(hash)
                                )
                );
    }

    @NonNull
    @Override
    public Flux<UserSearchQueryEntity> findAll(@NonNull LocalDateTime from, @NonNull LocalDateTime to) {
        return this.userAccessor.get()
                .map(UserEntity::getHash)
                .flatMapMany(userHash ->
                        this.userQueryRepository.findAllByUserKeyAndCreatedBetween(userHash, from, to, Sort.by(Sort.Direction.DESC, "created"))
                );
    }

    @NonNull
    @Override
    public Mono<Void> deleteAll() {
        return this.userAccessor.get()
                                    .map(UserEntity::getHash)
                                    .flatMap(this.userQueryRepository::deleteAllByUserKey);
    }

    @NonNull
    @Override
    public Mono<Boolean> delete(@NonNull String queryId) {
        return this.userQueryRepository.deleteById(queryId)
                                        .map(v -> true);
    }

    @NonNull
    private Mono<Void> deleteAllMoreThanLimit(@NonNull String userHash) {
        final Sort sort = Sort.by(Sort.Direction.DESC, "created");
        final Pageable pageable = PageRequest.of(1, this.userQueriesMaxCount, sort);
        return this.userQueryRepository.deleteAll(
                    this.userQueryRepository.findAllByUserKey(userHash, pageable)
                )
                .then();
    }
}
