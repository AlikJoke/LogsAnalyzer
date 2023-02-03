package org.analyzer.logs.management;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@Component
@Endpoint(id = "redisCache")
public class RedisCacheManagementEndpoint {

    @Autowired
    private ReactiveRedisTemplate<String, Object> template;

    @DeleteOperation
    public Mono<Void> clear(@Selector String cacheKey) {
        Objects.requireNonNull(cacheKey, "cacheKey");
        final String keysPattern = switch (cacheKey) {
            case "statistics", "users" -> cacheKey + ":*";
            case "all" -> "*:*";
            default -> throw new IllegalArgumentException("Unsupported cache key");
        };

        final ScanOptions scanOptions = ScanOptions
                                            .scanOptions()
                                                .match(keysPattern)
                                            .build();
        return this.template.scan(scanOptions)
                            .transform(this.template::delete)
                            .then();
    }

    @ReadOperation
    public Mono<Map<String, Object>> info(@Selector String cacheKey) {
        final String cacheKeyResult = cacheKey == null ? "all" : cacheKey;
        final String keysPattern = switch (cacheKeyResult) {
            case "statistics", "users" -> cacheKey + ":*";
            case "all" -> "*:*";
            default -> throw new IllegalArgumentException("Unsupported cache key");
        };

        final ScanOptions scanOptions = ScanOptions
                                            .scanOptions()
                                                .match(keysPattern)
                                            .build();
        return this.template.scan(scanOptions)
                            .count()
                            .map(count -> Collections.singletonMap(cacheKeyResult, count));
    }
}
