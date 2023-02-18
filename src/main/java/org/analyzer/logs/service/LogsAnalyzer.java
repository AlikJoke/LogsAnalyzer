package org.analyzer.logs.service;

import lombok.NonNull;
import org.analyzer.logs.model.LogRecordEntity;

import javax.annotation.Nonnull;
import java.util.List;

public interface LogsAnalyzer {

    @Nonnull
    MapLogsStatistics analyze(
            @NonNull List<LogRecordEntity> records,
            @NonNull AnalyzeQuery analyzeQuery);

    @Nonnull
    MapLogsStatistics composeBy(
            @NonNull List<MapLogsStatistics> statistics,
            @NonNull AnalyzeQuery analyzeQuery);
}
