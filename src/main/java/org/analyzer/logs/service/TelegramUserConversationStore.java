package org.analyzer.logs.service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public interface TelegramUserConversationStore {

    @Nullable
    CommandContext getLastCommandContext(@Nonnull Long userId);

    void updateLastCommandContext(@Nonnull Long userId, @Nonnull CommandContext context);

    void clearUserCommandContext(@Nonnull Long userId);

    @Nonnull
    CommandContext createUserCommandContext(@Nonnull Long userId, @Nonnull String command);

    interface CommandContext extends Map<String, Object> {

        @Nonnull
        String getCommandName();

        @Nonnull
        String getLastStage();

        void setLastStage(@Nonnull String stage);

        String getAttributeAsString(@Nonnull String key);
    }
}
