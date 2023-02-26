package org.analyzer.service.users.notifications.telegram.commands;

import org.analyzer.entities.UserEntity;
import org.analyzer.service.exceptions.UserNotDisabledException;
import org.analyzer.service.users.notifications.telegram.TelegramUserConversationStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.Optional;

import static org.analyzer.service.users.notifications.telegram.commands.EnableUserCommand.COMMAND_NAME;

@Component(COMMAND_NAME)
public class EnableUserCommand extends ApplicationBotCommand {

    static final String COMMAND_NAME = "enable_user_account";

    @Autowired
    private TelegramUserConversationStore userConversationStore;

    public EnableUserCommand() {
        super(COMMAND_NAME, "Enabling of user account in application", true);
    }

    @Override
    protected Optional<SendMessage> executeCommand(AbsSender absSender, User user, Chat chat, UserEntity userContext, String[] arguments) {

        String replyMessageText;
        try {
            this.userService.enable(userContext.getUsername());
            replyMessageText = "<b>Account enabled successfully.</b>";
        } catch (UserNotDisabledException ex) {
            replyMessageText = "<b>User already enabled.</b>";
        }

        this.userConversationStore.clearUserCommandContext(user.getId());
        return Optional.of(createReplyMessage(chat.getId(), replyMessageText));
    }
}