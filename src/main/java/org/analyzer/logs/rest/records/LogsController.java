package org.analyzer.logs.rest.records;

import org.analyzer.logs.rest.ControllerBase;
import org.analyzer.logs.rest.RootEntrypointResource;
import org.analyzer.logs.rest.hateoas.LinksCollector;
import org.analyzer.logs.rest.hateoas.NamedEndpoint;
import org.analyzer.logs.service.LogsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Set;

@RestController
@RequestMapping(LogsController.PATH_BASE)
public class LogsController extends ControllerBase {

    static final String PATH_BASE = "/logs";
    static final String PATH_INDEX = "/index";
    static final String PATH_SEARCH = "/query";

    @Autowired
    private LogsService service;
    @Autowired
    private WebUtils webUtils;
    @Autowired
    @Lazy
    private LinksCollector linksCollector;

    @PostMapping(PATH_INDEX)
    @ResponseStatus(HttpStatus.CREATED)
    @NamedEndpoint(value = "index.logs", includeTo = RootEntrypointResource.class)
    public Mono<IndexingResult> load(
            @RequestPart("file") Mono<FilePart> file,
            @RequestPart(value = "record_patterns", required = false) LogRecordFormatResource recordPattern) {

        final var tempFiles = file.flatMap(this.webUtils::createTempFile);

        return tempFiles
                .flatMap(tempFile ->
                        this.service
                                .index(Mono.just(tempFile), recordPattern)
                                .doOnNext(v -> tempFile.delete())
                )
                .map(indexingKey -> new IndexingResult(indexingKey, this.linksCollector.collectFor(IndexingResult.class)))
                .onErrorResume(this::onError);
    }

    @PostMapping(PATH_SEARCH)
    @ResponseStatus(HttpStatus.OK)
    @NamedEndpoint(value = "search", includeTo = RootEntrypointResource.class)
    public Mono<LogRecordsCollectionResource> read(@RequestBody RequestSearchQuery query) {
        return this.service.searchByQuery(query)
                            .collectList()
                            .map(LogRecordsCollectionResource::new)
                            .onErrorResume(this::onError);
    }

    @Override
    protected Set<HttpMethod> supportedMethods() {
        return Set.of(HttpMethod.OPTIONS, HttpMethod.POST);
    }
}
