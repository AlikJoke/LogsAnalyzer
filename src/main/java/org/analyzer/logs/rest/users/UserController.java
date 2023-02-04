package org.analyzer.logs.rest.users;

import org.analyzer.logs.rest.AnonymousRootEntrypointResource;
import org.analyzer.logs.rest.ControllerBase;
import org.analyzer.logs.rest.RootEntrypointResource;
import org.analyzer.logs.rest.hateoas.LinksCollector;
import org.analyzer.logs.rest.hateoas.NamedEndpoint;
import org.analyzer.logs.service.UserService;
import org.analyzer.logs.service.elastic.ElasticLogsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.security.Principal;
import java.util.Set;

@RestController
@RequestMapping(UserController.BASE_PATH)
public class UserController extends ControllerBase {

    private static final Logger logger = Loggers.getLogger(ElasticLogsService.class);

    static final String BASE_PATH = "/user";

    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    @Lazy
    private LinksCollector linksCollector;

    @GetMapping
    @NamedEndpoint(value = "self", includeTo = UserResource.class)
    @NamedEndpoint(value = "current.user", includeTo = RootEntrypointResource.class)
    public Mono<UserResource> read(Mono<Principal> principalMono) {
        return principalMono
                .map(Principal::getName)
                .flatMap(this.userService::findById)
                .map(user -> UserResource.convertFrom(user, this.linksCollector))
                .onErrorResume(this::onError);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @NamedEndpoint(value = "create.user", includeTo = AnonymousRootEntrypointResource.class)
    public Mono<UserResource> create(@RequestBody Mono<UserResource> resource) {
        final var user = resource.map(
                userResource -> userResource.composeEntity(this.passwordEncoder)
        );

        return this.userService.create(user)
                                .log(logger)
                                .map(created -> UserResource.convertFrom(created, this.linksCollector))
                                .onErrorResume(this::onError);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @NamedEndpoint(value = "disable", includeTo = UserResource.class)
    public Mono<Void> disable(Mono<Principal> principal) {
        return principal
                .map(Principal::getName)
                .log(logger)
                .flatMap(this.userService::disable)
                .onErrorResume(this::onError);
    }

    @PutMapping
    @NamedEndpoint(value = "edit", includeTo = UserResource.class)
    public Mono<UserResource> update(@RequestBody Mono<UserResource> resource) {
        final var user = resource.flatMap(
                userResource ->
                        this.userService
                                .findById(userResource.username())
                                .map(u -> userResource.update(u, this.passwordEncoder))
        );

        return this.userService.update(user)
                                .map(updatedUser -> UserResource.convertFrom(updatedUser, this.linksCollector))
                                .onErrorResume(this::onError);
    }

    @Override
    protected Set<HttpMethod> supportedMethods() {
        return Set.of(HttpMethod.OPTIONS, HttpMethod.GET, HttpMethod.DELETE, HttpMethod.PUT, HttpMethod.POST);
    }
}
