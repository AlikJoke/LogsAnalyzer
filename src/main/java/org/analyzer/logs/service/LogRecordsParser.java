package org.analyzer.logs.service;

import org.analyzer.logs.model.LogRecordEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public interface LogRecordsParser {

    @Nonnull
    LogRecordsPackageIterator parse(
            @Nonnull String logKey,
            @Nonnull File logFile,
            @Nullable LogRecordFormat recordFormat) throws IOException;

    @NotThreadSafe
    interface LogRecordsPackageIterator extends Iterator<List<LogRecordEntity>>, AutoCloseable {

        @Override
        void close();
    }
}
