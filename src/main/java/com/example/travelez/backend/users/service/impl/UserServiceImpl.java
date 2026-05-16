package com.example.travelez.backend.users.service.impl;

import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.exception.ApiException;
import com.example.travelez.backend.common.exception.Asserts;
import com.example.travelez.backend.common.utils.SecurityUtils;
import com.example.travelez.backend.infrastructure.filestorage.dto.UploadFileResult;
import com.example.travelez.backend.media.dto.response.MediaBaseResponse;
import com.example.travelez.backend.media.mapper.MediaMapper;
import com.example.travelez.backend.media.model.Media;
import com.example.travelez.backend.media.repository.MediaRepository;
import com.example.travelez.backend.media.service.MediaService;
import com.example.travelez.backend.security.util.JwtUtil;
import com.example.travelez.backend.social.dto.internal.RelationshipStatus;
import com.example.travelez.backend.social.service.FollowService;
import com.example.travelez.backend.users.dto.request.AdminUserFilterRequest;
import com.example.travelez.backend.users.dto.request.UserRegisterRequest;
import com.example.travelez.backend.users.dto.request.UserUpdateRequest;
import com.example.travelez.backend.users.dto.response.AdminUserResponse;
import com.example.travelez.backend.users.dto.response.UserDetailResponse;
import com.example.travelez.backend.users.dto.response.UserLoginResponse;
import com.example.travelez.backend.users.factory.UserFactory;
import com.example.travelez.backend.users.mapper.UserMapper;
import com.example.travelez.backend.users.model.User;
import com.example.travelez.backend.users.model.enums.RoleType;
import com.example.travelez.backend.users.model.enums.UserStatus;
import com.example.travelez.backend.users.repository.UserRepository;
import com.example.travelez.backend.users.repository.specification.UserSpecification;
import com.example.travelez.backend.users.service.UserService;


