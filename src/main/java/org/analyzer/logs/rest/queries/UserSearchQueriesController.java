package org.analyzer.logs.rest.queries;

import org.analyzer.logs.rest.ControllerBase;
import org.analyzer.logs.rest.hateoas.LinksCollector;
import org.analyzer.logs.rest.hateoas.NamedEndpoint;
import org.analyzer.logs.rest.users.UserResource;
import org.analyzer.logs.service.CurrentUserQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;

@RestController
@RequestMapping(UserSearchQueriesController.PATH_BASE)
public class UserSearchQueriesController extends ControllerBase {

    static final String PATH_BASE = "/queries";

    @Autowired
    private CurrentUserQueryService userQueryService;
    @Autowired
    @Lazy
    private LinksCollector linksCollector;

    @DeleteMapping("/{queryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @NamedEndpoint(value = "delete", includeTo = UserQueryResource.class)
    public Mono<Void> delete(@PathVariable("queryId") String queryId) {
        return this.userQueryService.delete(queryId)
                                    .switchIfEmpty(Mono.error(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND)))
                                    .onErrorResume(this::onError)
                                    .then();
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @NamedEndpoint(value = "delete", includeTo = UserResource.class)
    public Mono<Void> delete() {
        return this.userQueryService.deleteAll()
                                    .onErrorResume(this::onError);
    }

    @GetMapping
    @NamedEndpoint(value = "queries", includeTo = UserResource.class)
    @NamedEndpoint(value = "self", includeTo = UserQueriesCollection.class)
    public Mono<UserQueriesCollection> readQueries(
            @RequestParam(value = "from", required = false) LocalDateTime fromParam,
            @RequestParam(value = "to", required = false) LocalDateTime toParam) {
        final var from = fromParam == null
                ? LocalDateTime.ofInstant(Instant.EPOCH, ZoneId.systemDefault())
                : fromParam;
        final var to = toParam == null ? LocalDateTime.now() : toParam;
        return this.userQueryService.findAll(from, to)
                .map(query -> new UserQueryResource(query, this.linksCollector))
                .collectList()
                .map(queries -> new UserQueriesCollection(queries, this.linksCollector.collectFor(UserQueriesCollection.class)))
                .onErrorResume(this::onError);
    }

    @Override
    protected Set<HttpMethod> supportedMethods() {
        return Set.of(HttpMethod.OPTIONS, HttpMethod.GET, HttpMethod.DELETE);
    }
}
