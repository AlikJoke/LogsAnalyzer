package org.analyzer.logs.service.std;

import lombok.NonNull;
import org.analyzer.logs.service.LogsStatistics;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.HashMap;

@NotThreadSafe
public class MapLogsStatistics extends HashMap<String, Flux<?>> implements LogsStatistics {

    private static final String ERRORS_FREQUENCIES = "errors-frequencies";
    private static final String MOST_FREQUENT_ERRORS = "most-frequent-errors";
    private static final String ERRORS_AVERAGE_INTERVAL = "errors-average-interval";
    private static final String ERRORS_FREQUENCIES_BY_CATEGORY = "errors-frequencies-by-category";
    private static final String ERRORS_COUNT = "errors-count";
    private static final String WARNS_COUNT = "warns-count";
    private static final String MOST_FREQUENT_WARNS = "most-frequent-warns";
    private static final String AVERAGE_WRITE_RATE = "average-write-rage";
    private static final String RECORDS_FREQUENCY_BY_CATEGORY = "records-frequency-by-category";
    private static final String RECORDS_FREQUENCY_BY_THREAD = "records-frequency-by-thread";

    @NonNull
    @Override
    public Flux<Tuple2<String, Long>> errorsFrequencies() {
        return getStatByKey(ERRORS_FREQUENCIES);
    }

    @NonNull
    public MapLogsStatistics errorsFrequencies(@NonNull Flux<Tuple2<String, Long>> errorsFrequencies) {
        put(ERRORS_FREQUENCIES, errorsFrequencies);
        return this;
    }

    @NonNull
    @Override
    public Flux<Tuple2<String, Long>> mostFrequentErrors() {
        return getStatByKey(MOST_FREQUENT_ERRORS);
    }

    @NonNull
    public MapLogsStatistics mostFrequentErrors(@NonNull Flux<Tuple2<String, Long>> mostFrequentErrors) {
        put(MOST_FREQUENT_ERRORS, mostFrequentErrors);
        return this;
    }

    @NonNull
    @Override
    public Mono<Long> errorsAverageInterval() {
        return getStatByKey(ERRORS_AVERAGE_INTERVAL, true);
    }

    @NonNull
    public MapLogsStatistics errorsAverageInterval(@NonNull Mono<Long> errorsAverageInterval) {
        put(ERRORS_AVERAGE_INTERVAL, errorsAverageInterval.flux());
        return this;
    }

    @NonNull
    @Override
    public Flux<Tuple2<String, Long>> errorsByCategoryFrequencies() {
        return getStatByKey(ERRORS_FREQUENCIES_BY_CATEGORY);
    }

    @NonNull
    public MapLogsStatistics errorsByCategoryFrequencies(@NonNull Flux<Tuple2<String, Long>> errorsByCategoryFrequencies) {
        put(MOST_FREQUENT_ERRORS, errorsByCategoryFrequencies);
        return this;
    }

    @NonNull
    @Override
    public Mono<Long> errorsCount() {
        return getStatByKey(ERRORS_COUNT, true);
    }

    @NonNull
    public MapLogsStatistics errorsCount(@NonNull Mono<Long> errorsCount) {
        put(ERRORS_COUNT, errorsCount.flux());
        return this;
    }

    @NonNull
    @Override
    public Mono<Long> warnsCount() {
        return getStatByKey(WARNS_COUNT, true);
    }

    @NonNull
    public MapLogsStatistics warnsCount(@NonNull Mono<Long> warnsCount) {
        put(WARNS_COUNT, warnsCount.flux());
        return this;
    }

    @NonNull
    @Override
    public Flux<Tuple2<String, Long>> mostFrequentWarns() {
        return getStatByKey(MOST_FREQUENT_WARNS);
    }

    @NonNull
    public MapLogsStatistics mostFrequentWarns(@NonNull Flux<Tuple2<String, Long>> mostFrequentWarns) {
        put(MOST_FREQUENT_WARNS, mostFrequentWarns);
        return this;
    }

    @NonNull
    @Override
    public Mono<Long> averageWriteRate() {
        return getStatByKey(AVERAGE_WRITE_RATE, true);
    }

    @NonNull
    public MapLogsStatistics averageWriteRate(@NonNull Mono<Long> averageWriteRate) {
        put(AVERAGE_WRITE_RATE, averageWriteRate.flux());
        return this;
    }

    @NonNull
    @Override
    public Flux<Tuple2<String, Long>> recordsByCategoryFrequencies() {
        return getStatByKey(RECORDS_FREQUENCY_BY_CATEGORY);
    }

    @NonNull
    public MapLogsStatistics recordsByCategoryFrequencies(@NonNull Flux<Tuple2<String, Long>> recordsByCategoryFrequencies) {
        put(RECORDS_FREQUENCY_BY_CATEGORY, recordsByCategoryFrequencies);
        return this;
    }

    @NonNull
    @Override
    public Flux<Tuple2<String, Long>> recordsByThreadFrequencies() {
        return getStatByKey(RECORDS_FREQUENCY_BY_THREAD);
    }

    @NonNull
    public MapLogsStatistics recordsByThreadFrequencies(@NonNull Flux<Tuple2<String, Long>> recordsByThreadFrequencies) {
        put(RECORDS_FREQUENCY_BY_THREAD, recordsByThreadFrequencies);
        return this;
    }

    private <T> T getStatByKey(final String key) {
        return getStatByKey(key, false);
    }

    private <T> T getStatByKey(final String key, final boolean single) {
        final Flux<?> value = super.getOrDefault(key, Flux.empty());
        @SuppressWarnings("unchecked")
        final T result = single ? (T) value.singleOrEmpty() : (T) value;
        return result;
    }
}
