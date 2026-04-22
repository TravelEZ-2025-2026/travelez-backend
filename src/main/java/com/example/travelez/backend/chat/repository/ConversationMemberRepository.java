package com.example.travelez.backend.chat.repository;

import com.example.travelez.backend.chat.model.ConversationMember;
import com.example.travelez.backend.chat.model.ConversationMemberId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConversationMemberRepository extends JpaRepository<ConversationMember, ConversationMemberId> {

    @EntityGraph(attributePaths = {"user", "user.avatar"})
    @Query("SELECT m FROM ConversationMember m " +
            "WHERE m.conversation.id IN :conversationIds AND m.user.id != :userId")
    List<ConversationMember> findAllByConversationIdInAndUserIdNotEqual(@Param("conversationIds") List<Long> conversationIds, @Param("userId") Long userId);

    boolean existsByConversationIdAndUserId(Long conversationId, Long userId);

    @EntityGraph(attributePaths = {"user"})
    List<ConversationMember> findAllByConversationId(Long conversationId);


    @Query("SELECT m.user.id FROM ConversationMember m WHERE m.conversation.id = :conversationId")
    List<Long> findUserIdsByConversationId(Long conversationId);
}