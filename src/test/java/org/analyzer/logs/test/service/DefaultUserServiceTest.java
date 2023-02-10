package org.analyzer.logs.test.service;

import org.analyzer.logs.dao.UserRepository;
import org.analyzer.logs.service.UserService;
import org.analyzer.logs.service.exceptions.UserAlreadyDisabledException;
import org.analyzer.logs.service.exceptions.UserAlreadyExistsException;
import org.analyzer.logs.service.exceptions.UserNotFoundException;
import org.analyzer.logs.service.std.DefaultUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.analyzer.logs.test.fixtures.TestFixtures.TEST_USER;
import static org.analyzer.logs.test.fixtures.TestFixtures.createUser;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@Import(DefaultUserService.class)
public class DefaultUserServiceTest {

    @Autowired
    private UserService userService;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private ReactiveRedisTemplate<String, Object> redisTemplate;

    @Test
    public void shouldNewUserBeCreated() {

        final var user = createUser(TEST_USER);

        when(this.userRepository.existsById(user.getUsername()))
                .thenReturn(Mono.just(false));
        when(this.userRepository.save(user))
                .thenReturn(Mono.just(user));

        @SuppressWarnings("unchecked")
        final ReactiveValueOperations<String, Object> opsMock = mock(ReactiveValueOperations.class);
        when(this.redisTemplate.opsForValue())
                .thenReturn(opsMock);

        when(opsMock.set(anyString(), eq(user)))
                .thenReturn(Mono.just(true));

        StepVerifier
                .create(this.userService.create(Mono.just(user)))
                .expectNext(user)
                .verifyComplete();

        verify(this.userRepository).save(user);
        verify(this.redisTemplate).opsForValue();
    }

    @Test
    public void shouldUserCreationFailWithUserAlreadyExist() {

        final var user = createUser(TEST_USER);

        when(this.userRepository.existsById(user.getUsername()))
                .thenReturn(Mono.just(true));

        StepVerifier
                .create(this.userService.create(Mono.just(user)))
                .verifyError(UserAlreadyExistsException.class);

        verify(this.userRepository, never()).save(user);
        verify(this.redisTemplate, never()).opsForValue();
    }

    @Test
    public void shouldUserBeDisabled() {

        final var user = createUser(TEST_USER);

        when(this.userRepository.findById(user.getUsername()))
                .thenReturn(Mono.just(user));
        when(this.userRepository.save(user))
                .thenReturn(Mono.just(user));

        @SuppressWarnings("unchecked")
        final ReactiveValueOperations<String, Object> opsMock = mock(ReactiveValueOperations.class);
        when(this.redisTemplate.opsForValue())
                .thenReturn(opsMock);

        when(opsMock.delete(anyString()))
                .thenReturn(Mono.just(true));

        StepVerifier
                .create(this.userService.disable(user.getUsername()))
                .expectNext()
                .verifyComplete();

        assertFalse(user.isActive());
        verify(this.userRepository).save(user);
        verify(opsMock, times(2)).delete(anyString());
    }

    @Test
    public void shouldUserNotFoundThatBeDisabled() {

        final var username = TEST_USER;
        when(this.userRepository.findById(username))
                .thenReturn(Mono.empty());

        StepVerifier
                .create(this.userService.disable(username))
                .verifyError(UserNotFoundException.class);

        verify(this.userRepository, never()).save(any());
    }

    @Test
    public void shouldUserBeAlreadyDisabledThatBeDisabled() {

        final var user = createUser(TEST_USER).setActive(false);
        when(this.userRepository.findById(user.getUsername()))
                .thenReturn(Mono.just(user));

        StepVerifier
                .create(this.userService.disable(user.getUsername()))
                .verifyError(UserAlreadyDisabledException.class);

        verify(this.userRepository, never()).save(any());
    }

    @Test
    public void shouldUserBeUpdated() {

        final var user = createUser(TEST_USER);

        when(this.userRepository.existsById(user.getUsername()))
                .thenReturn(Mono.just(true));
        when(this.userRepository.save(user))
                .thenReturn(Mono.just(user));

        @SuppressWarnings("unchecked")
        final ReactiveValueOperations<String, Object> opsMock = mock(ReactiveValueOperations.class);
        when(this.redisTemplate.opsForValue())
                .thenReturn(opsMock);

        when(opsMock.set(anyString(), eq(user)))
                .thenReturn(Mono.just(true));

        StepVerifier
                .create(this.userService.update(Mono.just(user)))
                .expectNext(user)
                .verifyComplete();

        verify(this.userRepository).save(user);
        verify(this.redisTemplate, times(2)).opsForValue();
    }

    @Test
    public void shouldUserUpdateFailWhenUserNotFound() {

        final var user = createUser(TEST_USER);

        when(this.userRepository.existsById(user.getUsername()))
                .thenReturn(Mono.just(false));

        StepVerifier
                .create(this.userService.update(Mono.just(user)))
                .verifyError(UserNotFoundException.class);

        verify(this.userRepository, never()).save(user);
        verify(this.redisTemplate, never()).opsForValue();
    }

    @Test
    public void shouldCountUsers() {

        final var allCount = 5L;
        when(this.userRepository.count())
                .thenReturn(Mono.just(allCount));

        final var activeCount = 4L;
        when(this.userRepository.countByActive(true))
                .thenReturn(Mono.just(activeCount));

        StepVerifier
                .create(this.userService.findCount(false))
                .expectNext(allCount)
                .verifyComplete();

        StepVerifier
                .create(this.userService.findCount(true))
                .expectNext(activeCount)
                .verifyComplete();
    }

