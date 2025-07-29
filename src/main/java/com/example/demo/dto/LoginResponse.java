package com.example.demo.dto;

import java.util.Map;

public class LoginResponse {
    public String message;
    public Map<String, String> user;
    public String token;

    public LoginResponse(String message, Map<String, String> user, String token) {
        this.message = message;
        this.user = user;
        this.token = token;
    }
}
