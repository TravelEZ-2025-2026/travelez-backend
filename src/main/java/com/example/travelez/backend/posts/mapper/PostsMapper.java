package com.example.travelez.backend.posts.mapper;

import com.example.travelez.backend.media.mapper.MediaMapper;
import com.example.travelez.backend.media.model.Media;
import com.example.travelez.backend.poi.mapper.PoiMapper;
import com.example.travelez.backend.posts.dto.request.PostsCreateRequest;
import com.example.travelez.backend.posts.dto.request.PostsUpdateRequest;
import com.example.travelez.backend.posts.dto.response.PostResponse;
import com.example.travelez.backend.posts.dto.response.PostsDetailResponse;
import com.example.travelez.backend.posts.model.Posts;
import com.example.travelez.backend.users.mapper.UserMapper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", uses = {MediaMapper.class, UserMapper.class, PoiMapper.class})
public interface PostsMapper {

    @Mapping(target = "user.id", source = "userId")
    @Mapping(target = "poi.id", source = "poiId")
    Posts toPosts(PostsCreateRequest request, Long userId, Long poiId);

    @BeanMapping(nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
    void updatePostsFromRequest(PostsUpdateRequest request, @MappingTarget Posts posts);

    @Mapping(target = "author", source = "posts.user")
    PostsDetailResponse toPostsDetailResponse(Posts posts, List<Media> medias, Long commentCount);

    @Mapping(target = "author", source = "posts.user")
    @Mapping(target = "medias", source = "medias")
    @Mapping(target = "poiSummary", source = "posts.poi")
    @Mapping(target = "reactionCount", source = "reactionCount")
    @Mapping(target = "isReactedByMe", source = "isReactedByMe")
    PostResponse toPostResponse(Posts posts, List<Media> medias, Long commentCount, Long reactionCount, boolean isReactedByMe);
}
