package org.parser.app.service;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.List;

public interface LogRecordService {

    void index(@Nonnull File logFile, @Nonnull String originalLogFileName, @Nullable String logRecordPattern) throws IOException;

    void dropIndex();

    @Nonnegative
    long getAllRecordsCount();

    @Nonnull
    List<String> getRecordsByFilter(@Nonnull String filter);
}
