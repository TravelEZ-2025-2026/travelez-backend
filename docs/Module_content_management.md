# TÀI LIỆU THIẾT KẾ KIẾN TRÚC & TRIỂN KHAI: MODULE KIỂM DUYỆT NỘI DUNG TỰ ĐỘNG

## Dự án: Hệ thống Mạng xã hội Du lịch Travelez

Tài liệu này tổng hợp toàn bộ giải pháp kiến trúc, luồng xử lý và bộ prompt kiểm duyệt tự động để cung cấp cho AI thực thi lập trình (Code Generation AI).

---

## 1. TỔNG QUAN TÍNH NĂNG

Hệ thống kiểm duyệt nội dung (Content Moderation Engine) chịu trách nhiệm tự động quét, phát hiện và xử lý các nội dung vi phạm pháp luật, thuần phong mỹ tục Việt Nam hoặc nội dung rác (Spam) đối với:

- **Đối tượng kiểm duyệt:** Bài đăng (`Post`), Đánh giá địa điểm (`Review`), và các nội dung văn bản kèm phương tiện của Địa điểm (`POI`).
- **Dữ liệu đầu vào:** Văn bản (Tiêu đề, nội dung) và Hình ảnh (Mảng các URL ảnh đính kèm).
- **Phạm vi:** Chỉ kiểm duyệt **text content**. Images, Video sẽ không được kiểm duyệt trong phiên bản này.
- **Mô hình tiếp cận:** **Hybrid Model (Lai)** kết hợp giữa **Tiền kiểm đồng bộ bằng Từ khóa** và **Hậu kiểm bất đồng bộ bằng AI (LLM) qua Message Queue**.

---

## 2. KIẾN TRÚC HỆ THỐNG & LUỒNG XỬ LÝ (WORKFLOW)

Quy trình xử lý áp dụng cơ chế **Optimistic UI (Giao diện lạc quan)**, ưu tiên trải nghiệm người dùng không bị tắc nghẽn (Zero-blocking).

```text
[User] -> (Đăng Post/Review)
|
+---> [Bước 1: Tiền kiểm Đồng bộ] -> Quét Trie / Aho-Corasick trên RAM/Redis
| |
| +---> [VI PHẠM] -> Trả lỗi HTTP 400 lập tức (Chặn không lưu DB).
| +---> [HỢP LỆ] -> Lưu DB trạng thái 'PUBLISHED' -> Trả về HTTP 200 cho User.
|
+---> [Bước 2: Kích hoạt Message] -> Đẩy {targetId, targetType} vào RabbitMQ
|
v
[RabbitMQ Queue]
|
v
[Bước 3: Hậu kiểm Bất đồng bộ] <--------- [Worker Consumer]
|
+---> Fetch Text & Media URLs từ DB
+---> Gọi API Multimodal LLM (Gemini 1.5 Flash / GPT-4o) kèm System Prompt
|
+---> [Kết quả AI]
|
+---> SAFE -> Đặt aiScanStatus = CLEAN, giữ nguyên status = PUBLISHED.
+---> VIOLATION -> Đặt aiScanStatus = FLAGGED (status vẫn = PUBLISHED)
      -> Tạo bản ghi ModerationAlert
      -> Bài viết vẫn hiển thị công khai
      -> Chờ Admin review thủ công để BAN hoặc APPROVE.
```

### Chi tiết các bước:

1. **Bước 1 (Đồng bộ - Synchronous): Chặn từ khóa thô (Pre-processing Filter)**
   - Khi User bấm gửi nội dung, Spring Boot sử dụng cấu trúc dữ liệu **Trie** hoặc thuật toán **Aho-Corasick** quét qua danh sách từ khóa cấm được lưu trên **Redis/Caffeine Cache**.
   - Thời gian xử lý yêu cầu < 2ms. Nếu dính từ cấm, từ chối lưu vào DB, trả lỗi ngay.
