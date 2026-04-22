package com.example.travelez.backend.reaction.dto.response;

import com.example.travelez.backend.users.dto.response.UserSummaryResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ReactorsResponse {
    private UserSummaryResponse user;
    private boolean isFollowedByMe;
    private LocalDateTime createdAt;
}
