package org.analyzer.logs.sec;

import lombok.NonNull;
import org.analyzer.logs.model.UserEntity;
import org.analyzer.logs.service.CurrentUserAccessor;
import org.analyzer.logs.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class DefaultCurrentUserAccessor implements CurrentUserAccessor {

    @Autowired
    private UserService userService;

    @NonNull
    @Override
    public Mono<UserEntity> get() {
        return ReactiveSecurityContextHolder.getContext()
                                            .map(SecurityContext::getAuthentication)
                                            .map(Authentication::getName)
                                            .flatMap(this.userService::findById)
                                            .cache();
    }
}
