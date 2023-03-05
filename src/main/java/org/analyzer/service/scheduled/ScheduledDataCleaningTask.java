package org.analyzer.service.scheduled;

import io.micrometer.core.annotation.Timed;
import org.analyzer.entities.UserEntity;
import org.analyzer.service.users.UserDataStorageCleaner;
import org.analyzer.service.users.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

public class ScheduledDataCleaningTask {

    @Autowired
    private UserService userService;
    @Autowired
    private UserDataStorageCleaner storageCleaner;

    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    @Timed(
            value = "data-cleaning-task",
            longTask = true,
            extraTags = { "description", "Data cleaning task"}
    )
    public void run() {
        this.userService.findAllWithClearingSettings()
                        .forEach(this::clearData);
    }

    private void clearData(final UserEntity user) {
        this.storageCleaner.clear(user, createTimestamp(user.getSettings().getCleaningInterval()));
    }

    private LocalDateTime createTimestamp(final long intervalInMinutes) {
        return LocalDateTime.now().minusMinutes(intervalInMinutes);
    }
}
