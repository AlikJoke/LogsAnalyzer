package org.analyzer.logs.service.scheduled;

import io.micrometer.core.annotation.Timed;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.analyzer.logs.model.LogsStatisticsEntity;
import org.analyzer.logs.model.ScheduledIndexingSettings;
import org.analyzer.logs.model.UserEntity;
import org.analyzer.logs.service.CurrentUserAccessor;
import org.analyzer.logs.service.LogRecordFormat;
import org.analyzer.logs.service.LogsService;
import org.analyzer.logs.service.util.JsonConverter;
import org.apache.commons.io.FileUtils;
import org.asynchttpclient.AsyncCompletionHandlerBase;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Setter
@Slf4j
public class NetworkDataIndexer implements Runnable {

    private static final int REQUEST_TIMEOUT = 2 * 60_000;

    @Autowired
    private LogsService logsService;
    @Autowired
    private AsyncHttpClient asyncHttpClient;
    @Autowired
    private CurrentUserAccessor userAccessor;
    @Autowired
    private JsonConverter jsonConverter;
    @Autowired
    private DataIndexingTelegramNotifier telegramNotifier;
    @Autowired
    private DataIndexingMailNotifier mailNotifier;
    @Value("${logs.analyzer.indexing-timeout.seconds:60}")
    private long indexingTimeout;

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

        this.asyncHttpClient
                .prepareGet(url)
                .addHeader("Authorization", authToken)
                .setRequestTimeout(REQUEST_TIMEOUT)
                .execute(new AsyncCompletionHandlerBase() {
                    @Override
                    public void onThrowable(Throwable t) {
                        log.error("", t);
                    }

                    @Override
                    public Response onCompleted(Response response) throws Exception {
                        log.debug("Request completed with response status {}", response.getStatusCode());
                        return super.onCompleted(response);
                    }
                })
                .toCompletableFuture()
                .thenApply(Response::getResponseBodyAsStream)
                .thenAccept(this::processDownloadedData)
                .join();
    }

    private void processDownloadedData(final InputStream is) {
        final var dataFile = createTempFile();
        try (final var userContext = this.userAccessor.as(this.user)) {
            FileUtils.copyInputStreamToFile(is, dataFile);
            final var indexingProcess = this.logsService.index(dataFile, createLogRecordFormat());

            indexingProcess.processMonitor().get(this.indexingTimeout, TimeUnit.SECONDS);

            this.logsService.findStatisticsByKey(indexingProcess.indexingKey())
                            .ifPresent(this::onComplete);
        } catch (Exception e) {
            onError(e);
        } finally {
            dataFile.delete();
        }
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
        final var notificationSettings = this.indexingSettings.getNotificationSettings();
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
