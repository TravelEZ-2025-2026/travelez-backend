package com.example.travelez.backend.users.factory;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.travelez.backend.users.dto.request.UserRegisterRequest;
import com.example.travelez.backend.users.model.User;
import com.example.travelez.backend.users.model.enums.RoleType;

@Component
public class UserFactory {

    private final Map<RoleType, UserCreator> creatorMap;

    @Autowired
    public UserFactory(List<UserCreator> creators) {
        creatorMap = new EnumMap<>(RoleType.class);
        for (UserCreator creator : creators) {
            creatorMap.put(creator.getRole(), creator);
        }
    }

    public User create(UserRegisterRequest request) {
        UserCreator creator = creatorMap.get(request.getRole());
        if (creator == null) {
            throw new IllegalArgumentException("Invalid role: " + request.getRole());
        }
        return creator.create(request);
    }

}
