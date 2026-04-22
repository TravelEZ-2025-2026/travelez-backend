package com.example.travelez.backend.chat.model;

import com.example.travelez.backend.chat.model.enums.ConversationRole;
import com.example.travelez.backend.common.utils.DateTimesUtils;
import com.example.travelez.backend.users.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversation_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Builder
public class ConversationMember {
    @EmbeddedId
    private ConversationMemberId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("conversationId")
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private ConversationRole role;

    @CreatedDate
    @Column(name = "joined_at", updatable = false)
    private LocalDateTime joinedAt;

    @Column(name = "cleared_at", nullable = true)
    private LocalDateTime clearedAt;

    public LocalDateTime getEffectiveClearedAt() {
        return clearedAt == null ? DateTimesUtils.MIN_EPOCH_TIME : clearedAt;
    }
}
