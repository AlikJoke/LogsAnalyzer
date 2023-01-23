package org.analyzer.logs.dao;

import org.analyzer.logs.model.LogsStatistics;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface LogsStatisticsRepository extends ReactiveMongoRepository<String, LogsStatistics> {
}
