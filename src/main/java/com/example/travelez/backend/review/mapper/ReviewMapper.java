package com.example.travelez.backend.review.mapper;

import com.example.travelez.backend.media.mapper.MediaMapper;
import com.example.travelez.backend.poi.mapper.PoiMapper;
import com.example.travelez.backend.review.dto.request.ReviewCreateRequest;
import com.example.travelez.backend.review.dto.response.ReviewBaseResponse;
import com.example.travelez.backend.review.model.Review;
import com.example.travelez.backend.users.mapper.UserMapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {MediaMapper.class, PoiMapper.class, UserMapper.class})
public interface ReviewMapper {

    @Mapping(target = "medias", source = "medias")
    @Mapping(target = "author", source = "traveler")
    @Mapping(target = "poi", source = "poi")
    ReviewBaseResponse toReviewBaseResponse(Review review);

    @Mapping(target = "poi.id", source = "poiId")
    @Mapping(target = "traveler.id", source = "travelerId")
    Review toReview(ReviewCreateRequest request, Long poiId, Long travelerId);
}
