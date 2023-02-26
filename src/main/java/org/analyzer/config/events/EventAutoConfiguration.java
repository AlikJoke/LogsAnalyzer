package org.analyzer.config.events;

import org.analyzer.config.redis.ConditionalRedisAutoConfiguration;
import org.analyzer.events.EventSender;
import org.analyzer.events.local.LocalEventSender;
import org.analyzer.events.redis.RedisEventSender;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@AutoConfiguration
@AutoConfigureAfter(ConditionalRedisAutoConfiguration.class)
public class EventAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(RedisConnectionFactory.class)
    public EventSender localEventSender() {
        return new LocalEventSender();
    }

    @Bean
    @ConditionalOnBean(RedisConnectionFactory.class)
    public EventSender redisEventSender() {
        return new RedisEventSender();
    }
}
