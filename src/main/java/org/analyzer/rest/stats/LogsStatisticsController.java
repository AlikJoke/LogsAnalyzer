package org.analyzer.rest.stats;

import org.analyzer.rest.ControllerBase;
import org.analyzer.rest.RootEntrypointResource;
import org.analyzer.rest.hateoas.LinksCollector;
import org.analyzer.rest.hateoas.NamedEndpoint;
import org.analyzer.rest.records.IndexingResult;
import org.analyzer.rest.users.UserResource;
import org.analyzer.service.logs.LogsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping(LogsStatisticsController.PATH_BASE)
public class LogsStatisticsController extends ControllerBase {

    static final String PATH_BASE = "/logs/statistics";
    static final String PATH_GENERATE = "/generate";

    @Autowired
    private LogsService service;
    @Autowired
    @Lazy
    private LinksCollector linksCollector;

    @PostMapping(PATH_GENERATE)
    @ResponseStatus(HttpStatus.OK)
    @NamedEndpoint(value = "analyze.logs", includeTo = RootEntrypointResource.class)
    public Map<String, Object> analyze(@RequestBody RequestAnalyzeQuery query) {
        return this.service.analyze(query);
    }

    @GetMapping("/{statisticsKey}")
    @NamedEndpoint(value = "find.statistics.by.key", includeTo = IndexingResult.class)
    @NamedEndpoint(value = "self", includeTo = StatisticsResource.class)
    public StatisticsResource find(@PathVariable("statisticsKey") String statisticsKey) {
        return this.service.findStatisticsByKey(statisticsKey)
                            .map(stats -> new StatisticsResource(stats, this.linksCollector))
                            .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/history")
    @NamedEndpoint(value = "statistics.history", includeTo = UserResource.class)
    public List<StatisticsResource> readHistory() {
        return this.service.findAllStatisticsByCreationDate(LocalDateTime.now())
                            .stream()
                            .map(stats -> new StatisticsResource(stats, this.linksCollector))
                            .toList();
    }

    @Override
    protected Set<HttpMethod> supportedMethods() {
        return Set.of(HttpMethod.OPTIONS, HttpMethod.GET, HttpMethod.POST);
    }
}
