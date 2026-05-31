package com.example.travelez.backend.posts.service.impl;

import com.example.travelez.backend.comment.repository.CommentRepository;
import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.common.api.CursorResponse;
import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.exception.ApiException;
import com.example.travelez.backend.common.exception.Asserts;
import com.example.travelez.backend.common.utils.SecurityUtils;
import com.example.travelez.backend.infrastructure.filestorage.dto.UploadFileResult;
import com.example.travelez.backend.itinerary.model.Itinerary;
import com.example.travelez.backend.itinerary.repository.ItineraryRepository;
import com.example.travelez.backend.media.dto.enums.MediaTarget;
import com.example.travelez.backend.media.model.Media;
import com.example.travelez.backend.media.repository.MediaRepository;
import com.example.travelez.backend.media.service.MediaService;
import com.example.travelez.backend.poi.model.enums.PoiStatus;
import com.example.travelez.backend.poi.service.PoiService;
import com.example.travelez.backend.posts.dto.request.CursorPostsRequest;
import com.example.travelez.backend.posts.dto.request.PostsCreateRequest;
import com.example.travelez.backend.posts.dto.request.PostsSearchRequest;
import com.example.travelez.backend.posts.dto.request.PostsUpdateRequest;
import com.example.travelez.backend.posts.dto.response.PostResponse;
import com.example.travelez.backend.posts.dto.response.PostsCreatedPayload;
import com.example.travelez.backend.posts.dto.response.PostsDetailResponse;
import com.example.travelez.backend.posts.dto.response.PostsUpdatedPayload;
import com.example.travelez.backend.posts.event.PostsCreatedEvent;
import com.example.travelez.backend.posts.event.PostsDeleteEvent;
import com.example.travelez.backend.posts.event.PostsUpdateEvent;
import com.example.travelez.backend.posts.mapper.PostsMapper;
import com.example.travelez.backend.posts.model.Posts;
import com.example.travelez.backend.posts.model.enums.PostStatus;
import com.example.travelez.backend.posts.permission.PostsPermissionChecker;
import com.example.travelez.backend.posts.repository.PostsRepository;
import com.example.travelez.backend.posts.repository.specification.PostsSpecification;
import com.example.travelez.backend.posts.service.PostsAiService;
import com.example.travelez.backend.posts.service.PostsService;
import com.example.travelez.backend.reaction.model.enums.ReactionTargetType;
import com.example.travelez.backend.reaction.service.ReactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostsServiceImpl implements PostsService {

    private final ApplicationEventPublisher eventPublisher;

    private final TransactionTemplate transactionTemplate;

    private final PostsPermissionChecker postsPermissionChecker;

    private final MediaService mediaService;
    private final PoiService poiService;
    private final PostsAiService postsAiService;
    private final ReactionService reactionService;
    private final com.example.travelez.backend.moderation.service.ContentModerationService contentModerationService;

    private final PostsRepository postsRepository;
    private final CommentRepository commentRepository;
    private final MediaRepository mediaRepository;
    private final ItineraryRepository itineraryRepository;

    private final PostsMapper postsMapper;

    @Override
    public void createPost(PostsCreateRequest request) {
        // Check for banned keywords before processing
        var moderationResult = contentModerationService.checkKeywords(request.getTitle(), request.getContent());
        if (!moderationResult.isSafe()) {
            throw new ApiException(ResultCode.BAD_REQUEST, moderationResult.getReason());
        }
        
        String folder_id = UUID.randomUUID().toString();
        List<UploadFileResult> uploadedFiles = uploadFile(folder_id, request.getFiles());

//        2. luu thong tin file và bài post vao database
        try {
            Posts result = transactionTemplate.execute(status -> {
                if (request.getPoiId() != null) {
                    boolean poiExists = poiService.existsByIdAndSystemStatus(request.getPoiId(), PoiStatus.ACTIVE);
                    if (!poiExists) {
                        throw new ApiException(ResultCode.NOT_FOUND, "Poi not found");
                    }
                }
                if (request.getItineraryId() != null) {
                    Itinerary itinerary = itineraryRepository.findById(request.getItineraryId())
                            .orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND, "Itinerary not found"));

                    if (!Objects.equals(itinerary.getTraveler().getId(), SecurityUtils.getCurrentUserId())) {
                        throw new ApiException(ResultCode.FORBIDDEN, "You can only attach your own itinerary");
                    }

                    if (!Boolean.TRUE.equals(itinerary.getIsPublic())) {
                        throw new ApiException(ResultCode.VALIDATION_FAILED, "You can only attach public itineraries. Please make this itinerary public first.");
                    }
                }
                Posts post = postsMapper.toPosts(request, SecurityUtils.getCurrentUserId(), request.getPoiId(), request.getItineraryId());
                post.setFolderId(UUID.fromString(folder_id));
                Posts savedPost = postsRepository.save(post);
                mediaService.attachMediasToEntity(uploadedFiles, MediaTarget.POST, savedPost.getId());
                return savedPost;
            });

            eventPublisher.publishEvent(new PostsCreatedEvent(PostsCreatedPayload.builder()
                    .postId(result.getId())
                    .title(result.getTitle())
                    .content(result.getContent())
                    .status(result.getStatus())
                    .createdAt(result.getCreatedAt())
                    .build()));
            
            // Submit for AI moderation asynchronously
            contentModerationService.submitForAIModeration(
                    result.getId(), 
                    com.example.travelez.backend.moderation.model.enums.ModerationTargetType.POST
            );

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
    public void updatePost(Long postId, PostsUpdateRequest request) {
        Posts post = postsRepository.findByIdAndUserId(postId, SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND, "Post not found"));

        String folder_id = post.getFolderId().toString();
        List<UploadFileResult> uploadedFiles = uploadFile(folder_id, request.getNewFiles());
//        Luu cac file bi delete de xoa sau khi cap nhat thanh cong
        List<String> filesToDelete = new ArrayList<>();
        try {
//            cap nhat thong tin bai post va media trong transaction
            transactionTemplate.execute(status -> {
                postsMapper.updatePostsFromRequest(request, post);
                filesToDelete.addAll(mediaService.processMediaUpdate(post.getMedias(), request.getKeptMediaIds(), uploadedFiles));
                postsRepository.save(post);
                return null;
            });
            eventPublisher.publishEvent(new PostsUpdateEvent(PostsUpdatedPayload.builder()
                    .postId(post.getId())
                    .status(post.getStatus())
                    .build()));
        } catch (Exception e) {
            List<String> fileNames = uploadedFiles.stream().map(UploadFileResult::getCloudName).collect(Collectors.toList());
            mediaService.cleanupFilesAsync(fileNames);
            if (e.getCause() instanceof ApiException) {
                throw (ApiException) e.getCause();
            }
            throw new ApiException(ResultCode.INTERNAL_SERVER_ERROR, "Failed to save media info to database: " + e.getMessage());
        }
//        xoa file bi delete
        mediaService.cleanupFilesAsync(filesToDelete);
    }

    @Override
    public void deletePost(Long postId) {
        boolean isOwner = postsRepository.existsByIdAndUserId(postId, SecurityUtils.getCurrentUserId());
        if (!isOwner) {
            throw new ApiException(ResultCode.FORBIDDEN, "You are not allowed to delete this post");
        }
        List<Media> mediaList = mediaRepository.findAllByPostId(postId);
        List<Long> mediaIds = mediaList.stream().map(Media::getId).toList();
        List<String> fileNamesToDelete = mediaList.stream().map(Media::getCloudName).toList();
        transactionTemplate.execute(status -> {
            postsRepository.deletePostById(postId);
            mediaRepository.deleteAllByIdInBatch(mediaIds);
            return null;
        });
        mediaService.cleanupFilesAsync(fileNamesToDelete);
        eventPublisher.publishEvent(new PostsDeleteEvent(postId));
    }

    @Override
    public PostsDetailResponse getPostDetail(Long postId) {
        Posts post = postsRepository.findById(postId)
                .orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND, "Post not found"));
        if (!postsPermissionChecker.canUserViewPost(post)) {
            throw new ApiException(ResultCode.FORBIDDEN, "You are not allowed to view this post");
        }
        Long commentCount = commentRepository.countByPostId(postId);
        return postsMapper.toPostsDetailResponse(post, post.getMedias(), commentCount);
    }

    @Override
    public CursorResponse<PostResponse> getAllPosts(CursorPostsRequest request) {
        Pageable limit = PageRequest.of(0, request.getSize());
        Slice<Posts> postsSlice;
        if (request.getLastPostId() == null) {
            postsSlice = postsRepository.findAllPostsFirstPage(limit);
        } else {
            postsSlice = postsRepository.findAllPostsNextPage(request.getLastPostId(), limit);
        }
        return getPostsCursorResponse(postsSlice);
    }

    @Override
    public CommonPage<PostResponse> searchPosts(PostsSearchRequest searchRequest, Pageable pageable) {
        List<Long> matchedPostIds = postsAiService.searchWithPagination(searchRequest, pageable);

        if (matchedPostIds.isEmpty()) {
            return CommonPage.empty();
        }

        int limit = pageable.getPageSize();
        int offset = pageable.getPageNumber() * limit;
        List<Posts> posts = postsRepository.findAllById(matchedPostIds);
        Map<Long, Posts> postMap = posts.stream()
                .collect(Collectors.toMap(Posts::getId, post -> post));

        List<Posts> sortedPosts = matchedPostIds.stream()
                .map(postMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        boolean hasNext = matchedPostIds.size() == searchRequest.getSize();

        Page<Posts> pageResult = new PageImpl<>(sortedPosts, pageable, hasNext ? offset + limit + 1 : offset + sortedPosts.size());

        return getPostsPageResponse(pageResult, pageable);
    }

    @Override
    public CommonPage<PostResponse> getUserPosts(Long userId, Pageable pageable) {
        boolean isOwner = Objects.equals(SecurityUtils.getCurrentUserId(), userId);
        List<Specification<Posts>> specs = new ArrayList<>();
        specs.add(PostsSpecification.filterByUserId(userId));
        if (!isOwner) {
            specs.add(PostsSpecification.filterByStatus(List.of(PostStatus.PUBLISHED)));
        } else {
            specs.add(PostsSpecification.filterByStatus(List.of(PostStatus.PUBLISHED, PostStatus.ARCHIVED)));
        }
        Page<Posts> page = postsRepository.findAll(Specification.allOf(specs), pageable);
        return getPostsPageResponse(page, pageable);
    }

    private List<UploadFileResult> uploadFile(String folder_id, List<MultipartFile> files) {
        return mediaService.uploadFilesParallel(files, "posts/" + folder_id + "/");
    }

    private CursorResponse<PostResponse> getPostsCursorResponse(Slice<Posts> postsSlice) {
        List<PostResponse> posts = getPostResponses(postsSlice.getContent());
        Long lastPostId = postsSlice.isEmpty() ? null : posts.getLast().getId();
        String nextCursor = !postsSlice.hasNext() ? null : lastPostId.toString();
        return new CursorResponse<>(posts, nextCursor, postsSlice.hasNext());
    }

    private CommonPage<PostResponse> getPostsPageResponse(Page<Posts> page, Pageable pageable) {
        List<PostResponse> posts = getPostResponses(page.getContent());
        return new CommonPage<>(posts, page.getTotalPages(), page.getTotalElements(), pageable.getPageSize(), page.getNumber(), page.isEmpty());
    }

    private List<PostResponse> getPostResponses(List<Posts> posts) {
        if (posts.isEmpty()) return List.of();
        List<Long> postIds = posts.stream().map(Posts::getId).toList();
        Long userId = SecurityUtils.getCurrentUserId();

        CompletableFuture<Map<Long, List<Media>>> mediaFuture = CompletableFuture.supplyAsync(() -> getMapMediaPostIds(postIds));
        CompletableFuture<Map<Long, Long>> countFuture = CompletableFuture.supplyAsync(() -> getCountCommentPosts(postIds));
        CompletableFuture<Map<Long, Long>> reactionFuture = CompletableFuture.supplyAsync(() -> getCountReactionPosts(postIds));
        CompletableFuture<Set<Long>> reactedFuture = CompletableFuture.supplyAsync(() -> getUserReactedPostIds(userId, postIds));
        CompletableFuture.allOf(mediaFuture, countFuture, reactionFuture, reactedFuture).join();

        Map<Long, List<Media>> mediaMap = mediaFuture.join();
        Map<Long, Long> countMap = countFuture.join();
        Map<Long, Long> reactionMap = reactionFuture.join();
        Set<Long> reactedIds = reactedFuture.join();

        return posts.stream().map(post -> {
            List<Media> mediaList = mediaMap.getOrDefault(post.getId(), List.of());
            Long commentCount = countMap.getOrDefault(post.getId(), 0L);
            Long reactionCount = reactionMap.getOrDefault(post.getId(), 0L);
            boolean isReacted = reactedIds.contains(post.getId());
            return postsMapper.toPostResponse(post, mediaList, commentCount, reactionCount, isReacted);
        }).toList();
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

    private Set<Long> getUserReactedPostIds(Long userId, List<Long> postIds) {
        return reactionService.getUserReactedTargetIds(ReactionTargetType.POST, userId, postIds);
    }

    private Map<Long, List<Media>> getMapMediaPostIds(List<Long> postIds) {
        if (postIds.isEmpty()) return Map.of();
        List<Object[]> mediaPost = mediaRepository.findAllByPostIds(postIds);
        return mediaService.groupMediaByParentId(mediaPost);
    }
}




















