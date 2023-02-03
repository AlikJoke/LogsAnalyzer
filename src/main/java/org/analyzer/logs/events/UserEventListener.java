package org.analyzer.logs.events;

import lombok.NonNull;
import org.analyzer.logs.model.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserEventListener extends MongoEventGenerationListener<UserEntity> {

    @Autowired
    protected UserEventListener(
            @NonNull ReactiveRedisTemplate<String, Object> redisTemplate,
            @Value("${logs.analyzer.events.users.topic}") @NonNull String topicName) {
        super(redisTemplate, topicName);
    }

    @Override
    protected UserEntity buildEntityToSend(UserEntity sourceEntity) {
        return new UserEntity()
                    .setActive(sourceEntity.isActive())
                    .setHash(sourceEntity.getHash())
                    .setSettings(sourceEntity.getSettings())
                    .setUsername(sourceEntity.getUsername())
                    .setModified(sourceEntity.getModified());
    }
}
