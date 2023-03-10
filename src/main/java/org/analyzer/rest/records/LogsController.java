package org.analyzer.rest.records;

import lombok.extern.slf4j.Slf4j;
import org.analyzer.rest.ControllerBase;
import org.analyzer.rest.RootEntrypointResource;
import org.analyzer.rest.hateoas.LinksCollector;
import org.analyzer.rest.hateoas.NamedEndpoint;
import org.analyzer.rest.util.WebUtils;
import org.analyzer.service.logs.LogsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

@RestController
@RequestMapping(LogsController.PATH_BASE)
@Slf4j
public class LogsController extends ControllerBase {

    static final String PATH_BASE = "/logs";
    static final String PATH_INDEX = "/index";
    static final String PATH_SEARCH = "/query";
    static final String PATH_EXPORT = "/export";

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
    public DeferredResult<IndexingResult> load(
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "record_patterns", required = false) LogRecordFormatResource recordPattern) {

        final DeferredResult<IndexingResult> deferredResult = new DeferredResult<>();
        deferredResult.onError(ex -> log.error("", ex));

        final var tempFile = this.webUtils.createTempFile(file);
        this.service.index(tempFile, recordPattern).whenComplete((indexingKey, ex) -> {
            tempFile.delete();

            if (ex != null) {
                deferredResult.setErrorResult(ex);
            } else {
                deferredResult.setResult(new IndexingResult(indexingKey, this.linksCollector.collectFor(IndexingResult.class)));
            }
        });

        return deferredResult;
    }

    @PostMapping(PATH_SEARCH)
    @ResponseStatus(HttpStatus.OK)
    @NamedEndpoint(value = "search", includeTo = RootEntrypointResource.class)
    public LogRecordsCollectionResource read(@RequestBody RequestSearchQuery query) {
        final var records = this.service.searchByQuery(query);
        return new LogRecordsCollectionResource(records, new Paging(query.pageNumber(), records.size()));
    }

    @PostMapping(PATH_EXPORT)
    @ResponseStatus(HttpStatus.OK)
    @NamedEndpoint(value = "export.logs", includeTo = RootEntrypointResource.class)
    public ResponseEntity<Resource> exportToFile(@RequestBody RequestSearchQuery query) throws IOException {
        final var logsFile = this.service.searchAndExportByQuery(query);
        final var targetFilename = StringUtils.hasLength(query.exportToFile()) ? query.exportToFile() : "data.log";
        return this.webUtils.prepareResponse(targetFilename, new FileSystemResource(logsFile));
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @NamedEndpoint(value = "delete.logs", includeTo = RootEntrypointResource.class)
    public void delete(@RequestBody RequestSearchQuery query) {
        this.service.deleteByQuery(query);
    }

    @Override
    protected Set<HttpMethod> supportedMethods() {
        return Set.of(HttpMethod.OPTIONS, HttpMethod.POST, HttpMethod.DELETE);
    }
}