    @Test
    public void shouldFindUserByIdFromStorage() {

        final var user = createUser(TEST_USER);

        @SuppressWarnings("unchecked")
        final ReactiveValueOperations<String, Object> opsMock = mock(ReactiveValueOperations.class);
        when(this.redisTemplate.opsForValue())
                .thenReturn(opsMock);
        when(this.userRepository.findById(user.getUsername()))
                .thenReturn(Mono.just(user));
        when(opsMock.get(contains(user.getUsername())))
                .thenReturn(Mono.empty());
        when(opsMock.set(contains(user.getUsername()), eq(user)))
                .thenReturn(Mono.empty());

        StepVerifier
                .create(this.userService.findById(user.getUsername()))
                .expectNext(user)
                .verifyComplete();

        verify(this.userRepository).findById(user.getUsername());
        verify(opsMock).set(contains(user.getUsername()), eq(user));
    }

    @Test
    public void shouldFindUserByIdFromCache() {

        final var user = createUser(TEST_USER);

        @SuppressWarnings("unchecked")
        final ReactiveValueOperations<String, Object> opsMock = mock(ReactiveValueOperations.class);
        when(this.redisTemplate.opsForValue())
                .thenReturn(opsMock);

        when(opsMock.get(contains(user.getUsername())))
                .thenReturn(Mono.just(user));
        when(this.userRepository.findById(user.getUsername()))
                .thenReturn(Mono.just(user));

        StepVerifier
                .create(this.userService.findById(user.getUsername()))
                .expectNext(user)
                .verifyComplete();

        verify(opsMock, never()).set(contains(user.getUsername()), eq(user));
        verify(this.redisTemplate, times(1)).opsForValue();
    }

    @Test
    public void shouldFindUserByIdFail() {

        final var user = createUser(TEST_USER);

        @SuppressWarnings("unchecked")
        final ReactiveValueOperations<String, Object> opsMock = mock(ReactiveValueOperations.class);
        when(this.redisTemplate.opsForValue())
                .thenReturn(opsMock);

        when(opsMock.get(contains(user.getUsername())))
                .thenReturn(Mono.empty());
        when(this.userRepository.findById(user.getUsername()))
                .thenReturn(Mono.empty());
        when(opsMock.set(contains(user.getUsername()), eq(user)))
                .thenReturn(Mono.empty());

        StepVerifier
                .create(this.userService.findById(user.getUsername()))
                .verifyError(UserNotFoundException.class);

        verify(this.userRepository).findById(user.getUsername());
        verify(opsMock, never()).set(any(), any());
    }

    @Test
    public void shouldFindUserByHashFromStorage() {

        final var user = createUser(TEST_USER);

        @SuppressWarnings("unchecked")
        final ReactiveValueOperations<String, Object> opsMock = mock(ReactiveValueOperations.class);
        when(this.redisTemplate.opsForValue())
                .thenReturn(opsMock);
        when(this.userRepository.findByHash(user.getHash()))
                .thenReturn(Mono.just(user));
        when(opsMock.set(contains(user.getHash()), eq(user)))
                .thenReturn(Mono.empty());
        when(opsMock.get(contains(user.getHash())))
                .thenReturn(Mono.empty());

        StepVerifier
                .create(this.userService.findByUserHash(user.getHash()))
                .expectNext(user)
                .verifyComplete();

        verify(this.userRepository).findByHash(user.getHash());
        verify(opsMock).set(contains(user.getHash()), eq(user));
    }

    @Test
    public void shouldFindUserByHashFromCache() {

        final var user = createUser(TEST_USER);

        @SuppressWarnings("unchecked")
        final ReactiveValueOperations<String, Object> opsMock = mock(ReactiveValueOperations.class);
        when(this.redisTemplate.opsForValue())
                .thenReturn(opsMock);

        when(opsMock.get(contains(user.getHash())))
                .thenReturn(Mono.just(user));
        when(this.userRepository.findByHash(user.getHash()))
                .thenReturn(Mono.just(user));

        StepVerifier
                .create(this.userService.findByUserHash(user.getHash()))
                .expectNext(user)
                .verifyComplete();

        verify(opsMock, never()).set(contains(user.getHash()), eq(user));
        verify(this.redisTemplate, times(1)).opsForValue();
    }

    @Test
    public void shouldFindUserByHashFail() {

        final var user = createUser(TEST_USER);

        @SuppressWarnings("unchecked")
        final ReactiveValueOperations<String, Object> opsMock = mock(ReactiveValueOperations.class);
        when(this.redisTemplate.opsForValue())
                .thenReturn(opsMock);
        when(this.userRepository.findByHash(user.getHash()))
                .thenReturn(Mono.empty());
        when(opsMock.get(contains(user.getHash())))
                .thenReturn(Mono.empty());
        when(opsMock.set(contains(user.getHash()), eq(user)))
                .thenReturn(Mono.empty());

        StepVerifier
                .create(this.userService.findByUserHash(user.getHash()))
                .verifyError(UserNotFoundException.class);

        verify(this.userRepository).findByHash(user.getHash());
        verify(opsMock, never()).set(any(), any());
    }
}
