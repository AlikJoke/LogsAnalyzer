package org.analyzer.rest.queries;

import org.analyzer.rest.ControllerBase;
import org.analyzer.rest.hateoas.LinksCollector;
import org.analyzer.rest.hateoas.NamedEndpoint;
import org.analyzer.rest.users.UserResource;
import org.analyzer.service.queries.UserQueriesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;

@RestController
@RequestMapping(UserSearchQueriesController.PATH_BASE)
public class UserSearchQueriesController extends ControllerBase {

    static final String PATH_BASE = "/queries";

    @Autowired
    private UserQueriesService userQueryService;
    @Autowired
    @Lazy
    private LinksCollector linksCollector;

    @DeleteMapping("/{queryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @NamedEndpoint(value = "delete", includeTo = UserQueryResource.class)
    public void delete(@PathVariable("queryId") String queryId) {
        this.userQueryService.delete(queryId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @NamedEndpoint(value = "delete", includeTo = UserResource.class)
    public void delete() {
        this.userQueryService.deleteAll();
    }

    @GetMapping
    @NamedEndpoint(value = "queries", includeTo = UserResource.class)
    @NamedEndpoint(value = "self", includeTo = UserQueriesCollection.class)
    public UserQueriesCollection readQueries(
            @RequestParam(value = "from", required = false) LocalDateTime fromParam,
            @RequestParam(value = "to", required = false) LocalDateTime toParam) {
        final var from = fromParam == null
                ? LocalDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC)
                : fromParam;
        final var to = toParam == null ? LocalDateTime.now() : toParam;
        final var queries = this.userQueryService.findAll(from, to)
                                                    .stream()
                                                    .map(query -> new UserQueryResource(query, this.linksCollector))
                                                    .toList();
        return new UserQueriesCollection(queries, this.linksCollector.collectFor(UserQueriesCollection.class));
    }

    @Override
    protected Set<HttpMethod> supportedMethods() {
        return Set.of(HttpMethod.OPTIONS, HttpMethod.GET, HttpMethod.DELETE);
    }
}
