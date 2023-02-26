package org.analyzer.service.logs.std;

import lombok.NonNull;
import org.analyzer.service.logs.LogKeysFactory;
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
    public String createLogRecordKey(@NonNull final String indexedFileKey, final long recordId) {
        return indexedFileKey + "@" + recordId;
    }
}
