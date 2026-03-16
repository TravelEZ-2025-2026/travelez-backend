package com.example.travelez.backend.posts.dto.request;

import com.example.travelez.backend.posts.model.enums.PostStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class PostsUpdateRequest {
    private String title;

    private String content;

    @Enumerated(EnumType.STRING)
    private PostStatus status;

    private List<Long> keptMediaIds;

    private List<MultipartFile> newFiles;
}
