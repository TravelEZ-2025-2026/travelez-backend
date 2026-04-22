package com.example.travelez.backend.chat.repository;

import com.example.travelez.backend.chat.model.Conversation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("SELECT c FROM Conversation c " +
            "JOIN ConversationMember m1 ON c.id = m1.conversation.id " +
            "JOIN ConversationMember m2 ON c.id = m2.conversation.id " +
            "WHERE c.type = 'PRIVATE' AND " +
            "m1.user.id = :userId AND m2.user.id = :receiverId ")
    Optional<Conversation> findPrivateConversation(@Param("userId") Long userId, @Param("receiverId") Long receiverId);

    boolean existsByIdAndMembersUserId(Long conversationId, Long userId);

    @EntityGraph(attributePaths = {"lastMessageSender", "groupAvatar"})
    @Query("SELECT c FROM Conversation c " +
            "JOIN c.members m " +
            "WHERE m.user.id = :userId " +
            "AND (m.clearedAt IS NULL OR m.clearedAt < c.lastMessageAt) " +
            "ORDER BY c.lastMessageAt DESC, c.id DESC ")
    Slice<Conversation> findFirstPage(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"lastMessageSender", "groupAvatar"})
    @Query("SELECT c FROM Conversation c " +
            "JOIN c.members m " +
            "WHERE m.user.id = :userId " +
            "AND (c.lastMessageAt < :lastTime OR (c.lastMessageAt = :lastTime AND c.id < :lastConversationId)) " +
            "AND (m.clearedAt IS NULL OR m.clearedAt < c.lastMessageAt) " +
            "ORDER BY c.lastMessageAt DESC, c.id DESC ")
    Slice<Conversation> findNextPage(Long userId, LocalDateTime lastTime, Long lastConversationId, Pageable pageable);

    @Query("SELECT c FROM Conversation c " +
            "JOIN ConversationMember m ON c.id = m.conversation.id " +
            "WHERE c.id = :conversationId AND m.user.id = :userId")
    Optional<Conversation> findByIdAndMembersUserId(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
}
