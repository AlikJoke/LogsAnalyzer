package org.analyzer.logs.test.dao.events;

import org.analyzer.logs.dao.LogsStatisticsRepository;
import org.analyzer.logs.events.EntityDeletedEvent;
import org.analyzer.logs.events.EntitySavedEvent;
import org.analyzer.logs.events.StatisticsEventListener;
import org.analyzer.logs.model.LogsStatisticsEntity;
import org.analyzer.logs.test.dao.MongoRepositoryTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Objects;

import static org.analyzer.logs.test.fixtures.TestFixtures.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@DataMongoTest
@Import(StatisticsEventListener.class)
public class LogStatisticsEventListenerTest extends MongoRepositoryTest {

    @Autowired
    private LogsStatisticsRepository statisticsRepository;
    @MockBean
    private ReactiveRedisTemplate<String, Object> redisTemplate;
    @Value("${logs.analyzer.events.statistics.topic}")
    private String statsTopic;

    @Test
    public void shouldStatsSavedEventPopulateToRedis() {
        final var statsEntity = createStatisticsEntity(TEST_USER);

        when(this.redisTemplate.convertAndSend(eq(this.statsTopic), any(Object.class)))
                .thenReturn(Mono.just(1L));

        StepVerifier
                .create(this.statisticsRepository.save(statsEntity))
                .expectNextMatches(Objects::nonNull)
                .verifyComplete();

        final var captor = ArgumentCaptor.forClass(EntitySavedEvent.class);
        verify(this.redisTemplate, times(1)).convertAndSend(eq(this.statsTopic), captor.capture());

        assertEquals(captor.getAllValues().size(), 1);
        assertEquals(captor.getValue().getSourceCollection(), "statistics");

        final LogsStatisticsEntity entity = (LogsStatisticsEntity) captor.getValue().getEntity();
        assertEquals(entity.getId(), statsEntity.getId());
        assertEquals(entity.getStats(), statsEntity.getStats());
    }

    @Test
    public void shouldStatsDeletedEventPopulateToRedis() {
        final var statsEntity = createStatisticsEntity(TEST_USER);

        when(this.redisTemplate.convertAndSend(eq(this.statsTopic), any(Object.class)))
                .thenReturn(Mono.just(1L));

        StepVerifier
                .create(this.statisticsRepository.save(statsEntity)
                        .then(this.statisticsRepository.deleteById(statsEntity.getId())))
                .expectNextMatches(Objects::nonNull)
                .verifyComplete();

        final var captor = ArgumentCaptor.forClass(EntityDeletedEvent.class);
        verify(this.redisTemplate, times(2)).convertAndSend(eq(this.statsTopic), captor.capture());

        assertEquals(captor.getAllValues().size(), 2);

        // First event - saving
        final var event = captor.getAllValues().get(1);
        assertEquals(event.getSourceCollection(), "statistics");
        assertEquals(event.getEntityId(), statsEntity.getId());
    }

    @AfterEach
    public void clearData() {
        this.statisticsRepository.deleteAll().block();
    }
}