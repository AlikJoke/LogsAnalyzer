package org.analyzer.logs.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import org.analyzer.logs.model.LogRecord;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.Map;

public interface LogsAnalyzer {

    @Nonnull
    Mono<LogsStatistics> analyze(
            @NonNull Flux<LogRecord> records,
            @NonNull Map<String, JsonNode> aggregations);
}
