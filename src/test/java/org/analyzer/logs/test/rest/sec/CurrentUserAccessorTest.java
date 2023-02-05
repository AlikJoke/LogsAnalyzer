package org.analyzer.logs.test.rest.sec;

import org.analyzer.logs.model.UserEntity;
import org.analyzer.logs.sec.DefaultCurrentUserAccessor;
import org.analyzer.logs.service.CurrentUserAccessor;
import org.analyzer.logs.service.UserService;
import org.analyzer.logs.service.exceptions.UserNotFoundException;
import org.analyzer.logs.test.rest.fixtures.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

import static org.analyzer.logs.test.rest.fixtures.TestFixtures.TEST_USER;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class CurrentUserAccessorTest {

    @Autowired
    private CurrentUserAccessor userAccessor;
    @MockBean
    private UserService userService;

    @Test
    public void shouldSetUserContext() {
        final UserEntity user = TestFixtures.createUser(TEST_USER);
        final Context context = this.userAccessor.set(Mono.just(user));

        assertNotNull(context);
        assertFalse(context.isEmpty());

        final Mono<UserEntity> userEntityMono = Mono.just(1)
                                                    .flatMap(i -> this.userAccessor.get())
                                                    .contextWrite(context);
        StepVerifier
                .create(userEntityMono)
                .expectNext(user)
                .verifyComplete();
    }

    @Test
    public void shouldSetUserContextByKey() {
        final var user = TestFixtures.createUser(TEST_USER);
        when(this.userService.findByUserHash(user.getHash())).thenReturn(Mono.just(user));

        final var context = this.userAccessor.set(user.getHash());

        assertNotNull(context);
        assertFalse(context.isEmpty());

        final var userEntityMono = Mono.just(1)
                                        .flatMap(i -> this.userAccessor.get())
                                        .contextWrite(context);
        StepVerifier
                .create(userEntityMono)
                .expectNext(user)
                .verifyComplete();
    }

    @Test
    public void shouldSetUserContextByKeyFail() {
        final var user = TestFixtures.createUser(TEST_USER);
        when(this.userService.findByUserHash(user.getHash()))
                .thenReturn(Mono.error(new UserNotFoundException(TEST_USER)));

        final var context = this.userAccessor.set(user.getHash());
        assertNotNull(context);

        final var userEntityMono = Mono.just(1)
                                        .flatMap(i -> this.userAccessor.get())
                                        .contextWrite(context);
        StepVerifier
                .create(userEntityMono)
                .expectError(UserNotFoundException.class)
                .verify();
    }

    @Configuration
    static class TestContext {

        @Bean
        public CurrentUserAccessor currentUserAccessor() {
            return new DefaultCurrentUserAccessor();
        }
    }
}
