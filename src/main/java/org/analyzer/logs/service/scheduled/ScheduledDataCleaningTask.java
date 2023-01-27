package org.analyzer.logs.service.scheduled;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import org.analyzer.logs.model.UserEntity;
import org.analyzer.logs.service.LogKeysFactory;
import org.analyzer.logs.service.LogsService;
import org.analyzer.logs.service.SearchQuery;
import org.analyzer.logs.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class ScheduledDataCleaningTask {

    @Autowired
    private LogsService logsService;
    @Autowired
    private UserService userService;
    @Autowired
    private LogKeysFactory logKeysFactory;

    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    public void run() {
        this.userService.findAllWithClearingSettings()
                        .flatMap(this::clearData)
                        .subscribe();
    }

    private Mono<Void> clearData(final UserEntity user) {
        final LocalDateTime timestamp = createTimestamp(user.getSettings().getCleaningInterval());
        final Flux<String> indexingKeysFlux = this.logsService.deleteAllStatisticsByUserKeyAndCreationDate(user.getHash(), timestamp);
        return indexingKeysFlux
                    .collectList()
                    .map(
                            indexingKeys -> this.logsService.deleteByQuery(createSearchQueryToDelete(user, indexingKeys))
                    )
                    .then();
    }

    private LocalDateTime createTimestamp(final long intervalInMinutes) {
        return LocalDateTime.now().minusMinutes(intervalInMinutes);
    }

    private SearchQuery createSearchQueryToDelete(final UserEntity user, final List<String> indexingKeys) {
        final String indexingKeysString = indexingKeys
                                                .stream()
                                                .map(indexingKey -> this.logKeysFactory.createUserIndexingKey(user.getHash(), indexingKey))
                                                .map(key -> "id.keyword:" + key + "*")
                                                .collect(Collectors.joining(" OR "));
        return new SearchQuery() {
            @NonNull
            @Override
            public String query() {
                return indexingKeysString;
            }

            @Override
            public boolean extendedFormat() {
                return false;
            }

            @NonNull
            @Override
            public Map<String, JsonNode> postFilters() {
                return Collections.emptyMap();
            }

            @Override
            public int maxResults() {
                return 0;
            }

            @NonNull
            @Override
            public Map<String, Sort.Direction> sorts() {
                return Collections.emptyMap();
            }

            @NonNull
            @Override
            public String toJson() {
                return "{" + query() + "}";
            }
        };
    }
}
