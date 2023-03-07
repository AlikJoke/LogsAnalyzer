package org.analyzer.service.users.notifications.telegram.commands;

import lombok.NonNull;
import org.analyzer.entities.HttpArchiveEntity;
import org.analyzer.entities.UserEntity;
import org.analyzer.service.har.HttpArchiveService;
import org.analyzer.service.users.notifications.telegram.TelegramCommandConversationChain;
import org.analyzer.service.users.notifications.telegram.TelegramUserConversationStore;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.Optional;
import java.util.stream.Collectors;

import static org.analyzer.service.users.notifications.telegram.commands.UploadHarFileCommand.COMMAND_NAME;

@Component(COMMAND_NAME)
public class UploadHarFileCommand extends BaseUploadingFileBotCommand implements TelegramCommandConversationChain {

    static final String COMMAND_NAME = "upload_har_file";

    private static final String UPLOADING_FILE_STAGE = "uploadingFile";

    @Autowired
    private TelegramUserConversationStore userConversationStore;
    @Autowired
    private HttpArchiveService harService;

    public UploadHarFileCommand() {
        super(COMMAND_NAME, "Upload HAR file to application");
    }

    @Override
    protected Optional<SendMessage> executeCommand(AbsSender absSender, User user, Chat chat, UserEntity userContext, String[] arguments) {

        final var msg = createReplyMessage(chat.getId(), "Send HAR file to me.");

        final var context = this.userConversationStore.createUserCommandContext(user.getId(), COMMAND_NAME);
        context.setLastStage(UPLOADING_FILE_STAGE);

        this.userConversationStore.updateLastCommandContext(user.getId(), context);

        return Optional.of(msg);
    }

    @Override
    public Optional<PartialBotApiMethod<?>> onMessageReceived(
            @NonNull AbsSender absSender,
            @NonNull Long userId,
            @NonNull Message message) {

        final var context = this.userConversationStore.getLastCommandContext(userId);
        if (context == null) {
            throw new IllegalStateException("Illegal state of conversation");
        }

        final var result = handleFileUploadingStage(absSender, context, message);
        this.userConversationStore.clearUserCommandContext(userId);

        final var replyMsg = createReplyMessage(message.getChatId(), result);
        replyMsg.setReplyToMessageId(message.getMessageId());

        return Optional.of(replyMsg);
    }

    private String handleFileUploadingStage(
            final AbsSender absSender,
            final TelegramUserConversationStore.CommandContext context,
            final Message message) {

        if (message.getDocument() == null || StringUtils.isEmpty(message.getDocument().getFileId())) {
            return "<b>Expected file for uploading HAR file command.<b>";
        }

        final var uploadedFile = downloadFile(absSender, message.getDocument().getFileId());
        try {
            final var createdHar = executeInUserContext(message.getChatId(), () -> this.harService.create(uploadedFile));

            final var createdHarIds = createdHar
                                        .stream()
                                        .map(HttpArchiveEntity::getId)
                                        .collect(Collectors.joining(", "));
            return "HAR uploaded to application, key of created HAR: " + createdHarIds;
        } finally {
            uploadedFile.delete();
        }
    }
}
