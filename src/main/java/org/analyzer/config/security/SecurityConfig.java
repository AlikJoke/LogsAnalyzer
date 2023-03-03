package org.analyzer.config.security;

import org.analyzer.service.users.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HeaderWriterLogoutHandler;
import org.springframework.security.web.header.writers.ClearSiteDataHeaderWriter;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.session.MapSessionRepository;
import org.springframework.session.config.annotation.web.http.EnableSpringHttpSession;
import org.springframework.session.data.redis.RedisSessionRepository;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static org.analyzer.LogsAnalyzerApplication.RUN_MODE_PROPERTY;
import static org.springframework.security.web.header.writers.ClearSiteDataHeaderWriter.Directive.COOKIES;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@EnableWebSecurity
@EnableSpringHttpSession
@Configuration
@EnableConfigurationProperties(AdminAccountCredentials.class)
public class SecurityConfig {

    public static final String ADMIN_ROLE = "ADMIN";
    public static final String USER_ROLE = "USER";

    @Value("${logs.analyzer.web.client.allowed.origins:*}")
    private List<String> allowedOrigins;
    @Value("${logs.analyzer.sessions.max_per_user}")
    private int maxSessionsPerUser;

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
                .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                    .sessionFixation()
                        .changeSessionId()
                        .maximumSessions(this.maxSessionsPerUser)
                        .maxSessionsPreventsLogin(true)
                        .expiredUrl("/login")
                    .and()
                        .enableSessionUrlRewriting(true)
                        .invalidSessionUrl("/login")
                .and()
                .logout(logout ->
                        logout
                                .clearAuthentication(true)
                                .invalidateHttpSession(true)
                                .logoutSuccessUrl("/login")
                                .logoutUrl("/logout")
                                .permitAll()
                                .addLogoutHandler(sessionDestroyedPublishLogoutHandler())
                                .addLogoutHandler(new HeaderWriterLogoutHandler(new ClearSiteDataHeaderWriter(COOKIES)))
                )
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = RUN_MODE_PROPERTY, havingValue = "box")
    public MapSessionRepository sessionRepositoryInMemory() {
        return new MapSessionRepository(new ConcurrentHashMap<>());
    }

    @Bean
    @ConditionalOnProperty(name = RUN_MODE_PROPERTY, havingValue = "distributed", matchIfMissing = true)
    public RedisSessionRepository sessionRepositoryRedis(RedisOperations<String, Object> redisOperations) {
        return new RedisSessionRepository(redisOperations);
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public SessionDestroyedPublishLogoutHandler sessionDestroyedPublishLogoutHandler() {
        return new SessionDestroyedPublishLogoutHandler();
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
