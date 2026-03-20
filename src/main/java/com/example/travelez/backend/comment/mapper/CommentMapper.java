package com.example.travelez.backend.comment.mapper;

import com.example.travelez.backend.comment.dto.request.CommentCreateRequest;
import com.example.travelez.backend.comment.dto.request.CommentUpdateRequest;
import com.example.travelez.backend.comment.dto.response.CommentBaseResponse;
import com.example.travelez.backend.comment.model.Comment;
import com.example.travelez.backend.media.mapper.MediaMapper;
import com.example.travelez.backend.users.mapper.UserMapper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {MediaMapper.class, UserMapper.class})
public interface CommentMapper {
    @Mapping(target = "post.id", source = "postId")
    @Mapping(target = "user.id", source = "userId")
    Comment toComment(CommentCreateRequest request, Long postId, Long userId);

    @BeanMapping(nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
    void updateCommentFromRequest(CommentUpdateRequest request, @MappingTarget Comment comment);

    @Mapping(target = "postId", source = "comment.post.id")
    @Mapping(target = "parentCommentId", source = "comment.parentComment.id")
    @Mapping(target = "childCommentCount", source = "childCommentCount")
    @Mapping(target = "author", source = "comment.user")
    CommentBaseResponse toCommentBaseResponse(Comment comment, Long childCommentCount);
}
