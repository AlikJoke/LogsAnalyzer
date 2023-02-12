package org.analyzer.logs.service.std;

import lombok.NonNull;
import org.analyzer.logs.service.MapLogsStatistics;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@NotThreadSafe
public class StdMapLogsStatistics extends HashMap<String, List<?>> implements MapLogsStatistics {

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

    @NonNull
    @Override
    public Long commonCount() {
        return getStatByKey(ALL_RECORDS_COUNT);
    }

    @NonNull
    @Override
    public List<Pair<String, Long>> errorsFrequencies() {
        return getStatByKey(ERRORS_FREQUENCIES);
    }

    @NonNull
    public StdMapLogsStatistics errorsFrequencies(@NonNull List<Pair<String, Long>> errorsFrequencies) {
        put(ERRORS_FREQUENCIES, errorsFrequencies);
        return this;
    }

    @NonNull
    @Override
    public List<Pair<String, Long>> mostFrequentErrors() {
        return getStatByKey(MOST_FREQUENT_ERRORS);
    }

    @NonNull
    public StdMapLogsStatistics mostFrequentErrors(@NonNull List<Pair<String, Long>> mostFrequentErrors) {
        put(MOST_FREQUENT_ERRORS, mostFrequentErrors);
        return this;
    }

    @NonNull
    @Override
    public Double errorsAverageInterval() {
        return getStatByKey(ERRORS_AVERAGE_INTERVAL, true);
    }

    @NonNull
    public StdMapLogsStatistics errorsAverageInterval(@NonNull Double errorsAverageInterval) {
        put(ERRORS_AVERAGE_INTERVAL, List.of(errorsAverageInterval));
        return this;
    }

    @NonNull
    @Override
    public List<Pair<String, Long>> errorsByCategoryFrequencies() {
        return getStatByKey(ERRORS_FREQUENCIES_BY_CATEGORY);
    }

    @NonNull
    public StdMapLogsStatistics errorsByCategoryFrequencies(@NonNull List<Pair<String, Long>> errorsByCategoryFrequencies) {
        put(MOST_FREQUENT_ERRORS, errorsByCategoryFrequencies);
        return this;
    }

    @NonNull
    @Override
    public Long errorsCount() {
        return getStatByKey(ERRORS_COUNT, true);
    }

    @NonNull
    public StdMapLogsStatistics errorsCount(@NonNull Long errorsCount) {
        put(ERRORS_COUNT, List.of(errorsCount));
        return this;
    }

    @NonNull
    @Override
    public Long warnsCount() {
        return getStatByKey(WARNS_COUNT, true);
    }

    @NonNull
    public StdMapLogsStatistics warnsCount(@NonNull Long warnsCount) {
        put(WARNS_COUNT, List.of(warnsCount));
        return this;
    }

    @NonNull
    @Override
    public List<Pair<String, Long>> mostFrequentWarns() {
        return getStatByKey(MOST_FREQUENT_WARNS);
    }

    @NonNull
    public StdMapLogsStatistics mostFrequentWarns(@NonNull List<Pair<String, Long>> mostFrequentWarns) {
        put(MOST_FREQUENT_WARNS, mostFrequentWarns);
        return this;
    }

    @NonNull
    @Override
    public Double averageWriteRate() {
        return getStatByKey(AVERAGE_WRITE_RATE, true);
    }

    @NonNull
    public StdMapLogsStatistics averageWriteRate(@NonNull Double averageWriteRate) {
        put(AVERAGE_WRITE_RATE, List.of(averageWriteRate));
        return this;
    }

    @NonNull
    @Override
    public List<Pair<String, Long>> recordsByCategoryFrequencies() {
        return getStatByKey(RECORDS_FREQUENCY_BY_CATEGORY);
    }

    @NonNull
    public StdMapLogsStatistics recordsByCategoryFrequencies(@NonNull List<Pair<String, Long>> recordsByCategoryFrequencies) {
        put(RECORDS_FREQUENCY_BY_CATEGORY, recordsByCategoryFrequencies);
        return this;
    }

    @NonNull
    @Override
    public List<Pair<String, Long>> recordsByThreadFrequencies() {
        return getStatByKey(RECORDS_FREQUENCY_BY_THREAD);
    }

    @NonNull
    public StdMapLogsStatistics recordsByThreadFrequencies(@NonNull List<Pair<String, Long>> recordsByThreadFrequencies) {
        put(RECORDS_FREQUENCY_BY_THREAD, recordsByThreadFrequencies);
        return this;
    }

    @NonNull
    public StdMapLogsStatistics putOne(@NonNull final String statisticKey, @NonNull final List<?> value) {
        put(statisticKey, value);
        return this;
    }

    private <T> T getStatByKey(final String key) {
        return getStatByKey(key, false);
    }

    private <T> T getStatByKey(final String key, final boolean single) {
        final List<?> value = super.getOrDefault(key, Collections.emptyList());
        @SuppressWarnings("unchecked")
        final T result = single ? (T) (value.isEmpty() ? null : value.get(0)) : (T) value;
        return result;
    }
}
