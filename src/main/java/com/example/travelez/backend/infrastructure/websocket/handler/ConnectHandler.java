package com.example.travelez.backend.infrastructure.websocket.handler;

import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.exception.ApiException;
import com.example.travelez.backend.security.component.UserPrinciple;
import com.example.travelez.backend.security.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConnectHandler implements StompCommandHandler {
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    public StompCommand getCommand() {
        return StompCommand.CONNECT;
    }

    @Override
    public void handle(StompHeaderAccessor accessor) {
        String authorizationHeader = accessor.getFirstNativeHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            String username = jwtUtil.extractUsername(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (jwtUtil.validateToken(token, userDetails)) {
                Long userId = jwtUtil.extractUserId(token);
                String role = jwtUtil.extractRole(token);
                UserPrinciple userPrinciple = new UserPrinciple(userId, username, role);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userPrinciple, null, userDetails.getAuthorities());
                accessor.setUser(authentication);
                log.info("WebSocket Connected: User {}", username);
            } else {
                throw new ApiException(ResultCode.UNAUTHORIZED, "Invalid token");
            }
        } else {
            throw new ApiException(ResultCode.UNAUTHORIZED, "Missing Authorization header");
        }
    }
}
