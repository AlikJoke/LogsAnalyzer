package org.analyzer.logs.service.std;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.analyzer.logs.service.BroadcastUserNotifier;
import org.analyzer.logs.service.UserNotifier;
import org.analyzer.logs.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class TelegramBroadcastUserNotifier implements BroadcastUserNotifier {

    @Autowired
    private UserNotifier userNotifier;
    @Autowired
    private UserService userService;

    @Override
    @NonNull
    public Mono<Void> broadcast(@NonNull String message) {
        return this.userService.findAllWithTelegramId()
                    .flatMap(user -> this.userNotifier.notify(message, user))
                    .then();
    }
}
