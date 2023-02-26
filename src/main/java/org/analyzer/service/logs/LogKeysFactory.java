package org.analyzer.service.logs;

import javax.annotation.Nonnull;

public interface LogKeysFactory {

    @Nonnull
    String createUserIndexingKey(@Nonnull String userKey, @Nonnull String indexingId);

    @Nonnull
    String createIndexedLogFileKey(@Nonnull String userIndexingKey, @Nonnull String fileId);

    @Nonnull
    default String createIndexedLogFileKey(
            @Nonnull String userKey,
            @Nonnull String indexingId,
            @Nonnull String fileId) {
        return createIndexedLogFileKey(createUserIndexingKey(userKey, indexingId), fileId);
    }

    @Nonnull
    String createLogRecordKey(@Nonnull String indexedFileKey, long recordId);
}
