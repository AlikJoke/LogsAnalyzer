package org.analyzer.logs.service.management;

import lombok.NonNull;
import org.analyzer.logs.dao.LogsStatisticsRepository;
import org.analyzer.logs.model.LogsStatisticsEntity;
import org.analyzer.logs.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;

@Service
public class MongoDBStatisticsManagementService extends MongoDBManagementServiceWithUserCountersBase<LogsStatisticsEntity> implements StatisticsManagementService {

    private final LogsStatisticsRepository statisticsRepository;

    @Autowired
    MongoDBStatisticsManagementService(
            @NonNull LogsStatisticsRepository statisticsRepository,
            @NonNull UserService userService,
            @NonNull ReactiveMongoTemplate template) {
        super(userService, template, LogsStatisticsEntity.class);
        this.statisticsRepository = statisticsRepository;
    }

    @NonNull
    @Override
    public Mono<Long> commonCount() {
        return this.statisticsRepository.count();
    }

    @NonNull
    @Override
    public Flux<StatisticsManagementService.CountByUsers> countRecordsByUsers() {
        return composeCountByUsersFlux(
                group("user_key").sum("stats.common-count").as("total")
        );
    }
}
