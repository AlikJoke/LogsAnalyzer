package org.analyzer.logs.service;

import org.analyzer.logs.model.NotificationSettings;
import org.analyzer.logs.model.UserEntity;
import org.analyzer.logs.model.UserSettings;

import javax.annotation.Nonnull;
import java.util.Optional;

public interface UserNotifier {

    void notify(@Nonnull String message, @Nonnull Long userTelegramId);

    default void notify(@Nonnull String message, @Nonnull UserEntity user) {
        final var userTelegramId =
                Optional.ofNullable(user.getSettings())
                        .map(UserSettings::getNotificationSettings)
                        .map(NotificationSettings::getNotifyToTelegram)
                        .orElseThrow(() -> new IllegalStateException("TelegramId not found for user " + user.getUsername()));
        notify(message, userTelegramId);
    }
}
