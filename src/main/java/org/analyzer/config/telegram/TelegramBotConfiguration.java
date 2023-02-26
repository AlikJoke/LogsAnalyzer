package org.analyzer.config.telegram;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@Value
@RequiredArgsConstructor(onConstructor = @__(@ConstructorBinding))
@ConfigurationProperties("logs.analyzer.telegram.bot")
public class TelegramBotConfiguration {

    String operationTemplate;
    String userTokenKey;
    String messageKey;
    String name;
    String token;
    int maxThreads;
    int updatesLimit;
}
