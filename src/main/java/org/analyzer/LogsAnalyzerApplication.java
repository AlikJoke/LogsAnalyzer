package org.analyzer;

import org.analyzer.config.caching.CacheAutoConfiguration;
import org.analyzer.config.elastic.ConditionalElasticAutoConfiguration;
import org.analyzer.config.events.EventAutoConfiguration;
import org.analyzer.config.lucene.ConditionalLuceneAutoConfiguration;
import org.analyzer.config.redis.ConditionalRedisAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;

@SpringBootApplication(exclude = RedisAutoConfiguration.class)
@ImportAutoConfiguration({
		ConditionalLuceneAutoConfiguration.class,
		ConditionalElasticAutoConfiguration.class,
		ConditionalRedisAutoConfiguration.class,
		EventAutoConfiguration.class,
		CacheAutoConfiguration.class
})
public class LogsAnalyzerApplication {

	public static final String MASTER_NODE_PROPERTY = "logs.analyzer.master.node";
	public static final String RUN_MODE_PROPERTY = "logs.analyzer.run.mode";

	public static final String BOX_MODE = "box";
	public static final String DISTRIBUTED_MODE = "distributed";

	public static void main(String[] args) {
		SpringApplication.run(LogsAnalyzerApplication.class, args).registerShutdownHook();
	}
}
