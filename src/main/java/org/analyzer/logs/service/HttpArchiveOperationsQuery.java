package org.analyzer.logs.service;

import org.analyzer.logs.model.HttpArchiveBody;
import org.springframework.data.domain.Sort;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

public interface HttpArchiveOperationsQuery {

    boolean applyDefaultSorting();

    @Nonnull
    Map<String, Sort.Direction> sort();

    @Nonnull
    Set<String> filteringKeys();

    @Nullable
    SearchQuery additionalLogsSearchQuery();

    @Nonnull
    default HttpArchiveBody applyTo(@Nonnull HttpArchiveBody harBody) {
        var result = harBody;
        for (final var filteringKey : filteringKeys()) {
            result = harBody.applyFilterBy(filteringKey);
        }

        final var sortingEntry = sort().isEmpty() ? null : sort().entrySet().iterator().next();
        final var sortOrder = sortingEntry == null
                ? null
                : sortingEntry.getValue() == null || sortingEntry.getValue() == Sort.Direction.DESC
                    ? Sort.Order.desc(sortingEntry.getKey())
                    : Sort.Order.asc(sortingEntry.getKey());
        return sortOrder == null
                ? applyDefaultSorting()
                    ? result.toSortedByRequestsResponseTime(null)
                    : result
                : result.toSortedByRequestsResponseTime(sortOrder);
    }
}
