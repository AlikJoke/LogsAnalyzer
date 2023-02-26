package org.analyzer.config.security;

import lombok.Getter;
import lombok.NonNull;
import org.analyzer.config.security.SecurityConfig;
import org.analyzer.entities.UserEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collections;

@Getter
public final class UserDetailsWrapper extends User {

    private final UserEntity userEntity;

    public UserDetailsWrapper(@NonNull UserEntity userEntity) {
        super(
                userEntity.getUsername(),
                userEntity.getEncodedPassword(),
                userEntity.isActive(),
                true, true, true,
                Collections.singletonList(new SimpleGrantedAuthority(SecurityConfig.USER_ROLE)));
        this.userEntity = userEntity;
    }
}
