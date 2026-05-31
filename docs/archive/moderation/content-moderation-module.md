# Content Moderation Module

**Module:** moderation
**Completed:** 2026-05-26
**Developer:** AI (Antigravity)

---

## 📋 Tóm tắt

Module kiểm duyệt nội dung tự động (Content Moderation Engine) cho hệ thống Mạng xã hội Du lịch TravelEZ.
Sử dụng kiến trúc **Hybrid Model** kết hợp tiền kiểm đồng bộ bằng từ khóa và hậu kiểm bất đồng bộ bằng AI.

---

## 🎯 Chức năng

### Tiền kiểm đồng bộ (Synchronous Pre-filter)
- Khi user tạo Post/Review, nội dung được quét qua danh sách từ khóa cấm bằng **Aho-Corasick algorithm**
- Từ khóa cấm được cache trong **Redis** (warm-up lúc startup) với Caffeine fallback
- Nếu phát hiện từ khóa vi phạm: trả lỗi `400 Bad Request` ngay lập tức (không lưu DB)
- Thời gian xử lý < 5ms

### Hậu kiểm bất đồng bộ (Asynchronous AI Check)
- Nếu nội dung vượt qua tiền kiểm: lưu DB với trạng thái `PUBLISHED`, đẩy message vào **RabbitMQ**
- **ModerationWorker** consumer xử lý ngầm: gọi **Gemini 1.5 Flash API** với system prompt nghiêm ngặt
- Nếu AI phát hiện vi phạm: đặt `aiScanStatus = FLAGGED` (cột riêng, `status` vẫn `PUBLISHED`), tạo `ModerationAlert`
- Admin review thủ công: **APPROVE** (`aiScanStatus = CLEAN`) hoặc **BAN** (`status = BANNED` + `aiScanStatus = FLAGGED` + fire PostsStatusChangedEvent)

### Phạm vi kiểm duyệt
- ✅ **Post**: text (title + content) + images
- ✅ **Review**: text (content) + images
- ❌ Video: không kiểm duyệt trong phiên bản này
- ❌ POI: dự kiến mở rộng sau

### 5 Loại vi phạm (theo Luật An ninh mạng 2018)
1. `CHINH_TRI` - Chống nhà nước, xuyên tạc chính trị
2. `KHIEU_DAM` - Nội dung khiêu dâm, đồi trụy
3. `BAO_LUC` - Kích động bạo lực, kinh dị
4. `HANG_CAM` - Cờ bạc, ma túy, vũ khí
5. `NGON_TU_THU_HET` - Chửi thề, tin giả, phân biệt vùng miền

---

## 🔧 Implementation Details

### Files Created

**Database Migrations:**
- `src/main/resources/db/migration/V20__create_banned_keywords_table.sql` - Bảng banned_keywords
- `src/main/resources/db/migration/V21__create_moderation_alerts_table.sql` - Bảng moderation_alerts
- `src/main/resources/db/migration/V22__add_flagged_status_to_posts.sql` - Thêm FLAGGED vào post_status enum
- `src/main/resources/db/migration/V23__add_flagged_status_to_reviews.sql` - Thêm FLAGGED vào review_status_enum
- `src/main/resources/db/migration/V24__seed_banned_keywords.sql` - 21 sample keywords

**Enums:**
- `moderation/model/enums/ViolationType.java`
- `moderation/model/enums/KeywordSeverity.java` (LOW, MEDIUM, HIGH, CRITICAL)
- `moderation/model/enums/AlertStatus.java` (PENDING, APPROVED, BANNED, AUTO_RESOLVED)
- `moderation/model/enums/ModerationTargetType.java` (POST, REVIEW, POI)

**Entities:**
- `moderation/model/BannedKeyword.java`
- `moderation/model/ModerationAlert.java`

**DTOs:**
- `moderation/dto/request/BannedKeywordCreateRequest.java`
- `moderation/dto/request/BannedKeywordUpdateRequest.java`
- `moderation/dto/request/BannedKeywordSearchRequest.java`
- `moderation/dto/request/AlertReviewRequest.java`
- `moderation/dto/request/ModerationAlertSearchRequest.java`
- `moderation/dto/response/BannedKeywordResponse.java`
- `moderation/dto/response/ModerationAlertResponse.java`
- `moderation/dto/response/ModerationDashboardResponse.java`
- `moderation/dto/response/ModerationStatisticsResponse.java`
- `moderation/dto/internal/ModerationMessage.java`
- `moderation/dto/internal/ContentCheckResult.java`
- `moderation/dto/internal/AIModerationRequest.java`
- `moderation/dto/internal/AIModerationResponse.java`

