package org.analyzer.logs.test.rest;

import org.analyzer.logs.rest.ExceptionResource;
import org.analyzer.logs.rest.hateoas.LinksCollector;
import org.analyzer.logs.rest.users.UserController;
import org.analyzer.logs.rest.users.UserResource;
import org.analyzer.logs.service.UserService;
import org.analyzer.logs.service.exceptions.UserNotFoundException;
import org.analyzer.logs.test.rest.fixtures.UserFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@WebFluxTest(UserController.class)
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
    @WithMockUser(username = TEST_USER)
    public void shouldGetCurrentUser() {

        final var user = UserFixtures.createUser(TEST_USER);
        when(userService.findById(TEST_USER)).thenReturn(Mono.just(user));

        webClient
                .get()
                .uri("/user/current")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(UserResource.class)
                .isEqualTo(UserResource.convertFrom(user, this.linksCollector));
    }

    @Test
    @WithMockUser(username = TEST_USER)
    public void shouldGetCurrentUserFail() {

        when(userService.findById(TEST_USER)).thenReturn(Mono.error(new UserNotFoundException(TEST_USER)));

        webClient
                .get()
                .uri("/user/current")
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(ExceptionResource.class);
    }
}