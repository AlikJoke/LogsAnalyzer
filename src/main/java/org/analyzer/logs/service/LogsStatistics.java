package org.analyzer.logs.service;

import lombok.NonNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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

    @Nonnull
    default Mono<Map<String, Object>> toResultMap() {

        return Flux.fromIterable(entrySet())
                    .flatMap(
                            e -> Mono.just(e.getKey())
                                    .zipWith(e.getValue()
                                            .collectList()
                                            .filter(Predicate.not(List::isEmpty))
                                            .map(this::prepareToResponse)
                                    )
                    )
                    .collectMap(Tuple2::getT1, Tuple2::getT2);
    }

    @Nullable
    private Object prepareToResponse(@NonNull final List<?> values) {

        final var firstElem = values.get(0);
        if (firstElem instanceof Tuple2<?,?>) {
            return values
                    .stream()
                    .filter(v -> v instanceof Tuple2<?,?>)
                    .map(Tuple2.class::cast)
                    .collect(Collectors.toMap(Tuple2::getT1, Tuple2::getT2, (v1, v2) -> v1, LinkedHashMap::new));
        }

        return values.size() == 1 ? firstElem : values;
    }
}
