package org.parser.app.rest;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.NonNull;
import org.parser.app.service.SearchQuery;

import javax.annotation.Nullable;
import java.util.Map;

@JsonSerialize
@JsonAutoDetect
public record RequestQuery(
        @NonNull String query,
        @JsonProperty("extended_format") boolean extendedFormat,
        @JsonProperty("post_filters") @Nullable Map<String, JsonNode> postFilters) implements SearchQuery {
}
