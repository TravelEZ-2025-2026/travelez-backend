package com.example.travelez.backend.comment.dto.response;

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
public class CommentBaseResponse {
    private Long id;
    private Long postId;
    private Long parentCommentId;
    private UserSummaryResponse author;
    private String content;
    private LocalDateTime createdAt;
    private List<MediaBaseResponse> medias;
    private Long childCommentCount;
    private boolean isReactedByMe;
    private Long reactionCount;
}
