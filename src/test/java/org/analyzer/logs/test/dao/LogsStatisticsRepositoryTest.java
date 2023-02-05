package org.analyzer.logs.test.dao;

import org.analyzer.logs.dao.LogsStatisticsRepository;
import org.analyzer.logs.model.LogsStatisticsEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Predicate;

import static org.analyzer.logs.test.fixtures.TestFixtures.createStatisticsEntity;

@ExtendWith(SpringExtension.class)
@DataMongoTest
public class LogsStatisticsRepositoryTest extends MongoRepositoryTest {

    @Autowired
    private LogsStatisticsRepository statisticsRepository;

    @Test
    public void shouldNewStatsWillBeCreatedAndFoundedByQueries() {
        final var stats = createStatisticsEntity(UUID.randomUUID().toString());
        StepVerifier
                .create(this.statisticsRepository.save(stats)
                        .thenMany(this.statisticsRepository.findById(stats.getId())
                                    .concatWith(this.statisticsRepository.findByDataQueryRegexOrId(stats.getId(), stats.getId()))
                                    .concatWith(this.statisticsRepository.findByDataQueryRegexOrId("ERROR", "ERROR"))
                                    .concatWith(this.statisticsRepository.findAllByUserKeyAndCreationDateBefore(stats.getUserKey(), LocalDateTime.now()))
                        )
                )
                .expectNextMatches(statsEquals(stats))
                .expectNextMatches(statsEquals(stats))
                .expectNextMatches(statsEquals(stats))
                .expectNextMatches(statsEquals(stats))
                .verifyComplete();
    }

    @Test
    public void shouldDeleteStatsByUserAndCreatedDate() {
        final var userKey = UUID.randomUUID().toString();
        final var stats1 = createStatisticsEntity(userKey);
        final var stats2 = createStatisticsEntity(userKey).setCreated(LocalDateTime.now().minusDays(2));
        final var stats3 = createStatisticsEntity(userKey).setCreated(LocalDateTime.now().minusDays(3));

        StepVerifier
                .create(this.statisticsRepository.save(stats1)
                        .and(this.statisticsRepository.save(stats2))
                        .and(this.statisticsRepository.save(stats3))
                        .thenMany(this.statisticsRepository.count())
                )
                .expectNext(3L)
                .verifyComplete();

        StepVerifier
                .create(this.statisticsRepository.deleteAllByUserKeyAndCreationDateBefore(userKey, LocalDateTime.now().minusDays(2)))
                .expectNextMatches(statsEquals(stats3).or(statsEquals(stats2)))
                .expectNextMatches(statsEquals(stats3).or(statsEquals(stats2)))
                .verifyComplete();

        StepVerifier
                .create(this.statisticsRepository.findAll())
                .expectNextMatches(statsEquals(stats1))
                .verifyComplete();
    }

    @AfterEach
    public void clearData() {
        this.statisticsRepository.deleteAll().block();
    }

    private Predicate<LogsStatisticsEntity> statsEquals(final LogsStatisticsEntity expected) {
        return actual
                -> expected.getId().equals(actual.getId())
                && expected.getDataQuery().equals(actual.getDataQuery())
                && expected.getUserKey().equals(actual.getUserKey())
                && expected.getStats().equals(actual.getStats());
    }
}
