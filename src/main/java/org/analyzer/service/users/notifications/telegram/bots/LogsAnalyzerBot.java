package org.analyzer.service.users.notifications.telegram.bots;

import lombok.NonNull;
import org.analyzer.config.telegram.TelegramBotConfiguration;
import org.analyzer.service.users.notifications.telegram.TelegramCommandConversationChain;
import org.analyzer.service.users.notifications.telegram.TelegramUserConversationStore;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.ExponentialBackOff;

public class LogsAnalyzerBot extends TelegramLongPollingCommandBot {

    private final TelegramUserConversationStore userConversationStore;
    private final TelegramBotConfiguration configuration;

    public LogsAnalyzerBot(
            @NonNull TelegramBotConfiguration configuration,
            @NonNull TelegramUserConversationStore userConversationStore) {
        super(createBotOptions(configuration));
        this.configuration = configuration;
        this.userConversationStore = userConversationStore;

        registerDefaultAction((absSender, message) -> {
            final var commandUnknownMessage = new SendMessage();
            commandUnknownMessage.setChatId(message.getChatId());
            commandUnknownMessage.setText("The command '" + message.getText() + "' is not known by this bot. Use /help command.");

            sendMessage(commandUnknownMessage);
        });
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        try {
            final var userId = update.getMessage().getChat().getId();

            final var commandContext = this.userConversationStore.getLastCommandContext(userId);

            if (commandContext == null) {
                sendMessage(SendMessage.builder()
                                        .chatId(update.getMessage().getChatId())
                                        .text("No any command context found")
                                   .build()
                );
                return;
            }

            final var command = getRegisteredCommand(commandContext.getCommandName());
            if (command instanceof TelegramCommandConversationChain chain) {
                chain.onMessageReceived(this, userId, update.getMessage())
                        .ifPresent(this::sendMessage);
            }
        } catch (Exception ex) {
            sendMessage(SendMessage.builder()
                                        .chatId(update.getMessage().getChatId())
                                        .text(ex.getMessage())
                                   .build()
            );
        }
    }

    @Override
    public String getBotUsername() {
        return this.configuration.getName();
    }

    @Override
    @SuppressWarnings("deprecation")
    public String getBotToken() {
        return this.configuration.getToken();
    }

    private static DefaultBotOptions createBotOptions(final TelegramBotConfiguration configuration) {
        final var options = new DefaultBotOptions();
        options.setMaxThreads(configuration.getMaxThreads());
        options.setBackOff(new ExponentialBackOff.Builder().build());
        options.setGetUpdatesLimit(configuration.getUpdatesLimit());
        return options;
    }

    private void sendMessage(final SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
