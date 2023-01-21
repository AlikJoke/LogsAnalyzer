package org.analyzer.logs.service;

import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

public interface SearchQueryParser<T> {

    @Nonnull
    Mono<T> parse(@Nonnull SearchQuery query);
}
