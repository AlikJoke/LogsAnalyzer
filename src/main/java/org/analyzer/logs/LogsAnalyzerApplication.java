package org.analyzer.logs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.util.Loggers;

@SpringBootApplication
public class LogsAnalyzerApplication {

	public static void main(String[] args) {
		Loggers.useSl4jLoggers();
		SpringApplication.run(LogsAnalyzerApplication.class, args).registerShutdownHook();
	}

}
