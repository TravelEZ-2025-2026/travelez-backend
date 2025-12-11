package com.example.travelez.backend.review.mapper;

import com.example.travelez.backend.media.mapper.MediaMapper;
import com.example.travelez.backend.review.dto.request.ReviewCreateRequest;
import com.example.travelez.backend.review.dto.response.ReviewBaseResponse;
import com.example.travelez.backend.review.model.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {MediaMapper.class})
public interface ReviewMapper {

    @Mapping(target = "medias", source = "medias")
    ReviewBaseResponse toReviewBaseResponse(Review review);

    @Mapping(target = "poi.id", source = "poiId")
    @Mapping(target = "traveler.id", source = "travelerId")
    Review toReview(ReviewCreateRequest request, Long poiId, Long travelerId);
}
