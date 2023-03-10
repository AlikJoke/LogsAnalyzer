package org.analyzer.rest.users;

import lombok.NonNull;
import org.analyzer.rest.AnonymousRootEntrypointResource;
import org.analyzer.rest.ControllerBase;
import org.analyzer.rest.RootEntrypointResource;
import org.analyzer.rest.hateoas.LinksCollector;
import org.analyzer.rest.hateoas.NamedEndpoint;
import org.analyzer.service.users.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Set;

@RestController
@RequestMapping(UserController.BASE_PATH)
public class UserController extends ControllerBase {

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
    public UserResource read(@NonNull Principal principal) {
        final var user = this.userService.findById(principal.getName());
        return UserResource.convertFrom(user, this.linksCollector);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @NamedEndpoint(value = "create.user", includeTo = AnonymousRootEntrypointResource.class)
    public UserResource create(@RequestBody UserResource resource) {
        final var user = this.userService.create(resource.composeEntity(this.passwordEncoder));
        return UserResource.convertFrom(user, this.linksCollector);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @NamedEndpoint(value = "disable", includeTo = UserResource.class)
    public void disable(Principal principal) {
        this.userService.disable(principal.getName());
    }

    @PutMapping
    @NamedEndpoint(value = "edit", includeTo = UserResource.class)
    public UserResource update(@RequestBody UserResource resource) {
        final var user = this.userService.findById(resource.username());
        resource.update(user, this.passwordEncoder);

        final var updatedUser = this.userService.update(user);
        return UserResource.convertFrom(updatedUser, this.linksCollector);
    }

    @Override
    protected Set<HttpMethod> supportedMethods() {
        return Set.of(HttpMethod.OPTIONS, HttpMethod.GET, HttpMethod.DELETE, HttpMethod.PUT, HttpMethod.POST);
    }
}