**Mappers:**
- `moderation/mapper/BannedKeywordMapper.java`
- `moderation/mapper/ModerationAlertMapper.java`

**Repositories:**
- `moderation/repository/BannedKeywordRepository.java`
- `moderation/repository/ModerationAlertRepository.java`

**Services:**
- `moderation/service/BannedKeywordService.java` + `impl/BannedKeywordServiceImpl.java`
- `moderation/service/ContentModerationService.java` + `impl/ContentModerationServiceImpl.java`
- `moderation/service/AIModerationService.java` + `impl/AIModerationServiceImpl.java`
- `moderation/service/ModerationAlertService.java` + `impl/ModerationAlertServiceImpl.java`

**Infrastructure:**
- `moderation/filter/KeywordFilter.java` - Aho-Corasick implementation
- `moderation/cache/KeywordCacheManager.java` - Redis + Caffeine cache
- `moderation/worker/ModerationWorker.java` - RabbitMQ consumer
- `moderation/config/RabbitMQModerationConfig.java` - Queue/Exchange/DLQ config

**Controllers:**
- `moderation/controller/AdminModerationController.java` - `/api/admin/moderation`
- `moderation/controller/ModerationAlertController.java` - `/api/admin/moderation/alerts`

### Files Modified

- `posts/service/impl/PostsServiceImpl.java` - Integrate ContentModerationService trong createPost()
- `review/service/impl/ReviewServiceImpl.java` - Integrate ContentModerationService trong createReview()
- `posts/model/enums/PostStatus.java` - Xóa `FLAGGED` (đã chuyển sang AiScanStatus)
- `review/model/enums/ReviewStatus.java` - Xóa `FLAGGED` (đã chuyển sang AiScanStatus)
- `posts/model/Posts.java` - Thêm field `aiScanStatus` (default `PENDING_SCAN`)
- `review/model/Review.java` - Thêm field `aiScanStatus` (default `PENDING_SCAN`)
- `moderation/service/impl/ModerationAlertServiceImpl.java` - Dùng `aiScanStatus` thay vì `status` khi flag/approve
- `moderation/cache/KeywordCacheManager.java` - Dùng `List<KeywordCacheEntry>` thay `List<BannedKeyword>`
- `moderation/filter/KeywordFilter.java` - Dùng `KeywordCacheEntry` thay `BannedKeyword`
- `moderation/repository/BannedKeywordRepository.java` - Thêm `findAllActiveAsEntry()` projection query
- `infrastructure/config/RedisConfig.java` - Thêm JavaTimeModule cho LocalDateTime serialization
- `src/main/resources/application.yaml` - Thêm Redis, Cache, Moderation config
- `pom.xml` - Thêm Aho-Corasick, Redis, Caffeine, AMQP, Retry dependencies

### Files Created (moderation-fixes)

- `moderation/dto/internal/KeywordCacheEntry.java` - Lightweight cache record (5 fields)
- `posts/model/enums/AiScanStatus.java` - Enum: PENDING_SCAN, CLEAN, FLAGGED
- `db/migration/V25__add_ai_scan_status_column.sql` - Migration: thêm cột + migrate data

### Database Changes

**New tables:**
- `banned_keywords` (id, keyword, violation_type, severity, is_active, description, created_at, updated_at)
- `moderation_alerts` (id, target_id, target_type, violation_type, confidence_score, reason, status, reviewed_by, reviewed_at, admin_note, created_at, updated_at)

**Enum changes (initial):**
- `post_status`: thêm giá trị `FLAGGED` (V22)
- `review_status_enum`: thêm giá trị `FLAGGED` (V23)

**Enum changes (moderation-fixes V25):**
- `posts.ai_scan_status` (VARCHAR): cột mới, default `PENDING_SCAN`
- `review.ai_scan_status` (VARCHAR): cột mới, default `PENDING_SCAN`
- `post_status`: xóa `FLAGGED` → chỉ còn `PUBLISHED`, `ARCHIVED`, `BANNED`
- `review_status_enum`: xóa `FLAGGED` → chỉ còn `ACTIVE`, `BANNED`

**Indexes:**
- `idx_banned_keywords_active`, `idx_banned_keywords_violation_type`, `idx_banned_keywords_severity`
- `idx_moderation_alerts_status`, `idx_moderation_alerts_target`, `idx_moderation_alerts_created_at`, `idx_moderation_alerts_violation_type`

---

## 📡 API Documentation

