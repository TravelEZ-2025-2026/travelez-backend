package com.example.travelez.backend.chat.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class ChatRequest {
    private Long conversationId;
    private Long receiverId;
    private String content;
    private List<MultipartFile> files;
}
