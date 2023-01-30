package org.analyzer.logs.service.management;

import lombok.Data;
import lombok.NonNull;
import org.analyzer.logs.dao.LogsStatisticsRepository;
import org.analyzer.logs.model.LogsStatisticsEntity;
import org.analyzer.logs.model.UserEntity;
import org.analyzer.logs.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
public class MongoDBStatisticsManagementService extends MongoDBManagementServiceBase<LogsStatisticsEntity> implements StatisticsManagementService {

    private final LogsStatisticsRepository statisticsRepository;
    private final UserService userService;

    @Autowired
    MongoDBStatisticsManagementService(
            @NonNull LogsStatisticsRepository statisticsRepository,
            @NonNull UserService userService,
            @NonNull ReactiveMongoTemplate template) {
        super(template, LogsStatisticsEntity.class);
        this.userService = userService;
        this.statisticsRepository = statisticsRepository;
    }

    @NonNull
    @Override
    public Mono<Long> commonCount() {
        return this.statisticsRepository.count();
    }

    @NonNull
    @Override
    public Flux<StatisticsManagementService.CountByUsers> countStatsByUsers() {
        return composeCountByUsersFlux(
                group("user_key").count().as("total")
        );
    }

    @NonNull
    @Override
    public Flux<StatisticsManagementService.CountByUsers> countRecordsByUsers() {
        return composeCountByUsersFlux(
                group("user_key").sum("stats.common-count").as("total")
        );
    }

    private Flux<StatisticsManagementService.CountByUsers> composeCountByUsersFlux(final AggregationOperation groupOperation) {
        final Flux<CountByUsers> countByUsersFlux =
                this.template.aggregate(
                        newAggregation(
                                LogsStatisticsEntity.class,
                                groupOperation,
                                project("total").and("user_key").previousOperation(),
                                sort(Sort.Direction.DESC, "total")
                        ),
                        LogsStatisticsEntity.class,
                        CountByUsers.class);
        return countByUsersFlux
                .flatMap(data ->
                        this.userService.findByUserHash(data.getUserKey())
                                        .map(UserEntity::getUsername)
                                        .doOnNext(data::setUser_key)
                                        .thenReturn(data)
                );
    }

    @Data
    private static class CountByUsers implements StatisticsManagementService.CountByUsers {

        private String user_key;
        private long total;

        @NonNull
        @Override
        public String getUserKey() {
            return user_key;
        }

        @Override
        public long getCount() {
            return total;
        }
    }
}
