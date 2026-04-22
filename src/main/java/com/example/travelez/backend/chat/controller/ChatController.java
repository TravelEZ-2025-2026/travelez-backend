package com.example.travelez.backend.chat.controller;

import com.example.travelez.backend.chat.dto.request.ChatRequest;
import com.example.travelez.backend.chat.dto.request.ConversationLoadRequest;
import com.example.travelez.backend.chat.dto.request.MessageLoadRequest;
import com.example.travelez.backend.chat.dto.response.ConversationMembersResponse;
import com.example.travelez.backend.chat.dto.response.ConversationResponse;
import com.example.travelez.backend.chat.dto.response.MessageResponse;
import com.example.travelez.backend.chat.service.ChatService;
import com.example.travelez.backend.common.api.BaseResponse;
import com.example.travelez.backend.common.api.CursorResponse;
import com.example.travelez.backend.common.api.ResultCode;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "Chat endpoints")
public class ChatController {
    private final ChatService chatService;

    @PostMapping(name = "/send", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResponse<MessageResponse>> sendMessage(@ModelAttribute ChatRequest request) {
        MessageResponse response = chatService.sendMessage(request);
        return BaseResponse.success(response, ResultCode.SUCCESS, "Message sent successfully");
    }

    @GetMapping("/conversations")
    public ResponseEntity<BaseResponse<CursorResponse<ConversationResponse>>> getConversations(
            @ModelAttribute ConversationLoadRequest request
    ) {
        if (request.getSize() > 50) request.setSize(50);
        CursorResponse<ConversationResponse> response = chatService.getConversations(request);
        return BaseResponse.success(response, ResultCode.SUCCESS, "Conversations fetched successfully");
    }

    @DeleteMapping("/conversations/{conversationId}/clear")
    public ResponseEntity<BaseResponse<Void>> clearConversation(@PathVariable Long conversationId) {
        chatService.clearConversation(conversationId);
        return BaseResponse.success(null, ResultCode.SUCCESS, "Conversation cleared successfully");
    }

    @GetMapping("/conversations/{conversationId}/members")
    public ResponseEntity<BaseResponse<ConversationMembersResponse>> getConversationById(@PathVariable Long conversationId) {
        ConversationMembersResponse response = chatService.getConversationMembers(conversationId);
        return BaseResponse.success(response, ResultCode.SUCCESS, "Conversation fetched successfully");
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<BaseResponse<CursorResponse<MessageResponse>>> getMessageConversation(@PathVariable Long conversationId, @RequestParam(required = false, defaultValue = "10") int size, @RequestParam(required = false) Long lastMessageId) {
        MessageLoadRequest request = MessageLoadRequest.builder()
                .conversationId(conversationId)
                .size(size)
                .lastMessageId(lastMessageId)
                .build();
        CursorResponse<MessageResponse> response = chatService.getMessageConversation(request);
        return BaseResponse.success(response, ResultCode.SUCCESS, "Message fetched successfully");
    }

    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<BaseResponse<Void>> deleteMessage(@PathVariable Long messageId) {
        chatService.deleteMessage(messageId);
        return BaseResponse.success(null, ResultCode.SUCCESS, "Message deleted successfully");
    }

}
