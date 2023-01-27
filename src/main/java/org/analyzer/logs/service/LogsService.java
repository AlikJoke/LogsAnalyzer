package org.analyzer.logs.service;

import lombok.NonNull;
import org.analyzer.logs.model.LogsStatisticsEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.time.LocalDateTime;

public interface LogsService {

    @Nonnull
    Mono<String> index(
            @Nonnull Mono<File> logFile,
            @Nullable LogRecordFormat patternFormat);

    @Nonnull
    Flux<String> searchByQuery(@Nonnull SearchQuery query);

    @Nonnull
    Mono<MapLogsStatistics> analyze(@Nonnull AnalyzeQuery query);

    @Nonnull
    Mono<LogsStatisticsEntity> findStatisticsByKey(@Nonnull String key);

    @Nonnull
    Flux<LogsStatisticsEntity> findAllStatisticsByUserKeyAndCreationDate(
            @Nonnull String userKey,
            @Nonnull LocalDateTime beforeDate);

    @Nonnull
    Mono<Void> deleteStatistics(@Nonnull Flux<LogsStatisticsEntity> statsFlux);

    @Nonnull
    Flux<String> deleteAllStatisticsByUserKeyAndCreationDate(
            @NonNull String userKey,
            @NonNull LocalDateTime beforeDate);

    @Nonnull
    Mono<Void> deleteByQuery(@Nonnull SearchQuery deleteQuery);
}
