package org.analyzer.logs.service;

import org.analyzer.logs.model.IndexingNotificationSettings;
import org.analyzer.logs.model.ScheduledIndexingSettings;
import org.analyzer.logs.model.UserEntity;
import org.analyzer.logs.model.UserSettings;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.Objects;

public interface UserNotifier {

    @Nonnull
    Mono<Void> notify(@Nonnull String message, @Nonnull String userTelegramId);

    @Nonnull
    default Mono<Void> notify(@Nonnull String message, @Nonnull UserEntity user) {
        return Mono.justOrEmpty(user.getSettings())
                    .filter(settings -> settings.getScheduledIndexingSettings() != null)
                    .map(UserSettings::getScheduledIndexingSettings)
                    .map(settings ->
                            settings.stream()
                                    .map(ScheduledIndexingSettings::getNotificationSettings)
                                    .map(IndexingNotificationSettings::getNotifyToTelegram)
                                    .filter(Objects::nonNull)
                                    .findAny()
                                    .orElseThrow()
                    )
                    .flatMap(userTelegramId -> notify(message, userTelegramId));
    }
}
