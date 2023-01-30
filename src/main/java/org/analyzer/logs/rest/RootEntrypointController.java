package org.analyzer.logs.rest;

import org.analyzer.logs.rest.hateoas.LinksCollector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class RootEntrypointController extends ControllerBase {

    static final String PATH = "/";
    static final String PATH_ANON = "/anonymous";

    @Autowired
    @Lazy
    private LinksCollector linksCollector;

    @GetMapping(PATH)
    public Mono<RootEntrypointResource> read() {
        return Mono.just(new RootEntrypointResource(this.linksCollector.collectFor(RootEntrypointResource.class)));
    }

    @GetMapping(PATH_ANON)
    public Mono<AnonymousRootEntrypointResource> readAnonymous() {
        return Mono.just(new AnonymousRootEntrypointResource(this.linksCollector.collectFor(AnonymousRootEntrypointResource.class)));
    }
}
