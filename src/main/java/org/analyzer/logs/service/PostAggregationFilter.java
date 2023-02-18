package org.analyzer.logs.service;

import java.util.function.Consumer;

public interface PostAggregationFilter<T> extends Consumer<T> {

}
