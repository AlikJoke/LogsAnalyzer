package org.analyzer.logs.service.scheduled;

import org.analyzer.logs.model.NotificationSettings;

import javax.annotation.Nonnull;

public interface DataIndexingNotifier {

    void notifySuccess(
            @Nonnull String indexingKey,
            @Nonnull String successMessage,
            @Nonnull NotificationSettings notificationSettings);

    void notifyError(
            @Nonnull String errorMessage,
            @Nonnull NotificationSettings notificationSettings);
}
