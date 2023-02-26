package org.analyzer.service.users.notifications.telegram.commands;

import org.analyzer.entities.UserEntity;
import org.analyzer.service.exceptions.UserAlreadyDisabledException;
import org.analyzer.service.users.notifications.telegram.TelegramUserConversationStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.Optional;

import static org.analyzer.service.users.notifications.telegram.commands.DisableUserCommand.COMMAND_NAME;

@Component(COMMAND_NAME)
public class DisableUserCommand extends ApplicationBotCommand {

    static final String COMMAND_NAME = "disable_user_account";

    @Autowired
    private TelegramUserConversationStore userConversationStore;

    public DisableUserCommand() {
        super(COMMAND_NAME, "Disabling of user account in application", true);
    }

    @Override
    protected Optional<SendMessage> executeCommand(AbsSender absSender, User user, Chat chat, UserEntity userContext, String[] arguments) {

        String replyMessageText;
        try {
            this.userService.disable(userContext.getUsername());
            replyMessageText = "<b>Account disabled successfully.</b>";
        } catch (UserAlreadyDisabledException ex) {
            replyMessageText = "<b>User already disabled.</b>";
        }

        this.userConversationStore.clearUserCommandContext(user.getId());

        return Optional.of(createReplyMessage(chat.getId(), replyMessageText));
    }
}
