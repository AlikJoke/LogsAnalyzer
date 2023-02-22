package org.analyzer.logs.service;

import javax.annotation.Nonnull;
import java.util.function.Function;

public interface Aggregator<K, T> extends Function<K, T> {

    @Nonnull
    String getName();
}
