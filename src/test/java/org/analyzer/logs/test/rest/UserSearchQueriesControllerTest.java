package org.analyzer.logs.test.rest;

import org.analyzer.logs.rest.hateoas.LinksCollector;
import org.analyzer.logs.rest.queries.UserQueriesCollection;
import org.analyzer.logs.rest.queries.UserQueryResource;
import org.analyzer.logs.rest.queries.UserSearchQueriesController;
import org.analyzer.logs.service.CurrentUserQueryService;
import org.analyzer.logs.test.rest.config.TestSecurityConfig;
import org.analyzer.logs.test.fixtures.TestFixtures;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.analyzer.logs.test.rest.config.TestSecurityConfig.USER_ROLE;
import static org.analyzer.logs.test.fixtures.TestFixtures.TEST_USER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@WebFluxTest(UserSearchQueriesController.class)
@Import(TestSecurityConfig.class)
@WithMockUser(username = TEST_USER, authorities = USER_ROLE)
public class UserSearchQueriesControllerTest {

    @Autowired
    private WebTestClient webClient;
    @MockBean
    private CurrentUserQueryService userQueryService;
    @MockBean
    private LinksCollector linksCollector;

    @Test
    public void shouldDeleteSearchQueryFromHistory() {
        final var userQueryId = UUID.randomUUID().toString();

        when(this.userQueryService.delete(userQueryId)).thenReturn(Mono.just(true));

        this.webClient
                .delete()
                .uri("/queries/" + userQueryId)
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    @Test
    public void shouldDeleteSearchQueryFromFail() {
        final var userQueryId = UUID.randomUUID().toString();

        when(this.userQueryService.delete(userQueryId)).thenReturn(Mono.empty());

        this.webClient
                .delete()
                .uri("/queries/" + userQueryId)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    public void shouldDeleteAllSearchQueriesFromHistory() {

        when(this.userQueryService.deleteAll()).thenReturn(Mono.empty());

        this.webClient
                .delete()
                .uri("/queries")
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    @Test
    public void shouldGetAllSearchQueriesFromHistory() {

        final String userKey = UUID.randomUUID().toString();
        final var searchQuery1 = TestFixtures.createUserSearchQueryEntity(userKey);
        final var searchQuery2 = TestFixtures.createUserSearchQueryEntity(userKey);

        final var from = LocalDateTime.ofInstant(Instant.EPOCH, ZoneId.systemDefault());
        when(this.userQueryService.findAll(eq(from), any())).thenReturn(Flux.just(searchQuery1, searchQuery2));

        final var queries = List.of(
                new UserQueryResource(searchQuery1, this.linksCollector),
                new UserQueryResource(searchQuery2, this.linksCollector)
        );

        this.webClient
                .get()
                .uri("/queries")
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody(UserQueriesCollection.class)
                .isEqualTo(new UserQueriesCollection(queries, this.linksCollector.collectFor(UserQueriesCollection.class)));
    }

    @Test
    public void shouldGetAllSearchQueriesFromHistoryByInterval() {

        final String userKey = UUID.randomUUID().toString();
        final var searchQuery = TestFixtures.createUserSearchQueryEntity(userKey);

        final var from = LocalDateTime.now().minusDays(2);
        final var to = LocalDateTime.now().minusDays(1);
        when(this.userQueryService.findAll(eq(from), eq(to))).thenReturn(Flux.just(searchQuery));

        final var queries = List.of(new UserQueryResource(searchQuery, this.linksCollector));

        this.webClient
                .get()
                .uri(uriBuilder ->
                        uriBuilder
                                .path("/queries")
                                .queryParam("from", from)
                                .queryParam("to", to)
                                .build()
                )
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody(UserQueriesCollection.class)
                .isEqualTo(new UserQueriesCollection(queries, this.linksCollector.collectFor(UserQueriesCollection.class)));
    }
}
