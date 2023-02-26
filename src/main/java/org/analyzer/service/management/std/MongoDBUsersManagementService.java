package org.analyzer.service.management.std;

import lombok.NonNull;
import org.analyzer.entities.UserEntity;
import org.analyzer.service.management.UsersManagementService;
import org.analyzer.service.users.UserService;
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
