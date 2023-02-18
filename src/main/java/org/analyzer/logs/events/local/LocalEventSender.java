package org.analyzer.logs.events.local;

import lombok.NonNull;
import org.analyzer.logs.events.EventSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ApplicationEventMulticaster;

public class LocalEventSender implements EventSender {

    @Autowired
    private ApplicationEventMulticaster eventMulticaster;

    @Override
    public void send(@NonNull String channel, @NonNull final Object event) {
        this.eventMulticaster.multicastEvent(new ApplicationEvent(event) {});
    }
}
