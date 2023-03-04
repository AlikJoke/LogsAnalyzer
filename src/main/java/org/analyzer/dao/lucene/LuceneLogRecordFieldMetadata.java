package org.analyzer.dao.lucene;

import lombok.NonNull;
import org.apache.lucene.search.SortField;

import static org.analyzer.entities.LogRecordEntity.toEntityFieldName;

public class LuceneLogRecordFieldMetadata {

    @NonNull
    public SortField.Type getSortFieldType(@NonNull final String field) {
        return switch (toEntityFieldName(field)) {
            case "date", "time" -> SortField.Type.LONG;
            default -> SortField.Type.STRING;
        };
    }
}
