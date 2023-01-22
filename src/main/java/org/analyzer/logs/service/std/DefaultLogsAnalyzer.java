package org.analyzer.logs.service.std;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import org.analyzer.logs.model.LogRecord;
import org.analyzer.logs.service.Aggregator;
import org.analyzer.logs.service.LogsAnalyzer;
import org.analyzer.logs.service.LogsStatistics;
import org.analyzer.logs.service.std.aggregations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.Collections;
import java.util.Map;

@Component
public class DefaultLogsAnalyzer implements LogsAnalyzer {

    private final AggregatorFactory aggregatorsFactory;
    private final Flux<Tuple2<String, Aggregator<Object>>> defaultAggregations;

    @Autowired
    public DefaultLogsAnalyzer(@NonNull AggregatorFactory aggregatorsFactory) {
        this.aggregatorsFactory = aggregatorsFactory;
        this.defaultAggregations = createDefaultAggregationsFlux();
    }

    @Override
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
        return Mono.fromSupplier(MapLogsStatistics::new)
                    .doOnNext(
                            mapStats -> statistics
                                            .doOnNext(tuple -> mapStats.put(tuple.getT1(), tuple.getT2()))
                    )
                    .cast(LogsStatistics.class);
    }

    private Flux<Tuple2<String, Aggregator<Object>>> createDefaultAggregationsFlux() {

        return Flux.merge(
                Mono.just(MapLogsStatistics.ERRORS_FREQUENCIES)
                        .zipWith(this.aggregatorsFactory.create(FrequencyAggregator.NAME, new Frequency("record", 1, createAdditionalFilterErrors(), Integer.MAX_VALUE))),

                Mono.just(MapLogsStatistics.MOST_FREQUENT_ERRORS)
                        .zipWith(this.aggregatorsFactory.create(FrequencyAggregator.NAME, new Frequency("record", 1, createAdditionalFilterErrors(), 5))),

                Mono.just(MapLogsStatistics.MOST_FREQUENT_WARNS)
                        .zipWith(this.aggregatorsFactory.create(FrequencyAggregator.NAME, new Frequency("record", 1, createAdditionalFilterWarns(), 5))),

                Mono.just(MapLogsStatistics.ERRORS_COUNT)
                        .zipWith(this.aggregatorsFactory.create(CountAggregator.NAME, new Count(createAdditionalFilterErrors()))),

                Mono.just(MapLogsStatistics.WARNS_COUNT)
                        .zipWith(this.aggregatorsFactory.create(CountAggregator.NAME, new Count(createAdditionalFilterWarns()))),

                Mono.just(MapLogsStatistics.ERRORS_FREQUENCIES_BY_CATEGORY)
                        .zipWith(this.aggregatorsFactory.create(FrequencyAggregator.NAME, new Frequency("category", 1, createAdditionalFilterErrors(), Integer.MAX_VALUE))),

                Mono.just(MapLogsStatistics.RECORDS_FREQUENCY_BY_CATEGORY)
                        .zipWith(this.aggregatorsFactory.create(FrequencyAggregator.NAME, new Frequency("category", 1, Collections.emptyMap(), Integer.MAX_VALUE))),

                Mono.just(MapLogsStatistics.RECORDS_FREQUENCY_BY_THREAD)
                        .zipWith(this.aggregatorsFactory.create(FrequencyAggregator.NAME, new Frequency("thread", 1, Collections.emptyMap(), Integer.MAX_VALUE)))
                // TODO AVERAGE_WRITE_RATE & ERRORS_AVERAGE_INTERVAL
                );
    }

    private Map<String, Object> createAdditionalFilterErrors() {
        return Collections.singletonMap("level", LogLevel.ERROR.name());
    }

    private Map<String, Object> createAdditionalFilterWarns() {
        return Collections.singletonMap("level", LogLevel.WARN.name());
    }
}
