package org.parser.app.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import org.parser.app.service.SearchQuery;
import org.springframework.data.domain.Sort;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;

public record RequestSearchQuery(
        @NonNull String query,
        @JsonProperty("extended_format") boolean extendedFormat,
        @JsonProperty("post_filters") @Nullable Map<String, JsonNode> postFilters,
        @JsonProperty("max_results") @Nonnegative int maxResults,
        @JsonProperty("sorts") @Nullable Map<String, Sort.Direction> sorts,
        @JsonProperty("result_aggregation") @Nullable Map<String, JsonNode> aggregator) implements SearchQuery {

    @Override
    public int maxResults() {
        return maxResults;
    }

    @NonNull
    @Override
    public Map<String, Sort.Direction> sorts() {
        return sorts == null ? Collections.emptyMap() : sorts;
    }

    @NonNull
    @Override
    public Map<String, JsonNode> postFilters() {
        return postFilters == null ? Collections.emptyMap() : postFilters;
    }

    @NonNull
    @Override
    public Map<String, JsonNode> aggregator() {
        return aggregator == null ? Collections.emptyMap() : aggregator;
    }
}
