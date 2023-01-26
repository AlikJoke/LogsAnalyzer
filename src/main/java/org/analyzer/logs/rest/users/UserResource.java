package org.analyzer.logs.rest.users;

import lombok.NonNull;
import org.analyzer.logs.model.UserEntity;
import org.analyzer.logs.model.UserSettings;
import org.springframework.security.crypto.password.PasswordEncoder;

public record UserResource(
        @NonNull String username,
        @NonNull String password,
        @NonNull UserSettings settings) {

    @NonNull
    public UserEntity toEntity(final PasswordEncoder passwordEncoder) {
        return UserEntity
                .builder()
                    .settings(settings())
                    .username(username())
                    .encodedPassword(passwordEncoder.encode(password()))
                .build();
    }

    @NonNull
    public static UserResource convertFrom(@NonNull UserEntity user) {
        return new UserResource(user.getUsername(), "[protected]", user.getSettings());
    }
}
