package org.analyzer.entities;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Builder
public class NetworkDataSettings {

    @NonNull
    @Field("data_url")
    private String logsUrl;
    @Field("authentication_token")
    @ToString.Exclude
    private String authToken;
}
