package org.analyzer.logs.events;

import lombok.NonNull;
import org.analyzer.logs.model.LogsStatisticsEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class StatisticsEventListener extends MongoEventGenerationListener<LogsStatisticsEntity> {

    @Autowired
    protected StatisticsEventListener(
            @NonNull RedisTemplate<String, Object> redisTemplate,
            @Value("${logs.analyzer.events.statistics.topic}") @NonNull String topicName) {
        super(redisTemplate, topicName);
    }

    @Override
    protected LogsStatisticsEntity buildEntityToSend(LogsStatisticsEntity sourceEntity) {
        return sourceEntity;
    }
}
