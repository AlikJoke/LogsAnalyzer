package org.analyzer.service.users.notifications.telegram.commands;

import lombok.NonNull;
import org.analyzer.config.telegram.TelegramBotConfiguration;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;

abstract class BaseUploadingFileBotCommand extends ApplicationBotCommand {

    private static final int CONNECT_TIMEOUT = 5_000;
    private static final int READ_TIMEOUT = 10_000;

    @Autowired
    private TelegramBotConfiguration botConfiguration;

    protected BaseUploadingFileBotCommand(
            @NonNull String commandIdentifier,
            @Nullable String description) {
        super(commandIdentifier, description, true);
    }

    protected File downloadFile(final AbsSender sender, final String uploadedFileId) {

        final var uploadedFile = new GetFile(uploadedFileId);
        try {
            final var file = sender.execute(uploadedFile);
            final var fileUrl = file.getFileUrl(botConfiguration.getToken());

            final var fileUrlResource = new UrlResource(new URL(fileUrl));
            final File result = File.createTempFile(UUID.randomUUID().toString(), null);
            FileUtils.copyURLToFile(
                    fileUrlResource.getURL(),
                    result,
                    CONNECT_TIMEOUT,
                    READ_TIMEOUT
            );
            return result;
        } catch (TelegramApiException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
