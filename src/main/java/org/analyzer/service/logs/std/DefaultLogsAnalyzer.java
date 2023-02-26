package org.analyzer.service.logs.std;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import org.analyzer.entities.LogRecordEntity;
import org.analyzer.service.logs.AnalyzeQuery;
import org.analyzer.service.logs.LogsAggregator;
import org.analyzer.service.logs.LogsAnalyzer;
import org.analyzer.service.logs.MapLogsStatistics;
import org.analyzer.service.logs.std.aggregations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DefaultLogsAnalyzer implements LogsAnalyzer {

    private final AggregatorFactory aggregatorsFactory;
    private final Map<String, LogsAggregator<?>> defaultAggregations;
    private final Map<String, LogsAggregator<?>> aggregationsWithLimitations;

    @Autowired
    public DefaultLogsAnalyzer(@NonNull AggregatorFactory aggregatorsFactory) {
        this.aggregatorsFactory = aggregatorsFactory;
        this.defaultAggregations = createDefaultAggregationsMap();
        this.aggregationsWithLimitations = createDefaultAggregationsWithLimitationsMap();
    }

    @Override
    @NonNull
    public MapLogsStatistics analyze(
            @NonNull List<LogRecordEntity> records,
            @NonNull AnalyzeQuery analyzeQuery) {

        Map<String, LogsAggregator<?>> aggregations = getAggregationsFromQuery(analyzeQuery);
        if (aggregations.isEmpty()) {
            aggregations = this.defaultAggregations;
        }

        return aggregations.entrySet()
                            .stream()
                            .reduce(
                                    new StdMapLogsStatistics(),
                                    (acc, s) -> acc.putOne(s.getKey(), s.getValue().apply(records)),
                                    (s1, s2) -> s1
                            );
    }

    @Override
    public void applyFinalQueryLimitations(@NonNull MapLogsStatistics statistics, @NonNull AnalyzeQuery analyzeQuery) {
        final var aggregators = getAggregationsFromQuery(analyzeQuery);
        final var aggregatorsWithLimitations =
                aggregators.isEmpty()
                        ? this.aggregationsWithLimitations
                        : aggregators;

        statistics.forEach((k, v) -> {
            final var aggregatorWithLimitations = aggregatorsWithLimitations.get(k);
            if (aggregatorWithLimitations == null) {
                return;
            }

            @SuppressWarnings("unchecked")
            final var aggregator = (LogsAggregator<Object>) aggregatorWithLimitations;
            aggregator.postFilter()
                        .ifPresent(pf -> pf.accept(v));
        });
    }

    private Map<String, LogsAggregator<?>> getAggregationsFromQuery(final AnalyzeQuery analyzeQuery) {
        return analyzeQuery.aggregations().entrySet()
                .stream()
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                e -> createNonDefaultAggregator(e.getKey(), e.getValue()))
                );
    }

    private LogsAggregator<Object> createNonDefaultAggregator(final String name, final JsonNode settings) {
        final LogsAggregator<Object> aggregator = this.aggregatorsFactory.create(name, settings);
        if (aggregator.getParameters() instanceof Frequency frequency) {
            aggregator.setParameters(new Frequency(frequency.groupBy(), 1, frequency.additionalFilter(), Integer.MAX_VALUE));
        }

        return aggregator;
    }

    private Map<String, LogsAggregator<?>> createDefaultAggregationsWithLimitationsMap() {
        return Map.of(
                StdMapLogsStatistics.MOST_FREQUENT_ERRORS,
                this.aggregatorsFactory.create(FrequencyLogsAggregator.NAME, new Frequency("record", 1, createAdditionalFilterErrors(), 5)),

                StdMapLogsStatistics.MOST_FREQUENT_WARNS,
                this.aggregatorsFactory.create(FrequencyLogsAggregator.NAME, new Frequency("record", 1, createAdditionalFilterWarns(), 5))
        );
    }

    private Map<String, LogsAggregator<?>> createDefaultAggregationsMap() {

        final Map<String, LogsAggregator<?>> result = new HashMap<>();
        result.put(
                StdMapLogsStatistics.ERRORS_FREQUENCIES,
                this.aggregatorsFactory.create(FrequencyLogsAggregator.NAME, new Frequency("record", 1, createAdditionalFilterErrors(), Integer.MAX_VALUE))
        );

        result.put(
                StdMapLogsStatistics.MOST_FREQUENT_ERRORS,
                this.aggregatorsFactory.create(FrequencyLogsAggregator.NAME, new Frequency("record", 1, createAdditionalFilterErrors(), Integer.MAX_VALUE))
        );

        result.put(
                StdMapLogsStatistics.MOST_FREQUENT_WARNS,
                this.aggregatorsFactory.create(FrequencyLogsAggregator.NAME, new Frequency("record", 1, createAdditionalFilterWarns(), Integer.MAX_VALUE))
        );

        result.put(
                StdMapLogsStatistics.ERRORS_COUNT,
                this.aggregatorsFactory.create(CountLogsAggregator.NAME, new Count(createAdditionalFilterErrors()))
        );

        result.put(
                StdMapLogsStatistics.ALL_RECORDS_COUNT,
                this.aggregatorsFactory.create(CountLogsAggregator.NAME, new Count(Collections.emptyMap()))
        );

        result.put(
                StdMapLogsStatistics.WARNS_COUNT,
                this.aggregatorsFactory.create(CountLogsAggregator.NAME, new Count(createAdditionalFilterWarns()))
        );

        result.put(
                StdMapLogsStatistics.ERRORS_FREQUENCIES_BY_CATEGORY,
                this.aggregatorsFactory.create(FrequencyLogsAggregator.NAME, new Frequency("category", 1, createAdditionalFilterErrors(), Integer.MAX_VALUE))
        );

        result.put(
                StdMapLogsStatistics.RECORDS_FREQUENCY_BY_CATEGORY,
                this.aggregatorsFactory.create(FrequencyLogsAggregator.NAME, new Frequency("category", 1, Collections.emptyMap(), Integer.MAX_VALUE))
        );

        result.put(
                StdMapLogsStatistics.RECORDS_FREQUENCY_BY_THREAD,
                this.aggregatorsFactory.create(FrequencyLogsAggregator.NAME, new Frequency("thread", 1, Collections.emptyMap(), Integer.MAX_VALUE))
        );

        result.put(
                StdMapLogsStatistics.ERRORS_AVERAGE_INTERVAL,
                this.aggregatorsFactory.create(ErrorsAverageIntervalLogsAggregator.NAME, new Object())
        );

        return result;
    }

    private Map<String, Object> createAdditionalFilterErrors() {
        return Map.of("level", LogLevel.ERROR.name());
    }

    private Map<String, Object> createAdditionalFilterWarns() {
        return Map.of("level", LogLevel.WARN.name());
    }
}
