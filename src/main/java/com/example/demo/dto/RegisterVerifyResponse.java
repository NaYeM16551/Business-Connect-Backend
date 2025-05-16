package com.example.demo.dto;


public class RegisterVerifyResponse {
    private String message;
    private String token;

    public RegisterVerifyResponse(String message, String token) {
        this.message = message;
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public String getMessage() {
        return message;
    }
}
