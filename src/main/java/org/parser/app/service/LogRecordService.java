package org.parser.app.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;

public interface LogRecordService {

    Mono<Void> index(@Nonnull Mono<File> logFile, @Nonnull String originalLogFileName, @Nullable String logRecordPattern);

    Mono<Void> dropIndex();

    @Nonnegative
    Mono<Long> getAllRecordsCount();

    @Nonnull
    Flux<String> searchByQuery(@Nonnull SearchQuery query);
}
