package org.analyzer.logs.service.telegram;

import org.analyzer.logs.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class StartCommand extends BotCommand {

    @Autowired
    private UserService userService;

    public StartCommand() {
        super("start", "With this command you can start the Bot");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {

        final var userEntity = this.userService.findByTelegramId(user.getId());

        final var msg = new SendMessage();
        msg.setChatId(chat.getId());

        final var sb = new StringBuilder("Welcome to LogsAnalyzer Bot. ");
        if (userEntity.isEmpty()) {
            sb.append("Please, register your account in application via /register_user_account command.");
        } else {
            sb.append("Use the bot menu or /help command to work.");
        }

        msg.setText(sb.toString());

        try {
            absSender.execute(msg);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
