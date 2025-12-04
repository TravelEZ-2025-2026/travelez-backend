package com.example.travelez.backend.users.service;

import com.example.travelez.backend.users.dto.response.UserLoginResponse;

public interface OAuth2Service {

    public UserLoginResponse authenticateGoogle(String code);

}
