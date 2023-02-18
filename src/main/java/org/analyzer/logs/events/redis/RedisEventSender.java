package org.analyzer.logs.events.redis;

import lombok.NonNull;
import org.analyzer.logs.events.EventSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

public class RedisEventSender implements EventSender {

    @Autowired
    private RedisTemplate<String, Object> template;

    @Override
    public void send(@NonNull final String channel, @NonNull final Object event) {
        this.template.convertAndSend(channel, event);
    }
}
