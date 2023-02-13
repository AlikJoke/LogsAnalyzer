package org.analyzer.logs.service.management;

import lombok.NonNull;
import org.analyzer.logs.model.UserEntity;
import org.analyzer.logs.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class MongoDBUsersManagementService extends MongoDBManagementServiceBase<UserEntity> implements UsersManagementService {

    private final UserService userService;

    @Autowired
    MongoDBUsersManagementService(
            @NonNull UserService userService,
            @NonNull MongoTemplate template) {
        super(template, UserEntity.class);
        this.userService = userService;
    }

    @Override
    public void disableUser(@NonNull String username) {
        this.userService.disable(username);
    }

    @Override
    public long count(boolean onlyActive) {
        return this.userService.findCount(onlyActive);
    }
}
