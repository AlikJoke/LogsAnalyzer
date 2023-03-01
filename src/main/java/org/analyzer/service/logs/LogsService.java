package org.analyzer.service.logs;

import lombok.NonNull;
import org.analyzer.entities.LogsStatisticsEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface LogsService {

    @Nonnull
    CompletableFuture<String> index(
            @Nonnull File logFile,
            @Nullable LogRecordFormat patternFormat);

    @Nonnull
    List<String> searchByQuery(@Nonnull SearchQuery query);

    @Nonnull
    File searchAndExportByQuery(@Nonnull SearchQuery query);

    @Nonnull
    MapLogsStatistics analyze(@Nonnull AnalyzeQuery query);

    @Nonnull
    Optional<LogsStatisticsEntity> findStatisticsByKey(@Nonnull String key);

    @Nonnull
    List<LogsStatisticsEntity> findAllStatisticsByCreationDate(@Nonnull LocalDateTime beforeDate);

    void deleteStatistics(@Nonnull List<LogsStatisticsEntity> statsList);

    @Nonnull
    List<String> deleteAllStatisticsByCreationDate(@NonNull LocalDateTime beforeDate);

    void deleteByQuery(@Nonnull SearchQuery deleteQuery);
}
