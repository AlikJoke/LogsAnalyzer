package org.analyzer.logs.rest.users;

import org.analyzer.logs.model.UserEntity;
import org.analyzer.logs.service.UserService;
import org.analyzer.logs.service.elastic.ElasticLogsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.security.Principal;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private static final Logger logger = Loggers.getLogger(ElasticLogsService.class);

    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> create(@RequestBody Mono<UserResource> resource) {
        final var user = resource.map(
                userResource -> userResource.composeEntity(this.passwordEncoder)
        );

        return this.userService.create(user)
                                .log(logger)
                                .then()
                                .onErrorResume(this::onError);
    }

    @DeleteMapping("/disable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> disable(Mono<Principal> principal) {
        return principal
                .map(Principal::getName)
                .log(logger)
                .flatMap(this.userService::disable)
                .onErrorResume(this::onError);
    }

    @PutMapping("/update")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<UserResource> update(@RequestBody Mono<UserResource> resource) {
        final var user = resource.flatMap(
                userResource ->
                        this.userService
                                .findById(userResource.username())
                                .map(u -> userResource.update(u, this.passwordEncoder))
        );

        return this.userService.update(user)
                                .map(UserResource::convertFrom)
                                .onErrorResume(this::onError);
    }

    private <T> Mono<T> onError(final Throwable ex) {
        logger.error("", ex);
        return Mono.error(ex);
    }
}
