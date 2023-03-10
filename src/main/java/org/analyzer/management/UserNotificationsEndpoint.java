package org.analyzer.management;

import org.analyzer.service.exceptions.NotEnoughOperationParametersException;
import org.analyzer.service.exceptions.UnsupportedApplicationOperationException;
import org.analyzer.service.users.notifications.BroadcastUserNotifier;
import org.analyzer.service.users.notifications.UserNotifier;
import org.analyzer.service.users.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

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
    public void write(@Selector(match = Selector.Match.ALL_REMAINING) String[] operation) {
        if (operation == null || operation.length < 2) {
            throw new NotEnoughOperationParametersException("notify-users", 2, operation == null ? 0 : operation.length);
        }

        final var operationType = operation[0];
        switch (operationType) {
            case "broadcast" -> this.broadcastUserNotifier.broadcast(operation[1]);
            case "user" -> {
                if (operation.length != 3) {
                    throw new NotEnoughOperationParametersException("notify-user", 3, operation.length);
                }

                final var user = this.userService.findById(operation[1]);
                this.userNotifier.notify(operation[2], user);
            }
            default -> throw new UnsupportedApplicationOperationException(StringUtils.arrayToDelimitedString(operation, "/"));
        }
    }
}
