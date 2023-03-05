package org.analyzer.dao.lucene;

import lombok.NonNull;
import org.analyzer.entities.LogRecordEntity;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.search.SortField;

import java.time.temporal.TemporalAccessor;
import java.util.HashMap;
import java.util.Map;

import static org.analyzer.entities.LogRecordEntity.toStorageFieldName;

public class LuceneLogRecordFieldMetadata {

    private final Map<String, Class<? extends Field>> field2type;
    private final Map<String, SortField.Type> field2sort;

    public LuceneLogRecordFieldMetadata() {
        final var fields = LogRecordEntity.class.getDeclaredFields();
        final Map<String, Class<? extends Field>> tempAll = new HashMap<>(fields.length, 1);
        final Map<String, SortField.Type> tempSortFields = new HashMap<>(fields.length, 1);
        for (final var field : fields) {
            final var storageField = toStorageFieldName(field.getName());
            final var isLongFieldType = TemporalAccessor.class.isAssignableFrom(field.getType()) || Long.class == field.getType();
            tempAll.put(storageField, isLongFieldType ? LongField.class : TextField.class);

            final var sortType = isLongFieldType
                                    ? SortField.Type.LONG
                                    : SortField.Type.STRING;
            tempSortFields.put(storageField, sortType);
        }

        this.field2type = Map.copyOf(tempAll);
        this.field2sort = Map.copyOf(tempSortFields);
    }

    @NonNull
    public Map<String, Class<? extends Field>> getStorageFields() {
        return this.field2type;
    }

    @NonNull
    public SortField.Type getSortFieldType(@NonNull final String field) {
        final var storageField = toStorageFieldName(field);
        return this.field2sort.get(storageField);
    }
}
