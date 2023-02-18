package org.analyzer.logs.service.std;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import org.analyzer.logs.model.LogRecordEntity;
import org.analyzer.logs.service.Aggregator;
import org.analyzer.logs.service.AnalyzeQuery;
import org.analyzer.logs.service.LogsAnalyzer;
import org.analyzer.logs.service.MapLogsStatistics;
import org.analyzer.logs.service.std.aggregations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class DefaultLogsAnalyzer implements LogsAnalyzer {

    private final Set<String> mostFrequentAggregations =
            Set.of(StdMapLogsStatistics.MOST_FREQUENT_ERRORS, StdMapLogsStatistics.MOST_FREQUENT_WARNS);

    private final AggregatorFactory aggregatorsFactory;
    private final Map<String, Aggregator<Object>> defaultAggregations;

    @Autowired
    public DefaultLogsAnalyzer(@NonNull AggregatorFactory aggregatorsFactory) {
        this.aggregatorsFactory = aggregatorsFactory;
        this.defaultAggregations = createDefaultAggregationsMap();
    }

    @Override
    @NonNull
    public MapLogsStatistics analyze(
            @NonNull List<LogRecordEntity> records,
            @NonNull AnalyzeQuery analyzeQuery) {

        Map<String, Aggregator<Object>> aggregations = getAggregationsFromQuery(analyzeQuery);
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

    @NonNull
    @Override
    public MapLogsStatistics composeBy(
            @NonNull List<MapLogsStatistics> statistics,
            @NonNull AnalyzeQuery analyzeQuery) {
        final var result = new StdMapLogsStatistics();
        statistics.forEach(result::joinWith);

        final Map<String, Aggregator<Object>> aggregators = getAggregationsFromQuery(analyzeQuery);
        // TODO apply minFrequency & takeCount to frequencies stats

        return result;
    }

    private Map<String, Aggregator<Object>> getAggregationsFromQuery(final AnalyzeQuery analyzeQuery) {
        return analyzeQuery.aggregations().entrySet()
                .stream()
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                e -> createNonDefaultAggregator(e.getKey(), e.getValue()))
                );
    }

    private Aggregator<Object> createNonDefaultAggregator(final String name, final JsonNode settings) {
        final Aggregator<Object> aggregator = this.aggregatorsFactory.create(name, settings);
        if (aggregator.getParameters() instanceof Frequency frequency) {
            aggregator.setParameters(new Frequency(frequency.groupBy(), 1, frequency.additionalFilter(), Integer.MAX_VALUE));
        }

        return aggregator;
    }

    private Map<String, Aggregator<Object>> createDefaultAggregationsMap() {

        final Map<String, Aggregator<Object>> result = new HashMap<>();
        result.put(
                StdMapLogsStatistics.ERRORS_FREQUENCIES,
                this.aggregatorsFactory.create(FrequencyAggregator.NAME, new Frequency("record", 1, createAdditionalFilterErrors(), Integer.MAX_VALUE))
        );

        result.put(
                StdMapLogsStatistics.MOST_FREQUENT_ERRORS,
                this.aggregatorsFactory.create(FrequencyAggregator.NAME, new Frequency("record", 1, createAdditionalFilterErrors(), Integer.MAX_VALUE))
        );

        result.put(
                StdMapLogsStatistics.MOST_FREQUENT_WARNS,
                this.aggregatorsFactory.create(FrequencyAggregator.NAME, new Frequency("record", 1, createAdditionalFilterWarns(), Integer.MAX_VALUE))
        );

        result.put(
                StdMapLogsStatistics.ERRORS_COUNT,
                this.aggregatorsFactory.create(CountAggregator.NAME, new Count(createAdditionalFilterErrors()))
        );

        result.put(
                StdMapLogsStatistics.ALL_RECORDS_COUNT,
                this.aggregatorsFactory.create(CountAggregator.NAME, new Count(Collections.emptyMap()))
        );

        result.put(
                StdMapLogsStatistics.WARNS_COUNT,
                this.aggregatorsFactory.create(CountAggregator.NAME, new Count(createAdditionalFilterWarns()))
        );

        result.put(
                StdMapLogsStatistics.ERRORS_FREQUENCIES_BY_CATEGORY,
                this.aggregatorsFactory.create(FrequencyAggregator.NAME, new Frequency("category", 1, createAdditionalFilterErrors(), Integer.MAX_VALUE))
        );

        result.put(
                StdMapLogsStatistics.RECORDS_FREQUENCY_BY_CATEGORY,
                this.aggregatorsFactory.create(FrequencyAggregator.NAME, new Frequency("category", 1, Collections.emptyMap(), Integer.MAX_VALUE))
        );

        result.put(
                StdMapLogsStatistics.RECORDS_FREQUENCY_BY_THREAD,
                this.aggregatorsFactory.create(FrequencyAggregator.NAME, new Frequency("thread", 1, Collections.emptyMap(), Integer.MAX_VALUE))
        );

        result.put(
                StdMapLogsStatistics.ERRORS_AVERAGE_INTERVAL,
                this.aggregatorsFactory.create(ErrorsAverageIntervalAggregator.NAME, new Object())
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
