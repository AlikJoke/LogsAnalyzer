package org.analyzer.logs.rest.stats;

import lombok.Getter;
import lombok.NonNull;
import org.analyzer.logs.model.LogsStatisticsEntity;
import org.analyzer.logs.rest.ResourceLink;
import org.analyzer.logs.rest.hateoas.LinksCollector;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
public class StatisticsResource {

    private final String statisticsName;
    private final String dataQuery;
    private final LocalDateTime createdAt;
    private final Map<String, Object> statistics;
    private final List<ResourceLink> links;

    public StatisticsResource(
            @NonNull final LogsStatisticsEntity logsStatistics,
            @NonNull final LinksCollector linksCollector) {
        this.statisticsName = logsStatistics.getTitle();
        this.statistics = logsStatistics.getStats();
        this.createdAt = logsStatistics.getCreated();
        this.dataQuery = logsStatistics.getDataQuery();
        this.links = linksCollector.collectFor(StatisticsResource.class);
    }
}
