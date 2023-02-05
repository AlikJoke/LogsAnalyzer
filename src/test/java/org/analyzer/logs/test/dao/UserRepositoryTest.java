package org.analyzer.logs.test.dao;

import org.analyzer.logs.dao.UserRepository;
import org.analyzer.logs.model.UserEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.function.Predicate;

import static org.analyzer.logs.test.fixtures.TestFixtures.TEST_USER;
import static org.analyzer.logs.test.fixtures.TestFixtures.createUser;

@ExtendWith(SpringExtension.class)
@DataMongoTest
public class UserRepositoryTest extends MongoRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void shouldNewUserWillBeCreated() {
        final var userEntity = createUser(TEST_USER);
        StepVerifier
                .create(this.userRepository.save(userEntity)
                                            .then(this.userRepository.findById(TEST_USER))
                )
                .expectNextMatches(usersEquals(userEntity))
                .verifyComplete();
    }

    @Test
    public void shouldFindUsersWithTelegramId() {
        final var userEntity = createUser(TEST_USER);
        StepVerifier
                .create(this.userRepository.save(userEntity)
                        .thenMany(this.userRepository.findAllWithTelegramId())
                )
                .expectNextMatches(usersEquals(userEntity))
                .verifyComplete();
    }

    @Test
    public void shouldFindUsersWithClearingSettings() {
        final var userEntity1 = createUser(TEST_USER + 1).setSettings(null);
        final var userEntity2 = createUser(TEST_USER + 2);

        final var userEntity3 = createUser(TEST_USER + 3);
        userEntity3.getSettings().setCleaningInterval(0);

        StepVerifier
                .create(this.userRepository.save(userEntity1)
                        .and(this.userRepository.save(userEntity2))
                        .and(this.userRepository.save(userEntity3))
                        .thenMany(this.userRepository.findAllWithClearingSettings())
                )
                .expectNextMatches(usersEquals(userEntity2))
                .verifyComplete();
    }

    @Test
    public void shouldFindUsersWithScheduledIndexingSettings() {
        final var userEntity1 = createUser(TEST_USER + 1)
                                    .setSettings(null)
                                    .setModified(LocalDateTime.now().minusDays(1));
        final var userEntity2 = createUser(TEST_USER + 2)
                                .setModified(LocalDateTime.now());

        final var userEntity3 = createUser(TEST_USER + 3).setActive(false);
        final var userEntity4 = createUser(TEST_USER + 4);
        final var userEntity5 = createUser(TEST_USER + 5).setModified(LocalDateTime.now().minusDays(3));

        StepVerifier
                .create(this.userRepository.save(userEntity1)
                        .and(this.userRepository.save(userEntity2))
                        .and(this.userRepository.save(userEntity3))
                        .and(this.userRepository.save(userEntity4))
                        .and(this.userRepository.save(userEntity5))
                        .thenMany(this.userRepository.findAllWithScheduledIndexingSettings(LocalDateTime.now().minusDays(2)))
                )
                .expectNextMatches(usersEquals(userEntity2))
                .expectNextMatches(usersEquals(userEntity4))
                .verifyComplete();
    }

    @Test
    public void shouldUpdatesWillBeSaved() {
        final var userEntity = createUser(TEST_USER);
        final var updatedUserEntity = new UserEntity()
                                            .setActive(false)
                                            .setHash(userEntity.getHash())
                                            .setUsername(userEntity.getUsername())
                                            .setModified(userEntity.getModified())
                                            .setEncodedPassword(userEntity.getEncodedPassword())
                                            .setSettings(userEntity.getSettings());
        final var updatedEntityMono =
                this.userRepository.save(userEntity)
                                    .then(this.userRepository.findById(TEST_USER))
                                    .doOnNext(user -> user.setActive(false))
                                    .flatMap(this.userRepository::save)
                                    .then(this.userRepository.findById(TEST_USER));
        StepVerifier
                .create(updatedEntityMono)
                .expectNextMatches(usersEquals(updatedUserEntity))
                .verifyComplete();
    }

    @Test
    public void shouldFindCreatedUsersByNameAndHash() {
        final var testUsername1 = TEST_USER + 1;
        final var testUsername2 = TEST_USER + 2;
        final var userEntity1 = createUser(testUsername1);
        final var userEntity2 = createUser(testUsername2);

        final var savedEntitiesFlux =
                this.userRepository.save(userEntity1)
                                    .and(this.userRepository.save(userEntity2))
                                    .thenMany(this.userRepository.findById(testUsername1)
                                                                    .concatWith(this.userRepository.findById(testUsername2)
                                                                            .concatWith(this.userRepository.findByHash(userEntity1.getHash())))
                                    );

        StepVerifier
                .create(savedEntitiesFlux)
                .expectNextMatches(usersEquals(userEntity1))
                .expectNextMatches(usersEquals(userEntity2))
                .expectNextMatches(usersEquals(userEntity1))
                .verifyComplete();
    }

    @Test
    public void shouldCountUsers() {
        final var testUsername1 = TEST_USER + 1;
        final var testUsername2 = TEST_USER + 2;
        final var testUsername3 = TEST_USER + 3;

        final var userEntity1 = createUser(testUsername1);
        final var userEntity2 = createUser(testUsername2).setActive(false);
        final var userEntity3 = createUser(testUsername3);

        final var countByActiveFlux =
                this.userRepository.save(userEntity1)
                        .and(this.userRepository.save(userEntity2))
                        .and(this.userRepository.save(userEntity3))
                        .thenMany(this.userRepository.countByActive(false)
                                        .concatWith(this.userRepository.countByActive(true))
                        );

        StepVerifier
                .create(countByActiveFlux)
                .expectNext(1L)
                .expectNext(2L)
                .verifyComplete();
    }

    @AfterEach
    public void clearData() {
        this.userRepository.deleteAll().block();
    }

    private Predicate<UserEntity> usersEquals(final UserEntity expected) {
        return actual
                -> expected.getHash().equals(actual.getHash())
                && expected.getUsername().equals(actual.getUsername())
                && expected.isActive() == actual.isActive()
                && expected.getEncodedPassword().equals(actual.getEncodedPassword())
                && expected.getSettings().equals(actual.getSettings());
    }
}
