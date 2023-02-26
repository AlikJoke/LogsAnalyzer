package org.analyzer.rest.har;

import com.fasterxml.jackson.databind.JsonNode;
import org.analyzer.entities.HttpArchiveEntity;
import org.analyzer.rest.ControllerBase;
import org.analyzer.rest.RootEntrypointResource;
import org.analyzer.rest.hateoas.LinksCollector;
import org.analyzer.rest.hateoas.NamedEndpoint;
import org.analyzer.rest.users.UserController;
import org.analyzer.rest.util.WebUtils;
import org.analyzer.service.har.HttpArchiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.analyzer.rest.har.HttpArchiveController.PATH_BASE;

@RestController
@RequestMapping(PATH_BASE)
public class HttpArchiveController extends ControllerBase {

    static final String PATH_BASE = "/har";
    static final String PATH_ANALYZE = "/analyze";
    static final String PATH_GROUP_LOGS = "/group-logs";
    static final String PATH_APPLY = "/apply";

    @Autowired
    private HttpArchiveService service;
    @Autowired
    private WebUtils webUtils;
    @Autowired
    @Lazy
    private LinksCollector linksCollector;

    @GetMapping("/{id}")
    @NamedEndpoint(value = "self", includeTo = HttpArchiveResource.class)
    public HttpArchiveResource read(@PathVariable("id") String id) {
        final var har = this.service.findById(id);
        return new HttpArchiveResource(har.getId(), har.getTitle(), har.getBodyNode(), this.linksCollector.collectFor(HttpArchiveResource.class));
    }

    @GetMapping
    @NamedEndpoint(value = "self", includeTo = HttpArchivesCollection.class)
    @NamedEndpoint(value = "har.collection", includeTo = UserController.class)
    public HttpArchivesCollection readAll(@RequestParam("page") int page, @RequestParam("pageSize") int pageSize) {
        final var collection =
                this.service.findAll(PageRequest.of(page, pageSize))
                            .stream()
                            .map(har ->
                                    new HttpArchiveResource(
                                            har.getId(),
                                            har.getTitle(),
                                            har.getBodyNode(),
                                            this.linksCollector.collectFor(HttpArchiveResource.class)
                                    )
                            )
                            .toList();
        return new HttpArchivesCollection(collection, this.linksCollector.collectFor(HttpArchivesCollection.class));
    }

    @PostMapping("/{id}" + PATH_ANALYZE)
    @NamedEndpoint(value = "analyze.har", includeTo = HttpArchiveResource.class)
    public Map<String, Object> analyze(
            @PathVariable("id") String id,
            @RequestBody(required = false) HttpArchiveRequestQuery query) {
        return this.service.analyze(id, query);
    }

    @PostMapping(PATH_ANALYZE)
    @NamedEndpoint(value = "analyze.har", includeTo = RootEntrypointResource.class)
    public Map<String, Object> analyze(
            @RequestPart(value = "file") MultipartFile har,
            @RequestPart(value = "operations", required = false) HttpArchiveRequestQuery query) {
        final var tempFile = this.webUtils.createTempFile(har);
        try {
            return this.service.analyze(tempFile, query);
        } finally {
            tempFile.delete();
        }
    }

    @PostMapping("/{id}" + PATH_GROUP_LOGS)
    @NamedEndpoint(value = "group.logs.by.requests", includeTo = HttpArchiveResource.class)
    public Map<JsonNode, List<String>> groupLogsByRequests(
            @PathVariable("id") String id,
            @RequestBody(required = false) HttpArchiveRequestQuery query) {
        return this.service.groupLogRecordsByRequests(id, query);
    }

    @PostMapping(PATH_GROUP_LOGS)
    @NamedEndpoint(value = "group.logs.by.requests", includeTo = RootEntrypointResource.class)
    public Map<JsonNode, List<String>> groupLogsByRequests(
            @RequestPart(value = "file") MultipartFile har,
            @RequestPart(value = "operations", required = false) HttpArchiveRequestQuery query) {
        final var tempFile = this.webUtils.createTempFile(har);
        try {
            return this.service.groupLogRecordsByRequests(tempFile, query);
        } finally {
            tempFile.delete();
        }
    }

    @PostMapping("/{id}" + PATH_APPLY)
    @NamedEndpoint(value = "ops.har", includeTo = HttpArchiveResource.class)
    public ResponseEntity<?> applyOps(
            @PathVariable("id") String id,
            @RequestBody(required = false) HttpArchiveRequestQuery query) throws IOException {
        final var har = this.service.applyOperations(id, query);
        return this.webUtils.prepareResponse(query.exportToFile(), har.body());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @NamedEndpoint(value = "delete.har", includeTo = HttpArchiveResource.class)
    public void delete(@PathVariable("id") String id) {
        this.service.deleteById(id);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @NamedEndpoint(value = "delete.har.all", includeTo = RootEntrypointResource.class)
    public void delete() {
        this.service.deleteAll();
    }

    @PostMapping(PATH_APPLY)
    @NamedEndpoint(value = "ops.har", includeTo = RootEntrypointResource.class)
    public JsonNode applyOps(
            @RequestPart(value = "file") MultipartFile har,
            @RequestPart(value = "operations", required = false) HttpArchiveRequestQuery query) {
        final var tempFile = this.webUtils.createTempFile(har);
        try {
            return this.service.applyOperations(tempFile, query).body();
        } finally {
            tempFile.delete();
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @NamedEndpoint(value = "save.har", includeTo = RootEntrypointResource.class)
    public HttpArchivesCollection create(@RequestPart(value = "file") MultipartFile har) {
        final var tempFile = this.webUtils.createTempFile(har);
        try {
            final var collection =
                    this.service.create(tempFile)
                                .stream()
                                .map(HttpArchiveEntity::getId)
                                .map(this.service::findById)
                                .map(harEntity ->
                                        new HttpArchiveResource(
                                                harEntity.getId(),
                                                harEntity.getTitle(),
                                                harEntity.getBodyNode(),
                                                this.linksCollector.collectFor(HttpArchiveResource.class)
                                        )
                                )
                                .toList();
            return new HttpArchivesCollection(collection, this.linksCollector.collectFor(HttpArchivesCollection.class));
        } finally {
            tempFile.delete();
        }
    }

    @Override
    protected Set<HttpMethod> supportedMethods() {
        return Set.of(HttpMethod.OPTIONS, HttpMethod.POST, HttpMethod.DELETE, HttpMethod.GET);
    }
}
