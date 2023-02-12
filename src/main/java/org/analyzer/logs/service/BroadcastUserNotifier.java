package org.analyzer.logs.service;

import javax.annotation.Nonnull;

public interface BroadcastUserNotifier {

    void broadcast(@Nonnull String message);
}
