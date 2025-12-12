package com.example.travelez.backend.users.controller;

import com.example.travelez.backend.common.api.BaseResponse;
import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.infrastructure.captcha.CaptchaService;
import com.example.travelez.backend.users.dto.request.UserLoginRequest;
import com.example.travelez.backend.users.dto.request.UserRegisterRequest;
import com.example.travelez.backend.users.dto.response.UserLoginResponse;
import com.example.travelez.backend.users.service.OAuth2Service;
import com.example.travelez.backend.users.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/sso")
@RequiredArgsConstructor
@Tag(name = "SSO", description = "Single Sign-On endpoints")
public class AuthController {

    private final UserService userService;
 
    private final OAuth2Service oAuth2Service;

    private final CaptchaService captchaService;

    @PostMapping("/register")
    public ResponseEntity<BaseResponse<Void>> register(@RequestBody @Valid UserRegisterRequest request) {
//        phan nay cho captcha
//        boolean isCaptchaValid = captchaService.verify(request.getRecaptchaToken());
//        if (!isCaptchaValid) {
//            return BaseResponse.failed(null, ResultCode.CAPTCHA_INVALID, "Captcha verification failed");
//        }

        userService.register(request);

        return BaseResponse.success(null, ResultCode.SUCCESS, "User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<BaseResponse<UserLoginResponse>> login(@RequestBody @Valid UserLoginRequest request) {
//        phan nay cho captcha
//        boolean isCaptchaValid = captchaService.verify(request.getRecaptchaToken());
//        if (!isCaptchaValid) {
//            return BaseResponse.failed(null, ResultCode.CAPTCHA_INVALID, "Captcha verification failed");
//        }

        UserLoginResponse response = userService.login(request.getUsername(), request.getPassword());
        return BaseResponse.success(response, ResultCode.SUCCESS, "Login successful");
    }

    @PostMapping("/google")
    public ResponseEntity<BaseResponse<UserLoginResponse>> googleLogin(@RequestBody Map<String, String> request) {
        String code = request.get("code");
        return BaseResponse.success(oAuth2Service.authenticateGoogle(code), ResultCode.SUCCESS,
                "Google login successful");
    }

}
