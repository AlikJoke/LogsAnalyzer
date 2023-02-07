package org.analyzer.logs.test.rest;

import org.analyzer.logs.rest.hateoas.LinksCollector;
import org.analyzer.logs.rest.stats.LogsStatisticsController;
import org.analyzer.logs.rest.stats.RequestAnalyzeQuery;
import org.analyzer.logs.rest.stats.StatisticsResource;
import org.analyzer.logs.service.CurrentUserAccessor;
import org.analyzer.logs.service.LogsService;
import org.analyzer.logs.service.std.StdMapLogsStatistics;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.analyzer.logs.test.rest.config.TestSecurityConfig.USER_ROLE;
import static org.analyzer.logs.test.fixtures.TestFixtures.TEST_USER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@WebFluxTest(LogsStatisticsController.class)
@Import(TestSecurityConfig.class)
@WithMockUser(username = TEST_USER, authorities = USER_ROLE)
public class LogsStatisticsControllerTest {

    @Autowired
    private WebTestClient webClient;
    @MockBean
    private LogsService logsService;
    @MockBean
    private CurrentUserAccessor currentUserAccessor;
    @MockBean
    private LinksCollector linksCollector;

    @Test
    public void shouldReadNotEmptyHistory() {
        final var user = TestFixtures.createUser(TEST_USER);
        final var stats1 = TestFixtures.createStatisticsEntity(TEST_USER);
        final var stats2 = TestFixtures.createStatisticsEntity(TEST_USER);

        when(this.currentUserAccessor.get()).thenReturn(Mono.just(user));
        when(this.logsService.findAllStatisticsByUserKeyAndCreationDate(eq(user.getHash()), any(LocalDateTime.class)))
                .thenReturn(Flux.fromIterable(List.of(stats1, stats2)));

        this.webClient
                .get()
                .uri("/logs/statistics/history")
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(StatisticsResource.class)
                .hasSize(2)
                .contains(new StatisticsResource(stats1, this.linksCollector))
                .contains(new StatisticsResource(stats2, this.linksCollector));
    }

    @Test
    public void shouldReadEmptyHistory() {
        final var user = TestFixtures.createUser(TEST_USER);

        when(this.currentUserAccessor.get()).thenReturn(Mono.just(user));
        when(this.logsService.findAllStatisticsByUserKeyAndCreationDate(eq(user.getHash()), any(LocalDateTime.class)))
                .thenReturn(Flux.empty());

        this.webClient
                .get()
                .uri("/logs/statistics/history")
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(StatisticsResource.class)
                .hasSize(0);
    }

    @Test
    public void shouldGetStatByKey() {
        final var user = TestFixtures.createUser(TEST_USER);
        final var stats = TestFixtures.createStatisticsEntity(TEST_USER);

        when(this.currentUserAccessor.get()).thenReturn(Mono.just(user));
        when(this.logsService.findStatisticsByKey(stats.getId())).thenReturn(Mono.just(stats));

        this.webClient
                .get()
                .uri("/logs/statistics/" + stats.getId())
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody(StatisticsResource.class)
                .isEqualTo(new StatisticsResource(stats, this.linksCollector));
    }

    @Test
    public void shouldGetStatByKeyFailNotFound() {
        final var user = TestFixtures.createUser(TEST_USER);
        final var statsKey = UUID.randomUUID().toString();

        when(this.currentUserAccessor.get()).thenReturn(Mono.just(user));
        when(this.logsService.findStatisticsByKey(statsKey)).thenReturn(Mono.empty());

        this.webClient
                .get()
                .uri("/logs/statistics/" + statsKey)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    public void shouldAnalyzeLogs() {
        final var analyzeQuery = new RequestAnalyzeQuery("category:ERROR", false, null, null, null, false, 1000, 0, null);
        final var mapLogsStatistics = new StdMapLogsStatistics();
        mapLogsStatistics.putOne(StdMapLogsStatistics.ALL_RECORDS_COUNT, Flux.just(12));
        mapLogsStatistics.putOne(StdMapLogsStatistics.ERRORS_COUNT, Flux.just(13));
        mapLogsStatistics.putOne(StdMapLogsStatistics.ERRORS_FREQUENCIES_BY_CATEGORY, Flux.just(Map.of("category1", 6, "category2", 9)));

        when(this.logsService.analyze(analyzeQuery)).thenReturn(Mono.just(mapLogsStatistics));

        this.webClient
                .post()
                .uri("/logs/statistics/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(analyzeQuery)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody(Map.class)
                .isEqualTo(Objects.requireNonNull(mapLogsStatistics.toResultMap().block()));
    }
}
