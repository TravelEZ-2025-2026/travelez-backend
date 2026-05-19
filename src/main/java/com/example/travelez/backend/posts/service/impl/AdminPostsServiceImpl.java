package com.example.travelez.backend.posts.service.impl;

import com.example.travelez.backend.comment.repository.CommentRepository;
import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.exception.ApiException;
import com.example.travelez.backend.common.utils.DateTimesUtils;
import com.example.travelez.backend.common.utils.SecurityUtils;
import com.example.travelez.backend.dashboard.model.enums.ActivityCategory;
import com.example.travelez.backend.dashboard.service.impl.AuditLogService;
import com.example.travelez.backend.posts.dto.request.AdminPostsFilterRequest;
import com.example.travelez.backend.posts.dto.request.BanPostRequest;
import com.example.travelez.backend.posts.dto.request.PostStatRequest;
import com.example.travelez.backend.posts.dto.request.UnbanPostRequest;
import com.example.travelez.backend.posts.dto.response.AdminPostsResponse;
import com.example.travelez.backend.posts.dto.response.PostStatResponse;
import com.example.travelez.backend.posts.dto.response.TopPoiResponse;
import com.example.travelez.backend.posts.dto.response.TopTagResponse;
import com.example.travelez.backend.posts.event.PostsStatusChangedEvent;
import com.example.travelez.backend.posts.mapper.PostsMapper;
import com.example.travelez.backend.posts.model.Posts;
import com.example.travelez.backend.posts.model.enums.PostStatus;
import com.example.travelez.backend.posts.model.enums.PostStatusAction;
import com.example.travelez.backend.posts.repository.PostsRepository;
import com.example.travelez.backend.posts.repository.specification.PostsSpecification;
import com.example.travelez.backend.posts.service.AdminPostsService;
import com.example.travelez.backend.reaction.model.enums.ReactionTargetType;
import com.example.travelez.backend.reaction.service.ReactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminPostsServiceImpl implements AdminPostsService {

    private final ApplicationEventPublisher eventPublisher;

    private final ReactionService reactionService;

    private final PostsRepository postsRepository;
    private final CommentRepository commentRepository;

    private final PostsMapper postsMapper;

    private final AuditLogService auditLogService;

    @Override
    public PostStatResponse getStatistics(PostStatRequest request) {
        LocalDateTime start = DateTimesUtils.getStartOfDay(request.getStartDate());
        LocalDateTime end = DateTimesUtils.getEndOfDay(request.getEndDate());

        Long countPost = postsRepository.countPublicPosts(start, end);
        Long countComments = commentRepository.countComments(start, end);

        List<TopPoiResponse> topPoisResponse = postsRepository.getTopPois(start, end, PageRequest.of(0, 5))
                .stream()
                .map(postsMapper::toTopPoiResponse)
                .collect(Collectors.toList());

        List<TopTagResponse> topTagsResponse = postsRepository.getTopTags(start, end, PageRequest.of(0, 5))
                .stream()
                .map(postsMapper::toTopTagResponse)
                .collect(Collectors.toList());

        return PostStatResponse.builder()
                .totalPosts(countPost)
                .topPois(topPoisResponse)
                .topTags(topTagsResponse)
                .totalComments(countComments)
                .build();
    }

    @Override
    public CommonPage<AdminPostsResponse> getAllPosts(AdminPostsFilterRequest request, Pageable pageable) {
        List<Specification<Posts>> specs = new ArrayList<>();
        specs.add(PostsSpecification.filterByStatus(request.getStatuses()));
        specs.add(PostsSpecification.filterByTopicTag(request.getTopicTag()));
        specs.add(PostsSpecification.filterByPoiId(request.getPoiId()));
        specs.add(PostsSpecification.filterByDateRange(request.getFromDate(), request.getToDate()));
        Page<Posts> page = postsRepository.findAll(Specification.allOf(specs), pageable);
        List<Long> postIds = page.getContent().stream().map(Posts::getId).toList();

        CompletableFuture<Map<Long, Long>> countFuture = CompletableFuture.supplyAsync(() -> getCountCommentPosts(postIds));
        CompletableFuture<Map<Long, Long>> reactionFuture = CompletableFuture.supplyAsync(() -> getCountReactionPosts(postIds));
        CompletableFuture.allOf(countFuture, reactionFuture).join();
        Map<Long, Long> countMap = countFuture.join();
        Map<Long, Long> reactionMap = reactionFuture.join();

        List<AdminPostsResponse> adminPostsResponses = page.getContent().stream()
                .map(post -> postsMapper.toAdminPostsResponse(post, countMap.getOrDefault(post.getId(), 0L), reactionMap.getOrDefault(post.getId(), 0L)))
                .collect(Collectors.toList());
        return new CommonPage<>(adminPostsResponses, page.getTotalPages(), page.getTotalElements(), pageable.getPageSize(), page.getNumber(), page.isEmpty());
    }

    @Override
    @Transactional
    public void banPost(Long postId, BanPostRequest request) {
        Posts post = postsRepository.findByPostId(postId)
                .orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND, "Post not found"));
        if (post.getStatus() == PostStatus.BANNED) {
            throw new ApiException(ResultCode.BAD_REQUEST, "Post already banned");
        }
        PostStatus oldStatus = post.getStatus();
        post.setStatus(PostStatus.BANNED);
        postsRepository.save(post);
        Long adminId = SecurityUtils.getCurrentUserId();

        auditLogService.logActivity(
                ActivityCategory.CONTENT,
                "Admin ID #" + adminId + " banned Post #" + postId + ". Reason: " + request.getReason(),
                "Action Taken"
        );

        eventPublisher.publishEvent(new PostsStatusChangedEvent(post, adminId, post.getUser().getId(), oldStatus, PostStatus.BANNED, PostStatusAction.BANNED, request.getReason()));
    }

    @Override
    @Transactional
    public void unbanPost(Long postId, UnbanPostRequest request) {
        Posts post = postsRepository.findByPostId(postId)
                .orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND, "Post not found"));
        if (post.getStatus() != PostStatus.BANNED) {
            throw new ApiException(ResultCode.BAD_REQUEST, "Post is not banned");
        }
        PostStatus oldStatus = post.getStatus();
        post.setStatus(PostStatus.PUBLISHED);
        postsRepository.save(post);
        Long adminId = SecurityUtils.getCurrentUserId();

        auditLogService.logActivity(
                ActivityCategory.CONTENT,
                "Admin ID #" + adminId + " unbanned Post #" + postId,
                "Action Taken"
        );

        eventPublisher.publishEvent(new PostsStatusChangedEvent(post, adminId, post.getUser().getId(), oldStatus, PostStatus.PUBLISHED, PostStatusAction.UNBANNED, request.getReason()));
    }

    private Map<Long, Long> getCountCommentPosts(List<Long> postIds) {
        if (postIds.isEmpty()) return Map.of();
        List<Object[]> commentCounts = commentRepository.countByPostIds(postIds);
        return commentCounts.stream().collect(Collectors.toMap(
                row -> (Long) row[0],
                row -> (Long) row[1]
        ));
    }

    private Map<Long, Long> getCountReactionPosts(List<Long> postIds) {
        return reactionService.getReactionCounts(ReactionTargetType.POST, postIds);
    }
}
