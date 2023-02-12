package org.analyzer.logs.service;

import org.analyzer.logs.model.LogRecordEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.List;

public interface LogRecordsParser {

    @Nonnull
    List<LogRecordEntity> parse(
            @Nonnull String logKey,
            @Nonnull File logFile,
            @Nullable LogRecordFormat recordFormat);
}
