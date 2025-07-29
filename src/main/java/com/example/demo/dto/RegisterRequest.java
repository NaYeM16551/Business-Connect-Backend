package com.example.demo.dto;

import java.util.List;

public class RegisterRequest {
    public String username;
    public String email;
    public String password;
    public List<String> industry; // optional
    public List<String> interests;
    public List<String> achievements; // optional
    public String role; // new field for user role
}
