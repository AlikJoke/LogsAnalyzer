package org.analyzer.logs.sec;

import lombok.NonNull;
import org.analyzer.logs.model.UserEntity;
import org.analyzer.logs.service.CurrentUserAccessor;
import org.analyzer.logs.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;

@Component
public class DefaultCurrentUserAccessor implements CurrentUserAccessor {

    @Autowired
    private UserService userService;

    @NonNull
    @Override
    public UserEntity get() {
        final var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new UserNotDefinedException();
        }

        final var principal = auth.getPrincipal();
        return ((UserDetailsWrapper) principal).getUserEntity();
    }

    @NonNull
    @Override
    public UserContext as(@NonNull String userKey) {
        return as(this.userService.findByUserHash(userKey));
    }

    @NonNull
    @Override
    public UserContext as(@NonNull UserEntity user) {
        final var userWrapper = new UserDetailsWrapper(user);
        final var authToken = new RunAsUserAuthenticationToken(userWrapper);
        final var securityContext = new SecurityContextImpl(authToken);

        SecurityContextHolder.setContext(securityContext);
        return SecurityContextHolder::clearContext;
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
