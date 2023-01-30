package org.analyzer.logs.service.std;

import lombok.NonNull;
import org.analyzer.logs.dao.UserRepository;
import org.analyzer.logs.model.UserEntity;
import org.analyzer.logs.service.UserService;
import org.analyzer.logs.service.exceptions.UserAlreadyExistsException;
import org.analyzer.logs.service.exceptions.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class DefaultUserService implements UserService {

    @Autowired
    private UserRepository userRepository;

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
                );
    }

    @NonNull
    @Override
    public Mono<Void> disable(@NonNull String username) {
        return this.userRepository.findById(username)
                                    .filter(UserEntity::disable)
                                    .flatMap(this.userRepository::save)
                                    .switchIfEmpty(Mono.error(() -> new UserAlreadyExistsException(username)))
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
    public Mono<UserEntity> findById(@NonNull String username) {
        return this.userRepository.findById(username)
                                    .switchIfEmpty(Mono.error(() -> new UserNotFoundException(username)));
    }

    @NonNull
    @Override
    public Mono<UserEntity> findByUserHash(@NonNull String userHash) {
        return this.userRepository.findByHash(userHash)
                                    .switchIfEmpty(Mono.error(() -> new UserNotFoundException(userHash)));
    }

    @NonNull
    @Override
    public Mono<Long> findCount(boolean onlyActive) {
        return onlyActive
                ? this.userRepository.countByActive(true)
                : this.userRepository.count();
    }
}
