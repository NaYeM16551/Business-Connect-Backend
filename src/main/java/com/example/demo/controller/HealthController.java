package com.example.demo.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.RedisHealthService;

/**
 * Health check controller for monitoring Redis connectivity
 */
@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    @Autowired
    private RedisHealthService redisHealthService;

    /**
     * Redis health check endpoint
     */
    @GetMapping("/redis")
    public ResponseEntity<Map<String, Object>> checkRedisHealth() {
        Map<String, Object> response = new HashMap<>();

        boolean isHealthy = redisHealthService.forceHealthCheck();

        response.put("redis_healthy", isHealthy);
        response.put("redis_connection_info", redisHealthService.getRedisConnectionInfo());
        response.put("timestamp", System.currentTimeMillis());

        if (isHealthy) {
            response.put("status", "UP");
            response.put("message", "Redis connection is healthy");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "DOWN");
            response.put("message", "Redis connection is not healthy");
            return ResponseEntity.status(503).body(response);
        }
    }

    /**
     * Overall application health check
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getApplicationStatus() {
        Map<String, Object> response = new HashMap<>();

        boolean redisHealthy = redisHealthService.isRedisHealthy();

        response.put("application_status", "UP");
        response.put("redis_status", redisHealthy ? "UP" : "DOWN");
        response.put("timestamp", System.currentTimeMillis());

        // Application can still function without Redis
        return ResponseEntity.ok(response);
    }
}
