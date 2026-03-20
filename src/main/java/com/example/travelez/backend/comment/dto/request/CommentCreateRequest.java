package com.example.travelez.backend.comment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class CommentCreateRequest {

    @NotBlank(message = "Content must not be blank")
    private String content;

    private Long parentId;

    private List<MultipartFile> files;
}
