package org.analyzer.service.users.notifications;

import org.analyzer.entities.NotificationSettings;
import org.analyzer.entities.UserEntity;
import org.analyzer.entities.UserSettings;

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
