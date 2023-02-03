package org.analyzer.logs.service.scheduled;

import io.micrometer.core.annotation.Timed;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.analyzer.logs.model.IndexingNotificationSettings;
import org.analyzer.logs.model.LogsStatisticsEntity;
import org.analyzer.logs.model.ScheduledIndexingSettings;
import org.analyzer.logs.model.UserEntity;
import org.analyzer.logs.service.CurrentUserAccessor;
import org.analyzer.logs.service.LogRecordFormat;
import org.analyzer.logs.service.LogsService;
import org.analyzer.logs.service.util.JsonConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Setter
@Slf4j
public class NetworkDataIndexer implements Runnable {

    @Autowired
    private LogsService logsService;
    @Autowired
    private WebClient webClient;
    @Autowired
    private CurrentUserAccessor userAccessor;
    @Autowired
    private JsonConverter jsonConverter;
    @Autowired
    private DataIndexingTelegramNotifier telegramNotifier;
    @Autowired
    private DataIndexingMailNotifier mailNotifier;

    private UserEntity user;
    private ScheduledIndexingSettings indexingSettings;

    @Override
    @Timed(
            value = "network-data-indexing",
            longTask = true,
            extraTags = { "description", "Network data indexing task"}
    )
    public void run() {

        final var url = indexingSettings.getNetworkSettings().getLogsUrl();
        final var authToken = indexingSettings.getNetworkSettings().getAuthToken();
        final var dataFile = createTempFile();
        final var dataBufferFlux = this.webClient
                                            .get()
                                            .uri(url)
                                            .header("Authorization", authToken)
                                            .retrieve()
                                            .bodyToFlux(DataBuffer.class);
        final var fileMono = DataBufferUtils.write(dataBufferFlux, dataFile.toPath(), StandardOpenOption.CREATE)
                                        .share()
                                        .thenReturn(dataFile);

        this.logsService.index(fileMono, createLogRecordFormat())
                            .flatMap(this.logsService::findStatisticsByKey)
                            .doOnSuccess(this::onComplete)
                            .doOnError(this::onError)
                            .contextWrite(this.userAccessor.set(Mono.just(this.user)))
                            .subscribe();
    }

    private void onComplete(final LogsStatisticsEntity statistics) {

        log.trace("Logs indexing for user {} completed: {}", this.user.getUsername(), statistics.getId());

        final var notificationSettings = this.indexingSettings.getNotificationSettings();
        if (!notificationSettings.isAggregationNotificationsEnabled()) {
            log.trace("Aggregation notifications for user {} disabled", this.user.getUsername());
            return;
        }

        final var statsJson = this.jsonConverter.convertToJson(statistics.getStats());
        if (this.indexingSettings.getNotificationSettings().getNotifyToEmail() != null) {
            this.mailNotifier.notifySuccess(statistics.getId(), statsJson, this.indexingSettings.getNotificationSettings());
            log.info("Success mail notification for user {} sent", this.user.getUsername());
        }

        if (this.indexingSettings.getNotificationSettings().getNotifyToTelegram() != null) {
            this.telegramNotifier.notifySuccess(statistics.getId(), statsJson, this.indexingSettings.getNotificationSettings());
            log.info("Success telegram notification for user {} sent", this.user.getUsername());
        }
    }

    private void onError(final Throwable ex) {
        log.error("Exception while indexing for user " + this.user.getUsername(), ex);
        final IndexingNotificationSettings notificationSettings = this.indexingSettings.getNotificationSettings();
        if (!notificationSettings.isErrorNotificationsEnabled()) {
            log.trace("Error notifications for user {} disabled", this.user.getUsername());
            return;
        }

        if (this.indexingSettings.getNotificationSettings().getNotifyToEmail() != null) {
            this.mailNotifier.notifyError(ex.getMessage(), this.indexingSettings.getNotificationSettings());
            log.info("Error mail notification for user {} sent", this.user.getUsername());
        }

        if (this.indexingSettings.getNotificationSettings().getNotifyToTelegram() != null) {
            this.telegramNotifier.notifyError(ex.getMessage(), this.indexingSettings.getNotificationSettings());
            log.info("Error telegram notification for user {} sent", this.user.getUsername());
        }
    }

    private File createTempFile() {

        final var uuid = UUID.randomUUID().toString();
        try {
            final Path destDirPath = Files.createTempDirectory(uuid);
            final var destDir = destDirPath.toFile();
            destDir.deleteOnExit();

            return destDirPath.resolveSibling(uuid).toFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private LogRecordFormat createLogRecordFormat() {

        return new LogRecordFormat() {
            @NonNull
            @Override
            public String pattern() {
                return indexingSettings.getLogRecordPattern();
            }

            @NonNull
            @Override
            public String timeFormat() {
                return indexingSettings.getTimeFormat();
            }

            @NonNull
            @Override
            public String dateFormat() {
                return indexingSettings.getDateFormat();
            }
        };
    }
}
