package org.analyzer.logs.service;

import org.analyzer.logs.model.LogsStatisticsEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Map;

public interface LogsService {

    @Nonnull
    Mono<String> index(
            @Nonnull Mono<File> logFile,
            @Nullable LogRecordFormat patternFormat,
            boolean preAnalyze);

    @Nonnull
    Flux<String> searchByQuery(@Nonnull SearchQuery query);

    @Nonnull
    Mono<LogsStatistics> analyze(@Nonnull AnalyzeQuery query);

    @Nonnull
    Mono<Map<String, Object>> findStatisticsByKey(@Nonnull String key);
}
