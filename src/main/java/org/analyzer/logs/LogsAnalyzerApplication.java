package org.analyzer.logs;

import org.analyzer.logs.config.caching.CacheAutoConfiguration;
import org.analyzer.logs.config.events.EventAutoConfiguration;
import org.analyzer.logs.config.redis.ConditionalRedisAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import reactor.util.Loggers;

@SpringBootApplication(exclude = RedisAutoConfiguration.class)
@ImportAutoConfiguration({ ConditionalRedisAutoConfiguration.class, EventAutoConfiguration.class, CacheAutoConfiguration.class })
public class LogsAnalyzerApplication {

	public static final String STANDALONE_MODE_PROPERTY = "logs.analyzer.standalone.mode";

	public static void main(String[] args) {
		Loggers.useSl4jLoggers();
		SpringApplication.run(LogsAnalyzerApplication.class, args).registerShutdownHook();
	}
}
