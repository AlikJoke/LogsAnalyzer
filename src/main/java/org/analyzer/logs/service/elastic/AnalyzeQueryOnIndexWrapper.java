package org.analyzer.logs.service.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import org.analyzer.logs.service.AnalyzeQuery;
import org.analyzer.logs.service.SearchQuery;
import org.analyzer.logs.service.util.JsonConverter;
import org.springframework.data.domain.Sort;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;
import java.util.Collections;
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
        return Collections.emptyMap();
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
        return Collections.emptyMap();
    }

    @NonNull
    @Override
    public String toJson(@NonNull final JsonConverter jsonConverter) {
        return "{\"query\":\"" + query() + "\"}";
    }
}
