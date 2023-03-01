package org.analyzer.service.users.notifications.telegram.commands;

import lombok.NonNull;
import org.analyzer.entities.UserEntity;
import org.analyzer.service.logs.LogRecordFormat;
import org.analyzer.service.logs.LogsService;
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

import static org.analyzer.service.users.notifications.telegram.commands.IndexLogsFileCommand.COMMAND_NAME;

@Component(COMMAND_NAME)
public class IndexLogsFileCommand extends BaseUploadingFileBotCommand implements TelegramCommandConversationChain {

    static final String COMMAND_NAME = "index_logs_file";

    private static final String RECORD_PATTERN_STAGE = "logRecordPattern";
    private static final String RECORD_PATTERN_DATE_FORMAT_STAGE = "logRecordDateFormat";
    private static final String RECORD_PATTERN_TIME_FORMAT_STAGE = "logRecordTimeFormat";
    private static final String UPLOADING_FILE_STAGE = "uploadingFile";

    @Autowired
    private TelegramUserConversationStore userConversationStore;
    @Autowired
    private LogsService logsService;

    public IndexLogsFileCommand() {
        super(COMMAND_NAME, "Upload file and index logs from this file");
    }

    @Override
    protected Optional<SendMessage> executeCommand(AbsSender absSender, User user, Chat chat, UserEntity userContext, String[] arguments) {

        final var msg = createReplyMessage(chat.getId(), "Enter expected log records pattern (or %s to use default patterns set):".formatted(SKIP_STAGE_STR_FORMATTED));

        final var context = this.userConversationStore.createUserCommandContext(user.getId(), COMMAND_NAME);
        context.setLastStage(RECORD_PATTERN_STAGE);

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

        final var nextMsgText = switch (context.getLastStage()) {
            case RECORD_PATTERN_STAGE -> {
                context.put(context.getLastStage(), message.getText());
                context.setLastStage(RECORD_PATTERN_DATE_FORMAT_STAGE);
                yield "Enter expected record's date format (or %s to use default date format or if records without date part):".formatted(SKIP_STAGE_STR_FORMATTED);
            }
            case RECORD_PATTERN_DATE_FORMAT_STAGE -> {
                context.put(context.getLastStage(), message.getText());
                context.setLastStage(RECORD_PATTERN_TIME_FORMAT_STAGE);
                yield "Enter expected record's time format (or %s to use default time format):".formatted(SKIP_STAGE_STR_FORMATTED);
            }
            case RECORD_PATTERN_TIME_FORMAT_STAGE -> {
                context.put(context.getLastStage(), message.getText());
                context.setLastStage(UPLOADING_FILE_STAGE);
                yield "Send logs file to me.";
            }
            case UPLOADING_FILE_STAGE -> {
                this.userConversationStore.clearUserCommandContext(userId);
                yield handleFileUploadingStage(absSender, context, message);
            }
            default -> "<b>Unsupported stage of command</b>";
        };

        return Optional.of(createReplyMessage(message.getChatId(), nextMsgText));
    }

    private String handleFileUploadingStage(
            final AbsSender absSender,
            final TelegramUserConversationStore.CommandContext context,
            final Message message) {

        if (message.getDocument() == null || StringUtils.isEmpty(message.getDocument().getFileId())) {
            return "<b>Expected file for indexing command.<b>";
        }

        final var uploadedFile = downloadFile(absSender, message.getDocument().getFileId());
        final var logRecordFormat = createLogRecordFormatFromContext(context);
        this.logsService.index(uploadedFile, logRecordFormat)
                .whenComplete((indexingKey, ex) -> {
                    uploadedFile.delete();

                    final String resultMsgText;
                    if (ex != null) {
                        resultMsgText = "Indexing process completed with error: " + ex.getMessage();
                    } else {
                        resultMsgText = "Indexing process completed successfully, indexing key: " + indexingKey;
                    }

                    final var resultMsg = createReplyMessage(message.getChatId(), resultMsgText);
                    resultMsg.setReplyToMessageId(message.getMessageId());
                    sendMessage(absSender, resultMsg);
                });

        return "Indexing process starts, when it completes you will be notified.";
    }

    private LogRecordFormat createLogRecordFormatFromContext(final TelegramUserConversationStore.CommandContext context) {

        final var passedPattern = context.getAttributeAsString(RECORD_PATTERN_STAGE);
        final var passedDateFormat = context.getAttributeAsString(RECORD_PATTERN_DATE_FORMAT_STAGE);
        final var passedTimeFormat = context.getAttributeAsString(RECORD_PATTERN_TIME_FORMAT_STAGE);

        final var resultPattern = SKIP_STAGE_STR.equals(passedPattern) ? null : passedPattern;
        final var resultDateFormat = SKIP_STAGE_STR.equals(passedDateFormat) ? null : passedDateFormat;
        final var resultTimeFormat = SKIP_STAGE_STR.equals(passedTimeFormat) ? null : passedTimeFormat;

        if (resultPattern == null && resultDateFormat == null && resultTimeFormat == null) {
            return null;
        }

        return new LogRecordFormat() {
            @Override
            public String pattern() {
                return resultPattern;
            }

            @Override
            public String timeFormat() {
                return resultTimeFormat;
            }

            @Override
            public String dateFormat() {
                return resultDateFormat;
            }
        };
    }
}
