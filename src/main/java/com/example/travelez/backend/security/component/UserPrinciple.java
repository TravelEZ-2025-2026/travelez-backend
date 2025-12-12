package com.example.travelez.backend.security.component;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.security.Principal;

@Getter
@AllArgsConstructor
public class UserPrinciple implements Principal {
    private final Long userId;
    private final String username;
    private final String role;

    @Override
    public String getName() {
        return "";
    }
}
