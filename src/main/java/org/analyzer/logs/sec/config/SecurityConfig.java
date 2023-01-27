package org.analyzer.logs.sec.config;

import org.analyzer.logs.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

@EnableWebFluxSecurity
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .authorizeExchange()
                    .pathMatchers("/actuator**")
                    .hasAuthority("ADMIN")
                .anyExchange()
                    .authenticated()
                    .and()
                    .formLogin()
                    .and()
                    .build();
    }

    @Bean
    public UserDetailsRepositoryReactiveAuthenticationManager authenticationManager(
            ReactiveUserDetailsService userDetailsService,
            BCryptPasswordEncoder passwordEncoder) {
        final UserDetailsRepositoryReactiveAuthenticationManager manager = new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
        manager.setPasswordEncoder(passwordEncoder);

        return manager;
    }

    @Bean
    public ReactiveUserDetailsService userDetailsService(UserService userService) {
        return username -> userService
                            .findById(username)
                            .map(user -> User
                                            .withUsername(user.getUsername())
                                            .password(user.getEncodedPassword())
                                            .roles("USER")
                                            .disabled(!user.isActive())
                                            .build()
                            );
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
