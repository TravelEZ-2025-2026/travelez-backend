package com.example.travelez.backend.posts.listener;

import com.example.travelez.backend.posts.event.PostsStatusChangedEvent;
import com.example.travelez.backend.posts.model.PostStatusHistory;
import com.example.travelez.backend.posts.repository.PostStatusHistoryRepository;
import com.example.travelez.backend.users.model.User;
import com.example.travelez.backend.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostsAuditListener {
    private final PostStatusHistoryRepository historyRepository;
    private final UserRepository userRepository;

    // Chạy trong cùng Transaction với AdminPostService
    @EventListener
    public void onPostStatusChanged(PostsStatusChangedEvent event) {
        User adminRef = userRepository.getReferenceById(event.getAdminId());

        PostStatusHistory history = PostStatusHistory.builder()
                .posts(event.getPosts())
                .admin(adminRef)
                .oldStatus(event.getOldStatus())
                .newStatus(event.getNewStatus())
                .reason(event.getReason())
                .createdAt(LocalDateTime.now())
                .build();

        historyRepository.save(history);
    }
}