### Keyword Management (Admin Only)

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| POST | `/api/admin/moderation/keywords` | Thêm từ khóa cấm |
| GET | `/api/admin/moderation/keywords` | Danh sách từ khóa (filter, pagination) |
| GET | `/api/admin/moderation/keywords/{id}` | Chi tiết từ khóa |
| PATCH | `/api/admin/moderation/keywords/{id}` | Cập nhật từ khóa |
| DELETE | `/api/admin/moderation/keywords/{id}` | Xóa từ khóa |
| PATCH | `/api/admin/moderation/keywords/{id}/toggle` | Bật/tắt từ khóa |
| POST | `/api/admin/moderation/keywords/refresh-cache` | Refresh Redis cache |

### Alert Management (Admin Only)

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| GET | `/api/admin/moderation/alerts` | Danh sách cảnh báo (filter, pagination) |
| GET | `/api/admin/moderation/alerts/{id}` | Chi tiết cảnh báo |
| POST | `/api/admin/moderation/alerts/{id}/approve` | Duyệt (về PUBLISHED) |
| POST | `/api/admin/moderation/alerts/{id}/ban` | Ban (về BANNED) |
| GET | `/api/admin/moderation/dashboard` | Dashboard thống kê |

---

## 📝 Notes

### Lưu ý quan trọng
1. **Optimistic UI**: Bài viết FLAGGED vẫn hiển thị công khai cho user, chờ admin review
2. **Cache warm-up**: 21 keywords được load vào Redis khi app khởi động
3. **Dead Letter Queue**: Messages fail 3 lần sẽ vào `content.moderation.dlq`
4. **Retry**: AI call retry 3 lần với backoff 1s → 2s → 4s

### Bugs Fixed During Implementation
1. `KeywordCacheManager.run()` - Added try/catch để không crash app khi table chưa tồn tại
2. `V23 migration` - Sửa tên enum type từ `review_status` → `review_status_enum`
3. `PostStatus` và `ReviewStatus` enums - Thêm `FLAGGED` value (đã refactor lại, xem bên dưới)
4. `updateTargetStatus()` - Implement thực sự thay vì no-op stub
5. `RedisConfig` - Thêm JavaTimeModule để serialize `LocalDateTime` đúng
6. `LazyInitializationException` in `ModerationWorker` - Sửa bằng cách eager fetch `medias` collection bằng `findByIdWithMedias` method trong `PostsRepository` và `ReviewRepository`

### Fixes (moderation-fixes — 2026-05-31)
1. **Redis cache load thừa data** — Thay `List<BannedKeyword>` bằng `List<KeywordCacheEntry>` (5 fields), dùng JPQL projection query `findAllActiveAsEntry()`
2. **`FLAGGED` state trộn lẫn vào main status** — Tách thành cột `ai_scan_status` riêng (`AiScanStatus` enum: PENDING_SCAN/CLEAN/FLAGGED). `PostStatus` và `ReviewStatus` không còn `FLAGGED`
3. **`updateTargetStatus()`** — Giờ set `aiScanStatus` thay vì `status`; `approveAlert()` set `CLEAN`, `banAlert()` set `BANNED` + `aiScanStatus=FLAGGED`

### Future Improvements
- POI content moderation
- Video moderation
- Bulk approve/ban alerts
- Real-time notification cho admin
- Auto-ban based on confidence score threshold > 0.95

---

## 🧪 Testing

Xem file: `docs/plans/content-moderation-module/TEST_GUIDE.md`

### Quick DB checks
```sql
-- Keywords
SELECT violation_type, COUNT(*) FROM banned_keywords GROUP BY violation_type;
-- Alerts
SELECT target_type, violation_type, confidence_score, status FROM moderation_alerts;
-- Post AI scan status check
SELECT id, title, status, ai_scan_status FROM posts WHERE ai_scan_status = 'FLAGGED';
SELECT id, title, status, ai_scan_status FROM posts WHERE status = 'BANNED';
-- Review AI scan status check
SELECT id, status, ai_scan_status FROM review WHERE ai_scan_status = 'FLAGGED';
```

---

## 🚀 Deployment Notes

### Environment Variables Required
- `REDIS_HOST`, `REDIS_PORT` (already in .env)
- `RABBITMQ_HOST`, `RABBITMQ_USERNAME`, `RABBITMQ_PASSWORD` (already in .env)
- `GEMINI_API_KEY` (already in .env)

### Database Migration
Flyway tự động chạy khi app khởi động. Thứ tự V20 → V25.
```bash
# V25 migration sẽ:
# 1. Thêm cột ai_scan_status vào posts và review
# 2. Migrate rows FLAGGED → aiScanStatus=FLAGGED, status=PUBLISHED/ACTIVE
# 3. Xóa FLAGGED khỏi post_status và review_status_enum
```
