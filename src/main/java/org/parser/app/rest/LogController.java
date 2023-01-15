package org.parser.app.rest;

import org.parser.app.service.LogRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class LogController {

    @Autowired
    private LogRecordService service;

    @PostMapping("/index")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void load(
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "recordPattern", required = false) String recordPattern) throws IOException {

        final var fileName = file.getOriginalFilename();
        final var logFile = File.createTempFile(fileName, null);
        try {
            file.transferTo(logFile);
            this.service.index(logFile, fileName, recordPattern);
        } finally {
            logFile.delete();
        }
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete() {
        this.service.dropIndex();
    }

    @GetMapping("/count")
    public Long readAllCount() {
        return this.service.getAllRecordsCount();
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<String> read(@RequestParam("query") String query) {
        return this.service.getRecordsByFilter(query);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public List<String> read(@RequestBody RequestQuery query) {
        return this.read(query.query());
    }
}
