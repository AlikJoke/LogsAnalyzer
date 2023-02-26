package org.analyzer.dao;

import org.analyzer.entities.LogRecordEntity;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import javax.annotation.Nonnull;

public interface LogRecordRepository extends ElasticsearchRepository<LogRecordEntity, String> {

    void deleteAllByIdRegex(@Nonnull String id);
}
