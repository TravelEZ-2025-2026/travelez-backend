package com.example.travelez.backend.comment.service.impl;

import com.example.travelez.backend.comment.dto.request.CommentCreateRequest;
import com.example.travelez.backend.comment.dto.request.CommentUpdateRequest;
import com.example.travelez.backend.comment.dto.response.CommentBaseResponse;
import com.example.travelez.backend.comment.mapper.CommentMapper;
import com.example.travelez.backend.comment.model.Comment;
import com.example.travelez.backend.comment.repository.CommentRepository;
import com.example.travelez.backend.comment.service.CommentService;
import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.exception.ApiException;
import com.example.travelez.backend.common.exception.Asserts;
import com.example.travelez.backend.common.utils.SecurityUtils;
import com.example.travelez.backend.infrastructure.filestorage.dto.UploadFileResult;
import com.example.travelez.backend.media.mapper.MediaMapper;
import com.example.travelez.backend.media.model.Media;
import com.example.travelez.backend.media.repository.MediaRepository;
import com.example.travelez.backend.media.service.MediaService;
import com.example.travelez.backend.posts.model.Posts;
import com.example.travelez.backend.posts.model.enums.PostStatus;
import com.example.travelez.backend.posts.repository.PostsRepository;
import com.example.travelez.backend.posts.service.PostsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {
    private final TransactionTemplate transactionTemplate;

    private final MediaService mediaService;
    private final PostsService postService;

    private final CommentRepository commentRepository;
    private final MediaRepository mediaRepository;
    private final PostsRepository postsRepository;

    private final CommentMapper commentMapper;
    private final MediaMapper mediaMapper;

    @Override
    public void createComment(Long postId, CommentCreateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
//        tim bai post bang postId
        Posts post = postsRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
        if (post == null || !postService.isUserCommentPost(userId, post)) {
            Asserts.fail(ResultCode.FORBIDDEN, "User not allowed to comment on this post");
        }
        Comment parentComment = getParentComment(request.getParentId());
        List<UploadFileResult> uploadedFiles = uploadFile(post.getFolderId().toString(), userId.toString(), request.getFiles());
        try {
            transactionTemplate.execute(status -> {
                Comment comment = commentMapper.toComment(request, postId, userId);
                comment.setParentComment(parentComment);
                if (!uploadedFiles.isEmpty()) {
                    List<Media> mediaEntities = uploadedFiles.stream().map(mediaMapper::toMedia).toList();
                    List<Media> savedMedia = mediaRepository.saveAll(mediaEntities);
                    comment.setMedias(savedMedia);
                }
                commentRepository.save(comment);
                return null;
            });
        } catch (Exception e) {
            List<String> fileNames = uploadedFiles.stream().map(UploadFileResult::getCloudName).toList();
            mediaService.cleanupFilesAsync(fileNames);
            if (e.getCause() instanceof ApiException) {
                throw (ApiException) e.getCause();
            }
            Asserts.fail(ResultCode.INTERNAL_SERVER_ERROR, "Failed to save media info to database: " + e.getMessage());
        }
    }

    @Override
    public void updateComment(Long commentId, CommentUpdateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        Comment comment = commentRepository.findByIdAndUserId(commentId, userId)
                .orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND, "Comment not found"));
        List<UploadFileResult> uploadedFiles = uploadFile(comment.getPost().getFolderId().toString(), userId.toString(), request.getNewFiles());
//        Luu thong tin cac file can xoa sau khi cap nhat thanh cong
        List<String> filesToDelete = new ArrayList<>();
        try {
//            cap nhat thong tin comment va media trong db
            transactionTemplate.execute(status -> {
                commentMapper.updateCommentFromRequest(request, comment);
                filesToDelete.addAll(mediaService.processMediaUpdate(comment.getMedias(), request.getKeptMediaIds(), uploadedFiles));
                commentRepository.save(comment);
                return null;
            });
        } catch (Exception e) {
            List<String> fileNames = uploadedFiles.stream().map(UploadFileResult::getCloudName).collect(Collectors.toList());
            mediaService.cleanupFilesAsync(fileNames);
            if (e.getCause() instanceof ApiException) {
                throw (ApiException) e.getCause();
            }
            throw new ApiException(ResultCode.INTERNAL_SERVER_ERROR, "Failed to save media info to database: " + e.getMessage());
        }
