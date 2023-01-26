package org.analyzer.logs.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Builder
public class NetworkDataSettings {

    @Field("data_url")
    private String logsUrl;
    @Field("authentication_token")
    @ToString.Exclude
    private String authToken;
}
