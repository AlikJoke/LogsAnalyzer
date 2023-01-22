package org.analyzer.logs.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.function.Function;

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
            type = FieldType.Date,
            format = DateFormat.date
    )
    private LocalDate date;

    @Field(
            type = FieldType.Date,
            format = DateFormat.hour_minute_second_millis
    )
    private LocalTime time;

    @Field
    private String level;

    @Field
    @NonNull
    @ToString.Exclude
    private String source;

    @Field
    private String category;

    @Field
    private String thread;

    @Field
    private String record;

    @NonNull
    public static Function<LogRecord, Object> field2FieldValueFunction(@NonNull final String fieldName) {
        return switch (fieldName) {
            case "thread", "thread.keyword" -> LogRecord::getThread;
            case "category", "category.keyword" -> LogRecord::getCategory;
            case "record", "record.keyword" -> LogRecord::getRecord;
            case "date", "date.keyword" -> LogRecord::getDate;
            case "time", "time.keyword" -> LogRecord::getTime;
            case "level", "level.keyword" -> LogRecord::getLevel;
            case "id", "id.keyword" -> LogRecord::getId;
            default -> throw new IllegalArgumentException("Unsupported field: " + fieldName);
        };
    }
}
