package com.example.demo.config;

import java.time.Duration;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * RedisConfig sets up everything Spring needs to connect to Redis
 * using the Lettuce client with robust connection pooling and timeout
 * configurations.
 * 
 * This configuration is optimized for Azure VM deployment with proper
 * resilience settings.
 */
@Configuration
public class RedisConfig {

    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    @Value("${spring.redis.timeout:5000}")
    private int timeout;

    @Value("${spring.redis.lettuce.pool.max-active:8}")
    private int maxActive;

    @Value("${spring.redis.lettuce.pool.max-idle:8}")
    private int maxIdle;

    @Value("${spring.redis.lettuce.pool.min-idle:0}")
    private int minIdle;

    @Value("${spring.redis.lettuce.pool.max-wait:2000}")
    private int maxWait;

    /**
     * Configure LettuceConnectionFactory with robust settings for Azure VM
     * deployment.
     * 
     * This configuration includes:
     * - Proper timeout settings
     * - Connection pooling for better performance
     * - Environment variable support for flexible deployment
     */
    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);

        // Configure connection pool
        GenericObjectPoolConfig<Object> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(maxActive);
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMinIdle(minIdle);
        poolConfig.setMaxWait(Duration.ofMillis(maxWait));
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setTimeBetweenEvictionRuns(Duration.ofMillis(30000));
        poolConfig.setMinEvictableIdleDuration(Duration.ofMillis(60000));
        poolConfig.setNumTestsPerEvictionRun(3);

        // Configure Lettuce client with connection pooling and timeouts
        LettuceClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
                .commandTimeout(Duration.ofMillis(timeout))
                .shutdownTimeout(Duration.ofMillis(100))
                .poolConfig(poolConfig)
                .build();

        LettuceConnectionFactory factory = new LettuceConnectionFactory(config, clientConfig);
        factory.setValidateConnection(true);
        factory.afterPropertiesSet();
        return factory;
    }

    /**
     * A generic RedisTemplate for storing arbitrary Java objects as JSON.
     *
     * Key type: String (serialized via StringRedisSerializer)
     * Value type: Object (serialized via GenericJackson2JsonRedisSerializer → JSON)
     * Hash Key: String (StringRedisSerializer)
     * Hash Value: Object (GenericJackson2JsonRedisSerializer → JSON)
     *
     * Use this whenever you want to save a Java object or a Map<String,Object> as a
     * Redis Hash or JSON blob.
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
     * Key type: String (StringRedisSerializer)
     * Value type: String (StringRedisSerializer)
     *
     * This is useful for:
     * - ZADD, ZREVRANGE, ZSCORE on sorted sets (keys & members are strings)
     * - Simple GET/SET of string values
     * - Caching JSON strings directly without additional serializers
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        // StringRedisTemplate auto-configures both key and value serializers as
        // StringRedisSerializer
        return new StringRedisTemplate(connectionFactory);
    }
}
