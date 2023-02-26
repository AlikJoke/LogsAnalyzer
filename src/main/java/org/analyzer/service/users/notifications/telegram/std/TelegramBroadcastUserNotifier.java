package org.analyzer.service.users.notifications.telegram.std;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.analyzer.service.users.notifications.BroadcastUserNotifier;
import org.analyzer.service.users.notifications.UserNotifier;
import org.analyzer.service.users.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TelegramBroadcastUserNotifier implements BroadcastUserNotifier {

    @Autowired
    private UserNotifier userNotifier;
    @Autowired
    private UserService userService;

    @Override
    public void broadcast(@NonNull String message) {
        this.userService.findAllWithTelegramId()
                            .forEach(user -> this.userNotifier.notify(message, user));
    }
}
