package org.analyzer.logs.service.std;

import lombok.NonNull;
import org.analyzer.logs.dao.UserRepository;
import org.analyzer.logs.model.UserEntity;
import org.analyzer.logs.service.UserService;
import org.analyzer.logs.service.exceptions.UserAlreadyDisabledException;
import org.analyzer.logs.service.exceptions.UserAlreadyExistsException;
import org.analyzer.logs.service.exceptions.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class DefaultUserService implements UserService {

    private static final String USERS_CACHE = "users";

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ReactiveRedisTemplate<String, Object> redisTemplate;

    @NonNull
    @Override
    public Mono<UserEntity> create(@NonNull Mono<UserEntity> newUser) {
        return newUser
                .flatMap(user ->
                        this.userRepository
                                .existsById(user.getUsername())
                                    .filter(exists -> !exists)
                                    .flatMap(exists -> this.userRepository.save(user))
                                    .switchIfEmpty(Mono.error(() -> new UserAlreadyExistsException(user.getUsername())))
                ).flatMap(user ->
                        this.redisTemplate.opsForValue()
                                            .set(createUserRedisKey(user.getUsername()), user)
                                            .thenReturn(user)
                );
    }

    @NonNull
    @Override
    public Mono<Void> disable(@NonNull String username) {
        return this.userRepository.findById(username)
                                    .switchIfEmpty(Mono.error(() -> new UserNotFoundException(username)))
                                    .filter(UserEntity::disable)
                                    .flatMap(this.userRepository::save)
                                    .switchIfEmpty(Mono.error(() -> new UserAlreadyDisabledException(username)))
                                    .flatMap(user ->
                                            this.redisTemplate.opsForValue().delete(createUserRedisKey(username))
                                                    .and(this.redisTemplate.opsForValue().delete(createUserRedisKey(user.getHash()))))
                                    .then();
    }

    @NonNull
    @Override
    public Mono<UserEntity> update(@NonNull Mono<UserEntity> userToSave) {
        return userToSave
                .flatMap(user ->
                        this.userRepository
                                .existsById(user.getUsername())
                                .filter(exists -> exists)
                                .flatMap(exists -> this.userRepository.save(user))
                                .switchIfEmpty(Mono.error(() -> new UserNotFoundException(user.getUsername())))
                )
                .flatMap(user ->
                        this.redisTemplate.opsForValue().set(createUserRedisKey(user.getUsername()), user)
                                .and(this.redisTemplate.opsForValue().set(createUserRedisKey(user.getHash()), user))
                                .thenReturn(user)
                );
    }

    @NonNull
    @Override
    public Flux<UserEntity> findAllWithClearingSettings() {
        return this.userRepository.findAllWithClearingSettings();
    }

    @NonNull
    @Override
    public Flux<UserEntity> findAllWithScheduledIndexingSettings(@NonNull final LocalDateTime modifiedAfter) {
        return this.userRepository.findAllWithScheduledIndexingSettings(modifiedAfter);
    }

    @NonNull
    @Override
    public Flux<UserEntity> findAllWithTelegramId() {
        return this.userRepository.findAllWithTelegramId();
    }

    @NonNull
    @Override
    public Mono<UserEntity> findById(@NonNull String username) {
        final var entityFromCache = this.redisTemplate.opsForValue().get(createUserRedisKey(username));
        final var entityFromStorage =
                this.userRepository.findById(username)
                                    .switchIfEmpty(Mono.error(() -> new UserNotFoundException(username)))
                                    .flatMap(user ->
                                            this.redisTemplate.opsForValue().set(createUserRedisKey(user.getUsername()), user)
                                                                            .thenReturn(user))
                                    .cache();
        return entityFromCache
                .cast(UserEntity.class)
                .switchIfEmpty(entityFromStorage);
    }

    @NonNull
    @Override
    public Mono<UserEntity> findByUserHash(@NonNull String userHash) {
        final var entityFromCache = this.redisTemplate.opsForValue().get(createUserRedisKey(userHash));
        final var entityFromStorage =
                this.userRepository.findByHash(userHash)
                                    .switchIfEmpty(Mono.error(() -> new UserNotFoundException(userHash)))
                                    .flatMap(user ->
                                            this.redisTemplate.opsForValue().set(createUserRedisKey(userHash), user)
                                                                            .thenReturn(user))
                                    .cache();
        return entityFromCache
                .cast(UserEntity.class)
                .switchIfEmpty(entityFromStorage);
    }

    @NonNull
    @Override
    public Mono<Long> findCount(boolean onlyActive) {
        return onlyActive
                ? this.userRepository.countByActive(true)
                : this.userRepository.count();
    }

    private String createUserRedisKey(final String key) {
        return USERS_CACHE + ":" + key;
    }
}
