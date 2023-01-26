package org.analyzer.logs.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Builder
public class ScheduledIndexingSettings {

    @Field("analyze_indexed_data")
    private boolean analyzeIndexedData;
    private String schedule;
    @Field("log_record_pattern")
    private String logRecordPattern;
    @Field("date_format")
    private String dateFormat;
    @Field("time_format")
    private String timeFormat;
    @Field("notification_settings")
    private IndexingNotificationSettings notificationSettings;
    @Field("network_settings")
    private NetworkDataSettings networkSettings;
}
