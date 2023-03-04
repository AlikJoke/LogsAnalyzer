package org.analyzer.entities;

import lombok.*;
import lombok.experimental.Accessors;
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
@Accessors(chain = true)
@NoArgsConstructor
public class LogRecordEntity {

    private static final String FIELD_SUFFIX = ".keyword";

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
        return switch (toEntityFieldName(fieldName)) {
            case "thread" -> LogRecordEntity::getThread;
            case "category" -> LogRecordEntity::getCategory;
            case "record" -> LogRecordEntity::getRecord;
            case "date" -> LogRecordEntity::getDate;
            case "time" -> LogRecordEntity::getTime;
            case "trace_id" -> LogRecordEntity::getTraceId;
            case "level" -> LogRecordEntity::getLevel;
            case "id" -> LogRecordEntity::getId;
            default -> throw new IllegalArgumentException("Unsupported field: " + fieldName);
        };
    }

    @Nonnull
    public static String toStorageFieldName(@NonNull final String field) {
        return field.endsWith(FIELD_SUFFIX) ? field : field + FIELD_SUFFIX;
    }

    @Nonnull
    public static String toEntityFieldName(@NonNull final String field) {
        return field.endsWith(FIELD_SUFFIX) ? field.replace(FIELD_SUFFIX, "") : field;
    }
}
