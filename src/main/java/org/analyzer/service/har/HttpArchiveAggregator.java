package org.analyzer.service.har;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.analyzer.service.Aggregator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface HttpArchiveAggregator<T> extends Aggregator<HttpArchiveBody, T> {

    @Override
    default T apply(@Nonnull HttpArchiveBody body) {
        return body.getFieldValueByPath("log", "entries")
                    .map(ArrayNode.class::cast)
                    .map(this::apply)
                    .orElse(null);
    }

    @Nullable
    T apply(@Nonnull ArrayNode requests);
}
