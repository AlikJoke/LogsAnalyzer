package org.analyzer.logs.test.rest.fixtures;

import org.analyzer.logs.model.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public abstract class TestFixtures {

    public static final String TEST_USER = "test";

    public static UserEntity createUser(final String username) {

        final var indexingNotificationSettings = IndexingNotificationSettings
                                                        .builder()
                                                            .aggregationNotificationsEnabled(true)
                                                            .errorNotificationsEnabled(false)
                                                            .notifyToEmail("test@gmail.ru")
                                                            .notifyToTelegram("telegram-1")
                                                        .build();
        final var networkSettings = NetworkDataSettings
                                        .builder()
                                            .authToken("Basic 1111")
                                            .logsUrl("http://tests.ru")
                                        .build();
        final var scheduledIndexingSettings = ScheduledIndexingSettings
                                                    .builder()
                                                        .schedule("* * * */1 ?")
                                                        .settingsId(UUID.randomUUID().toString())
                                                        .notificationSettings(indexingNotificationSettings)
                                                        .dateFormat("yyyy-MM-dd")
                                                        .timeFormat("hh:mm:ss.S")
                                                        .logRecordPattern("$1-$2-$3")
                                                        .networkSettings(networkSettings)
                                                    .build();
        final var settings = UserSettings
                                .builder()
                                    .cleaningInterval(5)
                                    .scheduledIndexingSettings(Collections.singletonList(scheduledIndexingSettings))
                                .build();

        return new UserEntity()
                    .setUsername(username)
                    .setModified(LocalDateTime.now().minusDays(1))
                    .setHash(UUID.randomUUID().toString())
                    .setActive(true)
                    .setEncodedPassword("-")
                    .setSettings(settings);
    }

    public static LogsStatisticsEntity createStatisticsEntity(final String userKey) {
        final ThreadLocalRandom rnd = ThreadLocalRandom.current();
        return new LogsStatisticsEntity()
                .setCreated(LocalDateTime.now().minusDays(1))
                .setId(UUID.randomUUID().toString())
                .setTitle("some-stat")
                .setDataQuery("{\"query\":\"level.keyword:ERROR\"}")
                .setUserKey(userKey)
                .setStats(Map.of("errors-count", rnd.nextInt(100), "errors-frequencies-by-category", Map.of("category1", rnd.nextInt(25), "category2", rnd.nextInt(25))));
    }

    public static UserSearchQueryEntity createUserSearchQueryEntity(final String userKey) {
        return new UserSearchQueryEntity()
                .setCreated(LocalDateTime.now().minusDays(1))
                .setId(UUID.randomUUID().toString())
                .setQuery("{\"query\":\"level.keyword:ERROR\"}")
                .setUserKey(userKey);
    }
}
