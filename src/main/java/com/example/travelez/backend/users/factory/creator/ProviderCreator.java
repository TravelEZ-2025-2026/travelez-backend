package com.example.travelez.backend.users.factory.creator;

import com.example.travelez.backend.users.dto.request.UserRegisterRequest;
import com.example.travelez.backend.users.factory.UserCreator;
import com.example.travelez.backend.users.mapper.UserMapper;
import com.example.travelez.backend.users.model.User;
import com.example.travelez.backend.users.model.User.RoleType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProviderCreator implements UserCreator {

    private final UserMapper userMapper;

    @Override
    public User create(UserRegisterRequest request) {
        return userMapper.toProvider(request);
    }

    @Override
    public RoleType getRole() {
        return RoleType.PROVIDER;
    }
}
