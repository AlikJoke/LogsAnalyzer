package org.parser.app.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDate;
import java.time.LocalTime;

@Document(indexName = "logs")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
public class LogRecord {

    @Id
    @EqualsAndHashCode.Include
    @NonNull
    private String id;

    @Field(
            excludeFromSource = true,
            type = FieldType.Date,
            format = DateFormat.date
    )
    private LocalDate date;

    @Field(
            excludeFromSource = true,
            type = FieldType.Date,
            format = DateFormat.time
    )
    private LocalTime time;

    @Field(excludeFromSource = true)
    private String level;

    @Field
    @NonNull
    @ToString.Exclude
    private String source;

    @Field(excludeFromSource = true)
    private String category;

    @Field(excludeFromSource = true)
    private String thread;

    @Field(excludeFromSource = true)
    private String record;
}
