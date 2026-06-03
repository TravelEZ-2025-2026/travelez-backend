package com.example.travelez.backend.users.mapper;

import com.example.travelez.backend.users.dto.request.UserRegisterRequest;
import com.example.travelez.backend.users.dto.response.AdminUserResponse;
import com.example.travelez.backend.users.dto.response.UserAdminDetailResponse;
import com.example.travelez.backend.users.dto.response.UserDetailResponse;
import com.example.travelez.backend.users.dto.response.UserSummaryResponse;
import com.example.travelez.backend.users.model.Admin;
import com.example.travelez.backend.users.model.Provider;
import com.example.travelez.backend.users.model.Traveler;
import com.example.travelez.backend.users.model.User;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "followerCount", defaultValue = "0", ignore = true)
    @Mapping(target = "followingCount", defaultValue = "0", ignore = true)
    User toUser(UserRegisterRequest request);

    UserDetailResponse toUserDetailResponse(User user, boolean isFollowedByMe, boolean isFollowingMe);

    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "followerCount", constant = "0L")
    @Mapping(target = "followingCount", constant = "0L")
    @Mapping(target = "authProvider", constant = "LOCAL")
    Traveler toTraveler(UserRegisterRequest request);

    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "followerCount", constant = "0L")
    @Mapping(target = "followingCount", constant = "0L")
    @Mapping(target = "authProvider", constant = "LOCAL")
    Provider toProvider(UserRegisterRequest request);

    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "followerCount", constant = "0L")
    @Mapping(target = "followingCount", constant = "0L")
    @Mapping(target = "authProvider", constant = "LOCAL")
    Admin toAdmin(UserRegisterRequest request);

    @Mapping(target = "userId", source = "id")
    UserSummaryResponse toUserSummary(User user);

    AdminUserResponse toAdminUserResponse(User user);

    UserAdminDetailResponse toUserAdminDetailResponse(User user);
}
