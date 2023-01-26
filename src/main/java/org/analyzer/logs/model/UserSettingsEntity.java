package org.analyzer.logs.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserSettingsEntity {

    private boolean errorNotificationsEnabled;
    private boolean aggregationNotificationsEnabled;
    private long cleaningInterval;
    private List<String> downloadLogsUrls;
    private String notificationEmail;
}
