package org.analyzer.service.users.notifications.telegram.commands;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import org.analyzer.entities.UserEntity;
import org.analyzer.service.users.notifications.telegram.TelegramCommandConversationChain;
import org.analyzer.service.users.notifications.telegram.TelegramUserConversationStore;
import org.analyzer.service.util.JsonConverter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

abstract class LogsByQueryCommand extends ApplicationBotCommand implements TelegramCommandConversationChain {

    static final String QUERY_STAGE = "query";
    static final String QUERY_FORMAT_STAGE = "queryFormat";
    static final String TERMINAL_STAGE = "terminal";
    static final String SORTS_STAGE = "sorts";
    static final String POST_FILTERS_STAGE = "postFilters";

    @Autowired
    protected TelegramUserConversationStore userConversationStore;
    @Autowired
    protected JsonConverter jsonConverter;

    protected LogsByQueryCommand(@NonNull String commandIdentifier, @Nullable String description) {
        super(commandIdentifier, description, true);
    }

    @Override
    protected Optional<SendMessage> executeCommand(AbsSender absSender, User user, Chat chat, UserEntity userContext, String[] arguments) {

        final var context = this.userConversationStore.createUserCommandContext(user.getId(), getCommandIdentifier());
        context.setLastStage(QUERY_FORMAT_STAGE);

        this.userConversationStore.updateLastCommandContext(user.getId(), context);

        return Optional.of(createReplyMessage(chat.getId(), "Enter query type (simple or extended):"));
    }

    @Override
    public Optional<PartialBotApiMethod<?>> onMessageReceived(
            @NonNull AbsSender sender,
            @NonNull Long userId,
            @NonNull Message message) {
        final var context = this.userConversationStore.getLastCommandContext(userId);
        if (context == null) {
            throw new IllegalStateException("Illegal state of conversation");
        }

        if (message.getText() == null || StringUtils.isWhitespace(message.getText())) {
            return Optional.of(createReplyMessage(message.getChatId(), "<b>Entered text must be not empty.<b>"));
        }

        final PartialBotApiMethod<?> result = switch (context.getLastStage()) {
            case QUERY_FORMAT_STAGE -> {
                context.put(context.getLastStage(), message.getText());
                context.setLastStage(QUERY_STAGE);
                final var nextMsg = "Enter raw query:";
                yield createReplyMessage(message.getChatId(), nextMsg);
            }
            case QUERY_STAGE -> {
                context.put(context.getLastStage(), message.getText());
                context.setLastStage(SORTS_STAGE);
                final var msgText = ("Enter sorts for logs in format \"sorting field\":\"sorting direction\" " +
                        "(to stop entering the sorts, enter a %s):").formatted(SKIP_STAGE_STR_FORMATTED);

                final var nextMsg = ("Enter sorts of logs (to stop entering the sorts, enter a %s):").formatted(SKIP_STAGE_STR_FORMATTED);
                yield createReplyMessage(message.getChatId(), nextMsg);
            }
            case SORTS_STAGE -> {
                @SuppressWarnings("unchecked")
                final Set<String> sortKeys = (Set<String>) context.getOrDefault(context.getLastStage(), new HashSet<>());
                if (!SKIP_STAGE_STR.equals(message.getText())) {
                    sortKeys.add(message.getText());
                    context.put(context.getLastStage(), sortKeys);
                    yield null;
                }

                context.put(context.getLastStage(), parseSorts(sortKeys));
                context.setLastStage(POST_FILTERS_STAGE);

                final var nextMsg = ("Enter post filters for logs in format \"post filter id\":\"post filter settings json\" (to stop entering the post filters, enter a %s):").formatted(SKIP_STAGE_STR_FORMATTED);
                yield createReplyMessage(message.getChatId(), nextMsg);
            }
            case TERMINAL_STAGE -> {
                this.userConversationStore.clearUserCommandContext(userId);
                yield processTerminalStage(message, context);
            }
            case POST_FILTERS_STAGE -> {
                @SuppressWarnings("unchecked")
                final Set<String> postFilters = (Set<String>) context.getOrDefault(context.getLastStage(), new HashSet<>());
                if (!SKIP_STAGE_STR.equals(message.getText())) {
                    postFilters.add(message.getText());
                    context.put(context.getLastStage(), postFilters);
                    yield null;
                }

                context.put(context.getLastStage(), parseSettingsMap(postFilters));
                yield processNonTerminalStage(message, context);
            }
            default -> processNonTerminalStage(message, context);
        };

        return Optional.ofNullable(result);
    }

    protected abstract PartialBotApiMethod<?> processNonTerminalStage(
            @NonNull Message userMessage,
            @NonNull TelegramUserConversationStore.CommandContext context
    );

    protected abstract PartialBotApiMethod<?> processTerminalStage(
            @NonNull Message userMessage,
            @NonNull TelegramUserConversationStore.CommandContext context
    );

    protected Map<String, JsonNode> parseSettingsMap(final Set<String> settings) {

        return settings
                .stream()
                .map(s -> s.split(":"))
                .collect(
                        Collectors.toMap(
                                s -> s[0],
                                s -> convertStringJsonPartsToNode(Arrays.copyOfRange(s, 1, s.length))
                        )
                );
    }

    private Map<String, Sort.Direction> parseSorts(final Set<String> sorts) {

        final Map<String, Sort.Direction> result = new HashMap<>(sorts.size(), 1);
        sorts.forEach(sort -> {
            final var sortingParts = sort.split(":");
            final Map<String, Sort.Direction> targetSort;
            if (sortingParts.length == 2) {
                Sort.Direction.fromOptionalString(sortingParts[1].toUpperCase())
                        .ifPresent(value -> result.put(sortingParts[0], value));
            }
        });

        return result;
    }

    private JsonNode convertStringJsonPartsToNode(final String[] parts) {
        final String json = Arrays.toString(parts);
        return this.jsonConverter.convert(json);
    }
}
