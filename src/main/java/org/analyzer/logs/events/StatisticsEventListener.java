package org.analyzer.logs.events;

import lombok.NonNull;
import org.analyzer.logs.model.LogsStatisticsEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StatisticsEventListener extends MongoEventGenerationListener<LogsStatisticsEntity> {

    @Autowired
    protected StatisticsEventListener(
            @NonNull EventSender eventSender,
            @Value("${logs.analyzer.events.statistics.channel}") @NonNull String channel) {
        super(eventSender, channel);
    }

    @Override
    protected LogsStatisticsEntity buildEntityToSend(LogsStatisticsEntity sourceEntity) {
        return sourceEntity;
    }
}
