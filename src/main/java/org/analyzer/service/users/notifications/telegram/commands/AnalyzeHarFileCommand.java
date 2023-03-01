package org.analyzer.service.users.notifications.telegram.commands;

import lombok.NonNull;
import org.analyzer.entities.UserEntity;
import org.analyzer.service.exceptions.EntityNotFoundException;
import org.analyzer.service.har.HttpArchiveOperationsQuery;
import org.analyzer.service.har.HttpArchiveService;
import org.analyzer.service.logs.SearchQuery;
import org.analyzer.service.users.notifications.telegram.TelegramCommandConversationChain;
import org.analyzer.service.users.notifications.telegram.TelegramUserConversationStore;
import org.analyzer.service.util.JsonConverter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.analyzer.service.users.notifications.telegram.commands.AnalyzeHarFileCommand.COMMAND_NAME;

@Component(COMMAND_NAME)
public class AnalyzeHarFileCommand extends BaseUploadingFileBotCommand implements TelegramCommandConversationChain {

    static final String COMMAND_NAME = "analyze_har";

    private static final String FILTERS_STAGE = "filteringKeys";
    private static final String SPECIFY_HAR_STAGE = "specifyHAR";

    @Autowired
    private TelegramUserConversationStore userConversationStore;
    @Autowired
    private HttpArchiveService httpArchiveService;
    @Autowired
    private JsonConverter jsonConverter;

    public AnalyzeHarFileCommand() {
        super(COMMAND_NAME, "Analyze HAR");
    }

    @Override
    protected Optional<SendMessage> executeCommand(AbsSender absSender, User user, Chat chat, UserEntity userContext, String[] arguments) {

        final var msg = createReplyMessage(chat.getId(), ("Enter keys to additional filter requests from HAR " +
                "(to stop entering the filter, enter a %s):").formatted(SKIP_STAGE_STR_FORMATTED));

        final var context = this.userConversationStore.createUserCommandContext(user.getId(), COMMAND_NAME);
        context.setLastStage(FILTERS_STAGE);

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

        return switch (context.getLastStage()) {
            case FILTERS_STAGE -> {
                @SuppressWarnings("unchecked")
                final Set<String> filteringKeys = (Set<String>) context.getOrDefault(context.getLastStage(), new HashSet<>());
                if (!SKIP_STAGE_STR.equals(message.getText())) {
                    filteringKeys.add(message.getText());
                    context.put(context.getLastStage(), filteringKeys);
                    yield Optional.empty();
                }

                context.setLastStage(SPECIFY_HAR_STAGE);
                final var msgText = "Upload HAR file or enter already uploaded HAR key:";
                yield Optional.of(createReplyMessage(message.getChatId(), msgText));
            }
            case SPECIFY_HAR_STAGE -> {
                this.userConversationStore.clearUserCommandContext(userId);
                final var replyMsg = handleHarAnalyzingStage(absSender, context, message);
                replyMsg.setReplyToMessageId(message.getMessageId());

                yield Optional.of(replyMsg);
            }
            default -> Optional.of(createReplyMessage(message.getChatId(), "<b>Unsupported stage of command</b>"));
        };
    }

    private SendMessage handleHarAnalyzingStage(
            final AbsSender absSender,
            final TelegramUserConversationStore.CommandContext context,
            final Message message) {

        final var wasUploadFile = message.getDocument() == null || StringUtils.isEmpty(message.getDocument().getFileId());
        if (StringUtils.isEmpty(message.getText()) && !wasUploadFile) {
            return createReplyMessage(message.getChatId(), "<b>Expected file or already uploaded HAR key for HAR analyzing command.<b>");
        }

        String resultText;
        final var operationsQuery = createOperationsQueryFromContext(context);
        try {
            final var result =
                    wasUploadFile
                            ? processUploadedFile(absSender, message, operationsQuery)
                            : this.httpArchiveService.analyze(message.getText(), operationsQuery);

            resultText = "Result of analyzing:\n<code>" + this.jsonConverter.convertToJson(result) + "</code>";
        } catch (EntityNotFoundException ex) {
            resultText = "Analyzing failed with error:\n<code>" + ex.getMessage() + "</code>";
        }

        return createReplyMessage(message.getChatId(), resultText);
    }

    private Map<String, Object> processUploadedFile(
            final AbsSender absSender,
            final Message message,
            final HttpArchiveOperationsQuery operationsQuery) {
        final var uploadedFile = downloadFile(absSender, message.getDocument().getFileId());
        try {
            return this.httpArchiveService.analyze(uploadedFile, operationsQuery);
        } finally {
            uploadedFile.delete();
        }
    }

    private HttpArchiveOperationsQuery createOperationsQueryFromContext(final TelegramUserConversationStore.CommandContext context) {

        @SuppressWarnings("unchecked")
        final Set<String> filteringKeys = (Set<String>) context.get(FILTERS_STAGE);
        if (filteringKeys == null || filteringKeys.isEmpty()) {
            return null;
        }

        return new HttpArchiveOperationsQuery() {
            @Override
            public boolean applyDefaultSorting() {
                return false;
            }

            @NonNull
            @Override
            public Map<String, Sort.Direction> sort() {
                return Map.of();
            }

            @NonNull
            @Override
            public Set<String> filteringKeys() {
                return filteringKeys;
            }

            @Nullable
            @Override
            public SearchQuery additionalLogsSearchQuery() {
                return null;
            }
        };
    }
}