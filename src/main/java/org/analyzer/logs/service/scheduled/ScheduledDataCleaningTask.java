package org.analyzer.logs.service.scheduled;

import com.fasterxml.jackson.databind.JsonNode;
import io.micrometer.core.annotation.Timed;
import lombok.NonNull;
import org.analyzer.logs.model.UserEntity;
import org.analyzer.logs.service.*;
import org.analyzer.logs.service.util.JsonConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
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
    @Autowired
    private CurrentUserAccessor userAccessor;

    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    @Timed(
            value = "data-cleaning-task",
            longTask = true,
            extraTags = { "description", "Data cleaning task"}
    )
    public void run() {
        this.userService.findAllWithClearingSettings()
                        .map(this::clearData)
                        .subscribe();
    }

    private Mono<Void> clearData(final UserEntity user) {
        final var timestamp = createTimestamp(user.getSettings().getCleaningInterval());
        final var indexingKeysFlux = this.logsService.deleteAllStatisticsByUserKeyAndCreationDate(user.getHash(), timestamp);
        return indexingKeysFlux
                    .collectList()
                    .map(
                            indexingKeys -> this.logsService.deleteByQuery(createSearchQueryToDelete(user, indexingKeys))
                    )
                    .contextWrite(this.userAccessor.set(Mono.just(user)))
                    .then();
    }

    private LocalDateTime createTimestamp(final long intervalInMinutes) {
        return LocalDateTime.now().minusMinutes(intervalInMinutes);
    }

    private SearchQuery createSearchQueryToDelete(final UserEntity user, final List<String> indexingKeys) {
        final var indexingKeysString = indexingKeys
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
            public String toJson(@NonNull JsonConverter jsonConverter) {
                return "{" + query() + "}";
            }
        };
    }
}