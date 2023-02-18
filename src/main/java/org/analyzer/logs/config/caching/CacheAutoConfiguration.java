package org.analyzer.logs.config.caching;

import org.analyzer.logs.config.redis.ConditionalRedisAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.cache.CacheKeyPrefix;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@AutoConfiguration
@AutoConfigureAfter(ConditionalRedisAutoConfiguration.class)
@EnableCaching(proxyTargetClass = true)
public class CacheAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(RedisConnectionFactory.class)
    public CacheManager cacheManagerLocal() {
        return new ConcurrentMapCacheManager();
    }

    @Bean
    @ConditionalOnBean(RedisConnectionFactory.class)
    public CacheManager cacheManagerRedis(
            RedisConnectionFactory factory,
            RedisCacheConfiguration cacheConfiguration) {
        return RedisCacheManager.builder(factory)
                                    .cacheDefaults(cacheConfiguration)
                                .build();
    }

    @Bean
    @ConditionalOnBean(RedisConnectionFactory.class)
    public RedisCacheConfiguration redisCacheConfiguration(GenericJackson2JsonRedisSerializer redisValueSerializer) {
        return RedisCacheConfiguration
                .defaultCacheConfig()
                .computePrefixWith(CacheKeyPrefix.simple())
                .entryTtl(Duration.ZERO)
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(redisValueSerializer));
    }
}
