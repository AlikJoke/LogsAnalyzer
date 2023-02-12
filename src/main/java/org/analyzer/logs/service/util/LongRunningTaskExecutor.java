package org.analyzer.logs.service.util;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Component
public class LongRunningTaskExecutor {

    private final ThreadPoolTaskExecutor taskExecutorDelegate;
    private final Counter longRunningTasksCounter;
    private final Timer timer;

    @Autowired
    public LongRunningTaskExecutor(
            @NonNull final ThreadPoolTaskExecutor taskExecutorDelegate,
            @NonNull final MeterRegistry meterRegistry) {
        this.taskExecutorDelegate = taskExecutorDelegate;
        this.longRunningTasksCounter = Counter.builder("long-running-tasks")
                                                .description("All scheduled long running tasks")
                                                .register(meterRegistry);
        this.timer = Timer.builder("long-running-tasks-timer")
                            .description("Long running tasks timer")
                            .publishPercentileHistogram()
                            .register(meterRegistry);
    }

    public void execute(@NonNull final Runnable task) {
        this.longRunningTasksCounter.increment();
        this.taskExecutorDelegate.submit(this.timer.wrap(task));
    }
}
