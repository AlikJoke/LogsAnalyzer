package org.analyzer.config.lucene;

import org.analyzer.dao.LogsStorage;
import org.analyzer.dao.lucene.LuceneIndexSearcherFactory;
import org.analyzer.dao.lucene.LuceneLogRecordBuilder;
import org.analyzer.dao.lucene.LuceneLogRecordFieldMetadata;
import org.analyzer.dao.lucene.LuceneLogsStorage;
import org.analyzer.service.logs.SearchQueryParser;
import org.analyzer.service.logs.lucene.LuceneSearchQueryParser;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.ConcurrentMergeScheduler;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.store.NIOFSDirectory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.nio.file.Path;

import static org.analyzer.LogsAnalyzerApplication.BOX_MODE;
import static org.analyzer.LogsAnalyzerApplication.RUN_MODE_PROPERTY;

@AutoConfiguration
@ConditionalOnProperty(name = RUN_MODE_PROPERTY, havingValue = BOX_MODE)
@EnableConfigurationProperties(LuceneConfiguration.class)
public class ConditionalLuceneAutoConfiguration {

    @Bean
    public SearchQueryParser<Query> luceneSearchQueryParser() {
        return new LuceneSearchQueryParser();
    }

    @Bean
    public LogsStorage luceneLogsStorage() {
        return new LuceneLogsStorage();
    }

    @Bean(destroyMethod = "close")
    public Directory luceneDirectory(LuceneConfiguration configuration) throws IOException {
        return switch (configuration.getType()) {
            case RAM -> new ByteBuffersDirectory();
            case NIO -> new NIOFSDirectory(Path.of(configuration.getStoragePath()));
            case MMAP -> new MMapDirectory(Path.of(configuration.getStoragePath()));
            case null -> throw new IllegalStateException("Index type not set");
        };
    }

    @Bean(destroyMethod = "close")
    public IndexWriter luceneIndexWriter(
            LuceneConfiguration configuration,
            Directory directory) throws IOException {

        final var analyzer = new StandardAnalyzer();
        final var indexWriterConfig =
                new IndexWriterConfig(analyzer)
                    .setCommitOnClose(true)
                    .setCheckPendingFlushUpdate(true)
                    .setMaxBufferedDocs(IndexWriterConfig.DISABLE_AUTO_FLUSH)
                    .setMergeScheduler(new ConcurrentMergeScheduler())
                    .setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND)
                    .setRAMBufferSizeMB(configuration.getBufferSizeMb());
        return new IndexWriter(directory, indexWriterConfig);
    }

    @Bean
    public LuceneIndexSearcherFactory luceneIndexReaderFactory(IndexWriter indexWriter) throws IOException {
        return new LuceneIndexSearcherFactory(indexWriter);
    }

    @Bean
    public LuceneLogRecordFieldMetadata luceneLogRecordFieldMetadata() {
        return new LuceneLogRecordFieldMetadata();
    }

    @Bean
    public LuceneLogRecordBuilder luceneLogRecordBuilder() {
        return new LuceneLogRecordBuilder();
    }
}
