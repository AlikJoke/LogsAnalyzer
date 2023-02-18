package org.analyzer.logs.service;

import org.analyzer.logs.model.LogRecordEntity;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public interface Aggregator<T> extends Function<List<LogRecordEntity>, T> {

    @Nonnull
    Object getParameters();

    void setParameters(@Nonnull Object parameters);

    @Nonnull
    Class<?> getParametersClass();

    @Nonnull
    String getName();

    @Nonnull
    default Optional<PostAggregationFilter<T>> postFilter() {
        return Optional.empty();
    }
}
