package org.analyzer.logs.sec;

import lombok.NonNull;
import org.analyzer.logs.model.UserEntity;
import org.analyzer.logs.service.CurrentUserAccessor;
import org.analyzer.logs.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

@Component
public class DefaultCurrentUserAccessor implements CurrentUserAccessor {

    @Autowired
    private UserService userService;

    @NonNull
    @Override
    public Mono<UserEntity> get() {
        return ReactiveSecurityContextHolder.getContext()
                                            .map(SecurityContext::getAuthentication)
                                            .map(Authentication::getPrincipal)
                                            .cast(UserDetailsWrapper.class)
                                            .map(UserDetailsWrapper::getUserEntity)
                                            .cache();
    }

    @NonNull
    @Override
    public Context set(@NonNull String userKey) {
        return set(userService.findByUserHash(userKey)
                                .onErrorStop()
        );
    }

    @NonNull
    @Override
    public Context set(@NonNull Mono<UserEntity> user) {
        final var securityContext = user
                                    .map(UserDetailsWrapper::new)
                                    .map(RunAsUserAuthenticationToken::new)
                                    .map(SecurityContextImpl::new);
        return ReactiveSecurityContextHolder.withSecurityContext(securityContext);
    }

    private static class RunAsUserAuthenticationToken extends AbstractAuthenticationToken {

        private final UserDetailsWrapper principal;

        RunAsUserAuthenticationToken(final UserDetailsWrapper principal) {
            super(null);
            setAuthenticated(true);
            this.principal = principal;
        }

        @Override
        public Object getCredentials() {
            return principal.getPassword();
        }

        @Override
        public Object getPrincipal() {
            return this.principal;
        }
    }

}
