package org.analyzer.logs.rest.har;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.Accessors;
import org.analyzer.logs.rest.records.RequestSearchQuery;
import org.analyzer.logs.service.HttpArchiveOperationsQuery;
import org.springframework.data.domain.Sort;

import java.util.Map;
import java.util.Set;

@Value
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
public class HttpArchiveGroupLogsByRequestsQuery extends HttpArchiveRequestQuery implements HttpArchiveOperationsQuery.GroupLogsByRequestsQuery{

    RequestSearchQuery additionalLogsSearchQuery;

    @JsonCreator
    public HttpArchiveGroupLogsByRequestsQuery(
            @JsonProperty("apply_default_sort") boolean applyDefaultSort,
            @JsonProperty("sort") Map<String, Sort.Direction> sort,
            @JsonProperty("filtering_keys") Set<String> filteringKeys,
            @JsonProperty("additional_logs_search_query") RequestSearchQuery additionalLogsSearchQuery) {
        super(applyDefaultSort, sort, filteringKeys);
        this.additionalLogsSearchQuery = additionalLogsSearchQuery;
    }
}
