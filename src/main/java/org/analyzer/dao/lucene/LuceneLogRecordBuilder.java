package org.analyzer.dao.lucene;

import lombok.NonNull;
import org.analyzer.entities.LogRecordEntity;
import org.apache.lucene.document.Document;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

import static org.analyzer.entities.LogRecordEntity.toStorageFieldName;

public class LuceneLogRecordBuilder {

    @NonNull
    public LogRecordEntity build(@NonNull final Document document) {

        return new LogRecordEntity()
                    .setId(getStringFieldValue(document, "id"))
                    .setTime(parseTime(document))
                    .setDate(parseDate(document))
                    .setLevel(getStringFieldValue(document, "level"))
                    .setThread(getStringFieldValue(document, "thread"))
                    .setTraceId(getStringFieldValue(document, "traceId"))
                    .setCategory(getStringFieldValue(document, "category"))
                    .setSource(getStringFieldValue(document, "source"))
                    .setRecord(getStringFieldValue(document, "record"));
    }

    private LocalDate parseDate(final Document document) {
        final var instant = getInstantFieldValue(document, "date");
        return instant == null ? null : LocalDate.ofInstant(instant, ZoneId.systemDefault());
    }

    private LocalTime parseTime(final Document document) {
        final var instant = getInstantFieldValue(document, "time");
        return instant == null ? null : LocalTime.ofInstant(instant, ZoneId.systemDefault());
    }

    private Instant getInstantFieldValue(final Document document, final String entityFieldName) {
        final var field = document.getField(toStorageFieldName(entityFieldName));
        if (field == null) {
            return null;
        } else if (field.numericValue() == null) {
            throw new IllegalStateException("Illegal non-numeric value in field '" + entityFieldName + "'");
        }

        return Instant.ofEpochMilli(field.numericValue().longValue());
    }

    private String getStringFieldValue(final Document document, final String entityField) {
        return document.get(toStorageFieldName(entityField));
    }
}
