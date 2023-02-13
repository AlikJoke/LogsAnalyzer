package org.analyzer.logs.dao;

import org.analyzer.logs.model.LogRecordEntity;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface LogRecordRepository extends ElasticsearchRepository<LogRecordEntity, String> {
}
