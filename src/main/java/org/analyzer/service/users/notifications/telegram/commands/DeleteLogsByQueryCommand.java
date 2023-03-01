package org.analyzer.service.users.notifications.telegram.commands;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import org.analyzer.rest.records.RequestSearchQuery;
import org.analyzer.service.logs.LogsService;
import org.analyzer.service.users.notifications.telegram.TelegramCommandConversationChain;
import org.analyzer.service.users.notifications.telegram.TelegramUserConversationStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Map;

import static org.analyzer.service.users.notifications.telegram.commands.DeleteLogsByQueryCommand.COMMAND_NAME;

@Component(COMMAND_NAME)
public class DeleteLogsByQueryCommand extends LogsByQueryCommand implements TelegramCommandConversationChain {

    static final String COMMAND_NAME = "delete_logs_by_query";

    @Autowired
    private LogsService logsService;

    public DeleteLogsByQueryCommand() {
        super(COMMAND_NAME, "Delete log records by query");
    }

    @Override
    protected PartialBotApiMethod<?> processNonTerminalStage(
            @NonNull Message userMessage,
            @NonNull TelegramUserConversationStore.CommandContext context) {
        if (POST_FILTERS_STAGE.equals(context.getLastStage())) {
            final var nextMsg = "Confirm deletion of logs (enter %s to cancel or any another string to confirm):".formatted(SKIP_STAGE_STR_FORMATTED);
            return createReplyMessage(userMessage.getChatId(), nextMsg);
        }

        return createReplyMessage(userMessage.getChatId(), "<b>Unsupported stage of command.</b>");
    }

    @Override
    protected PartialBotApiMethod<?> processTerminalStage(
            @NonNull Message userMessage,
            @NonNull TelegramUserConversationStore.CommandContext context) {

        final var confirmation = context.getAttributeAsString(TERMINAL_STAGE);
        if (SKIP_STAGE_STR.equals(confirmation)) {
            return createReplyMessage(userMessage.getChatId(), "Deletion cancelled.");
        }

        final var query = context.getAttributeAsString(QUERY_STAGE);
        final var extendedFormat = "extended".equalsIgnoreCase(context.getAttributeAsString(QUERY_FORMAT_STAGE));
        @SuppressWarnings("unchecked")
        final var postFiltersMap = (Map<String, JsonNode>) context.get(POST_FILTERS_STAGE);
        @SuppressWarnings("unchecked")
        final var sortsMap = (Map<String, Sort.Direction>) context.get(SORTS_STAGE);

        final var searchQuery = new RequestSearchQuery(query, extendedFormat, postFiltersMap, 0, 0, sortsMap, null);
        this.logsService.deleteByQuery(searchQuery);

        return createReplyMessage(userMessage.getChatId(), "<b>Logs by query deleted from storage.</b>");
    }
}
