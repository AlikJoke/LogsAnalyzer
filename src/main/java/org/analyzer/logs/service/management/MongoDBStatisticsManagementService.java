package org.analyzer.logs.service.management;

import lombok.NonNull;
import org.analyzer.logs.dao.LogsStatisticsRepository;
import org.analyzer.logs.model.LogsStatisticsEntity;
import org.analyzer.logs.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;

@Service
public class MongoDBStatisticsManagementService extends MongoDBManagementServiceWithUserCountersBase<LogsStatisticsEntity> implements StatisticsManagementService {

    private final LogsStatisticsRepository statisticsRepository;

    @Autowired
    MongoDBStatisticsManagementService(
            @NonNull LogsStatisticsRepository statisticsRepository,
            @NonNull UserService userService,
            @NonNull MongoTemplate template) {
        super(userService, template, LogsStatisticsEntity.class);
        this.statisticsRepository = statisticsRepository;
    }

    @Override
    public long commonCount() {
        return this.statisticsRepository.count();
    }

    @NonNull
    @Override
    public List<StatisticsManagementService.CountByUsers> countRecordsByUsers() {
        return composeCountByUsers(
                group("user_key").sum("stats.common-count").as("total")
        );
    }
}
