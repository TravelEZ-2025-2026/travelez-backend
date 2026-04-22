package com.example.travelez.backend.chat.mapper;

import com.example.travelez.backend.chat.dto.response.ConversationResponse;
import com.example.travelez.backend.chat.model.Conversation;
import com.example.travelez.backend.media.mapper.MediaMapper;
import com.example.travelez.backend.users.mapper.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {MediaMapper.class, UserMapper.class})
public interface ConversationMapper {
    //    avatar, name, isGroup
    @Mapping(target = "avatar", source = "groupAvatar.url")
    ConversationResponse toConversationResponse(Conversation conversation);
}
