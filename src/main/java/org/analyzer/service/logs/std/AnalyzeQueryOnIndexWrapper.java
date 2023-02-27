package org.analyzer.service.logs.std;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import org.analyzer.service.logs.AnalyzeQuery;
import org.analyzer.service.logs.SearchQuery;
import org.analyzer.service.util.JsonConverter;
import org.springframework.data.domain.Sort;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;
import java.util.Map;

public final class AnalyzeQueryOnIndexWrapper implements AnalyzeQuery {

    private final String key;
    private final int pageNumber;

    public AnalyzeQueryOnIndexWrapper(final String key) {
        this(key, 0);
    }

    private AnalyzeQueryOnIndexWrapper(final String key, final int pageNumber) {
        this.key = key;
        this.pageNumber = pageNumber;
    }

    @NonNull
    @Override
    public Map<String, JsonNode> aggregations() {
        return Map.of();
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
    public SearchQuery toSearchQuery(@Nonnegative int pageNumber) {
        return new AnalyzeQueryOnIndexWrapper(key, pageNumber);
    }

    @NonNull
    @Override
    public String getId() {
        return key;
    }

    @NonNull
    @Override
    public String query() {
        return "id.keyword:*#" + key + "$*";
    }

    @Override
    public boolean extendedFormat() {
        return false;
    }

    @NonNull
    @Override
    public Map<String, JsonNode> postFilters() {
        return Map.of();
    }

    @Override
    public int pageSize() {
        return 0;
    }

    @Override
    public int pageNumber() {
        return this.pageNumber;
    }

    @NonNull
    @Override
    public SearchQuery toNextPageQuery() {
        return new AnalyzeQueryOnIndexWrapper(key, this.pageNumber + 1);
    }

    @NonNull
    @Override
    public Map<String, Sort.Direction> sorts() {
        return Map.of();
    }

    @NonNull
    @Override
    public String toJson(@NonNull final JsonConverter jsonConverter) {
        return "{\"query\":\"" + query() + "\"}";
    }
}
