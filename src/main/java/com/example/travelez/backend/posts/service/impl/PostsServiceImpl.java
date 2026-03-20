package com.example.travelez.backend.posts.service.impl;

import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.common.api.CursorResponse;
import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.exception.ApiException;
import com.example.travelez.backend.common.exception.Asserts;
import com.example.travelez.backend.common.utils.SecurityUtils;
import com.example.travelez.backend.infrastructure.filestorage.dto.UploadFileResult;
import com.example.travelez.backend.media.mapper.MediaMapper;
import com.example.travelez.backend.media.model.Media;
import com.example.travelez.backend.media.repository.MediaRepository;
import com.example.travelez.backend.media.service.MediaService;
import com.example.travelez.backend.posts.dto.request.CursorPostsRequest;
import com.example.travelez.backend.posts.dto.request.PostsCreateRequest;
import com.example.travelez.backend.posts.dto.request.PostsSearchRequest;
import com.example.travelez.backend.posts.dto.request.PostsUpdateRequest;
import com.example.travelez.backend.posts.dto.response.PostResponse;
import com.example.travelez.backend.posts.dto.response.PostsDetailResponse;
import com.example.travelez.backend.posts.mapper.PostsMapper;
import com.example.travelez.backend.posts.model.Posts;
import com.example.travelez.backend.posts.model.enums.PostStatus;
import com.example.travelez.backend.posts.repository.PostsRepository;
import com.example.travelez.backend.posts.repository.specification.PostsSpecification;
import com.example.travelez.backend.posts.service.PostsService;
import com.example.travelez.backend.security.component.UserPrinciple;
import com.example.travelez.backend.users.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostsServiceImpl implements PostsService {

    private final TransactionTemplate transactionTemplate;

    private final MediaService mediaService;

    private final PostsRepository postsRepository;
    private final MediaRepository mediaRepository;

    private final PostsMapper postsMapper;
    private final MediaMapper mediaMapper;

    @Override
    public void createPost(PostsCreateRequest request) {
        String folder_id = UUID.randomUUID().toString();
        List<UploadFileResult> uploadedFiles = uploadFile(folder_id, request.getFiles());

//        2. luu thong tin file và bài post vao database
        try {
            transactionTemplate.execute(status -> {
                Posts post = postsMapper.toPosts(request, SecurityUtils.getCurrentUserId());
                post.setFolderId(UUID.fromString(folder_id));
                if (!uploadedFiles.isEmpty()) {
                    List<Media> mediaEntities = uploadedFiles.stream().map(mediaMapper::toMedia).toList();
                    List<Media> savedMedia = mediaRepository.saveAll(mediaEntities);
                    post.setMedias(savedMedia);
                }
                postsRepository.save(post);
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
    }

    @Override
    public boolean isUserCommentPost(Long userId, Posts posts) {
        if (userId == null) {
            return false;
        }
        PostStatus status = posts.getStatus();
        if (status == PostStatus.BANNED || status == PostStatus.DRAFT) {
            return false;
        }
        if (status == PostStatus.ARCHIVED) {
//        only owner can comment
            return posts.getUser().getId() == userId;
        }
//        public posts
        return true;
    }

    @Override
    public PostsDetailResponse getPostDetail(Long postId) {
        Posts post = postsRepository.findById(postId)
                .orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND, "Post not found"));
        if (!canUserViewPost(post)) {
            throw new ApiException(ResultCode.FORBIDDEN, "You are not allowed to view this post");
        }
//        Long commentCount = commentRepository.countByPostId(postId);
        Long commentCount = 0L;
        return postsMapper.toPostsDetailResponse(post, post.getMedias(), commentCount);
    }

    @Override
    public CursorResponse<PostResponse> getFriendsPostsCursor(Long userId, CursorPostsRequest request) {
        Pageable limit = PageRequest.of(0, request.getSize());
        Slice<Posts> postsSlice;
        if (request.getLastPostId() == null) {
            postsSlice = postsRepository.findFriendPostsFirstPage(userId, limit);
        } else {
            postsSlice = postsRepository.findFriendPostsNextPage(userId, request.getLastPostId(), limit);
        }
        return getPostsCursorResponse(postsSlice);
    }

    @Override
    public CursorResponse<PostResponse> getSuggestedPostsCursor(Long userId, CursorPostsRequest request) {
        Pageable limit = PageRequest.of(0, request.getSize());
        Slice<Posts> postsSlice;
        if (request.getLastPostId() == null) {
            postsSlice = postsRepository.findSuggestPostsFirstPage(userId, limit);
        } else {
            postsSlice = postsRepository.findSuggestedPostsNextPage(userId, request.getLastPostId(), limit);
        }
        return getPostsCursorResponse(postsSlice);
    }

    @Override
    public CommonPage<PostResponse> searchPost(PostsSearchRequest searchRequest, Pageable pageable) {
        List<Specification<Posts>> specs = new ArrayList<>();
        specs.add(PostsSpecification.filterByKeyword(searchRequest.getKeyword()));
        specs.add(PostsSpecification.filterByDateRange(searchRequest.getFromDate(), searchRequest.getToDate()));
        specs.add(PostsSpecification.filterByStatus(List.of(PostStatus.PUBLISHED)));
        Page<Posts> page = postsRepository.findAll(Specification.allOf(specs), pageable);
        return getPostsPageResponse(page, pageable);
    }

    @Override
    public CommonPage<PostResponse> getUserPosts(Long userId, Pageable pageable) {
        boolean isOwner = Objects.equals(SecurityUtils.getCurrentUserId(), userId);
        List<Specification<Posts>> specs = new ArrayList<>();
        specs.add(PostsSpecification.filterByUserId(userId));
        if (!isOwner) {
            specs.add(PostsSpecification.filterByStatus(List.of(PostStatus.PUBLISHED)));
        } else {
            specs.add(PostsSpecification.filterByStatus(List.of(PostStatus.PUBLISHED, PostStatus.ARCHIVED, PostStatus.DRAFT)));
        }
        Page<Posts> page = postsRepository.findAll(Specification.allOf(specs), pageable);
        return getPostsPageResponse(page, pageable);
    }

    private boolean canUserViewPost(Posts post) {
        UserPrinciple user = SecurityUtils.getUserPrinciple();
        boolean isAdmin = user != null && user.getRole().equals(User.RoleType.ADMIN.name());
        if (isAdmin) return true;
        boolean isOwner = user != null && post.getUser().getId() == user.getUserId();
        if (post.getStatus() == PostStatus.ARCHIVED || post.getStatus() == PostStatus.BANNED || post.getStatus() == PostStatus.DRAFT) {
            return isOwner;
        }
        return true;
    }

    private List<UploadFileResult> uploadFile(String folder_id, List<MultipartFile> files) {
        return mediaService.uploadFilesParallel(files, "posts/" + folder_id + "/");
    }

//    private Map<Long, Long> getCountCommentPosts(List<Long> postIds) {
//        if (postIds.isEmpty()) return Map.of();
//        List<Object[]> commentCounts = commentRepository.countByPostIds(postIds);
//        return commentCounts.stream().collect(Collectors.toMap(
//                row -> (Long) row[0],
//                row -> (Long) row[1]
//        ));
//    }

    private CursorResponse<PostResponse> getPostsCursorResponse(Slice<Posts> postsSlice) {
        List<Long> postIds = postsSlice.getContent().stream().map(Posts::getId).toList();
        Map<Long, List<Media>> mediaMap = getMapMediaPostIds(postIds);
//        Map<Long, Long> countMap = getCountCommentPosts(postIds);
        Map<Long, Long> countMap = Map.of();
        List<PostResponse> posts = postsSlice.getContent().stream().map(post -> postsMapper.toPostResponse(post, mediaMap.getOrDefault(post.getId(), List.of()), countMap.getOrDefault(post.getId(), 0L), true)).toList();
        Long lastPostId = postsSlice.isEmpty() ? null : posts.getLast().getId();
        String nextCursor = !postsSlice.hasNext() ? null : lastPostId.toString();
        return new CursorResponse<>(posts, nextCursor, postsSlice.hasNext());
    }

    private CommonPage<PostResponse> getPostsPageResponse(Page<Posts> page, Pageable pageable) {
        List<Long> postIds = page.getContent().stream().map(Posts::getId).toList();
        Map<Long, List<Media>> mediaMap = getMapMediaPostIds(postIds);
//        Map<Long, Long> countMap = getCountCommentPosts(postIds);
        Map<Long, Long> countMap = Map.of();
        List<PostResponse> posts = page.getContent().stream().map(post -> postsMapper.toPostResponse(post, mediaMap.getOrDefault(post.getId(), List.of()), countMap.getOrDefault(post.getId(), 0L), true)).toList();
        return new CommonPage<>(posts, page.getTotalPages(), page.getTotalElements(), pageable.getPageSize(), page.getNumber(), page.isEmpty());
    }

    private Map<Long, List<Media>> getMapMediaPostIds(List<Long> postIds) {
        if (postIds.isEmpty()) return Map.of();
        List<Object[]> mediaPost = mediaRepository.findAllByPostIds(postIds);
        return mediaService.groupMediaByParentId(mediaPost);
    }


}




















