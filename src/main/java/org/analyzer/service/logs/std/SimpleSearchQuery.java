package org.analyzer.service.logs.std;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import org.analyzer.service.logs.SearchQuery;
import org.analyzer.service.util.JsonConverter;
import org.springframework.data.domain.Sort;

import javax.annotation.Nonnegative;
import java.util.Map;

public class SimpleSearchQuery implements SearchQuery {

    private final String query;
    private final Map<String, JsonNode> postFilters;
    private final Map<String, Sort.Direction> sorts;

    private int pageNumber;

    public SimpleSearchQuery(@NonNull SearchQuery searchQuery) {
        this(searchQuery.query(), searchQuery.pageNumber(), searchQuery.postFilters(), searchQuery.sorts());
    }
    public SimpleSearchQuery(@NonNull String query) {
        this(query, Map.of(), Map.of());
    }

    public SimpleSearchQuery(
            @NonNull String query,
            @NonNull Map<String, JsonNode> postFilters,
            @NonNull Map<String, Sort.Direction> sorts) {
        this(query, 0, postFilters, sorts);
    }

    private SimpleSearchQuery(
            @NonNull String query,
            @Nonnegative int pageNumber,
            @NonNull Map<String, JsonNode> postFilters,
            @NonNull Map<String, Sort.Direction> sorts) {
        this.query = query;
        this.pageNumber = pageNumber;
        this.postFilters = postFilters;
        this.sorts = sorts;
    }

    @NonNull
    @Override
    public String query() {
        return this.query;
    }

    @Override
    public boolean extendedFormat() {
        return false;
    }

    @NonNull
    @Override
    public Map<String, JsonNode> postFilters() {
        return this.postFilters;
    }

    @Override
    public int pageSize() {
        return 0;
    }

    @Override
    public int pageNumber() {
        return this.pageNumber;
    }

    @Override
    @NonNull
    public SearchQuery toNextPageQuery() {
        this.pageNumber++;
        return this;
    }

    @NonNull
    @Override
    public Map<String, Sort.Direction> sorts() {
        return this.sorts;
    }

    @NonNull
    @Override
    public String toJson(@NonNull JsonConverter jsonConverter) {
        return "{" + query() + "}";
    }
}
