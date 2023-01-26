package org.analyzer.logs.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Data
@Builder
public class UserSettingsEntity {

    @Field("cleaning_interval")
    private long cleaningInterval;
    @Field("scheduled_indexing_settings")
    private List<ScheduledIndexingSettings> scheduledIndexingSettings;
}
