package org.analyzer.logs.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import javax.annotation.Nonnull;
import java.util.Map;

public interface LogsStatistics extends Map<String, Flux<?>> {

    @Nonnull
    Flux<Tuple2<String, Long>> errorsFrequencies();

    @Nonnull
    Flux<Tuple2<String, Long>> mostFrequentErrors();

    @Nonnull
    Mono<Double> errorsAverageInterval();

    @Nonnull
    Flux<Tuple2<String, Long>> errorsByCategoryFrequencies();

    @Nonnull
    Mono<Long> errorsCount();

    @Nonnull
    Mono<Long> warnsCount();

    @Nonnull
    Flux<Tuple2<String, Long>> mostFrequentWarns();

    @Nonnull
    Mono<Double> averageWriteRate();

    @Nonnull
    Flux<Tuple2<String, Long>> recordsByCategoryFrequencies();

    @Nonnull
    Flux<Tuple2<String, Long>> recordsByThreadFrequencies();
}
