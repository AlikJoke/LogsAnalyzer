package org.parser.app.service;

import org.parser.app.model.LogRecord;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;

public interface LogRecordParser {

    @Nonnull
    Flux<LogRecord> parse(
            @Nonnull Mono<File> logFile,
            @Nonnull String fileName,
            @Nullable LogRecordFormat recordFormat);
}
