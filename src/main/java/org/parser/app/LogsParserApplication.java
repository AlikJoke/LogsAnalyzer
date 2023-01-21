package org.parser.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.util.Loggers;

@SpringBootApplication
public class LogsParserApplication {

	public static void main(String[] args) {
		Loggers.useSl4jLoggers();
		SpringApplication.run(LogsParserApplication.class, args).registerShutdownHook();
	}

}
