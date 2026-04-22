package com.example.travelez.backend.notification.service.impl;


import com.example.travelez.backend.chat.dto.response.ConversationSocketResponse;
import com.example.travelez.backend.chat.dto.response.MessageRecalledPayload;
import com.example.travelez.backend.chat.dto.response.MessageResponse;
import com.example.travelez.backend.chat.service.ChatService;
import com.example.travelez.backend.common.dto.SocketEvent;
import com.example.travelez.backend.common.model.enums.SocketEventType;
import com.example.travelez.backend.infrastructure.websocket.SocketService;
import com.example.travelez.backend.notification.dto.NewMessageAlert;
import com.example.travelez.backend.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final SocketService socketService;
    private final ChatService chatService;

    @Override
    public void handleNewMessageNotification(MessageResponse message, ConversationSocketResponse conversation) {
        // 1. SYNC DATA: Gửi tin nhắn vào phòng chat (cho những người đang mở cửa sổ chat)
        String chatTopic = "/topic/conversation." + message.getConversationId();
        SocketEvent<MessageResponse> chatEvent = SocketEvent.<MessageResponse>builder()
                .type(SocketEventType.NEW_MESSAGE)
                .payload(message)
                .build();
        socketService.sendToTopic(chatTopic, chatEvent);

        // // 2. ALERT: Gửi thông báo cho từng thành viên (cho những người đang lướt Newfeeds)
        // Lấy danh sách ID thành viên
        List<Long> memberIds = chatService.getConversationMemberIds(message.getConversationId());
        SocketEvent<NewMessageAlert> alertEvent = SocketEvent.<NewMessageAlert>builder()
                .type(SocketEventType.NEW_MESSAGE_ALERT)
                .payload(NewMessageAlert.builder()
                        .id(message.getId())
                        .conversationId(message.getConversationId())
                        .content(message.getContent())
                        .sender(message.getSender())
                        .medias(message.getMedias())
                        .createdAt(message.getCreatedAt())
                        .groupName(conversation.getGroupName())
                        .avatar(conversation.getAvatar())
                        .build())
                .build();
        for (Long memberId : memberIds) {
            // if (memberId.equals(message.getSender().getUserId())) {
            //     continue;
            // }
            socketService.sendToTopic("/topic/user." + memberId, alertEvent);
        }
    }

    @Override
    public void handleMessageRecalledNotification(MessageRecalledPayload payload) {
        String chatTopic = "/topic/conversation." + payload.getConversationId();
        SocketEvent<MessageRecalledPayload> chatEvent = SocketEvent.<MessageRecalledPayload>builder()
                .type(SocketEventType.MESSAGE_RECALLED)
                .payload(payload)
                .build();
        socketService.sendToTopic(chatTopic, chatEvent);
    }
}
