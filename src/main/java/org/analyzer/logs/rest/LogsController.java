package org.analyzer.logs.rest;

import lombok.extern.slf4j.Slf4j;
import org.analyzer.logs.service.LogsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

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
    public Mono<Map<String, Object>> analyze(@RequestBody RequestSearchQuery query) {
        return this.service.analyze(query)
                            .flatMapIterable(Map::entrySet)
                            .flatMap(
                                    e -> Mono.just(e.getKey())
                                                .zipWith(e.getValue()
                                                                .collectList()
                                                                .filter(Predicate.not(List::isEmpty))
                                                                .map(this.webUtils::prepareToResponse)
                                                )
                            )
                            .collectMap(Tuple2::getT1, Tuple2::getT2)
                            .onErrorResume(this::onError);
    }

    @PostMapping("/index")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> load(
            @RequestPart("file") Flux<FilePart> file,
            @RequestPart(value = "recordPatterns", required = false) LogRecordFormatResource recordPattern) {

        final Flux<File> tempFiles = file.flatMap(this.webUtils::createTempFile);

        return file
                .zipWith(tempFiles)
                .flatMap(tuple ->
                        this.service
                                .index(Mono.just(tuple.getT2()), tuple.getT1().filename(), recordPattern)
                                .doOnNext(v -> tuple.getT2().delete())
                )
                .then()
                .onErrorResume(this::onError);
    }

    @PostMapping("/query")
    @ResponseStatus(HttpStatus.OK)
    public Mono<LogRecordsCollectionResource> read(@RequestBody RequestSearchQuery query) {
        if (!query.aggregations().isEmpty()) {
            return onError(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Aggregation isn't allowed in search query"));
        }

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
