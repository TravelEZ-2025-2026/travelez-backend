package com.example.travelez.backend.comment.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class CommentUpdateRequest {
    private String content;

    private List<Long> keptMediaIds;

    private List<MultipartFile> newFiles;
}

