package org.analyzer.service.management.mongodb;

import lombok.NonNull;
import org.analyzer.dao.LogsStatisticsRepository;
import org.analyzer.entities.LogsStatisticsEntity;
import org.analyzer.service.management.StatisticsManagementService;
import org.analyzer.service.users.UserService;
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
