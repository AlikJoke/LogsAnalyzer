package org.analyzer.service.logs;

import org.analyzer.entities.LogRecordEntity;
import org.analyzer.service.Aggregator;
import org.analyzer.service.PostAggregationFilter;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

public interface LogsAggregator<T> extends Aggregator<List<LogRecordEntity>, T> {

    @Nonnull
    Object getParameters();

    void setParameters(@Nonnull Object parameters);

    @Nonnull
    Class<?> getParametersClass();

    @Nonnull
    default Optional<PostAggregationFilter<T>> postFilter() {
        return Optional.empty();
    }
}
