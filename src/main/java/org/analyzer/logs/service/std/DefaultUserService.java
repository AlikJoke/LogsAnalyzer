package org.analyzer.logs.service.std;

import lombok.NonNull;
import org.analyzer.logs.dao.UserRepository;
import org.analyzer.logs.model.UserEntity;
import org.analyzer.logs.service.UserService;
import org.analyzer.logs.service.exceptions.UserAlreadyDisabledException;
import org.analyzer.logs.service.exceptions.UserAlreadyExistsException;
import org.analyzer.logs.service.exceptions.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DefaultUserService implements UserService {

    private static final String USERS_CACHE = "users";

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @NonNull
    @Override
    public UserEntity create(@NonNull UserEntity newUser) {
        try {
            final var savedUser = this.userRepository.save(newUser);
            this.redisTemplate.opsForValue().set(createUserRedisKey(savedUser.getUsername()), savedUser);

            return savedUser;
        } catch (OptimisticLockingFailureException ex) {
            throw new UserAlreadyExistsException(newUser.getUsername());
        }
    }

    @Override
    public void disable(@NonNull String username) {
        final var user = this.userRepository.findById(username)
                                            .orElseThrow(() -> new UserNotFoundException(username));

        if (!user.isActive()) {
            throw new UserAlreadyDisabledException(username);
        }

        user.disable();
        this.userRepository.save(user);

        this.redisTemplate.delete(List.of(createUserRedisKey(username), createUserRedisKey(user.getHash())));
    }

    @Override
    @NonNull
    public UserEntity update(@NonNull UserEntity userToSave) {
        if (!this.userRepository.existsById(userToSave.getUsername())) {
            throw new UserNotFoundException(userToSave.getUsername());
        }

        final var savedUser = this.userRepository.save(userToSave);
        this.redisTemplate.opsForValue().set(createUserRedisKey(userToSave.getUsername()), savedUser);
        this.redisTemplate.opsForValue().set(createUserRedisKey(userToSave.getHash()), savedUser);

        return savedUser;
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
    public UserEntity findById(@NonNull String username) {
        final var entityFromCache = (UserEntity) this.redisTemplate.opsForValue().get(createUserRedisKey(username));
        if (entityFromCache == null) {
            return this.userRepository.findById(username)
                                        .orElseThrow(() -> new UserNotFoundException(username));
        }

        return entityFromCache;
    }

    @NonNull
    @Override
    public UserEntity findByUserHash(@NonNull String userHash) {
        final var entityFromCache = (UserEntity) this.redisTemplate.opsForValue().get(createUserRedisKey(userHash));
        if (entityFromCache == null) {
            return this.userRepository.findByHash(userHash)
                                        .orElseThrow(() -> new UserNotFoundException(userHash));
        }

        return entityFromCache;
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
