package org.parser.app.service;

import org.parser.app.model.LogRecord;
import reactor.core.publisher.Flux;

import javax.annotation.Nonnull;
import java.util.function.Function;

public interface PostFilter<T> extends Function<Flux<LogRecord>, Flux<LogRecord>> {

    @Nonnull
    T getParameters();

    void setParameters(@Nonnull T parameters);

    @Nonnull
    Class<T> getParametersClass();

    @Nonnull
    String getName();
}
