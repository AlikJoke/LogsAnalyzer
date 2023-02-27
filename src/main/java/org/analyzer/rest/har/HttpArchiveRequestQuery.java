package org.analyzer.rest.har;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NonNull;
import org.analyzer.rest.records.RequestSearchQuery;
import org.analyzer.service.har.HttpArchiveOperationsQuery;
import org.analyzer.service.logs.SearchQuery;
import org.springframework.data.domain.Sort;

import java.util.Map;
import java.util.Set;

public class HttpArchiveRequestQuery implements HttpArchiveOperationsQuery {

    private final boolean applyDefaultSorting;
    private final Map<String, Sort.Direction> sort;
    private final Set<String> filteringKeys;
    private final RequestSearchQuery additionalLogsSearchQuery;
    private final String exportToFile;

    @JsonCreator
    public HttpArchiveRequestQuery(
            @JsonProperty("apply_default_sorting") boolean applyDefaultSorting,
            @JsonProperty("sort") Map<String, Sort.Direction> sort,
            @JsonProperty("filtering_keys") Set<String> filteringKeys,
            @JsonProperty("additional_logs_search_query") RequestSearchQuery additionalLogsSearchQuery,
            @JsonProperty("export_to_file") String exportToFile) {
        this.applyDefaultSorting = applyDefaultSorting;
        this.sort = sort;
        this.filteringKeys = filteringKeys;
        this.additionalLogsSearchQuery = additionalLogsSearchQuery;
        this.exportToFile = exportToFile;
    }

    @Override
    public boolean applyDefaultSorting() {
        return this.applyDefaultSorting;
    }

    @NonNull
    @Override
    public Map<String, Sort.Direction> sort() {
        return this.sort == null ? Map.of() : this.sort;
    }

    @NonNull
    @Override
    public Set<String> filteringKeys() {
        return this.filteringKeys == null ? Set.of() : this.filteringKeys;
    }

    @NonNull
    @Override
    public SearchQuery additionalLogsSearchQuery() {
        return this.additionalLogsSearchQuery;
    }

    public String exportToFile() {
        return this.exportToFile;
    }
}
