package org.analyzer.logs.test.dao;

import org.analyzer.logs.dao.UserQueryRepository;
import org.analyzer.logs.model.UserSearchQueryEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Predicate;

import static org.analyzer.logs.test.fixtures.TestFixtures.createUserSearchQueryEntity;

@ExtendWith(SpringExtension.class)
@DataMongoTest
public class UserQueryRepositoryTest extends MongoRepositoryTest {

    @Autowired
    private UserQueryRepository queryRepository;

    @Test
    @Order(0)
    public void shouldNewUserQueryWillBeCreated() {
        final var query = createUserSearchQueryEntity(UUID.randomUUID().toString());
        StepVerifier
                .create(this.queryRepository.save(query)
                        .then(this.queryRepository.findById(query.getId()))
                )
                .expectNextMatches(queriesEquals(query))
                .verifyComplete();
    }

    @Test
    @Order(1)
    public void shouldQueryWillBeDeleted() {
        final var query = createUserSearchQueryEntity(UUID.randomUUID().toString());
        StepVerifier
                .create(this.queryRepository.save(query)
                        .then(this.queryRepository.deleteById(query.getId()))
                        .then(this.queryRepository.existsById(query.getId()))
                )
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    @Order(2)
    public void shouldAllUserQueriesWillBeDeleted() {
        final var userKey = UUID.randomUUID().toString();
        final var query1 = createUserSearchQueryEntity(userKey);
        final var query2 = createUserSearchQueryEntity(userKey);

        StepVerifier
                .create(this.queryRepository.save(query1)
                        .and(this.queryRepository.save(query2))
                        .thenMany(this.queryRepository.existsById(query1.getId())
                                    .concatWith(this.queryRepository.existsById(query2.getId()))
                        )
                )
                .expectNext(true, true)
                .verifyComplete();

        StepVerifier
                .create(this.queryRepository.deleteAllByUserKey(userKey)
                        .then(this.queryRepository.count()))
                .expectNext(0L)
                .verifyComplete();
    }

    @Test
    @Order(3)
    public void shouldAllUserQueriesMoreThanMaxWillBeDeleted() {
        final var userKey = UUID.randomUUID().toString();
        final var query1 = createUserSearchQueryEntity(userKey).setCreated(LocalDateTime.now().minusDays(1));
        final var query2 = createUserSearchQueryEntity(userKey).setCreated(LocalDateTime.now());

        StepVerifier
                .create(this.queryRepository.save(query1)
                        .and(this.queryRepository.save(query2))
                        .thenMany(this.queryRepository.existsById(query1.getId())
                                .concatWith(this.queryRepository.existsById(query2.getId()))
                        )
                )
                .expectNext(true, true)
                .verifyComplete();

        StepVerifier
                .create(this.queryRepository.findAllByUserKey(userKey, PageRequest.of(1, 1, Sort.Direction.DESC, "created"))
                                            .transform(this.queryRepository::deleteAll)
                                            .thenMany(this.queryRepository.findAll())
                )
                .expectNext(query2)
                .verifyComplete();
    }

    @Test
    @Order(4)
    public void shouldFindAllUserQueriesBetweenTimestamps() {
        final var userKey = UUID.randomUUID().toString();
        final var query1 = createUserSearchQueryEntity(userKey).setCreated(LocalDateTime.now().minusDays(1));
        final var query2 = createUserSearchQueryEntity(userKey).setCreated(LocalDateTime.now().minusDays(3));

        StepVerifier
                .create(this.queryRepository.save(query1)
                        .and(this.queryRepository.save(query2))
                        .thenMany(this.queryRepository.findAllByUserKeyAndCreatedBetween(userKey, LocalDateTime.now().minusDays(2), LocalDateTime.now(), Sort.unsorted()))
                )
                .expectNext(query1)
                .verifyComplete();
    }

    @AfterEach
    public void clearData() {
        this.queryRepository.deleteAll().block();
    }

    private Predicate<UserSearchQueryEntity> queriesEquals(final UserSearchQueryEntity expected) {
        return actual
                -> expected.getId().equals(actual.getId())
                && expected.getQuery().equals(actual.getQuery())
                && expected.getUserKey().equals(actual.getUserKey());
    }
}
