package org.analyzer.logs.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.Map;

@Document("statistics")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
public class LogsStatisticsEntity {

    @Id
    @EqualsAndHashCode.Include
    @NonNull
    @Indexed
    private String id;
    @NonNull
    @Field("data_query")
    @Indexed
    private String dataQuery;
    @NonNull
    private LocalDateTime created;
    @NonNull
    @ToString.Exclude
    @Field
    private Map<String, Object> stats;
    @NonNull
    @Field("user_key")
    private String userKey;
    private String title;
}
