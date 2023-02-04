package org.analyzer.logs.test.rest;

import org.analyzer.logs.rest.AnonymousRootEntrypointResource;
import org.analyzer.logs.rest.ResourceLink;
import org.analyzer.logs.rest.RootEntrypointController;
import org.analyzer.logs.rest.RootEntrypointResource;
import org.analyzer.logs.rest.hateoas.LinksCollector;
import org.analyzer.logs.rest.stats.LogsStatisticsController;
import org.analyzer.logs.rest.users.UserController;
import org.analyzer.logs.rest.users.UserResource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@Import(LinksCollectorTest.TestContext.class)
public class LinksCollectorTest {

    private static final String API_PREFIX = "/api";

    @Autowired
    private LinksCollector linksCollector;

    @Test
    public void shouldCollectUserResourceLinks() {

        final List<ResourceLink> links = this.linksCollector.collectFor(UserResource.class);
        assertNotNull(links, "User links empty");

        final Map<String, ResourceLink> linksMap = links.stream().collect(Collectors.toMap(ResourceLink::rel, Function.identity()));

        makeLinkChecks(linksMap.get("self"), "Self link not found", RequestMethod.GET, "/user");
        makeLinkChecks(linksMap.get("edit"), "Edit link not found", RequestMethod.PUT, "/user");
        makeLinkChecks(linksMap.get("disable"), "Disable link not found", RequestMethod.DELETE, "/user");
        makeLinkChecks(linksMap.get("statistics.history"), "Stats history link not found", RequestMethod.GET, "/logs/statistics/history");
    }

    @Test
    public void shouldCollectRootEntrypointResourceLinks() {

        final List<ResourceLink> links = this.linksCollector.collectFor(RootEntrypointResource.class);
        assertNotNull(links, "Root links empty");

        final Map<String, ResourceLink> linksMap = links.stream().collect(Collectors.toMap(ResourceLink::rel, Function.identity()));

        makeLinkChecks(linksMap.get("current.user"), "Current user link not found", RequestMethod.GET, "/user");
        makeLinkChecks(linksMap.get("analyze.logs"), "Analyze logs ink not found", RequestMethod.POST, "/logs/statistics/generate");
    }

    @Test
    public void shouldCollectRootAnonEntrypointResourceLinks() {

        final List<ResourceLink> links = this.linksCollector.collectFor(AnonymousRootEntrypointResource.class);
        assertNotNull(links, "Anonymous links empty");

        final Map<String, ResourceLink> linksMap = links.stream().collect(Collectors.toMap(ResourceLink::rel, Function.identity()));

        makeLinkChecks(linksMap.get("create.user"), "Create user link not found", RequestMethod.POST, "/user");
    }

    private void makeLinkChecks(
            final ResourceLink link,
            final String messageOnNull,
            final RequestMethod method,
            final String hrefPart) {
        assertNotNull(link, messageOnNull);
        assertEquals(link.method(), method);
        assertTrue(link.href().startsWith(API_PREFIX + hrefPart));
    }

    @Configuration
    static class TestContext {

        @MockBean
        private UserController userController;
        @MockBean
        private RootEntrypointController rootEntrypointController;
        @MockBean
        private LogsStatisticsController statisticsController;

        @Bean
        public LinksCollector linksCollector(ApplicationContext applicationContext) {
            return new LinksCollector(applicationContext, API_PREFIX);
        }
    }
}
