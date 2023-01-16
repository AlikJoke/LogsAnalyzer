package org.parser.app.rest;

import org.parser.app.service.LogRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@RestController
@RequestMapping("/api/logs")
public class LogController {

    @Autowired
    private LogRecordService service;

    @PostMapping("/index")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> load(
            @RequestPart("file") Flux<FilePart> file,
            @RequestPart("recordPattern") String recordPattern) {

        final Flux<File> tempFiles = file.flatMap(this::createTempFile);

        return file
                .zipWith(tempFiles)
                .map(tuple ->
                        this.service
                                .index(Mono.just(tuple.getT2()), tuple.getT1().filename(), recordPattern)
                                .doOnNext(v -> tuple.getT2().delete())
                )
                .then();
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete() {
        return this.service.dropIndex();
    }

    @GetMapping("/count")
    public Mono<Long> readAllCount() {
        return this.service.getAllRecordsCount();
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Flux<String> read(@RequestParam("query") String query) {
        return this.service.getRecordsByFilter(query);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public Flux<String> read(@RequestBody RequestQuery query) {
        return this.read(query.query());
    }

    private Mono<File> createTempFile(final FilePart filePart) {
        try {
            final File result = Files.createTempFile(filePart.filename(), null).toFile();
            return filePart.transferTo(result).thenReturn(result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
