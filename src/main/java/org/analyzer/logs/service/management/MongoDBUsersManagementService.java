package org.analyzer.logs.service.management;

import lombok.NonNull;
import org.analyzer.logs.model.UserEntity;
import org.analyzer.logs.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class MongoDBUsersManagementService extends MongoDBManagementServiceBase<UserEntity> implements UsersManagementService {

    private final UserService userService;

    @Autowired
    MongoDBUsersManagementService(
            @NonNull UserService userService,
            @NonNull ReactiveMongoTemplate template) {
        super(template, UserEntity.class);
        this.userService = userService;
    }

    @NonNull
    @Override
    public Mono<Boolean> disableUser(@NonNull String username) {
        return this.userService.disable(username)
                                .thenReturn(true)
                                .onErrorStop();
    }

    @NonNull
    @Override
    public Mono<Long> count(boolean onlyActive) {
        return this.userService.findCount(onlyActive);
    }
}
