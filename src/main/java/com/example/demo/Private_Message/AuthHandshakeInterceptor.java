package com.example.demo.Private_Message;

import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import jakarta.servlet.http.HttpServletRequest;

// @Component
// public class AuthHandshakeInterceptor implements HandshakeInterceptor {

//     @Override
//     public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
//                                    WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
//         if (request instanceof ServletServerHttpRequest servletRequest) {
//             HttpServletRequest httpRequest = servletRequest.getServletRequest();
//             String path = httpRequest.getRequestURI();

//             if (path.contains("/ws/info")) {
//                 return true;
//             }

//             System.out.println(">>> Actual WebSocket handshake request");
//             System.out.println("Request URI: " + path);
//             System.out.println("Query string: " + httpRequest.getQueryString());

//             String token = httpRequest.getParameter("token");
//             System.out.println("Extracted token: " + token);
//         }

//         return true;
//     }

//     @Override
//     public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
//                                WebSocketHandler wsHandler, Exception exception) {}
// }

@Component
public class AuthHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {

        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpRequest = servletRequest.getServletRequest();
            String path = httpRequest.getRequestURI();

            if (path.contains("/ws/info")) {
                return true;
            }

            String token = httpRequest.getParameter("token");
            System.out.println(">>> WebSocket Handshake");
            System.out.println("Request path: " + path);
            System.out.println("Token from query param: " + token);

            // Optional: Validate token here
            if (token != null && !token.isEmpty()) {
                // attributes.put("userId", extractedUserIdFromToken);
                return true;
            }
        }

        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        // Do nothing
    }
}
