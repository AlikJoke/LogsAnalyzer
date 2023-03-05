package org.analyzer.dao.lucene;

import lombok.NonNull;
import org.analyzer.entities.LogRecordEntity;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexableField;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAccessor;
import java.util.Optional;

import static org.analyzer.entities.LogRecordEntity.field2FieldValueFunction;
import static org.analyzer.entities.LogRecordEntity.toStorageFieldName;

public class LuceneLogRecordBuilder {

    @Autowired
    private LuceneLogRecordFieldMetadata logRecordFieldMetadata;

    @NonNull
    public Document buildDocument(@NonNull LogRecordEntity entity) {
        final var doc = new Document();
        this.logRecordFieldMetadata.getStorageFields().forEach((field, type) -> {
            tryCreateField(field, type, entity)
                    .ifPresent(doc::add);
        });

        return doc;
    }

    @NonNull
    public LogRecordEntity buildEntity(@NonNull final Document document) {

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

    @NonNull
    private Optional<IndexableField> tryCreateField(
            @NonNull final String field,
            @NonNull final Class<? extends Field> type,
            @NonNull final LogRecordEntity logRecord) {
        final var valueFunc = field2FieldValueFunction(field);
        final var value = valueFunc.apply(logRecord);
        final var storageField = toStorageFieldName(field);

        if (value == null) {
            return Optional.empty();
        }

        final IndexableField result;
        if (type == LongField.class) {
            result = new LongField(storageField, Instant.from((TemporalAccessor) value).toEpochMilli());
        } else if (type == TextField.class) {
            result = new TextField(storageField, value.toString(), Field.Store.YES);
        } else {
            throw new IllegalArgumentException("Unsupported field type: " + type);
        }

        return Optional.of(result);
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
