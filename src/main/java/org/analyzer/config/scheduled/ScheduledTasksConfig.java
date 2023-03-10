package org.analyzer.config.scheduled;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.NonNull;
import org.analyzer.service.scheduled.ScheduledDataCleaningTask;
import org.analyzer.service.scheduled.ScheduledDataIndexingTask;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ForkJoinPoolFactoryBean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import javax.net.ssl.SSLException;
import java.util.concurrent.ThreadPoolExecutor;

import static org.analyzer.LogsAnalyzerApplication.*;

@Configuration
@EnableScheduling
@EnableConfigurationProperties(AsyncHttpRequestProperties.class)
public class ScheduledTasksConfig implements SchedulingConfigurer {

    @Value("${logs.task.scheduled.executor.pool-size:2}")
    private int poolSize;

    @Bean
    public AsyncHttpClient asyncHttpClient(
            @NonNull final AsyncHttpRequestProperties asyncHttpRequestProperties,
            @NonNull final LogAndStopIOExceptionFilter exceptionFilter) throws SSLException {

        final var sslContext = SslContextBuilder
                                    .forClient()
                                        .trustManager(InsecureTrustManagerFactory.INSTANCE)
                                    .build();
        final var config = Dsl
                            .config()
                                .setSslContext(sslContext)
                                .setThreadFactory(Thread.ofVirtual().factory())
                                .setConnectionTtl(asyncHttpRequestProperties.getConnectionTtl())
                                .setKeepAlive(asyncHttpRequestProperties.isKeepAlive())
                                .setThreadPoolName("async-http-requests-pool")
                                .setRequestTimeout(asyncHttpRequestProperties.getRequestTimeout())
                                .setIoThreadsCount(asyncHttpRequestProperties.getIoThreads())
                                .setFollowRedirect(asyncHttpRequestProperties.isFollowRedirects())
                                .setConnectTimeout(asyncHttpRequestProperties.getConnectTimeout())
                                .addIOExceptionFilter(exceptionFilter)
                            .build();

        return Dsl.asyncHttpClient(config);
    }

    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    @Bean
    @IndexingTasksPool
    @ConditionalOnProperty(name = RUN_MODE_PROPERTY, havingValue = DISTRIBUTED_MODE, matchIfMissing = true)
    public ThreadPoolTaskExecutor indexingThreadPoolDistributed(
            @Value("${logs.analyzer.indexing.threads}") int poolSize,
            @Value("${logs.analyzer.indexing.queue:100}") int queueCapacity) {
        final var threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setMaxPoolSize(poolSize);
        threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        threadPoolTaskExecutor.setCorePoolSize(poolSize / 2);
        threadPoolTaskExecutor.setThreadNamePrefix("indexing-tasks-pool");
        threadPoolTaskExecutor.setThreadFactory(Thread.ofVirtual().factory());
        threadPoolTaskExecutor.setQueueCapacity(queueCapacity);
        threadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        return threadPoolTaskExecutor;
    }

    @Bean
    @IndexingTasksPool
    @ConditionalOnProperty(name = RUN_MODE_PROPERTY, havingValue = BOX_MODE)
    public ForkJoinPoolFactoryBean indexingThreadPoolBox(@Value("${logs.analyzer.indexing.threads}") int poolSize) {
        final var threadPoolTaskExecutor = new ForkJoinPoolFactoryBean();
        threadPoolTaskExecutor.setParallelism(poolSize);

        return threadPoolTaskExecutor;
    }

    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        final var threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(this.poolSize);
        threadPoolTaskScheduler.setThreadNamePrefix("scheduled-tasks-pool");
        threadPoolTaskScheduler.setThreadFactory(Thread.ofVirtual().factory());
        threadPoolTaskScheduler.setWaitForTasksToCompleteOnShutdown(false);
        threadPoolTaskScheduler.setDaemon(true);
        return threadPoolTaskScheduler;
    }

    @Bean
    @LongRunningTasksPool
    public ThreadPoolTaskExecutor threadPoolTaskExecutor(@Value("${logs.task.executor.pool-size:8}") final int poolSize){
        final var threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setMaxPoolSize(poolSize);
        threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        threadPoolTaskExecutor.setCorePoolSize(poolSize / 4);
        threadPoolTaskExecutor.setThreadNamePrefix("long-running-tasks-pool");
        threadPoolTaskExecutor.setThreadFactory(Thread.ofVirtual().factory());
        threadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        return threadPoolTaskExecutor;
    }

    @Bean
    @ConditionalOnProperty(name = MASTER_NODE_PROPERTY, havingValue = "true")
    public ScheduledDataCleaningTask scheduledDataCleaningTask() {
        return new ScheduledDataCleaningTask();
    }

    @Bean
    @ConditionalOnProperty(name = MASTER_NODE_PROPERTY, havingValue = "true")
    public ScheduledDataIndexingTask scheduledDataIndexingTask() {
        return new ScheduledDataIndexingTask();
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setTaskScheduler(threadPoolTaskScheduler());
    }
}
