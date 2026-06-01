package com.example.travelez.backend.users.service.impl;

import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.exception.ApiException;
import com.example.travelez.backend.config.GoogleOAuthConfig;
import com.example.travelez.backend.security.util.JwtUtil;
import com.example.travelez.backend.users.dto.response.UserLoginResponse;
import com.example.travelez.backend.users.model.Traveler;
import com.example.travelez.backend.users.model.User;
import com.example.travelez.backend.users.model.UserOAthToken;
import com.example.travelez.backend.users.model.enums.AuthProvider;
import com.example.travelez.backend.users.model.enums.RoleType;
import com.example.travelez.backend.users.model.enums.UserStatus;
import com.example.travelez.backend.users.repository.TokenRepository;
import com.example.travelez.backend.users.repository.UserRepository;
import com.example.travelez.backend.users.service.OAuth2Service;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OAuth2ServiceImpl implements OAuth2Service {
    private final GoogleOAuthConfig googleOAuthConfig;

    private final UserRepository userRepository;

    private final TokenRepository tokenRepository;

    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public UserLoginResponse authenticateGoogle(String code) {
        try {
            // 1. Đổi Code lấy Token từ Google
            GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(new NetHttpTransport(),
                    GsonFactory.getDefaultInstance(), "https://oauth2.googleapis.com/token",
                    googleOAuthConfig.getClientId(), googleOAuthConfig.getClientSecret(), code,
                    googleOAuthConfig.getRedirectUri()).execute();

            // 2. Lấy thông tin User từ Google
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleOAuthConfig.getClientId()))
                    .build();
            GoogleIdToken idToken = verifier.verify(tokenResponse.getIdToken());
            if (idToken == null) {
                throw new ApiException(ResultCode.VALIDATION_FAILED, "Invalid Google ID token");
            }
            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();

            // 3. Kiểm tra User có tồn tại không
            User user = userRepository.findByEmail(email).orElseGet(() -> {
                Traveler newUser = Traveler.builder()
                        .username(email)
                        .email(email)
                        .fullName((String) payload.get("name"))
                        // .avatar((String) payload.get("picture"))
                        .followerCount(0L)
                        .followingCount(0L)
                        .authProvider(AuthProvider.GOOGLE)
                        .role(RoleType.TRAVELER)
                        .status(UserStatus.ACTIVE)
                        .googleId(payload.getSubject())
                        .build();
                return userRepository.save(newUser);
            });

            // Lưu REFRESH TOKEN vào database
            UserOAthToken tokenEntity = tokenRepository.findByUserIdAndProvider(user.getId(), AuthProvider.GOOGLE)
                    .orElse(new UserOAthToken());
            tokenEntity.setUser(user);
            tokenEntity.setAccessToken(tokenResponse.getAccessToken());
            tokenEntity.setProvider(AuthProvider.GOOGLE);
            if (tokenResponse.getRefreshToken() != null) {
                tokenEntity.setRefreshToken(tokenResponse.getRefreshToken());
            }
            tokenRepository.save(tokenEntity);

            String jwt = jwtUtil.generateToken(user.getUsername(), user.getRole().name(), user.getId());
            return UserLoginResponse.builder()
                    .token(jwt)
                    .userId(user.getId())
                    .role(user.getRole())
                    .build();
        } catch (IOException | GeneralSecurityException e) {
            throw new ApiException(ResultCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    @Transactional
    public void processCalendarCallback(String code, Long userId) {
        try {
            GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance(),
                    "https://oauth2.googleapis.com/token",
                    googleOAuthConfig.getClientId(),
                    googleOAuthConfig.getClientSecret(),
                    code,
                    googleOAuthConfig.getCalendarRedirectUri()).execute();

            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleOAuthConfig.getClientId()))
                    .build();
            GoogleIdToken idToken = verifier.verify(tokenResponse.getIdToken());
            if (idToken == null) {
                throw new ApiException(ResultCode.VALIDATION_FAILED, "Invalid Google ID token");
            }
            GoogleIdToken.Payload payload = idToken.getPayload();
            String googleId = payload.getSubject();

            // Validate: googleId không được thuộc về user khác
            userRepository.findByGoogleId(googleId).ifPresent(existingUser -> {
                if (existingUser.getId() != userId) {
                    throw new ApiException(ResultCode.VALIDATION_FAILED,
                            "This Google account is already linked to another user");
                }
            });

            // Gán googleId cho user hiện tại nếu chưa có
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND, "User not found"));
            if (user.getGoogleId() == null) {
                user.setGoogleId(googleId);
                userRepository.save(user);
            }

            // Upsert token với scopes
            String grantedScopes = "openid email profile https://www.googleapis.com/auth/calendar.events";
            UserOAthToken tokenEntity = tokenRepository.findByUserIdAndProvider(userId, AuthProvider.GOOGLE)
                    .orElse(new UserOAthToken());
            boolean isNew = tokenEntity.getId() == null;
            tokenEntity.setUser(user);
            tokenEntity.setProvider(AuthProvider.GOOGLE);
            tokenEntity.setAccessToken(tokenResponse.getAccessToken());
            tokenEntity.setScopes(grantedScopes);
            if (tokenResponse.getRefreshToken() != null) {
                tokenEntity.setRefreshToken(tokenResponse.getRefreshToken());
            } else if (isNew) {
                throw new ApiException(ResultCode.VALIDATION_FAILED,
                        "Google did not return a refresh token. Please revoke access and try again.");
            }
            if (tokenResponse.getExpiresInSeconds() != null) {
                tokenEntity.setExpiresAt(LocalDateTime.now().plusSeconds(tokenResponse.getExpiresInSeconds()));
            }
            tokenRepository.save(tokenEntity);

        } catch (ApiException e) {
            throw e;
        } catch (IOException | GeneralSecurityException e) {
            throw new ApiException(ResultCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

}
