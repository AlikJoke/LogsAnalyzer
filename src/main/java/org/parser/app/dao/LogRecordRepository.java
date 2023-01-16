package org.parser.app.dao;

import org.parser.app.model.LogRecord;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;

public interface LogRecordRepository extends ReactiveElasticsearchRepository<LogRecord, String> {
}
