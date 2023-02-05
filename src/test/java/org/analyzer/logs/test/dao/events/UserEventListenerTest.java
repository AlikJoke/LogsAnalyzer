package org.analyzer.logs.test.dao.events;

import org.analyzer.logs.dao.UserRepository;
import org.analyzer.logs.events.EntityDeletedEvent;
import org.analyzer.logs.events.EntitySavedEvent;
import org.analyzer.logs.events.UserEventListener;
import org.analyzer.logs.model.UserEntity;
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

import static org.analyzer.logs.test.fixtures.TestFixtures.TEST_USER;
import static org.analyzer.logs.test.fixtures.TestFixtures.createUser;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@DataMongoTest
@Import(UserEventListener.class)
public class UserEventListenerTest extends MongoRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    @MockBean
    private ReactiveRedisTemplate<String, Object> redisTemplate;
    @Value("${logs.analyzer.events.users.topic}")
    private String userTopic;

    @Test
    public void shouldUserSavedEventPopulateToRedis() {
        final var userEntity = createUser(TEST_USER);

        when(this.redisTemplate.convertAndSend(eq(this.userTopic), any(Object.class)))
                .thenReturn(Mono.just(1L));

        StepVerifier
                .create(this.userRepository.save(userEntity)
                                .doOnNext(user -> user.setActive(false))
                                .flatMap(this.userRepository::save)
                )
                .expectNextMatches(Objects::nonNull)
                .verifyComplete();

        final var captor = ArgumentCaptor.forClass(EntitySavedEvent.class);
        verify(this.redisTemplate, times(2)).convertAndSend(eq(this.userTopic), captor.capture());

        assertEquals(captor.getAllValues().size(), 2);
        captor.getAllValues().forEach(event -> {

            assertEquals(event.getSourceCollection(), "users");

            final UserEntity entity = (UserEntity) event.getEntity();
            assertEquals(entity.getHash(), userEntity.getHash());
            assertEquals(entity.getUsername(), userEntity.getUsername());
        });

        final UserEntity userActiveState = (UserEntity) captor.getAllValues().get(0).getEntity();
        final UserEntity userNotActiveState = (UserEntity) captor.getAllValues().get(1).getEntity();

        assertTrue(userActiveState.isActive());
        assertFalse(userNotActiveState.isActive());
    }

    @Test
    public void shouldUserDeletedEventPopulateToRedis() {
        final var userEntity = createUser(TEST_USER);

        when(this.redisTemplate.convertAndSend(eq(this.userTopic), any(Object.class)))
                .thenReturn(Mono.just(1L));

        StepVerifier
                .create(this.userRepository.save(userEntity)
                                .then(this.userRepository.deleteById(userEntity.getUsername()))
                )
                .expectNext()
                .verifyComplete();

        final var captor = ArgumentCaptor.forClass(EntityDeletedEvent.class);
        verify(this.redisTemplate, times(2)).convertAndSend(eq(this.userTopic), captor.capture());

        // First event - saving
        final var event = captor.getAllValues().get(1);
        assertEquals(event.getSourceCollection(), "users");
        assertEquals(event.getEntityId(), userEntity.getUsername());
    }

    @AfterEach
    public void clearData() {
        this.userRepository.deleteAll().block();
    }
}
