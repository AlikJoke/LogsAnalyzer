package org.analyzer.service.users.std;

import lombok.NonNull;
import org.analyzer.entities.UserEntity;
import org.analyzer.service.har.HttpArchiveService;
import org.analyzer.service.logs.LogKeysFactory;
import org.analyzer.service.logs.LogsService;
import org.analyzer.service.logs.SearchQuery;
import org.analyzer.service.logs.std.SimpleSearchQuery;
import org.analyzer.service.users.CurrentUserAccessor;
import org.analyzer.service.users.UserDataStorageCleaner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DefaultUserDataStorageCleaner implements UserDataStorageCleaner {

    @Autowired
    private LogsService logsService;
    @Autowired
    private LogKeysFactory logKeysFactory;
    @Autowired
    private CurrentUserAccessor userAccessor;
    @Autowired
    private HttpArchiveService httpArchiveService;

    @Override
    public void clear(@NonNull UserEntity user, @NonNull LocalDateTime deleteOlderThan) {

        try (final var userContext = this.userAccessor.as(user)) {
            final var indexingKeys = this.logsService.deleteAllStatisticsByCreationDate(deleteOlderThan);

            if (!indexingKeys.isEmpty()) {
                this.logsService.deleteByQuery(createSearchQueryToDelete(user, indexingKeys));
            }

            this.httpArchiveService.deleteAllByCreationDate(deleteOlderThan);
        }
    }

    private SearchQuery createSearchQueryToDelete(final UserEntity user, final List<String> indexingKeys) {
        final var indexingKeysString = indexingKeys
                                        .stream()
                                        .map(indexingKey -> this.logKeysFactory.createUserIndexingKey(user.getHash(), indexingKey))
                                        .map(key -> "id.keyword:" + key + "*")
                                        .collect(Collectors.joining(" OR "));
        return new SimpleSearchQuery(indexingKeysString);
    }
}
