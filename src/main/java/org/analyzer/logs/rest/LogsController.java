package org.analyzer.logs.rest;

import lombok.extern.slf4j.Slf4j;
import org.analyzer.logs.service.LogsService;
import org.analyzer.logs.service.LogsStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/logs")
@Slf4j
public class LogsController {

    @Autowired
    private LogsService service;
    @Autowired
    private WebUtils webUtils;

    @PostMapping("/analyze")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Map<String, Object>> analyze(@RequestBody RequestAnalyzeQuery query) {
        return this.service.analyze(query)
                            .flatMap(LogsStatistics::toResultMap)
                            .onErrorResume(this::onError);
    }

    @PostMapping("/index")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> load(
            @RequestPart("file") Flux<FilePart> file,
            @RequestPart(value = "recordPatterns", required = false) LogRecordFormatResource recordPattern,
            @RequestParam(value = "pre-analyze", required = false) boolean preAnalyze) {

        final var tempFiles = file.flatMap(this.webUtils::createTempFile);

        return file
                .zipWith(tempFiles)
                .flatMap(tuple ->
                        this.service
                                .index(Mono.just(tuple.getT2()), recordPattern, preAnalyze)
                                .doOnNext(v -> tuple.getT2().delete())
                )
                .then()
                .onErrorResume(this::onError);
    }

    @PostMapping("/query")
    @ResponseStatus(HttpStatus.OK)
    public Mono<LogRecordsCollectionResource> read(@RequestBody RequestSearchQuery query) {
        return this.service.searchByQuery(query)
                            .collectList()
                            .map(LogRecordsCollectionResource::new)
                            .onErrorResume(this::onError);
    }

    private <T> Mono<T> onError(final Throwable ex) {
        log.error("", ex);
        return Mono.error(ex);
    }
}
