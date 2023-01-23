package org.analyzer.logs.service.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import org.analyzer.logs.service.AnalyzeQuery;
import org.analyzer.logs.service.SearchQuery;
import org.springframework.data.domain.Sort;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;

public final class AnalyzeQueryOnIndexWrapper implements AnalyzeQuery {

    private final String key;

    public AnalyzeQueryOnIndexWrapper(final String key) {
        this.key = key;
    }

    @NonNull
    @Override
    public Map<String, JsonNode> aggregations() {
        return Collections.emptyMap();
    }

    @Override
    public boolean save() {
        return true;
    }

    @Nullable
    @Override
    public String analyzeResultName() {
        return key;
    }

    @NonNull
    @Override
    public SearchQuery toSearchQuery() {
        return this;
    }

    @NonNull
    @Override
    public String query() {
        return "id.keyword:*$" + key + "@*";
    }

    @Override
    public boolean extendedFormat() {
        return false;
    }

    @NonNull
    @Override
    public Map<String, JsonNode> postFilters() {
        return Collections.emptyMap();
    }

    @Override
    public int maxResults() {
        return -1;
    }

    @NonNull
    @Override
    public Map<String, Sort.Direction> sorts() {
        return Collections.emptyMap();
    }

    @NonNull
    @Override
    public String toJson() {
        return "{\"query\":\"" + query() + "\"}";
    }
}
