package org.analyzer.logs.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;

public interface LogsService {

    @Nonnull
    Mono<Void> index(
            @Nonnull Mono<File> logFile,
            @Nonnull String originalLogFileName,
            @Nullable LogRecordFormat patternFormat);

    @Nonnull
    Flux<String> searchByQuery(@Nonnull SearchQuery query);

    @Nonnull
    Mono<LogsStatistics> analyze(@Nonnull SearchQuery query);
}
