package org.analyzer.config.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSessionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.session.HttpSessionEventPublisher;

public class SessionDestroyedPublishLogoutHandler implements LogoutHandler {

    @Autowired
    private HttpSessionEventPublisher publisher;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

        final var session = request.getSession(false);
        if (session != null) {
            this.publisher.sessionDestroyed(new HttpSessionEvent(session));
        }
    }
}
