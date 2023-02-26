package org.analyzer.service.users.notifications.telegram.commands;

import org.analyzer.entities.UserEntity;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.Optional;

@Component
public class StartCommand extends ApplicationBotCommand {

    public StartCommand() {
        super("start", "With this command you can start the Bot", false);
    }

    @Override
    protected Optional<SendMessage> executeCommand(AbsSender absSender, User user, Chat chat, UserEntity userContext, String[] arguments) {

        final var sb = new StringBuilder("Welcome to LogsAnalyzer Bot. ");
        if (userContext == null) {
            sb.append("Please, register your account in application via /%s command.".formatted(RegisterUserCommand.COMMAND_NAME));
        } else {
            sb.append("Use the bot menu or /help command to work.");
        }

        return Optional.of(createReplyMessage(chat.getId(), sb.toString()));
    }
}