import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @Value("${travelez.secret-code}")
    private String secretCode;

    private final TransactionTemplate transactionTemplate;

    private final MediaService mediaService;
    private final FollowService followService;

    private final UserRepository userRepository;
    private final MediaRepository mediaRepository;

    private final UserMapper userMapper;
    private final MediaMapper mediaMapper;

    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    private final JwtUtil jwtUtil;

    private final UserFactory userFactory;

    @Override
    public User register(UserRegisterRequest request) {

        if (request.getRole() == RoleType.ADMIN && !request.getSecretCode().equals(secretCode)) {
            Asserts.fail(ResultCode.FORBIDDEN, "Admin registration is not allowed");
        }
        Optional<User> existingUser = userRepository.findByUsername(request.getUsername());
        if (existingUser.isPresent()) {
            Asserts.fail("Username already exists");
        }
        User user = userFactory.create(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        return user;
    }

    @Override
    public UserLoginResponse login(String username, String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            User user = loadUserByUsername(username);

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
        return user.getStatus() == UserStatus.ACTIVE;
    }

    @Override
    public User getUserById(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            Asserts.fail(ResultCode.NOT_FOUND, "User not found with id: " + userId);
        }
        return user.get();
    }

    @Override
    public UserDetailResponse getUserProfileById(Long userId) {
        Optional<User> user = userRepository.findUserProfileById(userId);
        if (user.isEmpty()) {
            Asserts.fail(ResultCode.NOT_FOUND, "User not found with id: " + userId);
        }
        RelationshipStatus relationshipStatus = followService.getRelationShip(SecurityUtils.getCurrentUserId(), userId);
        return userMapper.toUserDetailResponse(user.get(), relationshipStatus.isFollowedByMe(), relationshipStatus.isFollowingMe());
    }

    @Override
    public CommonPage<UserDetailResponse> searchUsers(String query, Pageable pageable) {
        List<Specification<User>> specs = new ArrayList<>();
        specs.add(UserSpecification.filterByKeyword(query));
        specs.add(UserSpecification.filterByStatus(List.of(UserStatus.ACTIVE)));
        specs.add(UserSpecification.filterByRole(List.of(RoleType.TRAVELER)));
        Page<User> users = userRepository.findAll(Specification.allOf(specs), pageable);
        return getUsersPageResponse(users, pageable);
    }

    @Override
    public MediaBaseResponse updateUserAvatar(MultipartFile file) {
        Long userId = SecurityUtils.getCurrentUserId();

        String uploadPath = "users/" + userId + "/avatar";
        UploadFileResult uploadedFile = mediaService.uploadFile(file, uploadPath);

        final AtomicReference<String> fileToDelete = new AtomicReference<>(null);

        try {
            Media savedMedia = transactionTemplate.execute(status -> {

                User user = userRepository.findById(userId).orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND, "User not found"));
                Media oldMedia = user.getAvatar();
                if (oldMedia != null) {
                    fileToDelete.set(oldMedia.getCloudName());
                }

                Media media = mediaMapper.toMedia(uploadedFile);
                media = mediaRepository.save(media);

                user.setAvatar(media);
                userRepository.save(user);
                if (oldMedia != null) {
                    mediaRepository.delete(oldMedia);
                }
                return media;
            });
            if (fileToDelete.get() != null) {
                mediaService.cleanupFilesAsync(Collections.singletonList(fileToDelete.get()));
            }
            return mediaMapper.toMediaBaseResponse(savedMedia);
        } catch (Exception e) {
            mediaService.cleanupFilesAsync(Collections.singletonList(uploadedFile.getCloudName()));
            if (e instanceof ApiException) {
                throw (ApiException) e;
            }
            throw new ApiException(ResultCode.INTERNAL_SERVER_ERROR, "Failed to update avatar: " + e.getMessage());
        }
    }

    @Override
    public MediaBaseResponse updateUserCover(MultipartFile file) {
        Long userId = SecurityUtils.getCurrentUserId();

        String uploadPath = "users/" + userId + "/cover";
        UploadFileResult uploadedFile = mediaService.uploadFile(file, uploadPath);

        final AtomicReference<String> fileToDelete = new AtomicReference<>(null);

        try {
            Media savedMedia = transactionTemplate.execute(status -> {

                User user = userRepository.findById(userId).orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND, "User not found"));
                Media oldMedia = user.getCover();
                if (oldMedia != null) {
                    fileToDelete.set(oldMedia.getCloudName());
                }

                Media media = mediaMapper.toMedia(uploadedFile);
                media = mediaRepository.save(media);

                user.setCover(media);
                userRepository.save(user);
                if (oldMedia != null) {
                    mediaRepository.delete(oldMedia);
                }
                return media;
            });
            if (fileToDelete.get() != null) {
                mediaService.cleanupFilesAsync(Collections.singletonList(fileToDelete.get()));
            }
            return mediaMapper.toMediaBaseResponse(savedMedia);
        } catch (Exception e) {
            mediaService.cleanupFilesAsync(Collections.singletonList(uploadedFile.getCloudName()));
            if (e instanceof ApiException) {
                throw (ApiException) e;
            }
            throw new ApiException(ResultCode.INTERNAL_SERVER_ERROR, "Failed to update cover: " + e.getMessage());
        }
    }

    @Override
    public UserDetailResponse updateUserInfo(UserUpdateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND, "User not found"));

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getDob() != null) {
            user.setDob(request.getDob());
        }

        userRepository.save(user);
        return userMapper.toUserDetailResponse(user, false, false);
    }

    private CommonPage<UserDetailResponse> getUsersPageResponse(Page<User> page, Pageable pageable) {
        List<UserDetailResponse> userDetailResponses = page.getContent().stream()
                .map(user -> userMapper.toUserDetailResponse(user, false, false))
                .toList();
        return new CommonPage<>(userDetailResponses, page.getTotalPages(), page.getTotalElements(), pageable.getPageSize(), page.getNumber(), page.isEmpty());
    }

}
