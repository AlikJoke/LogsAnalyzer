package org.analyzer.logs.service.scheduled.config;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@Value
@RequiredArgsConstructor(onConstructor = @__(@ConstructorBinding))
@ConfigurationProperties("logs.analyzer.notifications.telegram")
public class TelegramNotificationBotConfiguration {

    String operationTemplate;
    String userTokenKey;
    String messageKey;
}
