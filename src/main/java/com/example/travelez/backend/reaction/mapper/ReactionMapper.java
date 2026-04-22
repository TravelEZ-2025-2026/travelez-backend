package com.example.travelez.backend.reaction.mapper;

import com.example.travelez.backend.media.mapper.MediaMapper;
import com.example.travelez.backend.reaction.dto.response.ReactorsResponse;
import com.example.travelez.backend.reaction.model.Reaction;
import com.example.travelez.backend.users.mapper.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {MediaMapper.class, UserMapper.class})
public interface ReactionMapper {
    @Mapping(target = "user", source = "reaction.user")
    @Mapping(target = "isFollowedByMe", source = "isFollowedByMe", defaultValue = "false")
    @Mapping(target = "createdAt", source = "reaction.createdAt")
    ReactorsResponse toReactorsResponse(Reaction reaction, boolean isFollowedByMe);
}
