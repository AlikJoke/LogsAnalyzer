package org.analyzer.service.logs.std;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.NonNull;
import org.analyzer.config.scheduled.IndexingTasksPool;
import org.analyzer.dao.LogsStorage;
import org.analyzer.entities.LogRecordEntity;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Component
class LogRecordsIndexer {

    private final Executor indexingPool;
    private final LogsStorage logsStorage;
    private final Counter indexedRecordsCounter;
    private final Counter indexRequestsCounter;

    protected LogRecordsIndexer(
            @NonNull @IndexingTasksPool Executor indexingPool,
            @NonNull LogsStorage logsStorage,
            @NonNull MeterRegistry meterRegistry) {
        this.indexingPool = indexingPool;
        this.logsStorage = logsStorage;
        this.indexedRecordsCounter = meterRegistry.counter("logs.indexed.records", "description", "All logs indexed records count");
        this.indexRequestsCounter = meterRegistry.counter("logs.index.requests", "description", "All logs index requests to search engine count");
    }

    @Nonnull
    public final CompletableFuture<Void> index(@NonNull Collection<LogRecordEntity> records) {
        return this.execute(() -> {
            this.logsStorage.saveAll(records);
            this.indexedRecordsCounter.increment(records.size());
            this.indexRequestsCounter.increment();
        });
    }

    private CompletableFuture<Void> execute(@NonNull Runnable task) {
        return CompletableFuture.runAsync(task, this.indexingPool);
    }
}
