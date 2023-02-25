package org.analyzer.logs.service.scheduled;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.analyzer.logs.model.NotificationSettings;
import org.analyzer.logs.service.UserNotifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DataIndexingTelegramNotifier implements DataIndexingNotifier {

    @Autowired
    private UserNotifier userNotifier;

    @Override
    public void notifySuccess(@NonNull String indexingKey, @NonNull String successMessage, @NonNull NotificationSettings notificationSettings) {
        this.userNotifier.notify(
                "**Logs indexing completed** __(%s)__\n".formatted(indexingKey) + "```" + successMessage + "```",
                notificationSettings.getNotifyToTelegram()
        );
    }

    @Override
    public void notifyError(@NonNull String message, @NonNull NotificationSettings notificationSettings) {
        this.userNotifier.notify(
                "**Logs indexing failed with error**\n" + "__" + message + "__",
                notificationSettings.getNotifyToTelegram()
        );
    }
}
