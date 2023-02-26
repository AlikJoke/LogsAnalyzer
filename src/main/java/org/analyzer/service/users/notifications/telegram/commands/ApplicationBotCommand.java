package org.analyzer.service.users.notifications.telegram.commands;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.analyzer.entities.UserEntity;
import org.analyzer.service.users.CurrentUserAccessor;
import org.analyzer.service.users.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.annotation.Nullable;
import java.util.Optional;

@Slf4j
abstract class ApplicationBotCommand extends BotCommand {

    static final String SKIP_STAGE_STR = "-";
    static final String SKIP_STAGE_STR_FORMATTED = "<i>" + SKIP_STAGE_STR + "</i>";

    @Autowired
    protected UserService userService;
    @Autowired
    private CurrentUserAccessor userAccessor;

    private final boolean userContextRequired;

    public ApplicationBotCommand(
            @NonNull String commandIdentifier,
            @Nullable String description,
            boolean userContextRequired) {
        super(commandIdentifier, description);
        this.userContextRequired = userContextRequired;
    }

    @Override
    public final void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {

        final var userEntity = this.userService.findByTelegramId(user.getId());
        if (!this.userContextRequired) {
            executeCommand(absSender, user, chat, userEntity.orElse(null), arguments);
            return;
        }

        if (userEntity.isEmpty()) {
            final var message = createReplyMessage(chat.getId(), "<b>This command requires user authentication. Use /" + RegisterUserCommand.COMMAND_NAME + " command.</b>");
            sendMessage(absSender, message);
            return;
        }

        try (final var userContext = this.userAccessor.as(userEntity.get())) {
            executeCommand(absSender, user, chat, userEntity.get(), arguments)
                    .ifPresent(msg -> sendMessage(absSender, msg));
        }
    }

    protected abstract Optional<SendMessage> executeCommand(AbsSender absSender, User user, Chat chat, UserEntity userContext, String[] arguments);

    protected void sendMessage(final AbsSender sender, final SendMessage message) {
        try {
            sender.execute(message);
        } catch (TelegramApiException e) {
            log.error("", e);
            throw new RuntimeException(e);
        }
    }

    protected SendMessage createReplyMessage(final Long chatId, final String text) {
        final var msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText(text);
        msg.enableHtml(true);

        return msg;
    }
}
