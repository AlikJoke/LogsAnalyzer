package org.analyzer.logs.service.telegram;

import org.analyzer.logs.service.TelegramUserConversationStore;
import org.analyzer.logs.service.UserService;
import org.analyzer.logs.service.exceptions.UserAlreadyDisabledException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static org.analyzer.logs.service.telegram.DisableUserCommand.COMMAND_NAME;

@Component(COMMAND_NAME)
public class DisableUserCommand extends BotCommand {

    static final String COMMAND_NAME = "disable_user_account";

    @Autowired
    private TelegramUserConversationStore userConversationStore;
    @Autowired
    private UserService userService;

    public DisableUserCommand() {
        super(COMMAND_NAME, "Disabling of user account in application");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {

        final var msg = new SendMessage();
        msg.setChatId(chat.getId());
        msg.enableHtml(true);

        try {
            this.userService.findByTelegramId(user.getId())
                    .ifPresentOrElse(
                            userEntity -> {
                                this.userService.disable(userEntity.getUsername());
                                msg.setText("<b>Account disabled successfully.</b>");
                            },
                            () -> msg.setText("<b>Account not found to deactivation.</b>")
                    );
        } catch (UserAlreadyDisabledException ex) {
            msg.setText("<b>User already disabled.</b>");
        }

        this.userConversationStore.clearUserCommandContext(user.getId());

        try {
            absSender.execute(msg);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
