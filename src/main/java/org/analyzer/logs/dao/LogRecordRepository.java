package org.analyzer.logs.dao;

import org.analyzer.logs.model.LogRecord;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;

public interface LogRecordRepository extends ReactiveElasticsearchRepository<LogRecord, String> {
}
