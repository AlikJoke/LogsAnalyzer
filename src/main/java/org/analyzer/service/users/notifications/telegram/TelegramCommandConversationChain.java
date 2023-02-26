package org.analyzer.service.users.notifications.telegram;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;

import javax.annotation.Nonnull;
import java.util.Optional;

public interface TelegramCommandConversationChain {

    Optional<SendMessage> onMessageReceived(
            @Nonnull AbsSender absSender,
            @Nonnull Long userId,
            @Nonnull Message message);
}
