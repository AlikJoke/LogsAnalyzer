package org.analyzer.logs.service;

import org.analyzer.logs.model.LogRecordEntity;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Function;

public interface Aggregator<T> extends Function<List<LogRecordEntity>, T> {

    @Nonnull
    Object getParameters();

    void setParameters(@Nonnull Object parameters);

    @Nonnull
    Class<?> getParametersClass();

    @Nonnull
    String getName();
}
