package org.analyzer.service.users.notifications.telegram.std;

import lombok.*;
import org.analyzer.service.users.notifications.telegram.TelegramUserConversationStore;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TelegramUserConversationInMemoryStore implements TelegramUserConversationStore {

    private final Map<Long, CommandContext> contextByUser = new ConcurrentHashMap<>();

    @Nullable
    @Override
    public CommandContext getLastCommandContext(@NonNull Long userId) {
        return contextByUser.get(userId);
    }

    @Override
    public void updateLastCommandContext(@NonNull Long userId, @NonNull TelegramUserConversationStore.CommandContext context) {
        this.contextByUser.put(userId, context);
    }

    @Override
    public void clearUserCommandContext(@NonNull Long userId) {
        this.contextByUser.remove(userId);
    }

    @NonNull
    @Override
    public CommandContext createUserCommandContext(@NonNull Long userId, @NonNull String command) {
        return new SimpleCommandContext(command);
    }

    @EqualsAndHashCode(callSuper = false)
    @Getter
    @Setter
    @RequiredArgsConstructor
    private static class SimpleCommandContext extends ConcurrentHashMap<String, Object> implements CommandContext {

        private final String commandName;
        private String lastStage;

        @Override
        public String getAttributeAsString(@NonNull String key) {
            return (String) get(key);
        }
    }
}
