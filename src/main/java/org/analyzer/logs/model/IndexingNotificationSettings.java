package org.analyzer.logs.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Builder
public class IndexingNotificationSettings {

    @Field("error_notifications_enabled")
    private boolean errorNotificationsEnabled;
    @Field("aggregation_notifications_enabled")
    private boolean aggregationNotificationsEnabled;
    @Field("notify_email")
    private String notifyToEmail;
}
