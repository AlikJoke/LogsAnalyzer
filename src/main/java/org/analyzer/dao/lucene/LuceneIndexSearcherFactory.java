package org.analyzer.dao.lucene;

import jakarta.annotation.PreDestroy;
import lombok.NonNull;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

public class LuceneIndexSearcherFactory {

    private final IndexWriter indexWriter;
    private final AtomicReference<IndexSearcher> indexSearcher;

    @Autowired
    public LuceneIndexSearcherFactory(@NonNull IndexWriter writer) throws IOException {
        this.indexWriter = writer;
        final var reader = DirectoryReader.open(writer);
        this.indexSearcher = new AtomicReference<>(new IndexSearcher(reader));
    }

    @NonNull
    public IndexSearcher get() {
        return this.indexSearcher.updateAndGet(oldSearcher -> {
            try {
                final var indexReader = (DirectoryReader) oldSearcher.getIndexReader();
                final var newReader = DirectoryReader.openIfChanged(indexReader, this.indexWriter, false);
                return newReader == null ? oldSearcher : new IndexSearcher(newReader);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @PreDestroy
    private void destroy() throws IOException {
        this.indexSearcher.get().getIndexReader().close();
    }
}
