package org.analyzer.logs.sec;

import lombok.Getter;
import lombok.NonNull;
import org.analyzer.logs.model.UserEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collections;

import static org.analyzer.logs.config.sec.SecurityConfig.USER_ROLE;

@Getter
public final class UserDetailsWrapper extends User {

    private final UserEntity userEntity;

    public UserDetailsWrapper(@NonNull UserEntity userEntity) {
        super(
                userEntity.getUsername(),
                userEntity.getEncodedPassword(),
                userEntity.isActive(),
                true, true, true,
                Collections.singletonList(new SimpleGrantedAuthority(USER_ROLE)));
        this.userEntity = userEntity;
    }
}