2. **Bước 2 (Bất đồng bộ - Asynchronous): Đẩy tin nhắn vào Queue**
   - Nếu văn bản sạch vượt qua vòng 1, hệ thống lưu bản ghi vào database dưới trạng thái `PUBLISHED`.
   - Ngay lập tức, một thông điệp nhẹ dạng JSON dạng `{"targetId": 123, "targetType": "POST"}` được đẩy vào RabbitMQ queue mang tên `content.moderation.queue`. API hoàn tất và trả phản hồi thành công cho User.
3. **Bước 3 (Bất đồng bộ - Asynchronous): Worker & AI Processing**
   - **Consumer Component** lắng nghe queue, nhặt message lên xử lý ngầm (Background Thread).
   - Dựa vào `targetType` và `targetId`, nó query DB để lấy toàn bộ text (Title, Content) và mảng danh sách `media_url` (chỉ lấy ảnh, bỏ qua video).
   - Gửi gói dữ liệu (Text + Image URLs) qua API của Mô hình ngôn ngữ lớn (LLM Multimodal) kèm theo bộ quy tắc nghiêm ngặt bằng Prompt.
   - **Nếu AI phát hiện vi phạm:**
     - Đặt `aiScanStatus = FLAGGED` (cột riêng, không đổi `status`)
     - Tạo bản ghi `ModerationAlert` với thông tin vi phạm
     - Bài viết vẫn hiển thị công khai cho người dùng (`status = PUBLISHED`)
     - Chờ Admin review thủ công để quyết định BAN hoặc APPROVE
   - **Nếu AI xác nhận an toàn:** Đặt `aiScanStatus = CLEAN`, giữ `status = PUBLISHED`

---

## 3. BỘ QUY TẮC KIỂM DUYỆT (BÁM SÁT PHÁP LUẬT VIỆT NAM)

AI cần được cấu hình để đánh giá nội dung nghiêm ngặt theo 5 tiêu chí nền tảng của Luật An ninh mạng 2018 và Nghị định 72/2013/NĐ-CP:

1. **CHINH_TRI (Chính trị & Chủ quyền):** Tuyên truyền chống phá Nhà nước, nói xấu Đảng/Chính phủ, xuyên tạc lịch sử, kích động biểu tình, sử dụng hình ảnh bản đồ sai lệch chủ quyền Việt Nam (thiếu Hoàng Sa, Trường Sa hoặc có đường lưỡi bò).
2. **KHIEU_DAM (Thuần phong mỹ tục & Khiêu dâm):** Hình ảnh/ngôn từ đồi trụy, khỏa thân, kích dục, hở hang phản cảm, vi phạm nghiêm trọng thuần phong mỹ tục Việt Nam.
3. **BAO_LUC (Bạo lực & Kinh dị):** Hình ảnh hoặc văn bản mô tả cảnh máu me, rùng rợn, tai nạn kinh dị, kích động hành vi bạo lực, tự tử, tự làm hại bản thân, hành hạ động vật.
4. **HANG_CAM (Hàng hóa nguy hiểm & Tệ nạn):** Quảng cáo, lôi kéo tham gia cờ bạc, cá độ (tài xỉu, cá độ bóng đá), buôn bán ma túy, chất kích thích, vũ khí, vật liệu nổ, mại dâm hoặc các dịch vụ bất hợp pháp.
5. **NGON_TU_THU_HET (Ngôn từ độc hại & Tin giả):** Chửi thề tục tĩu, xúc phạm danh dự cá nhân, phân biệt vùng miền sâu sắc, phân biệt tôn giáo, chủng tộc, truyền bá thông tin sai sự thật chưa kiểm chứng gây hoang mang dư luận (Fake News).

---

## 4. CẤU TRÚC PROMPT AI VÀ ĐỊNH DẠNG ĐẦU RA (OUTPUT SPECIFICATION)

