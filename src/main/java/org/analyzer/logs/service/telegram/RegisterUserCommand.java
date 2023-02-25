package org.analyzer.logs.service.telegram;

import lombok.NonNull;
import org.analyzer.logs.model.NotificationSettings;
import org.analyzer.logs.model.UserEntity;
import org.analyzer.logs.model.UserSettings;
import org.analyzer.logs.service.TelegramUserConversationStore;
import org.analyzer.logs.service.UserService;
import org.analyzer.logs.service.exceptions.UserAlreadyExistsException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.analyzer.logs.service.telegram.RegisterUserCommand.COMMAND_NAME;

@Component(COMMAND_NAME)
public class RegisterUserCommand extends BotCommand implements TelegramCommandConversationChain {

    static final String COMMAND_NAME = "register_user_account";

    @Autowired
    private TelegramUserConversationStore userConversationStore;
    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public RegisterUserCommand() {
        super(COMMAND_NAME, "Registration of user account in application");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {

        final var context = this.userConversationStore.createUserCommandContext(user.getId(), COMMAND_NAME);
        context.setLastStage("username");

        this.userConversationStore.updateLastCommandContext(user.getId(), context);

        try {
            absSender.execute(createReplyMessage(chat.getId(), "Enter your application username:"));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<SendMessage> onMessageReceived(@NonNull Long userId, @NonNull Message message) {
        final TelegramUserConversationStore.CommandContext context = this.userConversationStore.getLastCommandContext(userId);
        if (context == null) {
            throw new IllegalStateException("Illegal state of conversation");
        }

        if (message.getText() == null || StringUtils.isWhitespace(message.getText())) {
            return Optional.of(createReplyMessage(message.getChatId(), "<b>Entered text must be not empty.<b>"));
        }

        context.put(context.getLastStage(), message.getText());

        final var nextMsg = switch (context.getLastStage()) {
            case "username" -> {
                context.setLastStage("password");
                yield "Enter your password:";
            }
            case "password" -> {
                context.setLastStage("email");
                yield "Enter your e-mail (optional) or \"-\":";
            }
            case "email" -> {
                final var email = "-".equals(context.getAttributeAsString("email")) ? null : context.getAttributeAsString("email");
                final var notificationsSettings = NotificationSettings.builder()
                                                                        .notifyToEmail(email)
                                                                        .notifyToTelegram(userId)
                                                                      .build();
                final var userSettings = UserSettings.builder()
                                                        .notificationSettings(notificationsSettings)
                                                     .build();
                final var password = this.passwordEncoder.encode(context.getAttributeAsString("password"));
                final var user = new UserEntity()
                                        .setUsername(context.getAttributeAsString("username"))
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

    private SendMessage createReplyMessage(final Long chatId, final String text) {
        final var msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText(text);
        msg.enableHtml(true);

        return msg;
    }
}
