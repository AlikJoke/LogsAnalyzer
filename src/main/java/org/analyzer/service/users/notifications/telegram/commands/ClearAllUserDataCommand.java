package org.analyzer.service.users.notifications.telegram.commands;

import org.analyzer.entities.UserEntity;
import org.analyzer.service.users.UserDataStorageCleaner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.analyzer.service.users.notifications.telegram.commands.ClearAllUserDataCommand.COMMAND_NAME;

@Component(COMMAND_NAME)
public class ClearAllUserDataCommand extends ApplicationBotCommand {

    static final String COMMAND_NAME = "clear_all_data";

    @Autowired
    private UserDataStorageCleaner storageCleaner;

    public ClearAllUserDataCommand() {
        super(COMMAND_NAME, "Delete all user data (logs, queries, stats, http archives) from storage", true);
    }

    @Override
    protected Optional<SendMessage> executeCommand(AbsSender absSender, User user, Chat chat, UserEntity userContext, String[] arguments) {
        this.storageCleaner.clear(userContext, LocalDateTime.now());
        return Optional.of(createReplyMessage(chat.getId(), "<b>All user data deleted (except of settings).</b>"));
    }
}
