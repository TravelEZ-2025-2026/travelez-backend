package com.example.travelez.backend.users.mapper;

import com.example.travelez.backend.users.dto.request.UserRegisterRequest;
import com.example.travelez.backend.users.dto.response.UserDetailResponse;
import com.example.travelez.backend.users.model.Admin;
import com.example.travelez.backend.users.model.Provider;
import com.example.travelez.backend.users.model.Traveler;
import com.example.travelez.backend.users.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "status", constant = "ACTIVE")
    User toUser(UserRegisterRequest request);

    UserDetailResponse toUserDetailResponse(User user);

    @Mapping(target = "status", constant = "ACTIVE")
    Traveler toTraveler(UserRegisterRequest request);

    @Mapping(target = "status", constant = "ACTIVE")
    Provider toProvider(UserRegisterRequest request);

    @Mapping(target = "status", constant = "ACTIVE")
    Admin toAdmin(UserRegisterRequest request);
}
