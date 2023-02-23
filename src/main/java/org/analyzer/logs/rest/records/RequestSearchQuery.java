package org.analyzer.logs.rest.records;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.analyzer.logs.service.SearchQuery;
import org.analyzer.logs.service.util.JsonConverter;
import org.springframework.data.domain.Sort;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;

@Getter
@ToString
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class RequestSearchQuery implements SearchQuery {

    private final String query;
    private final boolean extendedFormat;
    private final Map<String, JsonNode> postFilters;
    private final int pageSize;
    private final int pageNumber;
    private final Map<String, Sort.Direction> sorts;

    @JsonCreator
    public RequestSearchQuery(
            @JsonProperty("query") @NonNull String query,
            @JsonProperty("extended_format") boolean extendedFormat,
            @JsonProperty("post_filters") @Nullable Map<String, JsonNode> postFilters,
            @JsonProperty("page_size") @Nonnegative int pageSize,
            @JsonProperty("page_number") @Nonnegative int pageNumber,
            @JsonProperty("sorts") @Nullable Map<String, Sort.Direction> sorts) {
        this.query = query;
        this.extendedFormat = extendedFormat;
        this.postFilters = postFilters;
        this.pageSize = pageSize;
        this.pageNumber = pageNumber;
        this.sorts = sorts;
    }

    @NonNull
    @Override
    public Map<String, Sort.Direction> sorts() {
        return sorts == null ? Collections.emptyMap() : sorts;
    }

    @NonNull
    @Override
    @JsonIgnore
    public String toJson(@NonNull final JsonConverter jsonConverter) {
        return jsonConverter.convertToJson(this);
    }

    @NonNull
    @Override
    public Map<String, JsonNode> postFilters() {
        return postFilters == null ? Collections.emptyMap() : postFilters;
    }

    @NonNull
    @Override
    public SearchQuery toNextPageQuery() {
        return new RequestSearchQuery(this.query, this.extendedFormat, this.postFilters, this.pageSize, this.pageNumber + 1, this.sorts);
    }
}
