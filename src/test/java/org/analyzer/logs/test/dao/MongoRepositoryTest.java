package org.analyzer.logs.test.dao;

import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.distribution.Version;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;

public abstract class MongoRepositoryTest {

    @BeforeAll
    public static void beforeAll() throws IOException {
        final var mongodConfig = MongodConfig
                                    .builder()
                                        .version(Version.Main.V5_0)
                                        .stopTimeoutInMillis(5_000)
                                    .build();

        final var starter = MongodStarter.getDefaultInstance();
        starter.prepare(mongodConfig).start();
    }
}
