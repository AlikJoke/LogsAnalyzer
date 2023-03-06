package org.analyzer.dao.lucene;

import lombok.NonNull;
import org.analyzer.entities.LogRecordEntity;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexableField;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.analyzer.entities.LogRecordEntity.field2FieldValueFunction;
import static org.analyzer.entities.LogRecordEntity.toStorageFieldName;

public class LuceneLogRecordBuilder {

    @Autowired
    private LuceneLogRecordFieldMetadata logRecordFieldMetadata;

    @NonNull
    public Document buildDocument(@NonNull LogRecordEntity entity) {
        final var doc = new Document();
        this.logRecordFieldMetadata.getStorageFields()
                                    .forEach((field, type) ->
                                            tryAddFieldToDocument(field, type, entity, doc)
                                    );

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
                    .setSpanId(getStringFieldValue(document, "spanId"))
                    .setCategory(getStringFieldValue(document, "category"))
                    .setSource(getStringFieldValue(document, "source"))
                    .setRecord(getStringFieldValue(document, "record"));
    }

    private void tryAddFieldToDocument(
            final String field,
            final Class<? extends Field> type,
            final LogRecordEntity logRecord,
            final Document document) {
        final var valueFunc = field2FieldValueFunction(field);
        final var value = valueFunc.apply(logRecord);
        final var storageField = toStorageFieldName(field);

        if (value == null) {
            return;
        }

        final IndexableField result;
        if (type == LongField.class) {
            final var resultAsLong =
                    value instanceof LocalDate
                            ? ((LocalDate) value).toEpochDay()
                            : value instanceof LocalTime
                                ? ((LocalTime) value).toNanoOfDay()
                                : (long) value;
            result = new NumericDocValuesField(storageField, resultAsLong);
        } else if (type == TextField.class) {
            result = new TextField(storageField, value.toString(), Field.Store.YES);
        } else {
            throw new IllegalArgumentException("Unsupported field type: " + type);
        }

        document.add(result);
    }

    private LocalDate parseDate(final Document document) {
        final var value = getLongFieldValue(document, "date");
        return value < 0 ? null : LocalDate.ofEpochDay(value);
    }

    private LocalTime parseTime(final Document document) {
        final var value = getLongFieldValue(document, "time");
        return value < 0 ? null : LocalTime.ofNanoOfDay(value);
    }

    private long getLongFieldValue(final Document document, final String entityFieldName) {
        final var field = document.getField(toStorageFieldName(entityFieldName));
        if (field == null) {
            return -1;
        } else if (field.numericValue() == null) {
            throw new IllegalStateException("Illegal non-numeric value in field '" + entityFieldName + "'");
        }

        return field.numericValue().longValue();
    }

    private String getStringFieldValue(final Document document, final String entityField) {
        return document.get(toStorageFieldName(entityField));
    }
}
