package com.example.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(
            @org.springframework.lang.NonNull HttpServletRequest request,
            @org.springframework.lang.NonNull HttpServletResponse response,
            @org.springframework.lang.NonNull FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();
        System.out.println("Request path: " + path);


        // Skip filter for public routes
         if (path.equals("/api/v1/auth/login") || path.equals("/api/v1/auth/forgot-password") ||
                path.equals("/api/v1/auth/register-verify") || path.equals("/api/v1/auth/reset-password")
                || path.equals("/api/v1/auth/register")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        System.out.println("Authorization header: " + authHeader);
        String token = null;
        Long userID = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            System.out.println("Token: " + token);
            try {
                // wrap extraction in try/catch to catch any malformed or tampered JWT
                userID = jwtUtil.extractUserId(token);
                //System.out.println("Email in filter : " + email);
                if (userID != null
                        && SecurityContextHolder.getContext().getAuthentication() == null
                        && jwtUtil.validateToken(token)) {

                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userID, null,
                            null);
                    auth.setDetails(new WebAuthenticationDetailsSource()
                            .buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (io.jsonwebtoken.JwtException ex) {
                // Return 401 with a JSON body
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Invalid or expired token\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
