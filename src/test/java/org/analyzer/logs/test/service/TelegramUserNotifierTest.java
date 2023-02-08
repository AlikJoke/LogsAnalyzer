package org.analyzer.logs.test.service;

import org.analyzer.logs.service.std.TelegramUserNotifier;
import org.analyzer.logs.service.std.config.TelegramNotificationBotConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.analyzer.logs.test.fixtures.TestFixtures.TEST_USER;
import static org.analyzer.logs.test.fixtures.TestFixtures.createUser;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@Import({TelegramUserNotifier.class, TelegramUserNotifierTest.TelegramBotTestConfiguration.class})
public class TelegramUserNotifierTest {

    @Autowired
    private TelegramUserNotifier userNotifier;
    @MockBean
    private WebClient webClient;
    @Autowired
    private TelegramNotificationBotConfiguration notificationBotConfiguration;

    @Test
    public void shouldSendShortMessageToTelegramInOneRequest() {
        makeChecks("test", 1);
    }

    @Test
    public void shouldSendShortMessageToTelegramInMultipleRequests() {
        makeChecks(StringUtils.repeat("test", 4097), 5);
    }

    @Test
    public void shouldNotSendMessageToTelegramForUserWithoutTelegramId() {
        final var user = createUser(TEST_USER);
        user.getSettings().getScheduledIndexingSettings().forEach(s -> s.getNotificationSettings().setNotifyToTelegram(null));
        StepVerifier
                .create(this.userNotifier.notify("test", user))
                .expectError(IllegalStateException.class);

        StepVerifier
                .create(this.userNotifier.notify("test", user.setSettings(null)))
                .expectError(IllegalStateException.class);
    }

    private void makeChecks(final String message, final int webClientInvocationTimes) {

        final var userTelegramId = UUID.randomUUID().toString();

        final var requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        when(this.webClient.post()).thenReturn(requestBodyUriSpec);

        final var uriCaptor = ArgumentCaptor.forClass(String.class);

        final var requestBodySpec = mock(WebClient.RequestBodySpec.class);
        when(requestBodyUriSpec.uri(uriCaptor.capture())).thenReturn(requestBodySpec);

        final var responseSpec = mock(WebClient.ResponseSpec.class);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        final var responseEntity = mock(ResponseEntity.class);
        when(responseSpec.toBodilessEntity()).thenReturn(Mono.just(responseEntity));

        final var notifyResult = this.userNotifier.notify(message, userTelegramId);
        StepVerifier
                .create(notifyResult)
                .verifyComplete();

        verify(responseSpec, times(webClientInvocationTimes)).toBodilessEntity();

        assertNotNull(uriCaptor.getValue());
        assertTrue(uriCaptor.getValue().startsWith(notificationBotConfiguration.getOperationTemplate()));
        assertTrue(uriCaptor.getValue().contains(notificationBotConfiguration.getUserTokenKey() + "=" + userTelegramId));
    }

    @Configuration
    static class TelegramBotTestConfiguration {

        @Bean
        TelegramNotificationBotConfiguration telegramNotificationBotConfiguration() {
            return new TelegramNotificationBotConfiguration("https://api.telegram.org/bot123/sendMessage", "chat_id", "text");
        }
    }
}
