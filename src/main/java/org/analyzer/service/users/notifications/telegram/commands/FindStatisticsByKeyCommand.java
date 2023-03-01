package org.analyzer.service.users.notifications.telegram.commands;

import lombok.NonNull;
import org.analyzer.entities.LogsStatisticsEntity;
import org.analyzer.entities.UserEntity;
import org.analyzer.service.logs.LogsService;
import org.analyzer.service.users.notifications.telegram.TelegramCommandConversationChain;
import org.analyzer.service.users.notifications.telegram.TelegramUserConversationStore;
import org.analyzer.service.util.JsonConverter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.UUID;

import static org.analyzer.service.users.notifications.telegram.commands.FindStatisticsByKeyCommand.COMMAND_NAME;

@Component(COMMAND_NAME)
public class FindStatisticsByKeyCommand extends ApplicationBotCommand implements TelegramCommandConversationChain {

    static final String COMMAND_NAME = "find_stats_by_key";
    private static final String KEY_STAGE = "statsKey";

    @Autowired
    private TelegramUserConversationStore userConversationStore;
    @Autowired
    private LogsService logsService;
    @Autowired
    private JsonConverter jsonConverter;

    public FindStatisticsByKeyCommand() {
        super(COMMAND_NAME, "Find logs statistics by key", true);
    }

    @Override
    public Optional<PartialBotApiMethod<?>> onMessageReceived(@NonNull AbsSender absSender, @NonNull Long userId, @NonNull Message message) {

        final var context = this.userConversationStore.getLastCommandContext(userId);
        if (context == null) {
            throw new IllegalStateException("Illegal state of conversation");
        }

        if (message.getText() == null || StringUtils.isWhitespace(message.getText())) {
            return Optional.of(createReplyMessage(message.getChatId(), "<b>Entered text must be not empty.<b>"));
        }

        final var stats = this.logsService.findStatisticsByKey(message.getText()).orElse(null);
        return stats == null
                    ? Optional.of(createStatsNotFoundMessage(message))
                    : Optional.of(createStatsFileResultMessage(stats, message));

    }

    @Override
    protected Optional<SendMessage> executeCommand(AbsSender absSender, User user, Chat chat, UserEntity userContext, String[] arguments) {

        final var context = this.userConversationStore.createUserCommandContext(user.getId(), COMMAND_NAME);
        context.setLastStage(KEY_STAGE);

        this.userConversationStore.updateLastCommandContext(user.getId(), context);

        return Optional.of(createReplyMessage(chat.getId(), "Enter statistics key:"));
    }

    private SendMessage createStatsNotFoundMessage(final Message message) {
        final var result = createReplyMessage(message.getChatId(), "Statistics not found by key.");
        result.setReplyToMessageId(message.getMessageId());

        return result;
    }

    private PartialBotApiMethod<?> createStatsFileResultMessage(final LogsStatisticsEntity stats, final Message message) {

        try {
            final var json = this.jsonConverter.convertToJson(stats.getStats());
            final var tempDir = Files.createTempDirectory(UUID.randomUUID().toString());
            final var statsFile = tempDir.resolve(stats.getId() + ".txt");
            Files.writeString(statsFile, json, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
            statsFile.toFile().deleteOnExit();

            final var resultMessage = new SendDocument();
            resultMessage.setChatId(message.getChatId());
            resultMessage.setReplyToMessageId(message.getMessageId());
            resultMessage.setDocument(new InputFile(statsFile.toFile()));

            return resultMessage;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
