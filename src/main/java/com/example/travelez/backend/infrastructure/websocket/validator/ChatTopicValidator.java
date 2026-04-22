package com.example.travelez.backend.infrastructure.websocket.validator;

import com.example.travelez.backend.chat.repository.ConversationRepository;
import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.exception.ApiException;
import com.example.travelez.backend.security.component.UserPrinciple;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

@Component
@RequiredArgsConstructor
public class ChatTopicValidator implements TopicValidator {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final ConversationRepository conversationRepository;

    @Override
    public boolean supports(String destination) {
        return pathMatcher.match("/topic/conversation.*", destination);
    }

    @Override
    public void validate(UserPrinciple user, String destination) {
        try {
            String idStr = destination.substring(destination.lastIndexOf('.') + 1);
            Long conversationId = Long.parseLong(idStr);

            // Check user is a member of this conversation
            boolean isMember = conversationRepository.existsByIdAndMembersUserId(conversationId, user.getUserId());

            if (!isMember) throw new ApiException(ResultCode.FORBIDDEN, "You are not a member of this conversation");
        } catch (NumberFormatException e) {
            throw new ApiException(ResultCode.BAD_REQUEST, "Invalid conversation ID format");
        }
    }
}
