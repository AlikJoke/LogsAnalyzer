package org.analyzer.logs.dao;

import org.analyzer.logs.model.LogsStatisticsEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface LogsStatisticsRepository extends ReactiveMongoRepository<LogsStatisticsEntity, String> {
}
