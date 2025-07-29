package com.example.demo.dto;

import java.util.Map;

public class RegisterResponse {
    private String message;
    private Map<String, Object> data;
    

    public RegisterResponse(String message, Map<String, Object> data) {
        this.message = message;
        this.data = data;
        
    }

    // Getters and setters (or use Lombok @Data)
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Map<String, Object> getData() { return data; }
    public void setData(Map<String, Object> data) { this.data = data; }

    
}