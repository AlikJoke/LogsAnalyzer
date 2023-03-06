package org.analyzer.dao.lucene;

import lombok.NonNull;
import org.analyzer.dao.LogsStorage;
import org.analyzer.entities.LogRecordEntity;
import org.analyzer.service.logs.SearchQueryParser;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.store.Directory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.analyzer.entities.LogRecordEntity.toStorageFieldName;

public class LuceneLogsStorage implements LogsStorage {

    @Autowired
    private SearchQueryParser<Query> queryParser;
    @Autowired
    private Directory directory;
    @Autowired
    private IndexWriter indexWriter;
    @Autowired
    private LuceneIndexSearcherFactory indexSearcherFactory;
    @Autowired
    private LuceneLogRecordFieldMetadata logRecordFieldMetadata;
    @Autowired
    private LuceneLogRecordBuilder logRecordBuilder;
    @Value("${logs.analyzer.search.default.max_results}")
    private int maxResultsDefault;

    @Override
    public void deleteAllByIdRegex(@NonNull String id) {
        final var prefixQuery = new PrefixQuery(new Term(toStorageFieldName("id"), id));
        try {
            this.indexWriter.deleteDocuments(prefixQuery);
            flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteByQuery(@NonNull StorageQuery query) {
        final var parsedQuery = this.queryParser.parse(query.query(), query.userKey());
        try {
            this.indexWriter.deleteDocuments(parsedQuery);
            flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void saveAll(@NonNull Collection<LogRecordEntity> records) {
        final var documents = records
                                .stream()
                                .map(this.logRecordBuilder::buildDocument)
                                .collect(Collectors.toList());
        try {
            this.indexWriter.addDocuments(documents);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void flush() {
        try {
            this.indexWriter.commit();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long allCount() {
        final var searcher = this.indexSearcherFactory.get();
        return searcher.getIndexReader().numDocs();
    }

    @NonNull
    @Override
    public List<LogRecordEntity> searchByQuery(@NonNull LogsStorage.StorageQuery storageQuery) {

        final var searcher = this.indexSearcherFactory.get();
        final var query = storageQuery.query();
        final var parsedQuery = this.queryParser.parse(query, storageQuery.userKey());
        final var pageSize = query.pageSize() == 0 || query.pageSize() > maxResultsDefault ? maxResultsDefault : query.pageSize();
        final var sort = buildSort(query.sorts());
        final var offset = query.pageNumber() * pageSize;

        try {
            final var docs = searcher.search(parsedQuery, offset + pageSize, sort);
            final var scoreDocs = docs.scoreDocs;

            final var storedFields = searcher.storedFields();

            final List<LogRecordEntity> result = new ArrayList<>();
            for (int i = offset; i < scoreDocs.length; i++) {
                final var recordDoc = storedFields.document(scoreDocs[i].doc);
                result.add(this.logRecordBuilder.buildEntity(recordDoc));
            }

            return result;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private Sort buildSort(final Map<String, org.springframework.data.domain.Sort.Direction> sorts) {

        final var sortFields = sorts
                                .entrySet()
                                .stream()
                                .map(e -> new SortField(e.getKey(), this.logRecordFieldMetadata.getSortFieldType(e.getKey()), e.getValue().isDescending()))
                                .toList();
        if (!sortFields.isEmpty()) {
            return new Sort(sortFields.toArray(new SortField[0]));
        }

        final var dateField = toStorageFieldName("date");
        final var timeField = toStorageFieldName("time");
        return new Sort(
                new SortField(dateField, this.logRecordFieldMetadata.getSortFieldType(dateField), true),
                new SortField(timeField, this.logRecordFieldMetadata.getSortFieldType(timeField), true)
        );
    }
}
