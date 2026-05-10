package com.example.travelez.backend.security.component;

import com.example.travelez.backend.security.util.JwtUtil;
import com.example.travelez.backend.users.service.impl.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    private final CustomUserDetailsService userDetailsService;
    @Value("${jwt.tokenHeader}")
    private String tokenHeader;
    @Value("${jwt.tokenHead}")
    private String tokenHead;

    // remaining setup authentication in filter
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // get token from header 'Bearer <token>'
        String authHeader = request.getHeader(this.tokenHeader);

        try {
            if (authHeader != null && authHeader.startsWith(this.tokenHead)) {
                // get token from header '<token>'
                String authToken = authHeader.substring(this.tokenHead.length());
                // extract username from token
                String username = jwtUtil.extractUsername(authToken);
                // if username is not null and authentication is null, load user details
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // if token is valid, set authentication
                    if (jwtUtil.validateToken(authToken, username)) {
                        Long userId = jwtUtil.extractUserId(authToken);
                        String role = jwtUtil.extractRole(authToken);
                        // create user principle
                        UserPrinciple userPrinciple = new UserPrinciple(userId, username, role);
                        // get authorities
                        List<SimpleGrantedAuthority> authorities = Collections
                                .singletonList(new SimpleGrantedAuthority("ROLE_" + role));
                        // create authentication token
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userPrinciple, null, authorities);
                        // set details
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        // set authentication
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}");
        }

        // continue filter chain
        chain.doFilter(request, response);
    }
}
