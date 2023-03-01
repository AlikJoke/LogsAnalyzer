package org.analyzer.service.users.notifications.telegram;

import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;

import javax.annotation.Nonnull;
import java.util.Optional;

public interface TelegramCommandConversationChain {

    Optional<PartialBotApiMethod<?>> onMessageReceived(
            @Nonnull AbsSender absSender,
            @Nonnull Long userId,
            @Nonnull Message message);
}
