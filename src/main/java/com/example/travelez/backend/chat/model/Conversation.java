package com.example.travelez.backend.chat.model;

import com.example.travelez.backend.chat.model.enums.ConversationType;
import com.example.travelez.backend.media.model.Media;
import com.example.travelez.backend.users.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "conversation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Builder
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_name")
    private String groupName;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(name = "last_message_content")
    private String lastMessageContent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_message_sender_id")
    private User lastMessageSender;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private ConversationType type;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToOne()
    @JoinColumn(name = "group_avatar_id")
    private Media groupAvatar;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ConversationMember> members;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Message> messages;
}
