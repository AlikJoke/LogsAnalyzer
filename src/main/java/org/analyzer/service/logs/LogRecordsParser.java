package org.analyzer.service.logs;

import org.analyzer.entities.LogRecordEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

public interface LogRecordsParser {

    @Nonnull
    LogRecordsPackageIterator parse(
            @Nonnull String logKey,
            @Nonnull File logFile,
            @Nullable LogRecordFormat recordFormat) throws IOException;

    @NotThreadSafe
    interface LogRecordsPackageIterator extends Iterator<Collection<LogRecordEntity>>, AutoCloseable {

        @Override
        void close();
    }
}
