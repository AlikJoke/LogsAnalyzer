package org.analyzer.logs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import reactor.util.Loggers;

@SpringBootApplication
@EnableScheduling
public class LogsAnalyzerApplication {

	public static void main(String[] args) {
		Loggers.useSl4jLoggers();
		SpringApplication.run(LogsAnalyzerApplication.class, args).registerShutdownHook();
	}

}
