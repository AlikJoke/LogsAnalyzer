package org.analyzer.logs.service.scheduled;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.analyzer.logs.model.IndexingNotificationSettings;
import org.analyzer.logs.service.scheduled.config.TelegramNotificationBotConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;

import java.util.List;

@Component
@Slf4j
public class DataIndexingTelegramNotifier implements DataIndexingNotifier {

    @Autowired
    private TelegramNotificationBotConfiguration telegramConfiguration;
    @Autowired
    private WebClient webClient;

    @Override
    public void notifySuccess(@NonNull String indexingKey, @NonNull String successMessage, @NonNull IndexingNotificationSettings notificationSettings) {
        this.sendMessage(
                notificationSettings.getNotifyToTelegram(),
                "**Logs indexing completed** __(%s)__\n".formatted(indexingKey) + "```" + successMessage + "```"
        );
    }

    @Override
    public void notifyError(@NonNull String message, @NonNull IndexingNotificationSettings notificationSettings) {
        this.sendMessage(
                notificationSettings.getNotifyToTelegram(),
                "**Logs indexing failed with error**\n" + "__" + message + "__"
        );
    }

    private void sendMessage(final String userToken, final String message) {
        Flux.fromArray(message.split("(?<=\\G.{4096})"))
                .flatMapSequential(part ->
                    this.webClient
                            .post()
                            .uri(buildOperationUrl(userToken, part))
                            .retrieve()
                            .toBodilessEntity(),
                        1
                )
                .onErrorStop()
                .subscribe(
                        response -> log.debug("Message sent with code {}", response.getStatusCode()),
                        ex -> log.error("", ex)
                );
    }

    private String buildOperationUrl(final String userToken, final String text) {
        return UriComponentsBuilder
                    .fromHttpUrl(this.telegramConfiguration.getOperationTemplate())
                    .queryParams(composeRequestParams(userToken, text))
                .build()
                .toString();
    }

    private MultiValueMap<String, String> composeRequestParams(
            final String userToken,
            final String message) {
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>(2);
        params.put(this.telegramConfiguration.getUserTokenKey(), List.of(userToken));
        params.put(this.telegramConfiguration.getMessageKey(), List.of(message));

        return params;
    }
}
