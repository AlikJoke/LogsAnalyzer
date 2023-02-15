package org.analyzer.logs.service.scheduled;

import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class FileSystemCleaningTask {

    private final long tempFilesCleaningIntervalMs;

    public FileSystemCleaningTask(@Value("${logs.analyzer.fs.clearing.interval:1440}") final long tempFilesCleaningInterval) {
        this.tempFilesCleaningIntervalMs = TimeUnit.MILLISECONDS.convert(tempFilesCleaningInterval, TimeUnit.MINUTES);
    }

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
    @Timed(
            value = "temp-data-cleaning-task-manager",
            extraTags = { "description", "Manager of cleaning temp data task" }
    )
    public void run() {
        log.info("Cleaning file system started...");
        clear(FileUtils.getTempDirectory(), true);
        log.info("Cleaning file system finished...");
    }

    private void clear(final File file, final boolean root) {
        final var lastModified = file.lastModified();
        if (file.isDirectory()) {
            final var childs = file.listFiles();
            if (childs != null) {
                for (final var child : childs) {
                    clear(child, false);
                }
            }
        }

        if (!root && lastModified <= System.currentTimeMillis() - this.tempFilesCleaningIntervalMs) {
            log.trace("File will be deleted: {}", file.getAbsolutePath());
            FileUtils.deleteQuietly(file);
        }
    }
}
