package org.analyzer.logs.test.rest;

import org.analyzer.logs.rest.AnonymousRootEntrypointResource;
import org.analyzer.logs.rest.RootEntrypointController;
import org.analyzer.logs.rest.RootEntrypointResource;
import org.analyzer.logs.rest.hateoas.LinksCollector;
import org.analyzer.logs.test.rest.config.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.analyzer.logs.test.rest.config.TestSecurityConfig.ADMIN_ROLE;
import static org.analyzer.logs.test.rest.config.TestSecurityConfig.USER_ROLE;
import static org.analyzer.logs.test.fixtures.TestFixtures.TEST_USER;

@ExtendWith(SpringExtension.class)
@WebFluxTest(RootEntrypointController.class)
@Import(TestSecurityConfig.class)
public class RootEntrypointControllerTest {

    @Autowired
    private WebTestClient webClient;
    @MockBean
    private LinksCollector linksCollector;

    @Test
    @WithMockUser(username = TEST_USER, authorities = USER_ROLE)
    public void shouldGetRootLinks() {

        webClient
                .get()
                .uri("/")
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody(RootEntrypointResource.class)
                .isEqualTo(new RootEntrypointResource(this.linksCollector.collectFor(RootEntrypointResource.class)));
    }

    @Test
    public void shouldGetRootLinksFailWhenUnauthorized() {

        webClient
                .get()
                .uri("/")
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    @WithMockUser(username = TEST_USER, authorities = ADMIN_ROLE)
    public void shouldGetRootLinksFailWhenUserHasAdminRole() {

        webClient
                .get()
                .uri("/")
                .exchange()
                .expectStatus()
                .isForbidden();
    }

    @Test
    public void shouldGetAnonRootLinks() {

        webClient
                .get()
                .uri("/anonymous")
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody(AnonymousRootEntrypointResource.class)
                .isEqualTo(new AnonymousRootEntrypointResource(this.linksCollector.collectFor(AnonymousRootEntrypointResource.class)));
    }
}
