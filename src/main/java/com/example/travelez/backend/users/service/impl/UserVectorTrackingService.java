package com.example.travelez.backend.users.service.impl;

import com.example.travelez.backend.poi.repository.PoiRepository;
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

    @Async
    @Transactional
    public void trackUserPreferenceOnLike(Long userId, Long poiId) {
        log.info("========== [VECTOR TRACKING] START ==========");
        log.info("[1] Start calculating Vector for User ID: {} based on POI ID: {}", userId, poiId);

        // 1. Lấy chuỗi vector của POI
        String poiVectorStr = poiRepository.findGeminiVectorStringById(poiId).orElse(null);
        if (poiVectorStr == null) {
            log.warn("[X] ABORT: Vector not found for POI ID: {}", poiId);
            return;
        }
        log.info("[2] Successfully retrieved Vector for POI ID: {} (String length: {})", poiId, poiVectorStr.length());

        // 2. Lấy đối tượng User (chỉ để kiểm tra tồn tại)
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("[X] ABORT: User ID not found: {}", userId);
            return;
        }

        // --- FIX LỖI PARSE VECTOR TẠI ĐÂY ---
        float[] poiVector = parseVectorString(poiVectorStr);

        // 3. Xử lý logic trên bảng UserProfileVector riêng
        UserProfileVector userVectorEntity = vectorRepository.findById(userId)
                .orElse(new UserProfileVector(userId, null, null));

        String currentProfileStr = userVectorEntity.getProfileVector();

        if (currentProfileStr == null || currentProfileStr.isBlank()) {
            // Cold-start MVP
            log.info("[3] STATUS: COLD-START. User {} does not have a Profile Vector yet.", userId);
            userVectorEntity.setProfileVector(Arrays.toString(poiVector));
            log.info("[4] DONE: Assigned 100% of POI {}'s Vector to User {}", poiId, userId);
        } else {
            // Cập nhật ngầm (EMA)
            log.info("[3] STATUS: UPDATE (EMA). User {} ALREADY has a Profile Vector.", userId);
            float[] userVector = parseVectorString(currentProfileStr);

            if (userVector.length != poiVector.length) {
                log.error("[X] VECTOR DIMENSION ERROR: User Vector ({}) differs from POI Vector ({})",
                        userVector.length, poiVector.length);
                return;
            }

            // --- FIX VÒNG LẶP ARRAY TẠI ĐÂY ---
            float[] newVector = new float[userVector.length];
            for (int i = 0; i < userVector.length; i++) {
                newVector[i] = (0.8f * userVector[i]) + (0.2f * poiVector[i]); // Thêm thuật toán EMA
            }

            userVectorEntity.setProfileVector(Arrays.toString(newVector));
            log.info("[4] DONE: Blended Vector (80% User + 20% POI).");
        }

        vectorRepository.save(userVectorEntity);
        log.info("========== [VECTOR TRACKING] SUCCESS ==========\n");
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
