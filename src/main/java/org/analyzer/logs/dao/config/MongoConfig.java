package org.analyzer.logs.dao.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@Configuration
@EnableReactiveMongoRepositories(basePackages = "org.analyzer.logs.dao")
public class MongoConfig {

    @Autowired
    public MongoConfig(MappingMongoConverter mongoConverter) {
        mongoConverter.setMapKeyDotReplacement("#^$");
    }
}