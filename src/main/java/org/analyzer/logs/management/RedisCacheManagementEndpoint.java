package org.analyzer.logs.management;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.data.redis.cache.CacheKeyPrefix;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

@Component
@Endpoint(id = "redisCache")
public class RedisCacheManagementEndpoint {

    @Autowired
    private RedisTemplate<String, Object> template;

    @DeleteOperation
    public void clear(@Selector String cacheKey) {
        Objects.requireNonNull(cacheKey, "cacheKey");
        final String keysPattern = switch (cacheKey) {
            case "statistics", "users" -> cacheKey + CacheKeyPrefix.SEPARATOR + "*";
            case "all" -> "*" + CacheKeyPrefix.SEPARATOR + "*";
            default -> throw new IllegalArgumentException("Unsupported cache key");
        };

        final ScanOptions scanOptions = ScanOptions
                                            .scanOptions()
                                                .match(keysPattern)
                                            .build();
        try (final var cursor = this.template.scan(scanOptions)) {
            while (cursor.hasNext()) {
                this.template.delete(cursor.next());
            }
        }
    }

    @ReadOperation
    public Map<String, Object> info(@Selector String cacheKey) {
        final String cacheKeyResult = cacheKey == null ? "all" : cacheKey;
        final String keysPattern = switch (cacheKeyResult) {
            case "statistics", "users" -> cacheKey + CacheKeyPrefix.SEPARATOR + "*";
            case "all" -> "*" + CacheKeyPrefix.SEPARATOR + "*";
            default -> throw new IllegalArgumentException("Unsupported cache key");
        };

        final ScanOptions scanOptions = ScanOptions
                                            .scanOptions()
                                                .match(keysPattern)
                                            .build();

        try (final var cursor = this.template.scan(scanOptions)) {
            return Map.of(cacheKeyResult, cursor.stream().count());
        }
    }
}
