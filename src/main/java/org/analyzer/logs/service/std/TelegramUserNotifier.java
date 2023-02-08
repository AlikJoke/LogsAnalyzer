package org.analyzer.logs.service.std;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.analyzer.logs.service.UserNotifier;
import org.analyzer.logs.service.std.config.TelegramNotificationBotConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
public class TelegramUserNotifier implements UserNotifier {

    @Autowired
    private TelegramNotificationBotConfiguration telegramConfiguration;
    @Autowired
    private WebClient webClient;

    @NonNull
    @Override
    public Mono<Void> notify(@NonNull String message, @NonNull String userId) {
        return Flux.fromArray(message.split("(?<=\\G.{4096})"))
                    .flatMapSequential(part ->
                                    this.webClient
                                            .post()
                                            .uri(buildOperationUrl(userId, part))
                                            .retrieve()
                                            .toBodilessEntity(),
                            1
                    )
                    .doOnError(ex -> log.error("", ex))
                    .doOnNext(response -> log.debug("Message sent with code {}", response.getStatusCode()))
                    .then();
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
