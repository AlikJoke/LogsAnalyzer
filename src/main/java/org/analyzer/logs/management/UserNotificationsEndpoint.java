package org.analyzer.logs.management;

import org.analyzer.logs.service.BroadcastUserNotifier;
import org.analyzer.logs.service.UserNotifier;
import org.analyzer.logs.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

@Component
@Endpoint(id = "notifications")
public class UserNotificationsEndpoint {

    @Autowired
    private BroadcastUserNotifier broadcastUserNotifier;
    @Autowired
    private UserNotifier userNotifier;
    @Autowired
    private UserService userService;

    @WriteOperation
    public Mono<Boolean> write(@Selector(match = Selector.Match.ALL_REMAINING) String[] operation) {
        if (operation == null || operation.length < 2) {
            return Mono.error(() -> new UnsupportedOperationException("Not enough parameters for notification operations"));
        }

        final String operationType = operation[0];
        return switch (operationType) {
            case "broadcast" -> this.broadcastUserNotifier.broadcast(operation[1])
                                                            .onErrorStop()
                                                            .thenReturn(true);
            case "user" -> {
                if (operation.length != 3) {
                    yield Mono.error(() -> new UnsupportedOperationException("Not enough parameters for single user notification (/user/{userTelegramId}/{message})"));
                }

                yield this.userService.findById(operation[1])
                                        .flatMap(user -> this.userNotifier.notify(operation[2], user))
                                        .onErrorStop()
                                        .thenReturn(true);
            }
            default -> Mono.error(() -> new UnsupportedOperationException(StringUtils.arrayToDelimitedString(operation, "/")));
        };
    }
}
