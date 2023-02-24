package org.analyzer.logs.rest.har;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NonNull;
import org.analyzer.logs.rest.records.RequestSearchQuery;
import org.analyzer.logs.service.HttpArchiveOperationsQuery;
import org.analyzer.logs.service.SearchQuery;
import org.springframework.data.domain.Sort;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class HttpArchiveRequestQuery implements HttpArchiveOperationsQuery {

    private final boolean applyDefaultSorting;
    private final Map<String, Sort.Direction> sort;
    private final Set<String> filteringKeys;
    private final RequestSearchQuery additionalLogsSearchQuery;

    @JsonCreator
    public HttpArchiveRequestQuery(
            @JsonProperty("apply_default_sorting") boolean applyDefaultSorting,
            @JsonProperty("sort") Map<String, Sort.Direction> sort,
            @JsonProperty("filtering_keys") Set<String> filteringKeys,
            @JsonProperty("additional_logs_search_query") RequestSearchQuery additionalLogsSearchQuery) {
        this.applyDefaultSorting = applyDefaultSorting;
        this.sort = sort;
        this.filteringKeys = filteringKeys;
        this.additionalLogsSearchQuery = additionalLogsSearchQuery;
    }

    @Override
    public boolean applyDefaultSorting() {
        return this.applyDefaultSorting;
    }

    @NonNull
    @Override
    public Map<String, Sort.Direction> sort() {
        return this.sort == null ? Collections.emptyMap() : this.sort;
    }

    @NonNull
    @Override
    public Set<String> filteringKeys() {
        return this.filteringKeys == null ? Collections.emptySet() : this.filteringKeys;
    }

    @NonNull
    @Override
    public SearchQuery additionalLogsSearchQuery() {
        return this.additionalLogsSearchQuery;
    }
}
