package org.analyzer.logs.service.management;

import lombok.NonNull;
import org.analyzer.logs.dao.UserQueryRepository;
import org.analyzer.logs.model.UserSearchQueryEntity;
import org.analyzer.logs.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class MongoDBUserQueriesManagementService extends MongoDBManagementServiceWithUserCountersBase<UserSearchQueryEntity> implements UserQueriesManagementService {

    private final UserQueryRepository userQueryRepository;

    @Autowired
    MongoDBUserQueriesManagementService(
            @NonNull UserQueryRepository userQueryRepository,
            @NonNull UserService userService,
            @NonNull ReactiveMongoTemplate template) {
        super(userService, template, UserSearchQueryEntity.class);
        this.userQueryRepository = userQueryRepository;
    }

    @NonNull
    @Override
    public Mono<Long> commonCount() {
        return this.userQueryRepository.count();
    }
}
