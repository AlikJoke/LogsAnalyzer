package org.analyzer.logs.rest;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import org.analyzer.logs.service.AnalyzeQuery;
import org.springframework.data.domain.Sort;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Value
@Accessors(fluent = true)
public class RequestAnalyzeQuery extends RequestSearchQuery implements AnalyzeQuery {

    String analyzeResultName;
    boolean save;
    Map<String, JsonNode> aggregations;

    @JsonCreator
    RequestAnalyzeQuery(
            @JsonProperty("query") @NonNull String query,
            @JsonProperty("extended_format") boolean extendedFormat,
            @JsonProperty("post_filters") @Nullable Map<String, JsonNode> postFilters,
            @JsonProperty("max_results") @Nonnegative int maxResults,
            @JsonProperty("sorts") @Nullable Map<String, Sort.Direction> sorts,
            @JsonProperty("aggregations") @Nullable Map<String, JsonNode> aggregations,
            @JsonProperty("save") boolean save,
            @JsonProperty("analyze_result_name") @Nullable String analyzeResultName) {
        super(query, extendedFormat, postFilters, maxResults, sorts);
        this.save = save;
        this.analyzeResultName = analyzeResultName;
        this.aggregations = aggregations;
    }

    @NonNull
    @Override
    public Map<String, JsonNode> aggregations() {
        return aggregations == null ? Collections.emptyMap() : aggregations;
    }
}
