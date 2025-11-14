package com.example.travelez.backend.users.service.impl;

import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.exception.Asserts;
import com.example.travelez.backend.security.util.JwtUtil;
import com.example.travelez.backend.users.dto.request.UserRegisterRequest;
import com.example.travelez.backend.users.dto.response.UserLoginResponse;
import com.example.travelez.backend.users.mapper.UserMapper;
import com.example.travelez.backend.users.model.User;
import com.example.travelez.backend.users.repository.UserRepository;

import java.util.Optional;

import com.example.travelez.backend.users.service.UserService;
import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final JwtUtil jwtUtil;

    @Override
    public User register(UserRegisterRequest request) {

        if (request.getRole() == User.RoleType.ADMIN) {
            Asserts.fail(ResultCode.FORBIDDEN, "Admin registration is not allowed");
        }
        Optional<User> existingUser = userRepository.findByUsername(request.getUsername());
        if (existingUser.isPresent()) {
            Asserts.fail("Username already exists");
        }
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        return user;
    }

    @Override
    public UserLoginResponse login(String username, String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            User user = loadUserByUsername(username);

            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new BadCredentialsException("Invalid password");
            }
            if (!isUserValid(user)) {
                Asserts.fail(ResultCode.FORBIDDEN, "User is not active");
            }
            String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name(), user.getId());
            return UserLoginResponse.builder()
                    .token(token)
                    .userId(user.getId())
                    .role(user.getRole())
                    .build();
        } catch (AuthenticationException e) {
            Asserts.fail(ResultCode.UNAUTHORIZED, e.getMessage());
        }
        return null;
    }

    @Override
    public User loadUserByUsername(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            Asserts.fail(ResultCode.NOT_FOUND, "User not found");
        }
        return user.get();
    }

    private boolean isUserValid(User user) {
        return user.getStatus() == User.UserStatus.ACTIVE;
    }

    @Override
    public User getUserById(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            Asserts.fail(ResultCode.NOT_FOUND, "User not found with id: " + userId);
        }
        return user.get();
    }

}
