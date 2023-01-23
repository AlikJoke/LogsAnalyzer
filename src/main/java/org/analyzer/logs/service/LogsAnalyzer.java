package org.analyzer.logs.service;

import lombok.NonNull;
import org.analyzer.logs.model.LogRecordEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

public interface LogsAnalyzer {

    @Nonnull
    Mono<LogsStatistics> analyze(
            @NonNull Flux<LogRecordEntity> records,
            @NonNull AnalyzeQuery analyzeQuery);
}
