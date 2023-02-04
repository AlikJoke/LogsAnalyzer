package org.analyzer.logs.test.rest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class TestSecurityConfig {

    public static final String USER_ROLE = "USER";
    public static final String ADMIN_ROLE = "ADMIN";

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .authorizeExchange()
                    .pathMatchers(HttpMethod.POST, "/user")
                    .permitAll()
                    .pathMatchers("/anonymous")
                    .permitAll()
                .anyExchange()
                    .hasAuthority(USER_ROLE)
                    .and()
                        .httpBasic()
                    .and()
                        .csrf()
                        .disable()
                .build();
    }
}