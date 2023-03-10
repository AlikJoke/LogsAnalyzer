package org.analyzer.rest.stats;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import org.analyzer.rest.records.RequestSearchQuery;
import org.analyzer.service.logs.AnalyzeQuery;
import org.analyzer.service.logs.SearchQuery;
import org.springframework.data.domain.Sort;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Value
@Accessors(fluent = true)
@JsonSerialize
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class RequestAnalyzeQuery extends RequestSearchQuery implements AnalyzeQuery {

    String analyzeResultName;
    boolean save;
    Map<String, JsonNode> aggregations;

    @JsonCreator
    public RequestAnalyzeQuery(
            @JsonProperty("query") @NonNull String query,
            @JsonProperty("extended_format") boolean extendedFormat,
            @JsonProperty("post_filters") @Nullable Map<String, JsonNode> postFilters,
            @JsonProperty("sorts") @Nullable Map<String, Sort.Direction> sorts,
            @JsonProperty("aggregations") @Nullable Map<String, JsonNode> aggregations,
            @JsonProperty("save") boolean save,
            @JsonProperty("page_size") @Nonnegative int pageSize,
            @JsonProperty("page_number") @Nonnegative int pageNumber,
            @JsonProperty("analyze_result_name") @Nullable String analyzeResultName) {
        super(query, extendedFormat, postFilters, pageSize, pageNumber, sorts, null);
        this.save = save;
        this.analyzeResultName = analyzeResultName;
        this.aggregations = aggregations;
    }

    @NonNull
    @Override
    public Map<String, JsonNode> aggregations() {
        return aggregations == null ? Map.of() : aggregations;
    }

    @NonNull
    @Override
    @JsonIgnore
    public SearchQuery toSearchQuery(@Nonnegative int pageNumber) {
        return new RequestSearchQuery(query(), extendedFormat(), postFilters(), pageSize(), pageNumber, sorts(), null);
    }

    @NonNull
    @Override
    @JsonIgnore
    public String getId() {
        return UUID.randomUUID().toString();
    }
}
