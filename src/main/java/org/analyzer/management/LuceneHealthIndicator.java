package org.analyzer.management;

import org.apache.lucene.index.IndexWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;

public class LuceneHealthIndicator extends AbstractHealthIndicator {

    @Autowired
    private IndexWriter indexWriter;

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        if (!this.indexWriter.isOpen()) {
            builder.down();
            return;
        }

        final var indexFiles = this.indexWriter.getDirectory().listAll();
        if (indexFiles == null || indexFiles.length == 0) {
            builder.outOfService();
        } else {
            builder.up();
        }

        builder.withDetail("index-files", indexFiles == null ? 0 : indexFiles.length);
        if (indexFiles == null) {
            return;
        }

        long filesSize = 0;
        for (final var file : indexFiles) {
            filesSize += this.indexWriter.getDirectory().fileLength(file);
        }
        builder.withDetail("index-size", filesSize);
    }
}
