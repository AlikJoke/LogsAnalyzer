package org.analyzer.service.logs;

import javax.annotation.Nonnull;

public interface SearchQueryParser<T> {

    @Nonnull
    T parse(@Nonnull SearchQuery query, @Nonnull String userKey);
}
