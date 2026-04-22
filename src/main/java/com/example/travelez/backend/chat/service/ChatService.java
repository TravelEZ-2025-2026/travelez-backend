package com.example.travelez.backend.chat.service;

import com.example.travelez.backend.chat.dto.request.ChatRequest;
import com.example.travelez.backend.chat.dto.request.ConversationLoadRequest;
import com.example.travelez.backend.chat.dto.request.MessageLoadRequest;
import com.example.travelez.backend.chat.dto.response.ConversationMembersResponse;
import com.example.travelez.backend.chat.dto.response.ConversationResponse;
import com.example.travelez.backend.chat.dto.response.MessageResponse;
import com.example.travelez.backend.common.api.CursorResponse;

import java.util.List;

public interface ChatService {
    MessageResponse sendMessage(ChatRequest request);

    CursorResponse<ConversationResponse> getConversations(ConversationLoadRequest request);

    void clearConversation(Long conversationId);

    ConversationMembersResponse getConversationMembers(Long conversationId);

    CursorResponse<MessageResponse> getMessageConversation(MessageLoadRequest request);

    List<Long> getConversationMemberIds(Long conversationId);

    void deleteMessage(Long messageId);
}
