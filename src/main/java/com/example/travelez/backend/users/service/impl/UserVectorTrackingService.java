package com.example.travelez.backend.users.service.impl;

import com.example.travelez.backend.poi.repository.PoiRepository;
import com.example.travelez.backend.users.model.User;
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

    @Async
    @Transactional
    public void trackUserPreferenceOnLike(Long userId, Long poiId) {
        log.info("========== [VECTOR TRACKING] START ==========");
        log.info("[1] Bắt đầu tính toán Vector cho User ID: {} dựa trên POI ID: {}", userId, poiId);

        // 1. Lấy chuỗi vector của POI
        String poiVectorStr = poiRepository.findGeminiVectorStringById(poiId).orElse(null);
        if (poiVectorStr == null) {
            log.warn("[X] HỦY BỎ: Không tìm thấy Vector cho POI ID: {}", poiId);
            return;
        }
        log.info("[2] Đã lấy thành công Vector của POI ID: {} (Chiều dài chuỗi: {})", poiId, poiVectorStr.length());

        // 2. Lấy đối tượng User
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("[X] HỦY BỎ: Không tìm thấy User ID: {}", userId);
            return;
        }

        // 3. Xử lý logic
        float[] poiVector = parseVectorString(poiVectorStr);
        String currentProfileStr = user.getProfileVector();

        if (currentProfileStr == null || currentProfileStr.isBlank()) {
            // Cold-start MVP
            log.info("[3] TRẠNG THÁI: COLD-START. User {} chưa có Profile Vector.", userId);
            user.setProfileVector(Arrays.toString(poiVector));
            log.info("[4] HOÀN TẤT: Đã gán 100% Vector của POI {} cho User {}", poiId, userId);
        } else {
            // Cập nhật ngầm (EMA)
            log.info("[3] TRẠNG THÁI: UPDATE (EMA). User {} ĐÃ có Profile Vector.", userId);
            float[] userVector = parseVectorString(currentProfileStr);

            if (userVector.length != poiVector.length) {
                log.error("[X] LỖI CHIỀU VECTOR: User Vector ({}) khác POI Vector ({})", userVector.length, poiVector.length);
                return;
            }

            float[] newVector = new float[userVector.length];
            for (int i = 0; i < userVector.length; i++) {
                newVector[i] = (0.8f * userVector[i]) + (0.2f * poiVector[i]);
            }

            user.setProfileVector(Arrays.toString(newVector));
            log.info("[4] HOÀN TẤT: Đã trộn Vector (80% User + 20% POI).");
        }

        userRepository.save(user);
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
