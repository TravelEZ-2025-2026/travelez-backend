package com.example.travelez.backend.review.service.impl;

import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.exception.ApiException;
import com.example.travelez.backend.infrastructure.filestorage.FileStorageService;
import com.example.travelez.backend.infrastructure.filestorage.dto.UploadFileResult;
import com.example.travelez.backend.media.model.Media;
import com.example.travelez.backend.media.model.enums.MediaType;
import com.example.travelez.backend.media.repository.MediaRepository;
import com.example.travelez.backend.media.service.MediaService;
import com.example.travelez.backend.poi.model.enums.PoiStatus;
import com.example.travelez.backend.poi.service.PoiService;
import com.example.travelez.backend.review.dto.request.ReviewCreateRequest;
import com.example.travelez.backend.review.dto.request.ReviewFilterRequest;
import com.example.travelez.backend.review.dto.response.ReviewBaseResponse;
import com.example.travelez.backend.review.mapper.ReviewMapper;
import com.example.travelez.backend.review.model.Review;
import com.example.travelez.backend.review.model.enums.ReviewStatus;
import com.example.travelez.backend.review.repository.ReviewRepository;
import com.example.travelez.backend.review.repository.specification.ReviewSpecification;
import com.example.travelez.backend.review.service.ReviewService;
import com.example.travelez.backend.security.component.UserPrinciple;
import com.example.travelez.backend.users.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final PoiService poiService;
    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final FileStorageService fileStorageService;
    private final MediaService mediaService;
    private final MediaRepository mediaRepository;

    @Override
    public CommonPage<ReviewBaseResponse> getReviewByPoiId(ReviewFilterRequest reviewFilterRequest,
                                                           Pageable pageable) {
        // check poi is active
        poiService.findByIdAndSystemStatus(reviewFilterRequest.getPoiId(), PoiStatus.ACTIVE)
                .orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND, "Poi not found"));

        List<Specification<Review>> specs = new ArrayList<>();
        specs.add(ReviewSpecification.filterByPoiId(reviewFilterRequest.getPoiId()));
        specs.add(ReviewSpecification.filterByRating(reviewFilterRequest.getRating()));
        specs.add(ReviewSpecification.filterBySystemStatus(ReviewStatus.ACTIVE));

        Page<Review> reviews = reviewRepository.findAll(Specification.allOf(specs), pageable);
        List<ReviewBaseResponse> reviewDetailResponses = reviews.stream()
                .map(reviewMapper::toReviewBaseResponse)
                .toList();

        return new CommonPage<>(reviewDetailResponses, reviews.getTotalPages(), reviews.getTotalElements(),
                pageable.getPageSize(), reviews.getNumber(), reviews.isEmpty());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createReview(Long poiId, ReviewCreateRequest request, List<MultipartFile> files) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrinciple userPrinciple = (UserPrinciple) authentication.getPrincipal();
        poiService.findByIdAndSystemStatus(poiId, PoiStatus.ACTIVE)
                .orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND, "Poi not found"));

        List<UploadFileResult> uploadedFiles = new ArrayList<>();
        try {
            uploadedFiles = mediaService.uploadFilesParallel(files,
                    "poi/" + poiId + "/review/" + userPrinciple.getUserId() + "/");
        } catch (Exception e) {
            throw new ApiException(ResultCode.INTERNAL_SERVER_ERROR, "Failed to upload review images");
        }

        try {
            Review review = reviewMapper.toReview(request, poiId, userPrinciple.getUserId());
            review.setStatus(ReviewStatus.ACTIVE);
            review.setIsCrawled(false);

            if (!uploadedFiles.isEmpty()) {
                List<Media> mediaEntities = uploadedFiles.stream().map(dto -> {
                    Media media = new Media();
                    media.setUrl(dto.getPublicUrl());
                    media.setCloudName(dto.getCloudName());
                    media.setType(MediaType.IMAGE);
                    media.setCloudName(dto.getCloudName());
                    return media;
                }).collect(Collectors.toList());
                List<Media> savedMedia = mediaRepository.saveAll(mediaEntities);
                review.setMedias(savedMedia);
            }

            reviewRepository.save(review);
        } catch (Exception e) {
            mediaService.cleanupFilesAsync(uploadedFiles);
            throw new ApiException(ResultCode.INTERNAL_SERVER_ERROR, "Failed to create review");
        }
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrinciple userPrinciple = (UserPrinciple) authentication.getPrincipal();

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND, "Review not found"));

        boolean isAdmin = userPrinciple.getRole().equals(User.RoleType.ADMIN.name());
        boolean isOwner = review.getTraveler() != null
                && review.getTraveler().getId() == userPrinciple.getUserId();

        if (!isAdmin && !isOwner) {
            throw new ApiException(ResultCode.FORBIDDEN, "You are not allowed to delete this review");
        }

        List<Media> mediaList = review.getMedias();
        for (Media media : mediaList) {
            if (media.getCloudName() != null) {
                try {
                    fileStorageService.deleteFile(media.getCloudName());
                } catch (Exception e) {
                    log.error("Failed to delete review image: {}", media.getCloudName(), e);
                }
            }
        }
        mediaRepository.deleteAll(mediaList);
        reviewRepository.delete(review);
    }
}
