package org.analyzer.logs.test.rest;

import org.analyzer.logs.rest.hateoas.LinksCollector;
import org.analyzer.logs.rest.records.*;
import org.analyzer.logs.service.LogRecordFormat;
import org.analyzer.logs.service.LogsService;
import org.analyzer.logs.test.rest.config.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.analyzer.logs.test.fixtures.TestFixtures.TEST_USER;
import static org.analyzer.logs.test.rest.config.TestSecurityConfig.USER_ROLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@WebFluxTest(LogsController.class)
@Import({TestSecurityConfig.class, WebUtils.class})
@WithMockUser(username = TEST_USER, authorities = USER_ROLE)
public class LogsControllerTest {

    @Autowired
    private WebTestClient webClient;
    @MockBean
    private LogsService logsService;
    @MockBean
    private LinksCollector linksCollector;

    @Test
    public void shouldSearchLogRecordsByQuery() {

        final var searchQuery = new RequestSearchQuery("category:ERROR", false, null, 1000, 0, null);

        final var records = List.of("1", "2", "3", "4");
        when(this.logsService.searchByQuery(searchQuery)).thenReturn(Flux.fromIterable(records));

        this.webClient
                .post()
                .uri("/logs/query")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(searchQuery)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody(LogRecordsCollectionResource.class)
                .isEqualTo(new LogRecordsCollectionResource(records, new Paging(0, records.size())));
    }

    @Test
    public void shouldLoadLogFiles() throws IOException {

        final var indexingKey = UUID.randomUUID().toString();
        final var file = new ClassPathResource("/test.log");
        final var fileLength = file.getFile().length();
        final var body = generateBody(file);
        when(this.logsService.index(any(), nullable(LogRecordFormat.class)))
                .thenReturn(Mono.just(indexingKey));

        this.webClient
                .post()
                .uri("/logs/index")
                .body(BodyInserters.fromMultipartData(generateBody(file)))
                .exchange()
                .expectStatus()
                .isCreated()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody(IndexingResult.class)
                .isEqualTo(new IndexingResult(indexingKey, this.linksCollector.collectFor(IndexingResult.class)));

        final var captor = ArgumentCaptor.forClass(File.class);
        verify(this.logsService).index(captor.capture(), nullable(LogRecordFormat.class));
        assertEquals(captor.getValue().getName(), file.getFilename());
        assertFalse(captor.getValue().exists());
    }

    private MultiValueMap<String, HttpEntity<?>> generateBody(final ClassPathResource file) {
        var builder = new MultipartBodyBuilder();
        builder.part("file", file);
        return builder.build();
    }
}
