package com.example.travelez.backend.posts.mapper;

import com.example.travelez.backend.itinerary.mapper.ItineraryMapper;
import com.example.travelez.backend.media.mapper.MediaMapper;
import com.example.travelez.backend.media.model.Media;
import com.example.travelez.backend.poi.mapper.PoiMapper;
import com.example.travelez.backend.posts.dto.request.PostsCreateRequest;
import com.example.travelez.backend.posts.dto.request.PostsUpdateRequest;
import com.example.travelez.backend.posts.dto.response.*;
import com.example.travelez.backend.posts.model.Posts;
import com.example.travelez.backend.posts.repository.projection.TopPoiProjection;
import com.example.travelez.backend.posts.repository.projection.TopTagProjection;
import com.example.travelez.backend.users.mapper.UserMapper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", uses = {MediaMapper.class, UserMapper.class, PoiMapper.class, ItineraryMapper.class})
public interface PostsMapper {

    @Mapping(target = "user.id", source = "userId")
    @Mapping(target = "poi.id", source = "poiId")
    @Mapping(target = "itinerary.id", source = "itineraryId")
    Posts toPosts(PostsCreateRequest request, Long userId, Long poiId, Long itineraryId);

    @BeanMapping(nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
    void updatePostsFromRequest(PostsUpdateRequest request, @MappingTarget Posts posts);

    @Mapping(target = "author", source = "posts.user")
    @Mapping(target = "poiSummary", source = "posts.poi")
    @Mapping(source = "posts.itinerary", target = "itinerarySummary")
    PostsDetailResponse toPostsDetailResponse(Posts posts, List<Media> medias, Long commentCount);

    @Mapping(target = "author", source = "posts.user")
    @Mapping(target = "medias", source = "medias")
    @Mapping(target = "poiSummary", source = "posts.poi")
    @Mapping(target = "reactionCount", source = "reactionCount")
    @Mapping(target = "isReactedByMe", source = "isReactedByMe")
    @Mapping(source = "posts.itinerary", target = "itinerarySummary")
    PostResponse toPostResponse(Posts posts, List<Media> medias, Long commentCount, Long reactionCount, boolean isReactedByMe);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "countPost", source = "countPost")
    TopPoiResponse toTopPoiResponse(TopPoiProjection topPoi);

    @Mapping(target = "name", source = "topicTag")
    @Mapping(target = "countPost", source = "countPost")
    TopTagResponse toTopTagResponse(TopTagProjection topTag);

    // Admin Posts Response
    @Mapping(target = "poiSummary", source = "posts.poi")
    @Mapping(target = "author", source = "posts.user")
    @Mapping(target = "commentCount", source = "commentCount")
    @Mapping(target = "reactionCount", source = "reactionCount")
    AdminPostsResponse toAdminPostsResponse(Posts posts, Long commentCount, Long reactionCount);

}
