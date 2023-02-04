package org.analyzer.logs.test.rest.fixtures;

import org.analyzer.logs.model.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

public abstract class UserFixtures {

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
}
