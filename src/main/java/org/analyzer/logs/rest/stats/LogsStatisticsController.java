package org.analyzer.logs.rest.stats;

import org.analyzer.logs.model.UserEntity;
import org.analyzer.logs.rest.ControllerBase;
import org.analyzer.logs.rest.RootEntrypointResource;
import org.analyzer.logs.rest.hateoas.LinksCollector;
import org.analyzer.logs.rest.hateoas.NamedEndpoint;
import org.analyzer.logs.rest.records.IndexingResult;
import org.analyzer.logs.service.CurrentUserAccessor;
import org.analyzer.logs.service.LogsService;
import org.analyzer.logs.service.MapLogsStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
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
    private CurrentUserAccessor userAccessor;
    @Autowired
    private LinksCollector linksCollector;

    @PostMapping(PATH_GENERATE)
    @ResponseStatus(HttpStatus.OK)
    @NamedEndpoint(value = "analyze.logs", includeTo = RootEntrypointResource.class)
    public Mono<Map<String, Object>> analyze(@RequestBody RequestAnalyzeQuery query) {
        return this.service.analyze(query)
                            .flatMap(MapLogsStatistics::toResultMap)
                            .onErrorResume(this::onError);
    }

    @GetMapping("/{statisticsKey}")
    @NamedEndpoint(value = "find.statistics.by.key", includeTo = IndexingResult.class)
    @NamedEndpoint(value = "self", includeTo = StatisticsResource.class)
    public Mono<StatisticsResource> find(@PathVariable("statisticsKey") String statisticsKey) {
        return this.service.findStatisticsByKey(statisticsKey)
                            .map(stats -> new StatisticsResource(stats, this.linksCollector))
                            .onErrorResume(this::onError);
    }

    @GetMapping("/history")
    @NamedEndpoint(value = "statistics.history", includeTo = RootEntrypointResource.class)
    public Flux<StatisticsResource> readHistory() {
        return this.userAccessor.get()
                .map(UserEntity::getHash)
                .flatMapMany(userKey -> this.service.findAllStatisticsByUserKeyAndCreationDate(userKey, LocalDateTime.now()))
                .map(stats -> new StatisticsResource(stats, this.linksCollector))
                .onErrorResume(this::onError);
    }

    @Override
    protected Set<HttpMethod> supportedMethods() {
        return Set.of(HttpMethod.OPTIONS, HttpMethod.GET, HttpMethod.POST);
    }
}
