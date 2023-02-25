package org.analyzer.logs.service.telegram;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import javax.annotation.Nonnull;
import java.util.Optional;

public interface TelegramCommandConversationChain {

    Optional<SendMessage> onMessageReceived(@Nonnull Long userId, @Nonnull Message message);
}
