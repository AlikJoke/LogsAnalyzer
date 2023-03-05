package org.analyzer.config.telegram;

import org.analyzer.service.users.notifications.telegram.TelegramUserConversationStore;
import org.analyzer.service.users.notifications.telegram.bots.LogsAnalyzerBot;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.helpCommand.HelpCommand;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.List;

import static org.analyzer.LogsAnalyzerApplication.MASTER_NODE_PROPERTY;

@Configuration
@EnableConfigurationProperties(TelegramBotConfiguration.class)
public class TelegramConfig {

    @Bean
    @ConditionalOnProperty(name = MASTER_NODE_PROPERTY, havingValue = "true")
    public TelegramBotsApi telegramBotsApi(LogsAnalyzerBot logsAnalyzerBot) throws TelegramApiException {
        final var api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(logsAnalyzerBot);

        return api;
    }

    @Bean
    @ConditionalOnProperty(name = MASTER_NODE_PROPERTY, havingValue = "true")
    public LogsAnalyzerBot logsAnalyzerBot(
            TelegramBotConfiguration configuration,
            List<BotCommand> commands,
            TelegramUserConversationStore userConversationStore) {
        final var bot = new LogsAnalyzerBot(configuration, userConversationStore);
        bot.registerAll(commands.toArray(new BotCommand[0]));

        return bot;
    }

    @Bean
    @ConditionalOnProperty(name = MASTER_NODE_PROPERTY, havingValue = "true")
    public HelpCommand helpCommand() {
        return new HelpCommand();
    }
}
