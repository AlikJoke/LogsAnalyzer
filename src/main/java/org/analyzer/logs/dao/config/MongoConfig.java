package org.analyzer.logs.dao.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@Configuration
@EnableReactiveMongoRepositories(basePackages = "org.analyzer.logs.dao")
public class MongoConfig {

}
