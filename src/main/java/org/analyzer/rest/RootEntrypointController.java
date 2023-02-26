package org.analyzer.rest;

import org.analyzer.rest.hateoas.LinksCollector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootEntrypointController extends ControllerBase {

    static final String PATH = "/";
    static final String PATH_ANON = "/anonymous";

    @Autowired
    @Lazy
    private LinksCollector linksCollector;

    @GetMapping(PATH)
    public RootEntrypointResource read() {
        return new RootEntrypointResource(this.linksCollector.collectFor(RootEntrypointResource.class));
    }

    @GetMapping(PATH_ANON)
    public AnonymousRootEntrypointResource readAnonymous() {
        return new AnonymousRootEntrypointResource(this.linksCollector.collectFor(AnonymousRootEntrypointResource.class));
    }
}
