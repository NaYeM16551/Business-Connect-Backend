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
import java.util.List;

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
            @org.springframework.lang.NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1) Print out the request path for debugging
        String path = request.getServletPath();
        System.out.println("Request path: " + path);

        // 2) Skip the JWT check for any public/auth endpoints
        if (path.equals("/api/v1/auth/login") ||
            path.equals("/api/v1/auth/forgot-password") ||
            path.equals("/api/v1/auth/register-verify") ||
            path.equals("/api/v1/auth/reset-password") ||
            path.equals("/api/v1/auth/register")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3) Read the "Authorization" header
        String authHeader = request.getHeader("Authorization");
        System.out.println("Authorization header: " + authHeader);

        // 4) If no Authorization header is present, immediately 401
        if (authHeader == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Invalid or expired token\"}");
            response.setContentType("application/json");
            return;
        }

        // 5) Must start with "Bearer "; otherwise reject
        if (!authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Invalid or expired token\"}");
            response.setContentType("application/json");
            return;
        }

        // 6) At this point, header is non-null and startsWith "Bearer "
        String token = authHeader.substring(7);
        System.out.println("Token: " + token);

        try {
            // 7) Extract the userId (or throw if invalid)
            Long userID = jwtUtil.extractUserId(token);
            System.out.println("User ID extracted from token: " + userID);

            // 8) If we have no Authentication in the SecurityContext, and token is valid, set it
            if (userID != null
                    && SecurityContextHolder.getContext().getAuthentication() == null
                    && jwtUtil.validateToken(token)) {

                // 9) Create an authenticated token with an empty authority list
                UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                        userID.toString(),
                        null,
                        List.of()  // no roles in this example, but this ensures "authenticated"
                    );

                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }

        } catch (io.jsonwebtoken.JwtException ex) {
            // 10) If token parsing/validation fails, return 401
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Invalid or expired token\"}");
            return;
        }

        // 11) Continue down the filter chain
        filterChain.doFilter(request, response);
    }
}
