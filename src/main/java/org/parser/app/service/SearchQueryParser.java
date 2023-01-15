package org.parser.app.service;

import javax.annotation.Nonnull;

public interface SearchQueryParser<T> {

    @Nonnull
    T parse(@Nonnull String queryString);
}
