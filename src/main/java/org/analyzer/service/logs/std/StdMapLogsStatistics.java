package org.analyzer.service.logs.std;

import lombok.NonNull;
import org.analyzer.service.logs.MapLogsStatistics;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@NotThreadSafe
public class StdMapLogsStatistics extends HashMap<String, Object> implements MapLogsStatistics {

    public static final String ERRORS_FREQUENCIES = "errors-frequencies";
    public static final String MOST_FREQUENT_ERRORS = "most-frequent-errors";
    public static final String ERRORS_COUNT = "errors-count";
    public static final String WARNS_COUNT = "warns-count";
    public static final String MOST_FREQUENT_WARNS = "most-frequent-warns";
    public static final String ERRORS_AVERAGE_INTERVAL = "errors-average-interval";
    public static final String ERRORS_FREQUENCIES_BY_CATEGORY = "errors-frequencies-by-category";
    public static final String AVERAGE_WRITE_RATE = "average-write-rate";
    public static final String RECORDS_FREQUENCY_BY_CATEGORY = "records-frequency-by-category";
    public static final String RECORDS_FREQUENCY_BY_THREAD = "records-frequency-by-thread";
    public static final String ALL_RECORDS_COUNT = "common-count";

    @Override
    public Long commonCount() {
        return getStatByKey(ALL_RECORDS_COUNT, true);
    }

    public void commonCount(@NonNull Long commonCount) {
        put(ALL_RECORDS_COUNT, commonCount);
    }

    @NonNull
    @Override
    public List<Pair<String, Long>> errorsFrequencies() {
        return getStatByKey(ERRORS_FREQUENCIES);
    }

    public void errorsFrequencies(@NonNull List<Pair<String, Long>> errorsFrequencies) {
        put(ERRORS_FREQUENCIES, errorsFrequencies);
    }

    @NonNull
    @Override
    public List<Pair<String, Long>> mostFrequentErrors() {
        return getStatByKey(MOST_FREQUENT_ERRORS);
    }

    public void mostFrequentErrors(@NonNull List<Pair<String, Long>> mostFrequentErrors) {
        put(MOST_FREQUENT_ERRORS, mostFrequentErrors);
    }

    @Override
    public Double errorsAverageInterval() {
        return getStatByKey(ERRORS_AVERAGE_INTERVAL, true);
    }

    public void errorsAverageInterval(@NonNull Double errorsAverageInterval) {
        put(ERRORS_AVERAGE_INTERVAL, errorsAverageInterval);
    }

    @NonNull
    @Override
    public List<Pair<String, Long>> errorsByCategoryFrequencies() {
        return getStatByKey(ERRORS_FREQUENCIES_BY_CATEGORY);
    }

    public void errorsByCategoryFrequencies(@NonNull List<Pair<String, Long>> errorsByCategoryFrequencies) {
        put(MOST_FREQUENT_ERRORS, errorsByCategoryFrequencies);
    }

    @Override
    public Long errorsCount() {
        return getStatByKey(ERRORS_COUNT, true);
    }

    public void errorsCount(@NonNull Long errorsCount) {
        put(ERRORS_COUNT, errorsCount);
    }

    @Override
    public Long warnsCount() {
        return getStatByKey(WARNS_COUNT, true);
    }

    public void warnsCount(@NonNull Long warnsCount) {
        put(WARNS_COUNT, warnsCount);
    }

    @NonNull
    @Override
    public List<Pair<String, Long>> mostFrequentWarns() {
        return getStatByKey(MOST_FREQUENT_WARNS);
    }

    public void mostFrequentWarns(@NonNull List<Pair<String, Long>> mostFrequentWarns) {
        put(MOST_FREQUENT_WARNS, mostFrequentWarns);
    }

    @Override
    public Double averageWriteRate() {
        return getStatByKey(AVERAGE_WRITE_RATE, true);
    }

    public void averageWriteRate(@NonNull Double averageWriteRate) {
        put(AVERAGE_WRITE_RATE, averageWriteRate);
    }

    @NonNull
    @Override
    public List<Pair<String, Long>> recordsByCategoryFrequencies() {
        return getStatByKey(RECORDS_FREQUENCY_BY_CATEGORY);
    }

