package org.analyzer.logs.dao;

import org.analyzer.logs.model.UserEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface UserRepository extends ReactiveMongoRepository<UserEntity, String> {
}
