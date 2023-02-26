package org.analyzer.service.logs;

import lombok.NonNull;
import org.analyzer.entities.LogRecordEntity;

import javax.annotation.Nonnull;
import java.util.List;

public interface LogsAnalyzer {

    @Nonnull
    MapLogsStatistics analyze(
            @NonNull List<LogRecordEntity> records,
            @NonNull AnalyzeQuery analyzeQuery);

    void applyFinalQueryLimitations(
            @NonNull MapLogsStatistics statistics,
            @NonNull AnalyzeQuery analyzeQuery);
}