    public void recordsByCategoryFrequencies(@NonNull List<Pair<String, Long>> recordsByCategoryFrequencies) {
        put(RECORDS_FREQUENCY_BY_CATEGORY, recordsByCategoryFrequencies);
    }

    @NonNull
    @Override
    public List<Pair<String, Long>> recordsByThreadFrequencies() {
        return getStatByKey(RECORDS_FREQUENCY_BY_THREAD);
    }

    public void recordsByThreadFrequencies(@NonNull List<Pair<String, Long>> recordsByThreadFrequencies) {
        put(RECORDS_FREQUENCY_BY_THREAD, recordsByThreadFrequencies);
    }

    @NonNull
    public StdMapLogsStatistics putOne(@NonNull final String statisticKey, @NonNull final Object value) {
        put(statisticKey, value);
        return this;
    }

    @Override
    @NonNull
    public MapLogsStatistics joinWith(@NonNull MapLogsStatistics statistics) {
        if (statistics.errorsCount() != null) {
            errorsCount((errorsCount() == null ? 0 : errorsCount()) + statistics.errorsCount());
        }
        if (statistics.warnsCount() != null) {
            warnsCount((warnsCount() == null ? 0 : warnsCount()) + statistics.warnsCount());
        }
        if (statistics.averageWriteRate() != null) {
            averageWriteRate((averageWriteRate() == null ? 0 : averageWriteRate()) + statistics.averageWriteRate());
        }
        if (statistics.commonCount() != null) {
            commonCount((commonCount() == null ? 0 : commonCount()) + statistics.commonCount());
        }
        if (statistics.errorsAverageInterval() != null) {
            errorsAverageInterval((errorsAverageInterval() == null ? 0 : errorsAverageInterval()) + statistics.errorsAverageInterval());
        }

        if (!statistics.errorsFrequencies().isEmpty()) {
            errorsFrequencies(joinValues(errorsFrequencies(), statistics.errorsFrequencies()));
        }
        if (!statistics.errorsByCategoryFrequencies().isEmpty()) {
            errorsByCategoryFrequencies(joinValues(errorsByCategoryFrequencies(), statistics.errorsByCategoryFrequencies()));
        }
        if (!statistics.mostFrequentErrors().isEmpty()) {
            mostFrequentErrors(joinValues(mostFrequentErrors(), statistics.mostFrequentErrors()));
        }
        if (!statistics.mostFrequentWarns().isEmpty()) {
            mostFrequentWarns(joinValues(mostFrequentWarns(), statistics.mostFrequentWarns()));
        }
        if (!statistics.recordsByThreadFrequencies().isEmpty()) {
            recordsByThreadFrequencies(joinValues(recordsByThreadFrequencies(), statistics.recordsByThreadFrequencies()));
        }
        if (!statistics.recordsByCategoryFrequencies().isEmpty()) {
            recordsByCategoryFrequencies(joinValues(recordsByCategoryFrequencies(), statistics.recordsByCategoryFrequencies()));
        }

        return this;
    }

    private List<Pair<String, Long>> joinValues(
            final List<Pair<String, Long>> oldValues,
            final List<Pair<String, Long>> newValues) {
        if (oldValues.isEmpty()) {
            return newValues;
        } else if (newValues.isEmpty()) {
            return oldValues;
        }

        final Map<String, Pair<String, Long>> oldValuesMap =
                oldValues
                        .stream()
                        .collect(Collectors.toMap(Pair::getLeft, Function.identity()));

        for (final var newVal : newValues) {
            final var oldVal = oldValuesMap.get(newVal.getKey());
            if (oldVal == null) {
                oldValues.add(newVal);
            } else {
                oldValues.set(oldValues.indexOf(oldVal), ImmutablePair.of(oldVal.getKey(), oldVal.getValue() + newVal.getValue()));
            }
        }

        return oldValues;
    }

    private <T> T getStatByKey(final String key) {
        return getStatByKey(key, false);
    }

    private <T> T getStatByKey(final String key, final boolean single) {
        final Object value = super.getOrDefault(key, single ? null : Collections.emptyList());
        @SuppressWarnings("unchecked")
        final T result = (T) value;
        return result;
    }
}
