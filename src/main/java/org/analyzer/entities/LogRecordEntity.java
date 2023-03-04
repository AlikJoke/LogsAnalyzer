package org.analyzer.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import javax.annotation.Nonnull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.function.Function;

@Document(indexName = "logs")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
public class LogRecordEntity {

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
    private String level;
    @NonNull
    @ToString.Exclude
    private String source;
    private String category;
    private String thread;
    @Field("trace_id")
    private String traceId;
    private String record;

    @Nonnull
    public static Function<LogRecordEntity, Object> field2FieldValueFunction(@Nonnull final String fieldName) {
        return switch (fieldName) {
            case "thread", "thread.keyword" -> LogRecordEntity::getThread;
            case "category", "category.keyword" -> LogRecordEntity::getCategory;
            case "record", "record.keyword" -> LogRecordEntity::getRecord;
            case "date", "date.keyword" -> LogRecordEntity::getDate;
            case "time", "time.keyword" -> LogRecordEntity::getTime;
            case "trace_id", "trace_id.keyword" -> LogRecordEntity::getTraceId;
            case "level", "level.keyword" -> LogRecordEntity::getLevel;
            case "id", "id.keyword" -> LogRecordEntity::getId;
            default -> throw new IllegalArgumentException("Unsupported field: " + fieldName);
        };
    }
}
