package org.analyzer.logs.service.util;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PreDestroy;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Component
public class LongRunningTaskExecutor {

    private final Scheduler scheduler;
    private final Counter longRunningTasksCounter;
    private final Timer timer;

    @Autowired
    public LongRunningTaskExecutor(
            @Value("${logs.task.executor.pool-size:4}") final int poolSize,
            @NonNull final MeterRegistry meterRegistry) {
        this.scheduler = Schedulers.newParallel("long-running-tasks-scheduler", poolSize);
        this.scheduler.init();

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
        this.scheduler.schedule(this.timer.wrap(task));
    }

    @PreDestroy
    private void destroy() {
        this.scheduler.dispose();
    }
}
