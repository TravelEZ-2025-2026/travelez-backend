package com.example.travelez.backend.users.service;

import com.example.travelez.backend.users.dto.request.UserRegisterRequest;
import com.example.travelez.backend.users.dto.response.UserLoginResponse;
import com.example.travelez.backend.users.model.User;

public interface UserService {

    public User register(UserRegisterRequest request);

    public UserLoginResponse login(String username, String password);

    public User loadUserByUsername(String username);

    public User getUserById(Long userId);

}
