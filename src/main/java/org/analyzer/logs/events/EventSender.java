package org.analyzer.logs.events;

import javax.annotation.Nonnull;

public interface EventSender {

    void send(@Nonnull String channel, @Nonnull Object event);
}
