package com.example.travelez.backend.moderation.model.enums;

public enum AlertStatus {
    PENDING,        // Chờ admin review
    APPROVED,       // Admin xác nhận an toàn
    BANNED,         // Admin xác nhận vi phạm và ban
    AUTO_RESOLVED   // Tự động đóng (bài viết bị xóa)
}
