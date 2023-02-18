package org.analyzer.logs.service;

import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

public interface MapLogsStatistics extends Map<String, Object> {

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
    MapLogsStatistics joinWith(@Nonnull MapLogsStatistics statistics);
}
