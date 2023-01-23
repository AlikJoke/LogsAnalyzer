package org.analyzer.logs.service;

import org.analyzer.logs.model.LogRecordEntity;
import reactor.core.publisher.Flux;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;

public interface LogRecordsParser {

    @Nonnull
    Flux<LogRecordEntity> parse(
            @Nonnull String logKey,
            @Nonnull File logFile,
            @Nullable LogRecordFormat recordFormat);
}
