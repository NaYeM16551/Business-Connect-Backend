// // src/main/java/com/example/demo/config/JwtHandshakeInterceptor.java

package com.example.demo.config;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.example.demo.security.JwtService;

// import jakarta.servlet.http.HttpServletRequest;

// @Component
// public class JwtHandshakeInterceptor implements HandshakeInterceptor {

//     @Autowired
//     private JwtService jwtService; // Your service that can decode/validate tokens

//     @Override
//     public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
//                                    WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
//         if (request instanceof ServletServerHttpRequest servletRequest) {
//             HttpServletRequest req = servletRequest.getServletRequest();
//             String token = req.getParameter("token");

//             if (token != null && jwtService.validateToken(token)) {
//                 String username = jwtService.extractUsername(token);
//                 attributes.put("username", username); // Pass into WebSocket session
//                 return true;
//             }
//         }

//         response.setStatusCode(HttpStatus.UNAUTHORIZED);
//         return false;
//     }

//     @Override
//     public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
//                                WebSocketHandler wsHandler, Exception exception) {}
// }

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

// public class JwtHandshakeInterceptor extends HttpSessionHandshakeInterceptor {

//     private final JwtService jwtService;

//     public JwtHandshakeInterceptor(JwtService jwtService) {
//         this.jwtService = jwtService;
//     }

//     @Override
//     public boolean beforeHandshake(
//             ServerHttpRequest request,
//             org.springframework.http.server.ServerHttpResponse response,
//             org.springframework.web.socket.WebSocketHandler wsHandler,
//             Map<String, Object> attributes
//     ) throws Exception {

//         if (request instanceof ServletServerHttpRequest servletRequest) {
//             HttpServletRequest httpRequest = servletRequest.getServletRequest();

//             String token = httpRequest.getParameter("token"); // ✅ GET token from URL

//             System.out.println("Token from query: " + token);

//             if (token != null && jwtService.validateToken(token)) {
//                 String username = jwtService.extractUsername(token);
//                 attributes.put("username", username); // ✅ Set for use in WebSocket session
//             }
//         }

//         return super.beforeHandshake(request, response, wsHandler, attributes);
//     }
// }

public class JwtHandshakeInterceptor extends HttpSessionHandshakeInterceptor {

    private final JwtService jwtService;

    public JwtHandshakeInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
    if (request instanceof ServletServerHttpRequest servletRequest) {
        HttpServletRequest httpRequest = servletRequest.getServletRequest();
        String path = httpRequest.getRequestURI();

        // Skip /ws/info requests (SockJS internal)
        if (path.contains("/ws/info")) {
            return true;
        }

        System.out.println(">>> Actual WebSocket handshake request");
        System.out.println("Request URI: " + path);
        System.out.println("Query string: " + httpRequest.getQueryString());

        String token = httpRequest.getParameter("token");
        System.out.println("Extracted token: " + token);
    }

    return true;
}

}
