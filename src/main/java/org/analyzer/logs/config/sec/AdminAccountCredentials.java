package org.analyzer.logs.config.sec;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@Value
@RequiredArgsConstructor(onConstructor = @__(@ConstructorBinding))
@ConfigurationProperties("logs.analyzer.admin.account.credentials")
public class AdminAccountCredentials {

    String username;
    String encodedPassword;
}
