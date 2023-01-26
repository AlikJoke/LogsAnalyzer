package org.analyzer.logs.rest.users;

import lombok.extern.slf4j.Slf4j;
import org.analyzer.logs.model.UserEntity;
import org.analyzer.logs.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RestController
@RequestMapping("/api/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> create(@RequestBody Mono<UserResource> resource) {
        final Mono<UserEntity> user = resource.map(
                userResource -> userResource.toEntity(this.passwordEncoder)
        );

        return this.userService.create(user)
                                .then()
                                .onErrorResume(this::onError);
    }

    @DeleteMapping("/disable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> disable(Mono<Principal> principal) {
        return principal
                .map(Principal::getName)
                .flatMap(this.userService::disable)
                .onErrorResume(this::onError);
    }

    @PutMapping("/update")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<UserResource> update(@RequestBody Mono<UserResource> resource) {
        final Mono<UserEntity> user = resource.map(
                userResource -> userResource.toEntity(this.passwordEncoder)
        );

        return this.userService.update(user)
                                .map(UserResource::convertFrom)
                                .onErrorResume(this::onError);
    }

    private <T> Mono<T> onError(final Throwable ex) {
        log.error("", ex);
        return Mono.error(ex);
    }
}
