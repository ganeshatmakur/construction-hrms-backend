package com.hrms.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

 
@Slf4j
@Configuration
@RequiredArgsConstructor
public class RedisConfig extends CachingConfigurerSupport {

    /**
     * Configures RedisTemplate for string operations.
     * Uses StringRedisSerializer for human-readable cache inspection.
     * 
     * @param connectionFactory Redis connection factory
     * @return configured RedisTemplate
     */
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        log.info("[v0] Configuring RedisTemplate");
        
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Use string serialization for keys and values
        StringRedisSerializer serializer = new StringRedisSerializer();
        template.setKeySerializer(serializer);
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(serializer);
        template.setHashValueSerializer(serializer);
        
        template.afterPropertiesSet();
        return template;
    }

    /**
     * Custom cache error handler for graceful degradation.
     * 
     * Ticket LF-202 Fix:
     * When Redis operations fail:
     * 1. Log the error for monitoring
     * 2. Continue execution without throwing exception
     * 3. Return empty/default values from cache layer
     * 4. Services can optionally implement fallback logic
     * 
     * This prevents complete application failure due to Redis unavailability.
     * 
     * @return CacheErrorHandler
     */
    @Bean
    public CacheErrorHandler cacheErrorHandler() {
        log.info("[v0] Registering custom cache error handler");
        
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, org.springframework.cache.Cache cache, Object key) {
                log.warn("[v0] Cache GET error: cache={}, key={}, error={}", 
                    cache.getName(), key, exception.getMessage());
                // Don't throw - allow fallback
            }

            @Override
            public void handleCachePutError(RuntimeException exception, org.springframework.cache.Cache cache, Object key, Object value) {
                log.warn("[v0] Cache PUT error: cache={}, key={}, error={}", 
                    cache.getName(), key, exception.getMessage());
                // Don't throw - cache miss is not critical
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, org.springframework.cache.Cache cache, Object key) {
                log.warn("[v0] Cache EVICT error: cache={}, key={}, error={}", 
                    cache.getName(), key, exception.getMessage());
                // Don't throw - cache cleanup failure is not critical
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, org.springframework.cache.Cache cache) {
                log.warn("[v0] Cache CLEAR error: cache={}, error={}", 
                    cache.getName(), exception.getMessage());
                // Don't throw - cache clear failure is not critical
            }
        };
    }

    /**
     * Override default cache error handler to use our custom implementation
     */
    @Override
    public CacheErrorHandler errorHandler() {
        return cacheErrorHandler();
    }
}
