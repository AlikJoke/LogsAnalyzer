package org.analyzer.logs.management;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@Endpoint(id = "appCache")
public class ApplicationCacheManagementEndpoint {

    @Autowired
    private CacheManager cacheManager;

    @DeleteOperation
    public void clear(@Selector String cacheKey) {
        Objects.requireNonNull(cacheKey, "cacheKey");

        final var caches = "all".equals(cacheKey) ? this.cacheManager.getCacheNames() : List.of(cacheKey);
        caches.stream()
                .map(this.cacheManager::getCache)
                .filter(Objects::nonNull)
                .forEach(Cache::clear);
    }
}
