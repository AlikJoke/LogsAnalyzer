package org.analyzer.entities;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Builder
public class ScheduledIndexingSettings {

    @NonNull
    @Field("settings_id")
    private String settingsId;
    @NonNull
    private String schedule;
    @Field("log_record_pattern")
    private String logRecordPattern;
    @Field("date_format")
    private String dateFormat;
    @Field("time_format")
    private String timeFormat;
    @Field("network_settings")
    @NonNull
    private NetworkDataSettings networkSettings;
}
