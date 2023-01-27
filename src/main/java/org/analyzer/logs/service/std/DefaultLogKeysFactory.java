package org.analyzer.logs.service.std;

import lombok.NonNull;
import org.analyzer.logs.service.LogKeysFactory;
import org.springframework.stereotype.Component;

@Component
public class DefaultLogKeysFactory implements LogKeysFactory {

    @NonNull
    @Override
    public String createUserIndexingKey(@NonNull final String userKey, @NonNull final String indexingId) {
        return userKey + "#" + indexingId;
    }

    @NonNull
    @Override
    public String createIndexedLogFileKey(@NonNull final String userIndexingKey, @NonNull final String fileId) {
        return userIndexingKey + "$" + fileId;
    }

    @NonNull
    @Override
    public String createLogRecordKey(@NonNull final String indexedFileKey, @NonNull final Long recordId) {
        return indexedFileKey + "@" + recordId;
    }
}
