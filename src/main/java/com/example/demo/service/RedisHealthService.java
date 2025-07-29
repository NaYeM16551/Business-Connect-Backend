package com.example.demo.service;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Redis Health Service for monitoring Redis connection health
 * and providing connection retry logic for Azure VM deployment.
 */
@Service
public class RedisHealthService {

    private static final Logger logger = LoggerFactory.getLogger(RedisHealthService.class);
    private static final String HEALTH_CHECK_KEY = "redis-health-check";
    private static final String HEALTH_CHECK_VALUE = "ok";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisConnectionFactory connectionFactory;

    private volatile boolean isRedisHealthy = false;
    private volatile boolean isInitialized = false;

    /**
     * Initialize Redis health check when application starts
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeRedisHealth() {
        logger.info("Initializing Redis health check...");
        checkRedisHealth();
        isInitialized = true;
    }

    /**
     * Periodic health check every 30 seconds
     */
    @Scheduled(fixedRate = 30000)
    public void performPeriodicHealthCheck() {
        if (isInitialized) {
            checkRedisHealth();
        }
    }

    /**
     * Check Redis connection health with comprehensive error handling
     */
    public boolean checkRedisHealth() {
        try {
            // Test basic connectivity
            stringRedisTemplate.opsForValue().set(HEALTH_CHECK_KEY, HEALTH_CHECK_VALUE, 10, TimeUnit.SECONDS);
            String result = stringRedisTemplate.opsForValue().get(HEALTH_CHECK_KEY);

            if (HEALTH_CHECK_VALUE.equals(result)) {
                if (!isRedisHealthy) {
                    logger.info("✅ Redis connection restored successfully");
                    isRedisHealthy = true;
                }

                // Clean up test key
                stringRedisTemplate.delete(HEALTH_CHECK_KEY);

                // Additional test with RedisTemplate
                redisTemplate.opsForValue().set("template-test", "test-value", 10, TimeUnit.SECONDS);
                redisTemplate.delete("template-test");

                return true;
            } else {
                logRedisFailure("Health check value mismatch");
                return false;
            }

        } catch (Exception e) {
            logRedisFailure("Health check exception: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if Redis is currently healthy
     */
    public boolean isRedisHealthy() {
        return isRedisHealthy;
    }

    /**
     * Force a manual health check
     */
    public boolean forceHealthCheck() {
        logger.info("Forcing Redis health check...");
        return checkRedisHealth();
    }

    /**
     * Get Redis connection info for debugging
     */
    public String getRedisConnectionInfo() {
        try {
            return "Redis Connection Factory: " + connectionFactory.getClass().getSimpleName();
        } catch (Exception e) {
            return "Unable to get connection info: " + e.getMessage();
        }
    }

    /**
     * Test Redis with retry logic
     */
    public boolean testRedisWithRetry(int maxRetries) {
        for (int i = 0; i < maxRetries; i++) {
            if (checkRedisHealth()) {
                return true;
            }

            if (i < maxRetries - 1) {
                logger.warn("Redis health check failed, retrying in 2 seconds... (attempt {}/{})", i + 1, maxRetries);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        return false;
    }

    private void logRedisFailure(String reason) {
        if (isRedisHealthy) {
            logger.error("❌ Redis connection failed: {}", reason);
            logger.warn("Service will continue without Redis cache");
            isRedisHealthy = false;
        }
    }
}
