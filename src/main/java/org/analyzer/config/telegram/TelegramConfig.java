package org.analyzer.config.telegram;

import org.analyzer.service.users.notifications.telegram.TelegramUserConversationStore;
import org.analyzer.service.users.notifications.telegram.bots.LogsAnalyzerBot;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.helpCommand.HelpCommand;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.List;

@Configuration
@EnableConfigurationProperties(TelegramBotConfiguration.class)
public class TelegramConfig {

    @Bean
    public TelegramBotsApi telegramBotsApi(LogsAnalyzerBot logsAnalyzerBot) throws TelegramApiException {
        final var api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(logsAnalyzerBot);

        return api;
    }

    @Bean
    public LogsAnalyzerBot logsAnalyzerBot(
            TelegramBotConfiguration configuration,
            List<BotCommand> commands,
            TelegramUserConversationStore userConversationStore) {
        final var bot = new LogsAnalyzerBot(configuration, userConversationStore);
        bot.registerAll(commands.toArray(new BotCommand[0]));

        return bot;
    }

    @Bean
    public HelpCommand helpCommand() {
        return new HelpCommand();
    }
}
