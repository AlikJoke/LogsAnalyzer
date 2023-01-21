package org.analyzer.logs.service.std;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import org.analyzer.logs.model.LogRecord;
import org.analyzer.logs.service.Aggregator;
import org.analyzer.logs.service.LogsStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.Map;

public class LogsAnalyzer {

    private final AggregatorFactory aggregatorsFactory;
    private final Flux<Tuple2<String, Aggregator<Object>>> defaultAggregations;

    @Autowired
    public LogsAnalyzer(@NonNull AggregatorFactory aggregatorsFactory) {
        this.aggregatorsFactory = aggregatorsFactory;
        this.defaultAggregations = Flux.empty(); // TODO
    }

    @NonNull
    public Mono<LogsStatistics> analyze(
            @NonNull Flux<LogRecord> records,
            @NonNull Map<String, JsonNode> aggregations) {

        return Flux.fromIterable(aggregations.entrySet())
                    .flatMap(
                            e -> Mono.just(e.getKey())
                                        .zipWith(this.aggregatorsFactory.create(e.getKey(), e.getValue()))
                    )
                    .switchIfEmpty(defaultAggregations)
                    .map(
                            tuple -> tuple
                                        .mapT2(a -> a.apply(records))
                    )
                    .transform(this::composeStatistics)
                    .singleOrEmpty();
    }

    private Mono<LogsStatistics> composeStatistics(final Flux<Tuple2<String, Flux<Object>>> statistics) {
        // TODO
        return Mono.empty();
    }
}
