package org.parser.app.service;

import org.parser.app.model.LogRecord;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.List;

public interface LogRecordParser {

    @Nonnull
    List<LogRecord> parse(@Nonnull File logFile, @Nonnull String fileName, @Nullable String recordFormat);
}
