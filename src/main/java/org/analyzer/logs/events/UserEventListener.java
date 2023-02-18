package org.analyzer.logs.events;

import lombok.NonNull;
import org.analyzer.logs.model.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UserEventListener extends MongoEventGenerationListener<UserEntity> {

    @Autowired
    protected UserEventListener(
            @NonNull EventSender eventSender,
            @Value("${logs.analyzer.events.users.channel}") @NonNull String channel) {
        super(eventSender, channel);
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
