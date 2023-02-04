package org.analyzer.logs.test.rest;

import org.analyzer.logs.rest.hateoas.LinksCollector;
import org.analyzer.logs.rest.stats.LogsStatisticsController;
import org.analyzer.logs.service.CurrentUserAccessor;
import org.analyzer.logs.service.LogsService;
import org.analyzer.logs.test.rest.config.TestSecurityConfig;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

@ExtendWith(SpringExtension.class)
@WebFluxTest(LogsStatisticsController.class)
@Import(TestSecurityConfig.class)
public class LogsStatisticsControllerTest {

    private static final String TEST_USER = "test";

    @Autowired
    private WebTestClient webClient;
    @MockBean
    private LogsService logsService;
    @MockBean
    private CurrentUserAccessor currentUserAccessor;
    @MockBean
    private LinksCollector linksCollector;

    // TODO
}
