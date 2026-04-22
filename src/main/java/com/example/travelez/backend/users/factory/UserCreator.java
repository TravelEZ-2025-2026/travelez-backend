package com.example.travelez.backend.users.factory;

import com.example.travelez.backend.users.dto.request.UserRegisterRequest;
import com.example.travelez.backend.users.model.User;
import com.example.travelez.backend.users.model.enums.RoleType;

public interface UserCreator {
    User create(UserRegisterRequest request);

    RoleType getRole();
}
