package com.example.travelez.backend.chat.dto.response;

import com.example.travelez.backend.media.dto.response.MediaBaseResponse;
import com.example.travelez.backend.users.dto.response.UserSummaryResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private Long id;
    private Long conversationId;
    private String content;
    private UserSummaryResponse sender;
    private List<MediaBaseResponse> medias;
    private LocalDateTime createdAt;
}

