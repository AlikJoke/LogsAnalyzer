package org.analyzer.service.users.notifications;

import javax.annotation.Nonnull;

public interface BroadcastUserNotifier {

    void broadcast(@Nonnull String message);
}
