package org.parser.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LogsParserApplication {

	public static void main(String[] args) {
		SpringApplication.run(LogsParserApplication.class, args).registerShutdownHook();
	}

}
