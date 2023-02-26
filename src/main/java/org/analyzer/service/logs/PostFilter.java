package org.analyzer.service.logs;

import org.analyzer.entities.LogRecordEntity;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Function;

public interface PostFilter extends Function<List<LogRecordEntity>, List<LogRecordEntity>> {

    @Nonnull
    Object getParameters();

    void setParameters(@Nonnull Object parameters);

    @Nonnull
    Class<?> getParametersClass();

    @Nonnull
    String getName();
}
