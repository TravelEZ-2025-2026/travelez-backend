package com.example.travelez.backend.chat.mapper;

import com.example.travelez.backend.chat.dto.response.MemberResponse;
import com.example.travelez.backend.chat.model.ConversationMember;
import com.example.travelez.backend.media.mapper.MediaMapper;
import com.example.travelez.backend.users.mapper.UserMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {MediaMapper.class, UserMapper.class})
public interface ConversationMemberMapper {
    MemberResponse toMemberResponse(ConversationMember member);
}