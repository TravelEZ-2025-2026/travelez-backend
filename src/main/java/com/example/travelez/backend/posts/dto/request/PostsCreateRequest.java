package com.example.travelez.backend.posts.dto.request;

import com.example.travelez.backend.posts.model.enums.PostStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class PostsCreateRequest {

    private String title;

    private Long poiId;

    private Long itineraryId;

    private String topicTag;

    @NotBlank(message = "Content must not be blank")
    private String content;

    @NotNull(message = "Post status is required")
    @Enumerated(EnumType.STRING)
    private PostStatus status;

    private List<MultipartFile> files;
}
