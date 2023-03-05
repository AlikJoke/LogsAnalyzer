package org.analyzer.service.scheduled;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.analyzer.entities.ScheduledIndexingSettings;
import org.analyzer.entities.UserEntity;
import org.analyzer.service.users.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ScheduledDataIndexingTask {

    @Autowired
    private UserService userService;
    @Autowired
    private TaskScheduler taskScheduler;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private MeterRegistry meterRegistry;

    private volatile LocalDateTime lastScanInfoTime = LocalDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC);
    private final Map<UserEntity, Map<String, ScheduledFuture<?>>> settingsFuturesByUser = new ConcurrentHashMap<>();

    @PostConstruct
    private void init() {
        this.meterRegistry.gauge(
                "scheduled.indexing.tasks",
                Collections.singleton(Tag.of("description", "Number of scheduled indexing tasks for network data by users settings")),
                this.settingsFuturesByUser,
                map -> map.values()
                            .stream()
                            .mapToInt(Map::size)
                            .sum()
        );
    }

    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    @Timed(
            value = "data-indexing-tasks-manager",
            longTask = true,
            extraTags = { "description", "Manager of network data indexing tasks"}
    )
    public void run() {
        final var currentStamp = LocalDateTime.now();
        this.userService.findAllWithScheduledIndexingSettings(this.lastScanInfoTime)
                        .forEach(this::processChangedUser);
        this.lastScanInfoTime = currentStamp;
    }

    private void processChangedUser(final UserEntity userEntity) {
        final var settingsFutures = settingsFuturesByUser.getOrDefault(userEntity, new HashMap<>());
        final var scheduledSettingsIds = settingsFutures.keySet();
        final var userSettingsByKey =
                userEntity.getSettings().getScheduledIndexingSettings()
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        ScheduledIndexingSettings::getSettingsId,
                                        Function.identity()
                                )
                        );

        // Kill not actual / changed tasks
        scheduledSettingsIds
                .stream()
                .filter(Predicate.not(userSettingsByKey::containsKey))
                .peek(settingsFutures::remove)
                .map(settingsFutures::get)
                .forEach(future -> {
                    if (settingsFutures.isEmpty()) {
                        this.settingsFuturesByUser.remove(userEntity);
                    }

                    future.cancel(true);
                    log.debug("Scheduled task for user {} cancelled", userEntity.getUsername());
                });

        // Schedule not scheduled / changed tasks
        userSettingsByKey.keySet()
                .stream()
                .filter(Predicate.not(scheduledSettingsIds::contains))
                .map(userSettingsByKey::get)
                .forEach(indexingSettings -> {
                    final var indexer = createDataIndexer(userEntity, indexingSettings);
                    final var future = this.taskScheduler.schedule(indexer, composeCronTrigger(indexingSettings));
                    settingsFutures.put(indexingSettings.getSettingsId(), future);

                    log.debug("Schedule task for user {} by settings = {}", userEntity.getUsername(), indexingSettings.getSettingsId());
                });

        this.settingsFuturesByUser.putIfAbsent(userEntity, settingsFutures);
    }

    private NetworkDataIndexer createDataIndexer(final UserEntity user, final ScheduledIndexingSettings indexingSettings) {

        final var indexer = this.applicationContext.getBean(NetworkDataIndexer.class);
        indexer.setUser(user);
        indexer.setIndexingSettings(indexingSettings);
        indexer.setNotificationSettings(user.getSettings().getNotificationSettings());

        return indexer;
    }

    private Trigger composeCronTrigger(final ScheduledIndexingSettings indexingSettings) {
        return new CronTrigger(indexingSettings.getSchedule());
    }
}
