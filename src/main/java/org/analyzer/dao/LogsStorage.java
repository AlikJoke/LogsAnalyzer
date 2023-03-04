package org.analyzer.dao;

import org.analyzer.entities.LogRecordEntity;
import org.analyzer.service.logs.SearchQuery;

import javax.annotation.Nonnull;
import java.util.List;

public interface LogsStorage {

    void deleteAllByIdRegex(@Nonnull String id);

    void deleteByQuery(@Nonnull StorageQuery query);

    void saveAll(@Nonnull List<LogRecordEntity> records);

    long allCount();

    @Nonnull
    List<LogRecordEntity> searchByQuery(@Nonnull StorageQuery query);

    record StorageQuery(@Nonnull SearchQuery query, @Nonnull String userKey) {
    }
}
