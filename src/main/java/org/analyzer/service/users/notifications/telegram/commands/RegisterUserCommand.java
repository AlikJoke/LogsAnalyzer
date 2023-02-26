package org.analyzer.service.users.notifications.telegram.commands;

import lombok.NonNull;
import org.analyzer.entities.NotificationSettings;
import org.analyzer.entities.UserEntity;
import org.analyzer.entities.UserSettings;
import org.analyzer.service.exceptions.UserAlreadyExistsException;
import org.analyzer.service.users.notifications.telegram.TelegramCommandConversationChain;
import org.analyzer.service.users.notifications.telegram.TelegramUserConversationStore;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.analyzer.service.users.notifications.telegram.commands.RegisterUserCommand.COMMAND_NAME;

@Component(COMMAND_NAME)
public class RegisterUserCommand extends ApplicationBotCommand implements TelegramCommandConversationChain {

    static final String COMMAND_NAME = "register_user_account";

    static final String USERNAME_STAGE = "username";
    static final String PASSWORD_STAGE = "password";
    static final String EMAIL_STAGE = "e-mail";

    @Autowired
    private TelegramUserConversationStore userConversationStore;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public RegisterUserCommand() {
        super(COMMAND_NAME, "Registration of user account in application", false);
    }

    @Override
    protected Optional<SendMessage> executeCommand(AbsSender absSender, User user, Chat chat, UserEntity userContext, String[] arguments) {

        if (userContext != null) {
            final var replyMsg = createReplyMessage(chat.getId(), "Account in application already exists for this telegram account. Username: " + userContext.getUsername());
            return Optional.of(replyMsg);
        }

        final var context = this.userConversationStore.createUserCommandContext(user.getId(), COMMAND_NAME);
        context.setLastStage(USERNAME_STAGE);

        this.userConversationStore.updateLastCommandContext(user.getId(), context);

        return Optional.of(createReplyMessage(chat.getId(), "Pass your application username:"));
    }

    @Override
    public Optional<SendMessage> onMessageReceived(
            @NonNull AbsSender sender,
            @NonNull Long userId,
            @NonNull Message message) {
        final var context = this.userConversationStore.getLastCommandContext(userId);
        if (context == null) {
            throw new IllegalStateException("Illegal state of conversation");
        }

        if (message.getText() == null || StringUtils.isWhitespace(message.getText())) {
            return Optional.of(createReplyMessage(message.getChatId(), "<b>Passed text must be not empty.<b>"));
        }

        context.put(context.getLastStage(), message.getText());

        final var nextMsg = switch (context.getLastStage()) {
            case USERNAME_STAGE -> {
                context.setLastStage(PASSWORD_STAGE);
                yield "Pass your password:";
            }
            case PASSWORD_STAGE -> {
                context.setLastStage(EMAIL_STAGE);
                yield "Pass your e-mail (or %s to register without e-mail):".formatted(SKIP_STAGE_STR_FORMATTED);
            }
            case EMAIL_STAGE -> {
                final var email = SKIP_STAGE_STR.equals(context.getAttributeAsString(EMAIL_STAGE)) ? null : context.getAttributeAsString("email");
                final var notificationsSettings = NotificationSettings.builder()
                                                                        .notifyToEmail(email)
                                                                        .notifyToTelegram(userId)
                                                                      .build();
                final var userSettings = UserSettings.builder()
                                                        .notificationSettings(notificationsSettings)
                                                     .build();
                final var password = this.passwordEncoder.encode(context.getAttributeAsString(PASSWORD_STAGE));
                final var user = new UserEntity()
                                        .setUsername(context.getAttributeAsString(USERNAME_STAGE))
                                        .setEncodedPassword(password)
                                        .setHash(UUID.randomUUID().toString())
                                        .setModified(LocalDateTime.now())
                                        .setActive(true)
                                        .setSettings(userSettings);

                this.userConversationStore.clearUserCommandContext(userId);
                try {
                    this.userService.create(user);
                } catch (UserAlreadyExistsException ex) {
                    yield "<b>Account already exists with login " + user.getUsername() + ". Try another username.</b>";
                }

                yield "<b>Account successfully created. Use /help command or menu to work.</b>";
            }

            default -> "<b>Unsupported stage of command</b>";
        };

        return Optional.of(createReplyMessage(message.getChatId(), nextMsg));
    }
}
