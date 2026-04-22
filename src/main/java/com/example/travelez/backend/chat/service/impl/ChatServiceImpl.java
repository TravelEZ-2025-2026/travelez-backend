package com.example.travelez.backend.chat.service.impl;

import com.example.travelez.backend.chat.dto.request.ChatRequest;
import com.example.travelez.backend.chat.dto.request.ConversationLoadRequest;
import com.example.travelez.backend.chat.dto.request.MessageLoadRequest;
import com.example.travelez.backend.chat.dto.response.*;
import com.example.travelez.backend.chat.event.MessageRecalledEvent;
import com.example.travelez.backend.chat.event.MessageSaveEvent;
import com.example.travelez.backend.chat.mapper.ConversationMapper;
import com.example.travelez.backend.chat.mapper.ConversationMemberMapper;
import com.example.travelez.backend.chat.mapper.MessageMapper;
import com.example.travelez.backend.chat.model.Conversation;
import com.example.travelez.backend.chat.model.ConversationMember;
import com.example.travelez.backend.chat.model.ConversationMemberId;
import com.example.travelez.backend.chat.model.Message;
import com.example.travelez.backend.chat.model.enums.ConversationRole;
import com.example.travelez.backend.chat.model.enums.ConversationType;
import com.example.travelez.backend.chat.repository.ConversationMemberRepository;
import com.example.travelez.backend.chat.repository.ConversationRepository;
import com.example.travelez.backend.chat.repository.MessageRepository;
import com.example.travelez.backend.chat.service.ChatService;
import com.example.travelez.backend.common.api.CursorResponse;
import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.exception.ApiException;
import com.example.travelez.backend.common.exception.Asserts;
import com.example.travelez.backend.common.utils.PaginationUtils;
import com.example.travelez.backend.common.utils.SecurityUtils;
import com.example.travelez.backend.infrastructure.filestorage.dto.UploadFileResult;
import com.example.travelez.backend.media.dto.enums.MediaTarget;
import com.example.travelez.backend.media.mapper.MediaMapper;
import com.example.travelez.backend.media.model.Media;
import com.example.travelez.backend.media.repository.MediaRepository;
import com.example.travelez.backend.media.service.MediaService;
import com.example.travelez.backend.users.model.User;
import com.example.travelez.backend.users.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final TransactionTemplate transactionTemplate;
    private final ApplicationEventPublisher eventPublisher;

    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository conversationMemberRepository;
    private final MediaRepository mediaRepository;
    private final MessageRepository messageRepository;

    private final MediaService mediaService;
    private final UserService userService;

    private final MessageMapper messageMapper;
    private final MediaMapper mediaMapper;
    private final ConversationMapper conversationMapper;
    private final ConversationMemberMapper conversationMemberMapper;

    @Override
    public MessageResponse sendMessage(ChatRequest request) {
        if (request.getContent() == null && request.getFiles() == null) {
            Asserts.fail(ResultCode.BAD_REQUEST, "Content or files must be provided");
        }
        Conversation conversation = resolveConversation(request.getConversationId(), request.getReceiverId());
        Long conversationId = conversation.getId();

        String folder_path = "chat/" + conversationId.toString() + "/";
        List<UploadFileResult> uploadedFiles = mediaService.uploadFilesParallel(request.getFiles(), folder_path);
        try {
            MessageResponse messageResponse = transactionTemplate.execute(status -> {
                User sender = userService.getUserById(SecurityUtils.getCurrentUserId());
                Message message = messageMapper.toMessage(request, conversationId, sender);
                Message savedMessage = messageRepository.save(message);
                List<Media> medias = mediaService.attachMediasToEntity(uploadedFiles, MediaTarget.MESSAGE, savedMessage.getId());

                Conversation managedConv = conversationRepository.findById(conversationId).get();
                managedConv.setLastMessageAt(LocalDateTime.now());
                managedConv.setLastMessageContent(message.getContent());
                managedConv.setLastMessageSender(sender);
                return messageMapper.toMessageResponse(savedMessage, medias);
            });

            // MessageResponse messageResponse = messageMapper.toMessageResponse(resultMessage, resultMessage.getMedias());
            String groupName = conversation.getGroupName() != null ? conversation.getGroupName() : messageResponse.getSender().getFullName();
            String groupAvatar = conversation.getGroupAvatar() == null ? null : conversation.getGroupAvatar().getUrl();
            ConversationType convType = conversation.getType();
            ConversationSocketResponse socketResponse = ConversationSocketResponse.builder()
                    .avatar(groupAvatar)
                    .groupName(groupName)
                    .type(convType)
                    .isGroup(convType == ConversationType.GROUP)
                    .build();

            eventPublisher.publishEvent(new MessageSaveEvent(messageResponse, socketResponse));

            return messageResponse;
        } catch (Exception e) {
            List<String> fileNames = uploadedFiles.stream().map(UploadFileResult::getCloudName).toList();
            mediaService.cleanupFilesAsync(fileNames);
            if (e.getCause() instanceof ApiException) {
                throw (ApiException) e.getCause();
            }
            Asserts.fail(ResultCode.INTERNAL_SERVER_ERROR, "Failed to send message: " + e.getMessage());
        }

        return null;
    }

    @Override
    public CursorResponse<ConversationResponse> getConversations(ConversationLoadRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        Pageable limit = PaginationUtils.getPageable(0, request.getSize());
        Slice<Conversation> slice;
        if (request.getNextCursor() == null) {
            slice = conversationRepository.findFirstPage(userId, limit);
        } else {
            CursorConversationResponse cursorData = decodeCursor(request.getNextCursor());
            slice = conversationRepository.findNextPage(userId, cursorData.getLastMessageAt(), cursorData.getLastConversationId(), limit);
        }

//        get list conversation private
        List<Long> privateConversationIds = slice.getContent().stream()
                .filter(c -> c.getType() == ConversationType.PRIVATE)
                .map(Conversation::getId)
                .toList();
//        Fetch other member for private chat
        Map<Long, User> membersMap;
        if (!privateConversationIds.isEmpty()) {
//            fetch other user of conversation
            List<ConversationMember> members = conversationMemberRepository.findAllByConversationIdInAndUserIdNotEqual(privateConversationIds, userId);
            membersMap = members.stream().collect(Collectors.toMap(m -> m.getConversation().getId(), ConversationMember::getUser));
        } else {
            membersMap = Map.of();
        }

        List<ConversationResponse> responses = slice.getContent().stream().map(conv -> {
            ConversationResponse resp = conversationMapper.toConversationResponse(conv);
            boolean isGroup = conv.getType() == ConversationType.GROUP;
            resp.setGroup(isGroup);
//            private chat 1 - 1
            if (!isGroup) {
                User otherMember = membersMap.get(conv.getId());
                resp.setAvatar(otherMember.getAvatar() == null ? null : otherMember.getAvatar().getUrl());
                resp.setGroupName(otherMember.getFullName());
            }
            return resp;
        }).toList();
        String nextCursor = responses.isEmpty() ? null : encodeCursor(responses.getLast().getLastMessageAt(), responses.getLast().getId());
        return new CursorResponse<>(responses, nextCursor, slice.hasNext());
    }

    @Override
    @Transactional
    public void clearConversation(Long conversationId) {
        Long userId = SecurityUtils.getCurrentUserId();
        ConversationMember member = conversationMemberRepository.findById(new ConversationMemberId(conversationId, userId)).orElseThrow(() -> new ApiException("You are not a member of this conversation"));
        member.setClearedAt(LocalDateTime.now());
        conversationMemberRepository.save(member);
    }

    @Override
    public ConversationMembersResponse getConversationMembers(Long conversationId) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (!isMember(conversationId, userId)) {
            Asserts.fail(ResultCode.FORBIDDEN, "You are not a member of this conversation");
        }
        List<MemberResponse> members = conversationMemberRepository.findAllByConversationId(conversationId)
                .stream().map(conversationMemberMapper::toMemberResponse).toList();
        return ConversationMembersResponse.builder()
                .members(members)
                .numOfMem((long) members.size())
                .build();
    }

    @Override
    public CursorResponse<MessageResponse> getMessageConversation(MessageLoadRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (!isMember(request.getConversationId(), userId)) {
            Asserts.fail(ResultCode.FORBIDDEN, "You are not a member of this conversation");
        }
        Pageable limit = PageRequest.of(0, request.getSize());
        Slice<Message> slice;
        ConversationMember member = conversationMemberRepository.findById(new ConversationMemberId(request.getConversationId(), userId)).orElseThrow(() -> new ApiException("Member not found"));
        if (request.getLastMessageId() == null) {
            slice = messageRepository.findLatestMessages(request.getConversationId(), member.getEffectiveClearedAt(), limit);
        } else {
            slice = messageRepository.findOlderMessages(request.getConversationId(), request.getLastMessageId(), member.getEffectiveClearedAt(), limit);
        }
        List<Long> messageIds = slice.getContent().stream().map(Message::getId).toList();
        List<Object[]> mediasMessage = mediaRepository.findAllByMessageIds(messageIds);
        Map<Long, List<Media>> mediasMap = mediaService.groupMediaByParentId(mediasMessage);

        List<MessageResponse> responses = slice.getContent().stream().map(message -> {
            List<Media> medias = mediasMap.getOrDefault(message.getId(), List.of()).stream().toList();
            return messageMapper.toMessageResponse(message, medias);
        }).toList();
        String nextCursor = responses.isEmpty() ? null : responses.getLast().getId().toString();
        return new CursorResponse<>(responses, nextCursor, slice.hasNext());
    }

    @Override
    public List<Long> getConversationMemberIds(Long conversationId) {
        return conversationMemberRepository.findUserIdsByConversationId(conversationId);
    }

    @Override
    public void deleteMessage(Long messageId) {
        Long userId = SecurityUtils.getCurrentUserId();
        Message message = transactionTemplate.execute(status -> {
            Message foundMessage = messageRepository.findByIdAndSenderId(messageId, userId).orElseThrow(() -> new ApiException("Message not found"));
            if (foundMessage.getDeletedAt() != null) {
                return null;
            }
            foundMessage.setDeletedAt(LocalDateTime.now());
            return messageRepository.save(foundMessage);
        });
        if (message == null) {
            return;
        }
        eventPublisher.publishEvent(new MessageRecalledEvent(new MessageRecalledPayload(messageId, message.getConversation().getId())));
    }

    private Conversation resolveConversation(Long conversationId, Long receiverId) {
        return transactionTemplate.execute(status -> {
            if (conversationId != null) {
                return conversationRepository.findById(conversationId).orElseThrow(() -> new ApiException("Conversation not found"));
            } else if (receiverId != null) {
                Long senderId = SecurityUtils.getCurrentUserId();
                Optional<Conversation> existing = conversationRepository.findPrivateConversation(senderId, receiverId);
                return existing.orElseGet(() -> createPrivateConversation(receiverId));
            }
            Asserts.fail(ResultCode.BAD_REQUEST, "Invalid request: conversationId or receiverId required");
            return null;
        });
    }

    private Conversation createPrivateConversation(Long receiverId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (Objects.equals(currentUserId, receiverId)) {
            Asserts.fail(ResultCode.BAD_REQUEST, "You cannot create a conversation with yourself");
        }
        User sender = userService.getUserById(currentUserId);
        User receiver = userService.getUserById(receiverId);

        Conversation conversation = Conversation.builder()
                .type(ConversationType.PRIVATE)
                .build();
        List<ConversationMember> members = List.of(
                createMemberEntity(conversation, sender, ConversationRole.MEMBER),
                createMemberEntity(conversation, receiver, ConversationRole.MEMBER)
        );
        conversation.setMembers(members);
        return conversationRepository.save(conversation);
    }

    private String encodeCursor(LocalDateTime time, Long lastConversationId) {
        if (time == null) time = LocalDateTime.now();
        return time + "_" + lastConversationId;
    }

    private CursorConversationResponse decodeCursor(String cursor) {
        try {
            String[] parts = cursor.split("_");
            return new CursorConversationResponse(LocalDateTime.parse(parts[0]), Long.parseLong(parts[1]));
        } catch (Exception e) {
            throw new ApiException(ResultCode.BAD_REQUEST, "Invalid cursor format");
        }
    }

    private ConversationMember createMemberEntity(Conversation conv, User user, ConversationRole role) {
        ConversationMemberId memberId = new ConversationMemberId();
        memberId.setUserId(user.getId());
        return ConversationMember.builder()
                .id(memberId)
                .conversation(conv)
                .user(user)
                .role(role)
                .joinedAt(LocalDateTime.now())
                .build();
    }

    private boolean isMember(Long conversationId, Long userId) {
        return conversationMemberRepository.existsByConversationIdAndUserId(conversationId, userId);
    }

}
