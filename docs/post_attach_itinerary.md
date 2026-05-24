---

# 🚀 Tài liệu Tích hợp API - Mạng xã hội: Đính kèm Lộ trình (Itinerary) vào Bài viết (Posts)

Tài liệu này hướng dẫn Frontend (FE) tích hợp tính năng cho phép người dùng đính kèm một Lộ trình Công khai (Public Itinerary) vào Bài viết của họ trên bảng tin.

**Luồng hoạt động (User Flow):**
1. User bấm nút "Tạo bài viết mới".
2. User bấm vào icon "Đính kèm lộ trình" -> Mở Modal/Dropdown hiển thị danh sách các lộ trình **Public** của chính họ.
3. User chọn 1 lộ trình (lấy `itineraryId`), chọn địa điểm (nếu thích), viết nội dung, tải ảnh và Submit.
4. Bài viết hiển thị trên Feed kèm theo "Thẻ Lộ trình" thu gọn.

---

## 🛠️ BƯỚC 1: Lấy danh sách Lộ trình để người dùng chọn
Sử dụng API lấy Lộ trình Public của chính user đang đăng nhập để đổ data vào Dropdown/Modal chọn lộ trình.

- **Endpoint:** `GET /api/management/itineraries/users/{userId}/public`
- **Path Variables:** `userId` (Lấy từ state/context của user đang đăng nhập).
- **Lưu ý:** Lộ trình phải được public từ trước mới hiện ở đây. API này có phân trang.

---

## 📤 BƯỚC 2: Gửi Request Đăng bài viết (Create Post)
API tạo bài viết hiện tại nhận dữ liệu theo dạng `multipart/form-data` (vì có tải ảnh/video). FE chỉ cần append thêm field `itineraryId` vào form data.

- **Endpoint:** `POST /api/posts`
- **Content-Type:** `multipart/form-data`
- **Headers:** `Authorization: Bearer {token}`

**Các trường trong Form Data (Payload):**

| Field | Type | Required | Description |
|---|---|---|---|
| `content` | String | **Yes** | Nội dung bài viết |
| `status` | String | **Yes** | Trạng thái bài viết (`PUBLISHED`, `ARCHIVED`) |
| `poiId` | Long | No | ID của địa điểm check-in (nếu có) |
| `itineraryId` | Long | **No** | **ID của lộ trình muốn đính kèm** |
| `topicTag` | String | No | Hashtag/Chủ đề bài viết |
| `files` | File[] | No | Danh sách file ảnh/video đính kèm |

**Bắt lỗi (Error Handling) từ Backend:**
Backend đã rào logic cực kỳ chặt chẽ, FE cần chú ý bắt các mã lỗi sau:
- `403 FORBIDDEN`: *"You can only attach your own itinerary"* -> User cố tình hack API gửi `itineraryId` của người khác.
- `400 BAD REQUEST`: *"You can only attach public itineraries. Please make this itinerary public first."* -> Lộ trình này đang là Private.

---

## 🖼️ BƯỚC 3: Hiển thị Bài viết trên Feed & Chi tiết
Khi gọi các API lấy danh sách bài viết (`GET /api/posts`, `GET /api/posts/search`...) hoặc xem chi tiết (`GET /api/posts/{postId}`), dữ liệu trả về đã được tích hợp sẵn object `itinerarySummary`.

**Response Mẫu (200 OK):**
```json
{
  "success": true,
  "code": 200,
  "message": "Posts fetched successfully",
  "data": {
    "content": [
      {
        "id": 999,
        "title": "Chuyến đi tuyệt vời!",
        "content": "Cảnh đẹp, đồ ăn ngon, mọi người nên thử lịch trình này nhé!",
        "topicTag": "DuLichDaLat",
        "createdAt": "2026-05-25T08:00:00",
        "author": {
            "id": 1,
            "username": "hieuvm",
            "avatar": "https://..."
        },
        "medias": [
            // Ảnh/Video của bài viết
        ],
        
        // ĐỊA ĐIỂM ĐÍNH KÈM (POI)
        "poiSummary": {
            "id": 45,
            "name": "Hồ Tuyền Lâm",
            "address": "Đà Lạt, Lâm Đồng"
        },
        
        // 📌 LỘ TRÌNH ĐÍNH KÈM (ITINERARY) - Sẽ là null nếu user không đính kèm
        "itinerarySummary": {
            "id": 101,
            "title": "Khám phá Đà Lạt 3N2Đ",
            "destinationCities": ["da_lat"],
            "styles": ["Nature", "Photography"],
            "startDate": "2026-06-01",
            "endDate": "2026-06-03",
            "status": "PLANNING",
            "ownerUsername": "hieuvm"
        }
      }
    ]
  }
}

```

### 💡 FE Tips (Mẹo thiết kế UI/UX)

1. **Linh hoạt đính kèm:** Object `poiSummary` và `itinerarySummary` là hoàn toàn độc lập. Một bài viết có thể có cả 2, có 1 trong 2, hoặc không có cái nào (cả 2 đều `null`). FE cần render UI linh hoạt theo trạng thái này.
2. **Click vào Thẻ Lộ trình trên Bài viết:** Khi người dùng lướt Feed và bấm vào Thẻ (Card) lộ trình trên bài viết, hãy điều hướng (navigate) họ sang màn hình **Chi tiết Lộ trình** (Dùng API `GET /api/itineraries/{id}`).
3. **Xử lý kịch bản lỗi khi xem (Edge Case):** Giả sử tác giả đã đính kèm lộ trình Public lên bài viết, nhưng sau đó họ vào cài đặt chuyển lộ trình đó thành **Private**. Lúc này, Thẻ lộ trình vẫn hiện trên Feed, nhưng nếu user khác click vào thẻ đó sẽ bị văng lỗi `403 FORBIDDEN` từ API get Itinerary Detail. FE cần xử lý mượt mà: Bắt lỗi 403, không chuyển trang mà hiện Toast báo: *"Lộ trình này đã được tác giả chuyển về chế độ riêng tư"*.

```

```