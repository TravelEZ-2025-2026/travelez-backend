package com.example.travelez.backend.users.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserLoginRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    //    @NotBlank(message = "Recaptcha token is required")
    private String recaptchaToken;

}
