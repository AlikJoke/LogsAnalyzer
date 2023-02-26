package org.analyzer.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Builder
@AllArgsConstructor
public class NotificationSettings {

    @Field("error_notifications_enabled")
    private boolean errorNotificationsEnabled;
    @Field("aggregation_notifications_enabled")
    private boolean aggregationNotificationsEnabled;
    @Field("notify_email")
    @Indexed(unique = true)
    private String notifyToEmail;
    @Field("notify_telegram")
    @Indexed(unique = true)
    private Long notifyToTelegram;
}
