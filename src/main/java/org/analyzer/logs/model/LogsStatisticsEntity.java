package org.analyzer.logs.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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
    private String id;
    @NonNull
    private String dataQuery;
    @NonNull
    private LocalDateTime created;
    @NonNull
    @ToString.Exclude
    private Map<String, Object> stats;
    @NonNull
    private String userHash;
    private String title;
}
