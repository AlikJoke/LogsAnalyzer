package org.analyzer.logs.service;

import org.analyzer.logs.model.IndexingNotificationSettings;
import org.analyzer.logs.model.ScheduledIndexingSettings;
import org.analyzer.logs.model.UserEntity;
import org.analyzer.logs.model.UserSettings;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Optional;

public interface UserNotifier {

    void notify(@Nonnull String message, @Nonnull String userTelegramId);

    default void notify(@Nonnull String message, @Nonnull UserEntity user) {
        final var userTelegramId =
                Optional.ofNullable(user.getSettings())
                        .map(UserSettings::getScheduledIndexingSettings)
                        .flatMap(settings ->
                                settings.stream()
                                        .map(ScheduledIndexingSettings::getNotificationSettings)
                                        .map(IndexingNotificationSettings::getNotifyToTelegram)
                                        .filter(Objects::nonNull)
                                        .findAny()
                        )
                        .orElseThrow(() -> new IllegalStateException("TelegramId not found for user " + user.getUsername()));
        notify(message, userTelegramId);
    }
}
