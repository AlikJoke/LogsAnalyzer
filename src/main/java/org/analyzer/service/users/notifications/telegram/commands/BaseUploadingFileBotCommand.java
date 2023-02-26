package org.analyzer.service.users.notifications.telegram.commands;

import lombok.NonNull;
import org.analyzer.config.telegram.TelegramBotConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileUrlResource;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URL;

abstract class BaseUploadingFileBotCommand extends ApplicationBotCommand {

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

            final var fileUrlResource = new FileUrlResource(new URL(fileUrl));
            return fileUrlResource.getFile();
        } catch (TelegramApiException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
