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

import static org.analyzer.service.users.notifications.telegram.commands.SearchLogsByQueryCommand.COMMAND_NAME;

@Component(COMMAND_NAME)
public class SearchLogsByQueryCommand extends LogsByQueryCommand implements TelegramCommandConversationChain {

    static final String COMMAND_NAME = "search_logs_by_query";

    @Autowired
    private LogsService logsService;

    public SearchLogsByQueryCommand() {
        super(COMMAND_NAME, "Search log records by query");
    }

    @Override
    protected PartialBotApiMethod<?> processNonTerminalStage(
            @NonNull Message userMessage,
            @NonNull TelegramUserConversationStore.CommandContext context) {
        if (POST_FILTERS_STAGE.equals(context.getLastStage())) {
            final var nextMsg = "Enter file name to export command results:";
            return createReplyMessage(userMessage.getChatId(), nextMsg);
        }

        return createReplyMessage(userMessage.getChatId(), "<b>Unsupported stage of command</b>");
    }

    @Override
    protected PartialBotApiMethod<?> processTerminalStage(
            @NonNull Message userMessage,
            @NonNull TelegramUserConversationStore.CommandContext context) {

        final var query = context.getAttributeAsString(QUERY_STAGE);
        final var extendedFormat = "extended".equalsIgnoreCase(context.getAttributeAsString(QUERY_FORMAT_STAGE));
        final var filename = userMessage.getText();
        @SuppressWarnings("unchecked")
        final var postFiltersMap = (Map<String, JsonNode>) context.get(POST_FILTERS_STAGE);
        @SuppressWarnings("unchecked")
        final var sortsMap = (Map<String, Sort.Direction>) context.get(SORTS_STAGE);

        final var searchQuery = new RequestSearchQuery(query, extendedFormat, postFiltersMap, 0, 0, sortsMap, filename);

        final var resultFile = this.logsService.searchAndExportByQuery(searchQuery);
        final var resultMessage = new SendDocument();
        resultMessage.setChatId(userMessage.getChatId());
        resultMessage.setReplyToMessageId(userMessage.getMessageId());
        resultMessage.setDocument(new InputFile(resultFile, searchQuery.exportToFile()));

        return resultMessage;
    }
}