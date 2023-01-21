package org.analyzer.logs.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.Sort;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.Map;

public interface SearchQuery {

    @Nonnull
    String query();

    boolean extendedFormat();

    @Nonnull
    Map<String, JsonNode> postFilters();

    @Nonnegative
    int maxResults();

    @Nonnull
    Map<String, Sort.Direction> sorts();

    @Nonnull
    Map<String, JsonNode> aggregator();
}
