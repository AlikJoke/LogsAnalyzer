package org.analyzer.logs.rest.users;

import lombok.NonNull;
import org.analyzer.logs.model.UserEntity;
import org.analyzer.logs.model.UserSettings;
import org.analyzer.logs.rest.ResourceLink;
import org.analyzer.logs.rest.hateoas.LinksCollector;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record UserResource(
        @NonNull String username,
        @NonNull String password,
        @Nullable UserSettings settings,
        @Nullable List<ResourceLink> links) {

    @NonNull
    public UserEntity composeEntity(final PasswordEncoder passwordEncoder) {
        return new UserEntity()
                    .setUsername(username())
                    .setHash(UUID.randomUUID().toString())
                    .setSettings(settings())
                    .setEncodedPassword(passwordEncoder.encode(password()))
                    .setModified(LocalDateTime.now())
                    .setActive(true);
    }

    @NonNull
    public UserEntity update(
            @NonNull final UserEntity entity,
            @NonNull final PasswordEncoder passwordEncoder) {
        entity.setSettings(settings());
        entity.setEncodedPassword(passwordEncoder.encode(password()));
        entity.setModified(LocalDateTime.now());
        return entity;
    }

    @NonNull
    public static UserResource convertFrom(@NonNull UserEntity user, @NonNull LinksCollector linksCollector) {
        return new UserResource(user.getUsername(), "[protected]", user.getSettings(), linksCollector.collectFor(UserResource.class));
    }
}
