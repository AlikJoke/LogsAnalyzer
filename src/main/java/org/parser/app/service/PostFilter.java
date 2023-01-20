package org.parser.app.service;

import org.parser.app.model.LogRecord;
import reactor.core.publisher.Flux;

import javax.annotation.Nonnull;
import java.util.function.Function;

public interface PostFilter extends Function<Flux<LogRecord>, Flux<LogRecord>> {

    @Nonnull
    Object getParameters();

    void setParameters(@Nonnull Object parameters);

    @Nonnull
    Class<?> getParametersClass();

    @Nonnull
    String getName();
}
