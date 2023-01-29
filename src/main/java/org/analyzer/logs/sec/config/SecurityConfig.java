package org.analyzer.logs.sec.config;

import org.analyzer.logs.sec.UserDetailsWrapper;
import org.analyzer.logs.service.UserService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@EnableWebFluxSecurity
@Configuration
@EnableConfigurationProperties(AdminAccountCredentials.class)
public class SecurityConfig {

    public static final String ADMIN_ROLE = "ADMIN";
    public static final String USER_ROLE = "USER";

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .authorizeExchange()
                    .pathMatchers("/api/**")
                    .hasAuthority(USER_ROLE)
                    .pathMatchers("/actuator**")
                    .hasAuthority(ADMIN_ROLE)
                    .pathMatchers(HttpMethod.POST, "/api/user/create", "/api/anonymous")
                    .permitAll()
                .anyExchange()
                    .authenticated()
                    .and()
                    .formLogin()
                    .and()
                    .httpBasic()
                    .and()
                    .csrf()
                        .disable()
                    .build();
    }

    @Bean
    public UserDetailsRepositoryReactiveAuthenticationManager authenticationManager(
            ReactiveUserDetailsService userDetailsService,
            BCryptPasswordEncoder passwordEncoder) {
        final var manager = new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
        manager.setPasswordEncoder(passwordEncoder);

        return manager;
    }

    @Bean
    public ReactiveUserDetailsService userDetailsService(
            UserService userService,
            AdminAccountCredentials adminAccountCredentials) {
        final var adminUser = User.withUsername(adminAccountCredentials.getUsername())
                                    .password(adminAccountCredentials.getEncodedPassword())
                                    .disabled(false)
                                    .authorities(ADMIN_ROLE)
                                .build();
        final var adminUserMono = Mono.just(adminUser);
        return username ->
                adminAccountCredentials.getUsername().equals(username)
                        ? adminUserMono
                        : userService
                            .findById(username)
                            .onErrorMap(ex -> new UsernameNotFoundException(ex.getMessage()))
                            .onErrorStop()
                            .map(UserDetailsWrapper::new);
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