//        xoa cac file bi xoa
        mediaService.cleanupFilesAsync(filesToDelete);
    }

    @Override
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND, "Comment not found"));
        Long userId = SecurityUtils.getCurrentUserId();
        if (!isDeletedByUser(userId, comment)) {
            Asserts.fail(ResultCode.FORBIDDEN, "User not allowed to delete this comment");
        }
        List<Object[]> mediaData = mediaRepository.findAllInCommentTree(commentId);
        List<Long> mediaIds = mediaData.stream().map(objects -> ((Number) objects[0]).longValue()).toList();
        List<String> filesToDelete = mediaData.stream().map(objects -> (String) objects[1]).toList();
        try {
            transactionTemplate.execute(status -> {
                if (!mediaIds.isEmpty()) {
                    mediaRepository.deleteAllByIdInBatch(mediaIds);
                }
                commentRepository.deleteCommentById(commentId);
                return null;
            });
        } catch (Exception e) {
            if (e instanceof ApiException) {
                throw (ApiException) e;
            }
            Asserts.fail(ResultCode.INTERNAL_SERVER_ERROR, "Failed to delete comment and media: " + e.getMessage());
        }
        mediaService.cleanupFilesAsync(filesToDelete);
    }

    @Override
    public CommonPage<CommentBaseResponse> getCommentsByPostId(Long postId, Pageable pageable) {
        Page<Comment> comments = commentRepository.findAllByPostIdAndParentCommentNull(postId, pageable);
        return processCommentPage(comments, pageable);
    }

    @Override
    public CommonPage<CommentBaseResponse> getRepliesByCommentId(Long commentId, Pageable pageable) {
        Page<Comment> replies = commentRepository.findAllByParentCommentId(commentId, pageable);
        return processCommentPage(replies, pageable);
    }

    @Override
    public boolean isDeletedByUser(Long userId, Comment comment) {
        if (userId == null) {
            return false;
        }
        Posts posts = comment.getPost();
        PostStatus status = posts.getStatus();
        if (status == PostStatus.BANNED) {
            return false;
        }
        boolean isPostOwner = posts.getUser().getId() == userId;
        if (status == PostStatus.ARCHIVED || status == PostStatus.DRAFT) {
            return isPostOwner;
        }
        boolean isCommentOwner = comment.getUser().getId() == userId;
        return isPostOwner || isCommentOwner;
    }

    private Comment getParentComment(Long parentId) {
        if (parentId == null) {
            return null;
        }
        return commentRepository.findById(parentId)
                .orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND, "Parent comment not found"));
    }

    private List<UploadFileResult> uploadFile(String folder_id, String user_id, List<MultipartFile> files) {
        String path = "posts/" + folder_id + "/" + user_id + "/";
        return mediaService.uploadFilesParallel(files, path);
    }

    private CommonPage<CommentBaseResponse> processCommentPage(Page<Comment> commentsPage, Pageable pageable) {
        if (commentsPage.isEmpty()) {
            return new CommonPage<>(List.of(), commentsPage.getTotalPages(), commentsPage.getTotalElements(), pageable.getPageSize(), commentsPage.getNumber(), true);
        }
        List<Long> parentIds = commentsPage.getContent().stream()
                .map(Comment::getId)
                .toList();
        List<Object[]> counts = commentRepository.countRepliesForParents(parentIds);
        Map<Long, Long> countMap = counts.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
        List<CommentBaseResponse> responses = commentsPage.getContent().stream().map(comment -> commentMapper.toCommentBaseResponse(comment, countMap.getOrDefault(comment.getId(), null))).toList();
        return new CommonPage<>(responses, commentsPage.getTotalPages(), commentsPage.getTotalElements(), pageable.getPageSize(), commentsPage.getNumber(), commentsPage.isEmpty());
    }
}
