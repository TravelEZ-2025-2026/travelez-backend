package com.example.travelez.backend.chat.repository;

import com.example.travelez.backend.chat.model.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @EntityGraph(attributePaths = {"sender"})
    @Query("SELECT m FROM Message m " +
            "WHERE m.conversation.id = :conversationId " +
            "AND m.deletedAt IS NULL " +
            "AND m.createdAt > :clearedAt " +
            "ORDER BY m.id DESC")
    Slice<Message> findLatestMessages(Long conversationId, LocalDateTime clearedAt, Pageable pageable);

    @EntityGraph(attributePaths = {"sender"})
    @Query("SELECT m FROM Message m " +
            "WHERE m.conversation.id = :conversationId " +
            "AND m.deletedAt IS NULL " +
            "AND m.id < :lastMessageId " +
            "AND m.createdAt > :clearedAt " +
            "ORDER BY m.id DESC")
    Slice<Message> findOlderMessages(Long conversationId, Long lastMessageId, LocalDateTime clearedAt, Pageable pageable);

    @EntityGraph(attributePaths = {"conversation"})
    Optional<Message> findByIdAndSenderId(Long messageId, Long senderId);

    @Query("SELECT m.id FROM Message m " +
            "WHERE m.deletedAt IS NOT NULL " +
            "AND m.deletedAt < :thresholdDate")
    List<Long> findRecalledMessageIdsBefore(LocalDateTime thresholdDate, Pageable pageable);
}

