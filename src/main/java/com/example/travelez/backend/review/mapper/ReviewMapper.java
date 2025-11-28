package com.example.travelez.backend.review.mapper;

import com.example.travelez.backend.review.dto.response.ReviewBaseResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.travelez.backend.media.mapper.MediaMapper;
import com.example.travelez.backend.review.model.Review;

@Mapper(componentModel = "spring", uses = { MediaMapper.class })
public interface ReviewMapper {

    @Mapping(target = "medias", source = "medias")
    ReviewBaseResponse toReviewBaseResponse(Review review);
}
