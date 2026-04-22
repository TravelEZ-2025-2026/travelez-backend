package com.example.travelez.backend.users.dto.request;

import com.example.travelez.backend.users.model.enums.GenderType;
import com.example.travelez.backend.users.model.enums.RoleType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserRegisterRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    @NotNull(message = "Role is required")
    @Enumerated(EnumType.STRING)
    private RoleType role;

    private String fullName;

    @Enumerated(EnumType.STRING)
    private GenderType gender;

    private LocalDateTime dob;

    //    @NotBlank(message = "Recaptcha token is required")
    private String recaptchaToken;

}
