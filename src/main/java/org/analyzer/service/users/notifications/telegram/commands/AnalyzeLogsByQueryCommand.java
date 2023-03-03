package org.analyzer.service.users.notifications.telegram.commands;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import org.analyzer.rest.stats.RequestAnalyzeQuery;
import org.analyzer.service.logs.LogsService;
import org.analyzer.service.logs.MapLogsStatistics;
import org.analyzer.service.users.notifications.telegram.TelegramCommandConversationChain;
import org.analyzer.service.users.notifications.telegram.TelegramUserConversationStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.analyzer.service.users.notifications.telegram.commands.AnalyzeLogsByQueryCommand.COMMAND_NAME;

@Component(COMMAND_NAME)
public class AnalyzeLogsByQueryCommand extends LogsByQueryCommand implements TelegramCommandConversationChain {

    static final String COMMAND_NAME = "analyze_logs_by_query";

    static final String AGGREGATIONS_STAGE = "aggregations";

    @Autowired
    private LogsService logsService;

    public AnalyzeLogsByQueryCommand() {
        super(COMMAND_NAME, "Analyze log records by query");
    }

    @Override
    protected PartialBotApiMethod<?> processNonTerminalStage(
            @NonNull Message userMessage,
            @NonNull TelegramUserConversationStore.CommandContext context) {
        if (POST_FILTERS_STAGE.equals(context.getLastStage())) {
            final var nextMsg = ("Enter aggregations for logs in format <aggregation id>:<aggregation settings json> (to stop entering the aggregations, enter a %s):").formatted(SKIP_STAGE_STR_FORMATTED);
            context.setLastStage(POST_FILTERS_STAGE);
            return createReplyMessage(userMessage.getChatId(), nextMsg);
        } else if (AGGREGATIONS_STAGE.equalsIgnoreCase(context.getLastStage())) {
            @SuppressWarnings("unchecked")
            final Set<String> aggregations = (Set<String>) context.getOrDefault(context.getLastStage(), new HashSet<>());
            if (!SKIP_STAGE_STR.equals(userMessage.getText())) {
                aggregations.add(userMessage.getText());
                context.put(context.getLastStage(), aggregations);
                return null;
            }

            context.put(context.getLastStage(), parseSettingsMap(aggregations));
            context.setLastStage(TERMINAL_STAGE);

            final var nextMsg = ("Enter %s to not store the results of the operation in storage or file name to export data:").formatted(SKIP_STAGE_STR_FORMATTED);
            return createReplyMessage(userMessage.getChatId(), nextMsg);
        }

        return createReplyMessage(userMessage.getChatId(), "<b>Unsupported stage of command.</b>");
    }

    @Override
    protected PartialBotApiMethod<?> processTerminalStage(
            @NonNull Message userMessage,
            @NonNull TelegramUserConversationStore.CommandContext context) {

        final var query = context.getAttributeAsString(QUERY_STAGE);
        final var extendedFormat = "extended".equalsIgnoreCase(context.getAttributeAsString(QUERY_FORMAT_STAGE));
        final var filename = context.getAttributeAsString(TERMINAL_STAGE);
        @SuppressWarnings("unchecked")
        final var postFiltersMap = (Map<String, JsonNode>) context.get(POST_FILTERS_STAGE);
        @SuppressWarnings("unchecked")
        final var sortsMap = (Map<String, Sort.Direction>) context.get(SORTS_STAGE);
        @SuppressWarnings("unchecked")
        final var aggregationsMap = (Map<String, JsonNode>) context.get(AGGREGATIONS_STAGE);
        final var save = !SKIP_STAGE_STR.equals(filename);

        final var searchQuery = new RequestAnalyzeQuery(query, extendedFormat, postFiltersMap, sortsMap, aggregationsMap, save, 0, 0, filename);

        try {
            final var result = this.logsService.analyze(searchQuery);
            final var resultFile = createResultFile(result, save ? filename : UUID.randomUUID() + ".txt");

            final var resultMessage = new SendDocument();
            resultMessage.setChatId(userMessage.getChatId());
            resultMessage.setReplyToMessageId(userMessage.getMessageId());
            resultMessage.setDocument(new InputFile(resultFile, searchQuery.exportToFile()));

            return resultMessage;
        } catch (Exception ex) {
            return createReplyMessage(userMessage.getChatId(), "<b>Unable to analyze logs by query: </b>" + ex.getMessage());
        }
    }

    private File createResultFile(final MapLogsStatistics statistics, final String filename) throws IOException {
        final var json = this.jsonConverter.convertToJson(statistics);
        final var tempDir = Files.createTempDirectory(UUID.randomUUID().toString());
        final var statsFile = tempDir.resolve(filename);
        Files.writeString(statsFile, json, StandardCharsets.UTF_8, StandardOpenOption.CREATE);

        final var result = statsFile.toFile();
        result.deleteOnExit();

        return result;
    }
}