Để code Java (Jackson/Gson) dễ dàng parse kết quả trả về từ AI, yêu cầu nghiêm ngặt AI **chỉ trả về định dạng JSON thuần**, không bọc trong ký tự markdown (như \`\`\`json).

### Hệ thống System Prompt truyền cho AI:

````text
Bạn là một chuyên gia kiểm duyệt nội dung (Content Moderator) cấp cao cho nền tảng Mạng xã hội Du lịch Travelez tại Việt Nam. Nhiệm vụ của bạn là phân tích văn bản và danh sách hình ảnh được cung cấp nhằm phát hiện hành vi vi phạm pháp luật và tiêu chuẩn cộng đồng.
Hãy phân tích toàn bộ ngữ cảnh dữ liệu dựa trên 5 bộ quy tắc nghiêm ngặt sau:
1. CHINH_TRI: Chống nhà nước, bôi nhọ lãnh tụ, bàn luận chính trị nhạy cảm, bản đồ sai chủ quyền (thiếu Hoàng Sa, Trường Sa, có đường lưỡi bò).
2. KHIEU_DAM: Hình ảnh khỏa thân, khiêu dâm, ngôn từ đồi trụy, kích dục, phản cảm.
3. BAO_LUC: Máu me, kinh dị, kích động bạo lực, tự tử, ngược đãi.
4. HANG_CAM: Quảng cáo cá độ, đánh bạc (tài xỉu, bet), ma túy, vũ khí, chất cấm.
5. NGON_TU_THU_HET: Chửi thề tục tĩu, xúc phạm lăng mạ cá nhân, phân biệt vùng miền, tin giả (fake news).
YÊU CẦU ĐẦU RA:
Trả về kết quả DUY NHẤT dưới dạng chuỗi JSON chuẩn hóa, KHÔNG chứa ký tự bao bọc ```json, KHÔNG có văn bản giải thích thừa thãi bên ngoài.
Cấu trúc định dạng JSON bắt buộc:
{
  "isSafe": boolean,          // true nếu nội dung hoàn toàn sạch, false nếu vi phạm ít nhất 1 quy tắc.
  "violationType": "String",  // Điền 1 trong các giá trị: "CHINH_TRI", "KHIEU_DAM", "BAO_LUC", "HANG_CAM", "NGON_TU_THU_HET". Nếu isSafe là true, bắt buộc điền "NONE".
  "confidenceScore": number,  // Điểm số từ 0.0 đến 1.0 thể hiện độ chính xác tự tin của AI.
  "reason": "String"          // Giải thích ngắn gọn lý do vi phạm bằng tiếng Việt (Nếu isSafe là true, để chuỗi rỗng "").
}
````

---

## 5. CÔNG NGHỆ ÁP DỤNG & CHIẾN LƯỢC TỐI ƯU HIỆU NĂNG

Khi lập trình module này, cần tuân thủ các chỉ dẫn kỹ thuật sau để đảm bảo Non-Functional Requirement (NFR) đạt chuẩn:

- **Thuật toán xử lý chuỗi:** Sử dụng thư viện triển khai thuật toán **Aho-Corasick** (Ví dụ thư viện org.ahocorasick:ahocorasick) cho bộ lọc tiền kiểm. Tránh dùng vòng lặp .contains() thủ công gây nghẽn CPU (Complexity O(N) thay vì O(N\*M)).
- **Caching Strategy:** Toàn bộ danh sách từ cấm phải được nạp (Warm-up) trực tiếp vào **Redis** hoặc **Caffeine Local Cache** ngay khi Spring Boot khởi động (CommandLineRunner). Tuyệt đối không query trực tiếp xuống SQL Database ở vòng tiền kiểm.
- **Xử lý Media:** Images,Video sẽ không được kiểm duyệt trong phiên bản này.
- **Xử lý sự kiện đồng bộ trạng thái:** Khi Admin xác nhận BAN một bài viết (qua ModerationAlert), hệ thống cập nhật trạng thái bài viết thành BANNED và kích hoạt sự kiện PostsStatusChangedEvent (đã có sẵn trong hệ thống) để dọn dẹp các Report liên quan đang ở trạng thái PENDING sang trạng thái đóng tự động (AUTO_RESOLVED), tránh hiện tượng rác dữ liệu.

---

## 6. MÔ HÌNH DỮ LIỆU (DATA MODEL)

### 6.1. Trạng thái bài viết (Post/Review Status)

```java
// Trạng thái hiển thị công khai
public enum PostStatus {
    PUBLISHED,  // Bài viết công khai bình thường
    ARCHIVED,   // Lưu trữ (ẩn khỏi feed công khai)
    BANNED      // Admin xác nhận vi phạm và ban (ẩn khỏi công khai)
}

public enum ReviewStatus {
    ACTIVE,     // Review công khai bình thường
    BANNED      // Admin xác nhận vi phạm và ban
}

// Trạng thái quét AI — cột riêng `ai_scan_status`
public enum AiScanStatus {
    PENDING_SCAN,  // Mới đăng, chưa AI quét
    CLEAN,         // AI quét xong, không vi phạm
    FLAGGED        // AI phát hiện vi phạm, chờ admin review
}
```

> **Thiết kế:** `status` phục vụ public visibility; `aiScanStatus` phục vụ trạng thái quét AI. Hai khái niệm độc lập — bài `FLAGGED` về AI vẫn có `status = PUBLISHED` và hiển thị công khai cho đến khi admin quyết định BAN.

### 6.2. Entity: BannedKeyword (Từ khóa cấm)

```java
@Entity
@Table(name = "banned_keywords")
public class BannedKeyword extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "keyword", nullable = false, unique = true)
    private String keyword;  // Từ khóa cấm

    @Enumerated(EnumType.STRING)
    @Column(name = "violation_type", nullable = false)
    private ViolationType violationType;  // Loại vi phạm

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;  // Bật/tắt từ khóa

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false)
    private KeywordSeverity severity;  // Mức độ nghiêm trọng

    @Column(name = "description")
    private String description;  // Ghi chú
}

public enum KeywordSeverity {
    LOW,      // Cảnh báo nhẹ
    MEDIUM,   // Cảnh báo trung bình
    HIGH,     // Chặn ngay lập tức
    CRITICAL  // Chặn + log đặc biệt
}
```

### 6.3. Entity: ModerationAlert (Cảnh báo từ AI)

```java
@Entity
@Table(name = "moderation_alerts")
public class ModerationAlert extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "target_id", nullable = false)
    private Long targetId;  // ID của Post/Review/POI

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private ModerationTargetType targetType;  // POST, REVIEW, POI

    @Enumerated(EnumType.STRING)
    @Column(name = "violation_type", nullable = false)
    private ViolationType violationType;

    @Column(name = "confidence_score", nullable = false)
    private Double confidenceScore;  // 0.0 - 1.0

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;  // Lý do AI đưa ra

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AlertStatus status = AlertStatus.PENDING;

    @ManyToOne
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;  // Admin review

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "admin_note", columnDefinition = "TEXT")
    private String adminNote;  // Ghi chú của admin
}

public enum AlertStatus {
    PENDING,        // Chờ admin review
    APPROVED,       // Admin xác nhận an toàn (đổi bài viết về PUBLISHED)
    BANNED,         // Admin xác nhận vi phạm và ban (đổi bài viết thành BANNED)
    AUTO_RESOLVED   // Tự động đóng (nếu bài viết bị xóa)
}

public enum ModerationTargetType {
    POST,
    REVIEW,
    POI
}
```

---

## 7. API ENDPOINTS

### 7.1. Quản lý Từ khóa Vi phạm (Admin Only)

**Base URL:** `/api/admin/moderation/keywords`

| Method | Endpoint                                       | Description                            | Auth  |
| ------ | ---------------------------------------------- | -------------------------------------- | ----- |
| POST   | `/api/admin/moderation/keywords`               | Thêm từ khóa mới                       | ADMIN |
| GET    | `/api/admin/moderation/keywords`               | Danh sách từ khóa (phân trang, filter) | ADMIN |
| GET    | `/api/admin/moderation/keywords/{id}`          | Chi tiết từ khóa                       | ADMIN |
| PATCH  | `/api/admin/moderation/keywords/{id}`          | Cập nhật từ khóa                       | ADMIN |
| DELETE | `/api/admin/moderation/keywords/{id}`          | Xóa từ khóa                            | ADMIN |
| PATCH  | `/api/admin/moderation/keywords/{id}/toggle`   | Bật/tắt từ khóa                        | ADMIN |
| POST   | `/api/admin/moderation/keywords/refresh-cache` | Refresh cache Redis                    | ADMIN |

### 7.2. Quản lý Cảnh báo Vi phạm (Admin Only)

**Base URL:** `/api/admin/moderation/alerts`

| Method | Endpoint                                    | Description                                        | Auth  |
| ------ | ------------------------------------------- | -------------------------------------------------- | ----- |
| GET    | `/api/admin/moderation/alerts`              | Danh sách cảnh báo (filter: status, violationType) | ADMIN |
| GET    | `/api/admin/moderation/alerts/{id}`         | Chi tiết cảnh báo                                  | ADMIN |
| POST   | `/api/admin/moderation/alerts/{id}/approve` | Duyệt bài viết (đổi về PUBLISHED)                  | ADMIN |
| POST   | `/api/admin/moderation/alerts/{id}/ban`     | Ban bài viết (đổi thành BANNED)                    | ADMIN |
| GET    | `/api/admin/moderation/alerts/statistics`   | Thống kê cảnh báo                                  | ADMIN |

### 7.3. Dashboard (Admin Only)

**Base URL:** `/api/admin/moderation`

| Method | Endpoint                          | Description                                   | Auth  |
| ------ | --------------------------------- | --------------------------------------------- | ----- |
| GET    | `/api/admin/moderation/dashboard` | Tổng quan: số cảnh báo pending, tỷ lệ vi phạm | ADMIN |

---

## 8. REQUEST/RESPONSE DTOs

### 8.1. Banned Keyword DTOs

**BannedKeywordCreateRequest**

```java
@Data
public class BannedKeywordCreateRequest {
    @NotBlank(message = "Keyword is required")
    @Size(min = 1, max = 255)
    private String keyword;

    @NotNull(message = "Violation type is required")
    private ViolationType violationType;

    @NotNull(message = "Severity is required")
    private KeywordSeverity severity;

    private String description;
}
```

**BannedKeywordUpdateRequest**

```java
@Data
public class BannedKeywordUpdateRequest {
    private ViolationType violationType;
    private KeywordSeverity severity;
    private String description;
}
```

**BannedKeywordResponse**

```java
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BannedKeywordResponse {
    private Long id;
    private String keyword;
    private ViolationType violationType;
    private KeywordSeverity severity;
    private Boolean isActive;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

**BannedKeywordSearchRequest** (extends PaginationRequest)

```java
@Data
@EqualsAndHashCode(callSuper = true)
public class BannedKeywordSearchRequest extends PaginationRequest {
    private ViolationType violationType;  // Filter theo loại vi phạm
    private KeywordSeverity severity;     // Filter theo mức độ
    private Boolean isActive;             // Filter theo trạng thái
    private String keyword;               // Search theo keyword
}
```

### 8.2. Moderation Alert DTOs

**ModerationAlertResponse**

```java
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ModerationAlertResponse {
    private Long id;
    private Long targetId;
    private ModerationTargetType targetType;
    private ViolationType violationType;
    private Double confidenceScore;
    private String reason;
    private AlertStatus status;
    private LocalDateTime createdAt;

    // Thông tin bài viết bị cảnh báo
    private String targetTitle;
    private String targetContent;
    private String targetAuthorName;
    private Long targetAuthorId;

    // Thông tin review
    private UserSummaryResponse reviewedBy;
    private LocalDateTime reviewedAt;
    private String adminNote;
}
```

**AlertReviewRequest**

```java
@Data
public class AlertReviewRequest {
    @NotBlank(message = "Admin note is required")
    @Size(max = 1000)
    private String adminNote;
}
```

**ModerationAlertSearchRequest** (extends PaginationRequest)

```java
@Data
@EqualsAndHashCode(callSuper = true)
public class ModerationAlertSearchRequest extends PaginationRequest {
    private AlertStatus status;              // Filter theo trạng thái
    private ViolationType violationType;     // Filter theo loại vi phạm
    private ModerationTargetType targetType; // Filter theo loại đối tượng
    private Long targetAuthorId;             // Filter theo tác giả
    private LocalDateTime fromDate;          // Filter theo thời gian
    private LocalDateTime toDate;
}
```

**ModerationDashboardResponse**

```java
@Data
@Builder
public class ModerationDashboardResponse {
    private Long totalAlerts;
    private Long pendingAlerts;
    private Long approvedAlerts;
    private Long bannedAlerts;
    private Long totalBannedKeywords;
    private Long activeKeywords;

    // Thống kê theo loại vi phạm
    private Map<ViolationType, Long> violationTypeStats;

    // Thống kê theo target type
    private Map<ModerationTargetType, Long> targetTypeStats;
}
```

**ModerationStatisticsResponse**

```java
@Data
@Builder
public class ModerationStatisticsResponse {
    private LocalDateTime fromDate;
    private LocalDateTime toDate;

    // Thống kê theo ngày
    private List<DailyModerationStats> dailyStats;

    // Thống kê theo loại vi phạm
    private Map<ViolationType, Long> violationStats;

    // Top từ khóa vi phạm nhiều nhất
    private List<KeywordViolationStats> topViolatedKeywords;
}

@Data
@Builder
class DailyModerationStats {
    private LocalDate date;
    private Long totalAlerts;
    private Long approved;
    private Long banned;
}

@Data
@Builder
class KeywordViolationStats {
    private String keyword;
    private Long count;
    private ViolationType violationType;
}
```

---

## 9. CẤU TRÚC MODULE

```
com.example.travelez.backend/
├── moderation/
│   ├── controller/
│   │   ├── AdminModerationController.java      // Quản lý từ khóa + dashboard
│   │   └── ModerationAlertController.java      // Quản lý cảnh báo
│   ├── service/
│   │   ├── BannedKeywordService.java
│   │   ├── ModerationAlertService.java
│   │   ├── ContentModerationService.java       // Core service (tiền kiểm + hậu kiểm)
│   │   ├── AIModerationService.java            // Gọi AI (Gemini/GPT)
│   │   └── impl/
│   ├── repository/
│   │   ├── BannedKeywordRepository.java
│   │   └── ModerationAlertRepository.java
│   ├── model/
│   │   ├── BannedKeyword.java
│   │   ├── ModerationAlert.java
│   │   └── enums/
│   │       ├── ViolationType.java
│   │       ├── KeywordSeverity.java
│   │       ├── AlertStatus.java
│   │       └── ModerationTargetType.java
│   ├── dto/
│   │   ├── request/
│   │   │   ├── BannedKeywordCreateRequest.java
│   │   │   ├── BannedKeywordUpdateRequest.java
│   │   │   ├── BannedKeywordSearchRequest.java
│   │   │   ├── AlertReviewRequest.java
│   │   │   └── ModerationAlertSearchRequest.java
│   │   └── response/
│   │       ├── BannedKeywordResponse.java
│   │       ├── ModerationAlertResponse.java
│   │       ├── ModerationDashboardResponse.java
│   │       └── ModerationStatisticsResponse.java
│   ├── mapper/
│   │   ├── BannedKeywordMapper.java
│   │   └── ModerationAlertMapper.java
│   ├── worker/
│   │   └── ModerationWorker.java               // RabbitMQ Consumer
│   ├── filter/
│   │   └── KeywordFilter.java                  // Aho-Corasick implementation
│   ├── cache/
│   │   └── KeywordCacheManager.java            // Redis cache management
│   └── config/
│       └── RabbitMQModerationConfig.java
```

---

## 10. TECHNICAL IMPLEMENTATION NOTES

### 10.1. Cache Strategy

- **Redis** cho từ khóa cấm (distributed cache)
- **Caffeine** cho local cache (fallback nếu Redis down)
- **TTL:** 24h, tự động refresh khi admin update
- **Cache Key:** `moderation:keywords:all`
- **Warm-up:** Load tất cả keywords vào cache khi app khởi động (CommandLineRunner)
- **Cache format:** Lưu `List<KeywordCacheEntry>` (5 fields: id, keyword, violationType, severity, description) — không lưu JPA entity `BannedKeyword` để tránh serialize thừa metadata

### 10.2. Message Queue

- **Queue name:** `content.moderation.queue`
- **Dead Letter Queue:** `content.moderation.dlq` (xử lý failed messages)
- **Retry:** 3 lần với exponential backoff (1s, 2s, 4s)
- **Message format:** `{"targetId": 123, "targetType": "POST"}`

### 10.3. AI Integration

- **Primary:** Gemini 1.5 Flash (rẻ, nhanh)
- **Fallback:** GPT-4o mini (nếu Gemini fail)
- **Timeout:** 30 seconds
- **Rate limiting:** 100 requests/minute
- **Response format:** JSON only (không markdown)

### 10.4. Security

- Tất cả endpoints `/api/admin/moderation/**` yêu cầu role `ADMIN`
- Audit log mọi thao tác admin (thêm/sửa/xóa keyword, approve/ban alert)

### 10.5. Performance Optimization

- Aho-Corasick algorithm cho keyword matching (O(n + m + z) thay vì O(n\*m))
- Batch processing cho AI moderation (nếu có nhiều bài viết cùng lúc)
- Index database: `target_id`, `target_type`, `status`, `created_at`

### 10.6. Admin Actions Flow

**Khi Admin APPROVE alert:**

1. Cập nhật `ModerationAlert.status = APPROVED`
2. Cập nhật `Post/Review.aiScanStatus = CLEAN` (không đổi `status`)
3. Ghi `reviewedBy`, `reviewedAt`, `adminNote`
4. Log audit

**Khi Admin BAN alert:**

1. Cập nhật `ModerationAlert.status = BANNED`
2. Cập nhật `Post/Review.status = BANNED` + `aiScanStatus = FLAGGED`
3. Phát sự kiện `PostsStatusChangedEvent` (để đóng Reports liên quan)
4. Ghi `reviewedBy`, `reviewedAt`, `adminNote`
5. Log audit

---

## 11. TỔNG KẾT YÊU CẦU

### Phạm vi kiểm duyệt:

- ✅ Text content (title, content)
- ❌ Images (hình ảnh)
- ❌ Video (không kiểm duyệt trong phiên bản này)

### Tính năng đã loại bỏ:

- ❌ Import/Export hàng loạt từ khóa (CSV/JSON)
- ❌ Notification real-time cho admin khi có cảnh báo mới
- ❌ Notification cho tác giả khi bài viết bị FLAGGED
- ❌ Giới hạn thời gian review cho admin
- ❌ Bulk approve/ban nhiều alerts cùng lúc
- ❌ Kiểm duyệt video

### Tính năng giữ lại:

- ✅ Tiền kiểm đồng bộ bằng Aho-Corasick + Redis cache
- ✅ Hậu kiểm bất đồng bộ bằng AI (Gemini/GPT) qua RabbitMQ
- ✅ Kiểm duyệt text content và images
- ✅ Bài viết FLAGGED vẫn hiển thị công khai
- ✅ Admin quản lý từ khóa (CRUD + toggle active)
- ✅ Admin review thủ công alerts (approve/ban)
- ✅ Dashboard + Statistics
- ✅ Refresh cache thủ công

```

```
