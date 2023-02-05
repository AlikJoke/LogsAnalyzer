package org.analyzer.logs.rest.stats;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import org.analyzer.logs.model.LogsStatisticsEntity;
import org.analyzer.logs.rest.ResourceLink;
import org.analyzer.logs.rest.hateoas.LinksCollector;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Value
@AllArgsConstructor(onConstructor = @__(@JsonCreator))
public class StatisticsResource {

    @NonNull
    String id;
    String statisticsName;
    @NonNull
    String dataQuery;
    @NonNull
    LocalDateTime createdAt;
    @NonNull
    Map<String, Object> statistics;
    @NonNull
    List<ResourceLink> links;

    public StatisticsResource(
            @NonNull final LogsStatisticsEntity logsStatistics,
            @NonNull final LinksCollector linksCollector) {
        this.id = logsStatistics.getId();
        this.statisticsName = logsStatistics.getTitle();
        this.statistics = logsStatistics.getStats();
        this.createdAt = logsStatistics.getCreated();
        this.dataQuery = logsStatistics.getDataQuery();
        this.links = linksCollector.collectFor(StatisticsResource.class);
    }
}
