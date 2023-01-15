package org.parser.app.dao;

import org.parser.app.model.LogRecord;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface LogRecordRepository extends ElasticsearchRepository<LogRecord, String> {
}
