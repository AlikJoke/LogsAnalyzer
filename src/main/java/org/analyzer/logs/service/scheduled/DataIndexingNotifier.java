package org.analyzer.logs.service.scheduled;

import org.analyzer.logs.model.IndexingNotificationSettings;

import javax.annotation.Nonnull;

public interface DataIndexingNotifier {

    void notifySuccess(
            @Nonnull String indexingKey,
            @Nonnull String successMessage,
            @Nonnull IndexingNotificationSettings notificationSettings);

    void notifyError(
            @Nonnull String errorMessage,
            @Nonnull IndexingNotificationSettings notificationSettings);
}
