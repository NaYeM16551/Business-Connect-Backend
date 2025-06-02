package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory; 
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * RedisConfig sets up everything Spring needs to connect to Redis (or Valkey on Fedora)
 * using the Lettuce client.  We provide:
 *
 * 1) a RedisConnectionFactory (LettuceConnectionFactory) that reads host/port from application.properties
 * 2) a RedisTemplate<String,Object> for storing arbitrary Objects as JSON
 * 3) a StringRedisTemplate for simple String-based operations (e.g. ZSET, GET, SET)
 *
 * Note: application.properties must include at minimum:
 *    spring.redis.host=localhost
 *    spring.redis.port=6379
 * and (optionally) pool and timeout settings.
 */
@Configuration
public class RedisConfig {

    /**
     * Configure LettuceConnectionFactory.
     *
     * By default, LettuceConnectionFactory() will read:
     *   spring.redis.host
     *   spring.redis.port
     * from application.properties.  On Fedora, that points to Valkey (or redis-server),
     * listening on localhost:6379.
     *
     * If you later switch to a remote Redis host, you can override properties without touching this class.
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // No arguments → Lettuce picks up spring.redis.host & spring.redis.port
        return new LettuceConnectionFactory();
    }

    /**
     * A generic RedisTemplate for storing arbitrary Java objects as JSON.
     *
     * Key type:   String (serialized via StringRedisSerializer)
     * Value type: Object (serialized via GenericJackson2JsonRedisSerializer → JSON)
     * Hash Key:   String (StringRedisSerializer)
     * Hash Value: Object (GenericJackson2JsonRedisSerializer → JSON)
     *
     * Use this whenever you want to save a Java object or a Map<String,Object> as a Redis Hash or JSON blob.
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Use UTF-8 strings for normal keys
        template.setKeySerializer(new StringRedisSerializer());
        // Serialize values as JSON
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        // For Hash operations (opsForHash()), serialize hash keys as strings
        template.setHashKeySerializer(new StringRedisSerializer());
        // Serialize hash values as JSON
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        // After setting serializers, initialize the template
        template.afterPropertiesSet();
        return template;
    }

    /**
     * A convenience StringRedisTemplate for purely String-based operations.
     *
     * Key type:   String (StringRedisSerializer)
     * Value type: String (StringRedisSerializer)
     *
     * This is useful for:
     *  - ZADD, ZREVRANGE, ZSCORE on sorted sets (keys & members are strings)
     *  - Simple GET/SET of string values
     *  - Caching JSON strings directly without additional serializers
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        // StringRedisTemplate auto-configures both key and value serializers as StringRedisSerializer
        return new StringRedisTemplate(connectionFactory);
    }
}
