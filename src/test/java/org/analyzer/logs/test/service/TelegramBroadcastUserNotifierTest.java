package org.analyzer.logs.test.service;

import org.analyzer.logs.service.UserNotifier;
import org.analyzer.logs.service.UserService;
import org.analyzer.logs.service.std.TelegramBroadcastUserNotifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.analyzer.logs.test.fixtures.TestFixtures.TEST_USER;
import static org.analyzer.logs.test.fixtures.TestFixtures.createUser;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@Import(TelegramBroadcastUserNotifier.class)
public class TelegramBroadcastUserNotifierTest {

    @Autowired
    private TelegramBroadcastUserNotifier broadcastUserNotifier;
    @MockBean
    private UserNotifier userNotifier;
    @MockBean
    private UserService userService;

    @Test
    public void shouldSendMessagesToAll() {

        final var message = "test";
        final var user1 = createUser(TEST_USER + 1);
        final var user2 = createUser(TEST_USER + 2);

        when(this.userService.findAllWithTelegramId())
                .thenReturn(Flux.just(user1, user2));
        when(this.userNotifier.notify(message, user1))
                .thenReturn(Mono.empty());
        when(this.userNotifier.notify(message, user2))
                .thenReturn(Mono.empty());

        StepVerifier
                .create(this.broadcastUserNotifier.broadcast(message))
                .verifyComplete();

        verify(this.userNotifier, times(1)).notify(message, user1);
        verify(this.userNotifier, times(1)).notify(message, user2);
    }

    @Test
    public void shouldSendMessagesToAllWithError() {

        final var message = "test";
        final var user1 = createUser(TEST_USER + 1);
        final var user2 = createUser(TEST_USER + 2);

        when(this.userService.findAllWithTelegramId())
                .thenReturn(Flux.just(user1, user2));
        when(this.userNotifier.notify(message, user1))
                .thenReturn(Mono.error(IllegalStateException::new));
        when(this.userNotifier.notify(message, user2))
                .thenReturn(Mono.empty());

        StepVerifier
                .create(this.broadcastUserNotifier.broadcast(message))
                .verifyComplete();

        verify(this.userNotifier, times(1)).notify(message, user1);
        verify(this.userNotifier, times(1)).notify(message, user2);
    }
}
