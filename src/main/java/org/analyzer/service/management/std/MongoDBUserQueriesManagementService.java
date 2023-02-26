package org.analyzer.service.management.std;

import lombok.NonNull;
import org.analyzer.dao.UserQueryRepository;
import org.analyzer.entities.UserSearchQueryEntity;
import org.analyzer.service.management.UserQueriesManagementService;
import org.analyzer.service.users.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class MongoDBUserQueriesManagementService extends MongoDBManagementServiceWithUserCountersBase<UserSearchQueryEntity> implements UserQueriesManagementService {

    private final UserQueryRepository userQueryRepository;

    @Autowired
    MongoDBUserQueriesManagementService(
            @NonNull UserQueryRepository userQueryRepository,
            @NonNull UserService userService,
            @NonNull MongoTemplate template) {
        super(userService, template, UserSearchQueryEntity.class);
        this.userQueryRepository = userQueryRepository;
    }

    @Override
    public long commonCount() {
        return this.userQueryRepository.count();
    }
}
