package org.analyzer.logs.dao;

import org.analyzer.logs.model.LogRecordEntity;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;

public interface LogRecordRepository extends ReactiveElasticsearchRepository<LogRecordEntity, String> {
}
