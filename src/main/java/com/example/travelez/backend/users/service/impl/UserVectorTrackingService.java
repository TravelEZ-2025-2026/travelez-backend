package com.example.travelez.backend.users.service.impl;

import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.exception.ApiException;
import com.example.travelez.backend.poi.repository.PoiRepository;
import com.example.travelez.backend.posts.repository.PostsRepository;
import com.example.travelez.backend.reaction.model.enums.ReactionTargetType;
import com.example.travelez.backend.users.model.User;
import com.example.travelez.backend.users.model.UserProfileVector;
import com.example.travelez.backend.users.repository.UserProfileVectorRepository;
import com.example.travelez.backend.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserVectorTrackingService {

    private final UserRepository userRepository;
    private final PoiRepository poiRepository;
    private final UserProfileVectorRepository vectorRepository;
    private final PostsRepository postsRepository;

    @Async
    @Transactional
    public void handleReactionToggle(Long userId, Long targetId, ReactionTargetType targetType, boolean isLike) {
        if (targetType != ReactionTargetType.POST) {
            return;
        }

        postsRepository.findById(targetId).ifPresent(post -> {
            if (post.getPoi() != null) {
                if (isLike) {
                    trackUserPreferenceOnLike(userId, post.getPoi().getId());
                } else {
                    trackUserPreferenceOnUnlike(userId, post.getPoi().getId());
                }
            }
        });
    }

    public void trackUserPreferenceOnLike(Long userId, Long poiId) {

        String poiVectorStr = poiRepository.findGeminiVectorStringById(poiId)
                .orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND, "Vector not found for POI ID: " + poiId));

        float[] poiVector = parseVectorString(poiVectorStr);
        String currentProfileStr = vectorRepository.findById(userId).map(UserProfileVector::getProfileVector).orElse(null);
        String targetVectorStr;

        if (currentProfileStr == null || currentProfileStr.isBlank()) {
            float[] initialVector = new float[poiVector.length];
            for (int i = 0; i < poiVector.length; i++) {
                initialVector[i] = 0.2f * poiVector[i];
            }
            targetVectorStr = Arrays.toString(initialVector);

        } else {
            float[] userVector = parseVectorString(currentProfileStr);

            if (userVector.length != poiVector.length) {
                throw new ApiException(ResultCode.INTERNAL_SERVER_ERROR, "Vector dimension error between User and POI");
            }

            float[] newVector = new float[userVector.length];
            for (int i = 0; i < userVector.length; i++) {
                newVector[i] = (0.8f * userVector[i]) + (0.2f * poiVector[i]); // Thêm thuật toán EMA
            }

            targetVectorStr = Arrays.toString(newVector);
        }

        vectorRepository.upsertProfileVector(userId, targetVectorStr);
    }

    public void trackUserPreferenceOnUnlike(Long userId, Long poiId) {

        String poiVectorStr = poiRepository.findGeminiVectorStringById(poiId)
                .orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND, "Vector not found for POI ID: " + poiId));

        UserProfileVector userVectorEntity = vectorRepository.findById(userId).orElse(null);

        if (userVectorEntity == null || userVectorEntity.getProfileVector() == null || userVectorEntity.getProfileVector().isBlank()) {
            log.info("User has no vector profile yet, nothing to revert.");
            return;
        }

        float[] poiVector = parseVectorString(poiVectorStr);
        float[] currentUserVector = parseVectorString(userVectorEntity.getProfileVector());

        if (currentUserVector.length != poiVector.length) {
            throw new ApiException(ResultCode.INTERNAL_SERVER_ERROR, "Vector dimension error");
        }

        float[] revertedVector = new float[currentUserVector.length];
        boolean isAllZero = true;

        for (int i = 0; i < currentUserVector.length; i++) {
            // Đảo ngược công thức: V_old = (V_new - 0.2 * V_poi) / 0.8
            revertedVector[i] = (currentUserVector[i] - (0.2f * poiVector[i])) / 0.8f;

            if (Math.abs(revertedVector[i]) > 1e-5) {
                isAllZero = false;
            }
        }

        if (isAllZero) {
            vectorRepository.deleteById(userId);
        } else {
            vectorRepository.upsertProfileVector(userId, Arrays.toString(revertedVector));
        }
    }

    private float[] parseVectorString(String vectorStr) {
        String cleaned = vectorStr.replaceAll("[\\[\\]\\s]", "");
        String[] parts = cleaned.split(",");
        float[] vector = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            vector[i] = Float.parseFloat(parts[i]);
        }
        return vector;
    }
}
