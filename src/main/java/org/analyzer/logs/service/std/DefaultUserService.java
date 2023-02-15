package org.analyzer.logs.service.std;

import lombok.NonNull;
import org.analyzer.logs.dao.UserRepository;
import org.analyzer.logs.model.UserEntity;
import org.analyzer.logs.service.UserService;
import org.analyzer.logs.service.exceptions.UserAlreadyDisabledException;
import org.analyzer.logs.service.exceptions.UserAlreadyExistsException;
import org.analyzer.logs.service.exceptions.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DefaultUserService implements UserService {

    private static final String USERS_CACHE = "users";

    @Autowired
    private UserRepository userRepository;

    @NonNull
    @Override
    @CachePut(value = USERS_CACHE, key = "#result.getUsername()")
    public UserEntity create(@NonNull UserEntity newUser) {
        try {
            return this.userRepository.save(newUser);
        } catch (OptimisticLockingFailureException ex) {
            throw new UserAlreadyExistsException(newUser.getUsername());
        }
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = USERS_CACHE, key = "#result.getUsername()"),
            @CacheEvict(value = USERS_CACHE, key = "#result.getHash()")
    })
    @NonNull
    public UserEntity disable(@NonNull String username) {
        final var user = this.userRepository.findById(username)
                                            .orElseThrow(() -> new UserNotFoundException(username));

        if (!user.isActive()) {
            throw new UserAlreadyDisabledException(username);
        }

        user.disable();
        return this.userRepository.save(user);
    }

    @Override
    @NonNull
    @Caching(put = {
            @CachePut(value = USERS_CACHE, key = "#root.args[0].getUsername()"),
            @CachePut(value = USERS_CACHE, key = "#root.args[0].getHash()")
    })
    public UserEntity update(@NonNull UserEntity userToSave) {
        if (!this.userRepository.existsById(userToSave.getUsername())) {
            throw new UserNotFoundException(userToSave.getUsername());
        }

        return this.userRepository.save(userToSave);
    }

    @NonNull
    @Override
    public List<UserEntity> findAllWithClearingSettings() {
        return this.userRepository.findAllWithClearingSettings();
    }

    @NonNull
    @Override
    public List<UserEntity> findAllWithScheduledIndexingSettings(@NonNull final LocalDateTime modifiedAfter) {
        return this.userRepository.findAllWithScheduledIndexingSettings(modifiedAfter);
    }

    @NonNull
    @Override
    public List<UserEntity> findAllWithTelegramId() {
        return this.userRepository.findAllWithTelegramId();
    }

    @NonNull
    @Override
    @Cacheable(value = USERS_CACHE, key = "#root.args[0]")
    public UserEntity findById(@NonNull String username) {
        return this.userRepository.findById(username)
                                    .orElseThrow(() -> new UserNotFoundException(username));
    }

    @NonNull
    @Override
    @Cacheable(value = USERS_CACHE, key = "#root.args[0]")
    public UserEntity findByUserHash(@NonNull String userHash) {
        return this.userRepository.findByHash(userHash)
                                    .orElseThrow(() -> new UserNotFoundException(userHash));
    }

    @Override
    public long findCount(boolean onlyActive) {
        return onlyActive
                ? this.userRepository.countByActive(true)
                : this.userRepository.count();
    }

    private String createUserRedisKey(final String key) {
        return USERS_CACHE + ":" + key;
    }
}
