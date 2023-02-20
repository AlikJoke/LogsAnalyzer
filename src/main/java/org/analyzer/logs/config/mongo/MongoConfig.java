package org.analyzer.logs.config.mongo;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.*;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.packageresolver.Command;
import de.flapdoodle.embed.process.io.directories.UserHome;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import static org.analyzer.logs.LogsAnalyzerApplication.RUN_MODE_PROPERTY;

@Configuration
@EnableMongoRepositories(basePackages = "org.analyzer.logs.dao")
public class MongoConfig {

    @Value("${spring.data.mongodb.embedded.data.path}")
    private String embeddedDataPath;

    @Autowired
    public MongoConfig(MappingMongoConverter mongoConverter) {
        mongoConverter.setMapKeyDotReplacement("#^$");
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnProperty(name = RUN_MODE_PROPERTY, havingValue = "box")
    public MongodExecutable embeddedMongoServer(MongoProperties mongoProperties) {
        final var dataPath = new UserHome(this.embeddedDataPath);
        final var storage = new Storage(
                dataPath.asFile().getAbsolutePath(), null, 0
        );

        final var cmdOptions = MongoCmdOptions
                                    .builder()
                                        .from(MongoCmdOptions.defaults())
                                        .useDefaultSyncDelay(false)
                                        .syncDelay(60)
                                    .build();
        final var mongodConfig = MongodConfig
                                    .builder()
                                        .version(Version.Main.V5_0)
                                        .stopTimeoutInMillis(10_000)
                                        .net(new Net(mongoProperties.getHost(), mongoProperties.getPort(), false))
                                        .replication(storage)
                                        .cmdOptions(cmdOptions)
                                    .build();

        final var runtimeConfig = Defaults
                                    .runtimeConfigFor(Command.MongoD)
                                        .isDaemonProcess(true)
                                    .build();
        final var starter = MongodStarter.getInstance(runtimeConfig);
        return starter.prepare(mongodConfig);
    }
}
