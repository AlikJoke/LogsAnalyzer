package org.analyzer.config.security;

import org.analyzer.service.users.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@EnableWebSecurity
@Configuration
@EnableConfigurationProperties(AdminAccountCredentials.class)
public class SecurityConfig {

    public static final String ADMIN_ROLE = "ADMIN";
    public static final String USER_ROLE = "USER";

    @Value("${logs.analyzer.web.client.allowed.origins:*}")
    private List<String> allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(requests ->
                        requests
                            .requestMatchers(antMatcher("/actuator/**"))
                                .hasAuthority(ADMIN_ROLE)
                            .requestMatchers(HttpMethod.POST, "/user")
                                .anonymous()
                            .requestMatchers("/anonymous", "/docs", "/swagger-ui.html", "/webjars/**")
                                .anonymous()
                            .anyRequest()
                                .hasAuthority(USER_ROLE)
                )
                .formLogin()
                .and()
                .httpBasic()
                .and()
                .cors(corsSpec -> corsSpec.configurationSource(exchange -> {
                    final var allowedMethods =
                            Arrays.stream(RequestMethod.values())
                                    .map(RequestMethod::name)
                                    .toList();
                    final var result = new CorsConfiguration().applyPermitDefaultValues();
                    result
                            .setAllowedOriginPatterns(allowedOrigins)
                            .setAllowedMethods(allowedMethods);
                     return result;
                }))
                .csrf()
                    .disable()
                .build();
    }

    @Bean
    public AuthenticationProvider daoAuthenticationProvider(
            UserDetailsService userDetailsService,
            BCryptPasswordEncoder passwordEncoder) {
        final var provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder);
        provider.setUserDetailsService(userDetailsService);

        return provider;
    }

    @Bean
    public UserDetailsService userDetailsService(
            UserService userService,
            AdminAccountCredentials adminAccountCredentials) {
        final Supplier<UserDetails> adminUserSupplier = () ->
                User.withUsername(adminAccountCredentials.getUsername())
                        .password(adminAccountCredentials.getEncodedPassword())
                        .disabled(false)
                        .authorities(ADMIN_ROLE)
                        .build();
        return username ->
                adminAccountCredentials.getUsername().equals(username)
                        ? adminUserSupplier.get()
                        : new UserDetailsWrapper(userService.findById(username));
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
