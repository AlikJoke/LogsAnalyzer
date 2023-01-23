package org.analyzer.logs.service.util;

import jakarta.annotation.PreDestroy;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Component
public class LongRunningTaskExecutor {

    private final Scheduler scheduler;

    public LongRunningTaskExecutor(@Value("${logs.task.executor.pool-size:4}") final int poolSize) {
        this.scheduler = Schedulers.newParallel("long-running-tasks-scheduler", poolSize);
        this.scheduler.init();
    }

    public void execute(@NonNull final Runnable task) {
        this.scheduler.schedule(task);
    }

    @PreDestroy
    private void destroy() {
        this.scheduler.dispose();
    }
}
