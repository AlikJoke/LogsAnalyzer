package org.analyzer.logs.test.rest;

import org.analyzer.logs.model.UserEntity;
import org.analyzer.logs.rest.ExceptionResource;
import org.analyzer.logs.rest.hateoas.LinksCollector;
import org.analyzer.logs.rest.users.UserController;
import org.analyzer.logs.rest.users.UserResource;
import org.analyzer.logs.service.UserService;
import org.analyzer.logs.service.exceptions.UserAlreadyDisabledException;
import org.analyzer.logs.service.exceptions.UserAlreadyExistsException;
import org.analyzer.logs.service.exceptions.UserNotFoundException;
import org.analyzer.logs.test.rest.config.TestSecurityConfig;
import org.analyzer.logs.test.rest.fixtures.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.analyzer.logs.test.rest.config.TestSecurityConfig.USER_ROLE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@WebFluxTest(UserController.class)
@Import(TestSecurityConfig.class)
public class UserControllerTest {

    private static final String TEST_USER = "test";

    @Autowired
    private WebTestClient webClient;
    @MockBean
    private UserService userService;
    @MockBean
    private PasswordEncoder passwordEncoder;
    @MockBean
    private LinksCollector linksCollector;

    @Test
    @WithMockUser(username = TEST_USER, authorities = USER_ROLE)
    public void shouldGetCurrentUser() {

        final var user = TestFixtures.createUser(TEST_USER);
        when(userService.findById(TEST_USER)).thenReturn(Mono.just(user));

        webClient
                .get()
                .uri("/user")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(UserResource.class)
                .isEqualTo(UserResource.convertFrom(user, this.linksCollector));
    }

    @Test
    @WithMockUser(username = TEST_USER, authorities = USER_ROLE)
    public void shouldGetCurrentUserFail() {

        when(userService.findById(TEST_USER)).thenReturn(Mono.error(new UserNotFoundException(TEST_USER)));

        webClient
                .get()
                .uri("/user")
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(ExceptionResource.class);
    }

    @Test
    @WithMockUser(username = TEST_USER, authorities = USER_ROLE)
    public void shouldDisableCurrentUser() {

        when(userService.disable(TEST_USER)).thenReturn(Mono.empty());

        webClient
                .delete()
                .uri("/user")
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    @Test
    @WithMockUser(username = TEST_USER, authorities = USER_ROLE)
    public void shouldDisableDisabledCurrentUserFail() {

        final var user = TestFixtures.createUser(TEST_USER);
        when(userService.findById(TEST_USER)).thenReturn(Mono.just(user));

        when(userService.disable(TEST_USER)).thenReturn(Mono.error(new UserAlreadyDisabledException(TEST_USER)));

        webClient
                .delete()
                .uri("/user")
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(ExceptionResource.class);
    }

    @Test
    public void shouldCreateUser() {

        final var user = TestFixtures.createUser(TEST_USER);
        final var userResource = new UserResource(TEST_USER, "2", user.getSettings(), linksCollector.collectFor(UserResource.class));

        when(this.passwordEncoder.encode("2")).thenReturn("-");

        final var newUser = userResource.composeEntity(this.passwordEncoder);

        when(this.userService.create(any())).thenReturn(Mono.just(newUser));

        final var createdUser = new UserResource(TEST_USER, "[protected]", newUser.getSettings(), this.linksCollector.collectFor(UserResource.class));

        webClient
                .post()
                .uri("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userResource)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(UserResource.class)
                .isEqualTo(createdUser);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Mono<UserEntity>> captor = ArgumentCaptor.forClass(Mono.class);
        verify(this.userService).create(captor.capture());
        StepVerifier
                .create(captor.getValue())
                .expectNext(newUser)
                .verifyComplete();
    }

    @Test
    public void shouldCreateUserFail() {

        final var user = TestFixtures.createUser(TEST_USER);
        final var userResource = new UserResource(TEST_USER, "2", user.getSettings(), linksCollector.collectFor(UserResource.class));

        when(this.passwordEncoder.encode("2")).thenReturn("-");
        final var newUser = userResource.composeEntity(this.passwordEncoder);

        when(this.userService.create(any(Mono.class)))
                .thenReturn(Mono.error(new UserAlreadyExistsException(TEST_USER)));

        webClient
                .post()
                .uri("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userResource)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(ExceptionResource.class);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Mono<UserEntity>> captor = ArgumentCaptor.forClass(Mono.class);
        verify(this.userService).create(captor.capture());
        StepVerifier
                .create(captor.getValue())
                .expectNext(newUser)
                .verifyComplete();
    }
}