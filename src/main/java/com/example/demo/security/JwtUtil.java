package com.example.demo.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;


    @Value("${jwt.access-token.expiration}")
    private  long accessTokenExpiration; // 1 day in ms

    private Key getSignKey() {
        
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(String str) {
        return Jwts.builder()
                .setSubject(str)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + (accessTokenExpiration)))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmail(String token) {
        {
           
            return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
               
        }
        
        
                
               
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    
}
