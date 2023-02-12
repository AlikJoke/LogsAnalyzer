package org.analyzer.logs.service;

import lombok.NonNull;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.CollectionUtils;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public interface MapLogsStatistics extends Map<String, List<?>> {

    Long commonCount();

    @Nonnull
    List<Pair<String, Long>> errorsFrequencies();

    @Nonnull
    List<Pair<String, Long>> mostFrequentErrors();

    Double errorsAverageInterval();

    @Nonnull
    List<Pair<String, Long>> errorsByCategoryFrequencies();

    Long errorsCount();

    Long warnsCount();

    @Nonnull
    List<Pair<String, Long>> mostFrequentWarns();

    Double averageWriteRate();

    @Nonnull
    List<Pair<String, Long>> recordsByCategoryFrequencies();

    @Nonnull
    List<Pair<String, Long>> recordsByThreadFrequencies();

    @Nonnull
    default Map<String, Object> toResultMap() {

        return entrySet()
                    .stream()
                    .filter(Predicate.not(entry -> CollectionUtils.isEmpty(entry.getValue())))
                    .collect(Collectors.toMap(Map.Entry::getKey, this::prepareToResponse));
    }

    @Nonnull
    private Object prepareToResponse(@NonNull final List<?> values) {

        final var firstElem = values.get(0);
        if (firstElem instanceof Pair<?,?>) {
            return values
                    .stream()
                    .filter(v -> v instanceof Pair<?,?>)
                    .map(Pair.class::cast)
                    .collect(Collectors.toMap(Pair::getLeft, Pair::getRight, (v1, v2) -> v1, LinkedHashMap::new));
        }

        return values.size() == 1 ? firstElem : values;
    }
}
