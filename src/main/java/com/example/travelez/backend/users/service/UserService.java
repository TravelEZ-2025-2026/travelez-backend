package com.example.travelez.backend.users.service;

import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.media.dto.response.MediaBaseResponse;
import com.example.travelez.backend.users.dto.request.UserRegisterRequest;
import com.example.travelez.backend.users.dto.response.UserDetailResponse;
import com.example.travelez.backend.users.dto.response.UserLoginResponse;
import com.example.travelez.backend.users.model.User;

public interface UserService {

    User register(UserRegisterRequest request);

    UserLoginResponse login(String username, String password);

    User loadUserByUsername(String username);

    User getUserById(Long userId);

    UserDetailResponse getUserProfileById(Long userId);

    CommonPage<UserDetailResponse> searchUsers(String query, Pageable pageable);
    
    MediaBaseResponse updateUserAvatar(MultipartFile file);

    MediaBaseResponse updateUserCover(MultipartFile file);

}
