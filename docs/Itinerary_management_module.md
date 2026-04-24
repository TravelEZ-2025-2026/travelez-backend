
```markdown
# Tài liệu Tích hợp API Module Quản lý Lịch trình (Itinerary Management - M2)

Tài liệu này hướng dẫn cách sử dụng các API liên quan đến tính năng chia sẻ, quản lý quyền truy cập và các tiện ích mở rộng cho lịch trình.

## 1. Tổng quan luồng nghiệp vụ
- **Quyền sở hữu:** Chỉ **Chủ sở hữu (Owner)** của lịch trình mới có quyền chia sẻ hoặc thu hồi quyền truy cập của người khác.
- **Chia sẻ:** Việc chia sẻ được thực hiện thông qua `username` của người nhận.
- **Truy cập:** Khi được chia sẻ, lịch trình sẽ xuất hiện trong danh sách "Được chia sẻ với tôi" của người nhận.

---

## 2. Đối tượng dữ liệu (Data Models)

### ItinerarySummaryResponse
Dữ liệu tóm tắt dùng để hiển thị danh sách lịch trình (giống module M1).

```json
{
  "id": 10,
  "title": "Chuyến du lịch Đà Lạt",
  "destinationCities": ["da_lat"],
  "styles": ["Relaxing"],
  "startDate": "2025-12-01",
  "endDate": "2025-12-05",
  "status": "PLANNED",
  "createdAt": "2024-04-20T08:30:00",
  "ownerUsername": "nguyenvana"
}
```

---

## 3. Danh sách Endpoint Chi tiết

### 3.1. Chia sẻ lịch trình với người dùng khác
- **Endpoint:** `/api/management/itineraries/{id}/share`
- **Method:** `POST`
- **Mô tả:** Cấp quyền xem chi tiết lịch trình cho một người dùng khác thông qua tên đăng nhập (`username`).

**Tham số:**
- `id` (Path Variable): ID của lịch trình muốn chia sẻ.
- `username` (Query Param): Tên đăng nhập của người nhận.

**Ví dụ gọi:** `POST /api/management/itineraries/42/share?username=hieuvm`

**Ràng buộc:**
- Không thể tự chia sẻ cho chính mình.
- Người nhận phải tồn tại trong hệ thống.
- Chỉ chủ sở hữu lịch trình mới được thực hiện.

---

### 3.2. Thu hồi quyền chia sẻ
- **Endpoint:** `/api/management/itineraries/{id}/share/{username}`
- **Method:** `DELETE`
- **Mô tả:** Huỷ bỏ quyền truy cập vào lịch trình của một người dùng đã được chia sẻ trước đó.

**Tham số:**
- `id` (Path Variable): ID của lịch trình.
- `username` (Path Variable): Tên đăng nhập của người muốn thu hồi quyền.

**Ví dụ gọi:** `DELETE /api/management/itineraries/42/share/hieuvm`

---

### 3.3. Danh sách lịch trình được chia sẻ với tôi
- **Endpoint:** `/api/management/itineraries/shared-with-me`
- **Method:** `GET`
- **Mô tả:** Lấy danh sách phân trang các lịch trình mà người dùng khác đã chia sẻ cho bạn.

**💡 Lưu ý đặc biệt cho Frontend:**
- **Nhận diện người gửi:** Trong các field trả về, hãy lưu ý sử dụng field `ownerUsername`. Đây là dấu hiệu để hiển thị cho user biết ai là người đã tạo và share lịch trình này (Ví dụ UI: *"Được chia sẻ bởi hieuvm"*).
- **Xem chi tiết lịch trình:** Để xem chi tiết các hoạt động trong chuyến đi này, Frontend **không cần gọi API mới**. Hãy lấy `id` của lịch trình trong danh sách này và tái sử dụng lại API **Xem chi tiết (Get Detail)** của Module 1 (`GET /api/itineraries/{id}`). Hệ thống phân quyền của Backend đã tự động mở khoá API này cho những tài khoản nằm trong danh sách được share.

**Query Params:**
- `page` (Integer): Số thứ tự trang (Mặc định: 0).
- `size` (Integer): Số phần tử trên mỗi trang (Mặc định: 10).

**Response Data (`CommonPage<ItinerarySummaryResponse>`):**
```json
{
  "content": [
    {
      "id": 105,
      "title": "Hành trình xuyên Việt",
      "ownerUsername": "tranvanb",
      // ... các field tóm tắt khác
    }
  ],
  "totalElements": 1,
  "totalPages": 1
}
```

---

### 3.4. Xuất lịch trình sang Google Calendar (Sắp ra mắt)
- **Endpoint:** `/api/management/itineraries/{id}/export-calendar`
- **Method:** `POST`
- **Mô tả:** Đồng bộ các hoạt động trong lịch trình vào lịch cá nhân của Google Calendar.

**Lưu ý:** Hiện tại API này đang trả về lỗi `403 FORBIDDEN` do tính năng đang trong quá trình phát triển. Frontend có thể thiết kế sẵn nút bấm nhưng tạm thời chưa cần gọi API này.

---

## 4. Bảng mã lỗi đặc thù

| HTTP Code | Message | Giải thích |
| :--- | :--- | :--- |
| 403 | Only owner can share itinerary | Bạn không phải chủ sở hữu nên không có quyền chia sẻ. |
| 404 | Target user not found | Username người nhận không tồn tại. |
| 422 | You cannot share itinerary with yourself | Lỗi khi tự điền username của mình để chia sẻ. |
| 422 | This itinerary has already been shared... | Lịch trình này đã được chia sẻ cho người này trước đó rồi. |
```