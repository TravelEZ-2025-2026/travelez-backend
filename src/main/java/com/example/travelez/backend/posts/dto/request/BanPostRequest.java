package com.example.travelez.backend.posts.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BanPostRequest {
    @NotBlank(message = "Reason for banning is required")
    private String reason;
}