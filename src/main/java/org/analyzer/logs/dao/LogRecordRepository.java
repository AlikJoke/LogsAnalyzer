package org.analyzer.logs.dao;

import org.analyzer.logs.model.LogRecordEntity;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import javax.annotation.Nonnull;

public interface LogRecordRepository extends ElasticsearchRepository<LogRecordEntity, String> {

    void deleteAllByIdRegex(@Nonnull String id);
}
