

# 🚀 Tài liệu Tích hợp API - Itinerary Management (Public & Collaboration)

Tài liệu này cung cấp đặc tả chi tiết cho các API quản lý Lộ trình (Itinerary), phục vụ 3 nhóm tính năng chính: **Khám phá cộng đồng (Explore), Hồ sơ cá nhân (Profile), và Cộng tác nhóm (Collaboration)**.

---

## 🌟 PHẦN 1: TÍNH NĂNG KHÁM PHÁ & CỘNG ĐỒNG (EXPLORE)
Sử dụng cho màn hình trang chủ hoặc tab "Khám phá", nơi hiển thị các lộ trình được mọi người chia sẻ công khai.

### 1.1. Lấy toàn bộ Lộ trình Public (Feed Mặc định)
API này dùng để lấy danh sách tất cả các lộ trình đã được bật Public trên toàn hệ thống. Tự động sắp xếp mới nhất lên đầu.

- **Endpoint:** `GET /api/management/itineraries/public`
- **Query Parameters:**
  - `page` (int, default: 0): Trang hiện tại.
  - `size` (int, default: 10): Số lượng item mỗi trang.

**Response (200 OK):**
```json
{
  "success": true,
  "code": 200,
  "message": "Retrieve all public itineraries successfully",
  "data": {
    "content": [
      {
        "id": 101,
        "title": "Khám phá Tây Bắc mùa lúa chín",
        "destinationCities": ["sa_pa", "mu_cang_chai"],
        "styles": ["Nature", "Photography"],
        "startDate": "2026-09-10",
        "endDate": "2026-09-15",
        "status": "COMPLETED",
        "createdAt": "2026-05-20T08:00:00",
        "ownerUsername": "hieuvm",
        "isPublic": true
      }
    ],
    "totalPages": 5,
    "totalElements": 50,
    "pageSize": 10,
    "pageNumber": 0,
    "empty": false
  }
}


```

> 💡 **FE Tip:** Dùng API này khi user vừa vào tab Khám phá mà chưa gõ từ khóa nào. Các trường `styles` và `destinationCities` rất thích hợp để làm các thẻ tag (chip) nhỏ trên Card.

### 1.2. Tìm kiếm Lộ trình bằng Trí tuệ Nhân tạo (AI Semantic Search)

Tìm kiếm thông minh thông qua prompt tự nhiên của user (Vector Search).

* **Endpoint:** `GET /api/management/itineraries/public/search`
* **Query Parameters:**
* `prompt` (String, required): Ví dụ: *"Tìm chuyến đi biển 3 ngày cho gia đình có trẻ nhỏ"*.
* `page`, `size` (Phân trang).



**Response:** Giống hệt cấu trúc `1.1` (Trả về `CommonPage<ItinerarySummaryResponse>`).

---

## 👤 PHẦN 2: TÍNH NĂNG HỒ SƠ CÁ NHÂN (PROFILE)

Sử dụng khi xem trang cá nhân của một người dùng bất kỳ.

### 2.1. Lấy danh sách Lộ trình Public của một User

Render tab "Lộ trình" trên trang cá nhân của user.

* **Endpoint:** `GET /api/management/itineraries/users/{userId}/public`
* **Path Variables:**
* `userId` (Long): ID của user đang xem profile.


* **Query Parameters:** `page`, `size`.

**Response:** Giống cấu trúc `1.1` (Trả về `CommonPage<ItinerarySummaryResponse>`).

### 2.2. Bật / Tắt trạng thái Public của Lộ trình

Chỉ chủ sở hữu lộ trình mới có quyền gọi API này. Thường dùng cho nút Toggle Switch trong phần cài đặt chuyến đi.

* **Endpoint:** `PATCH /api/management/itineraries/{id}/public`
* **Path Variables:** `id` (Long - ID lộ trình)
* **Query Parameters:** - `isPublic` (boolean): `true` (Bật công khai) hoặc `false` (Chuyển về riêng tư).

**Response (200 OK):**

```json
{
  "success": true,
  "code": 200,
  "message": "Itinerary is now public", // hoặc "Itinerary is now private"
  "data": null
}

```

---

## 🤝 PHẦN 3: TÍNH NĂNG CỘNG TÁC (COLLABORATION / SHARE)

Dùng cho màn hình "Quản lý thành viên" bên trong chi tiết chuyến đi.

### 3.1. Chia sẻ Lộ trình cho User khác

Chủ phòng mời thêm bạn bè vào chuyến đi thông qua username.

* **Endpoint:** `POST /api/management/itineraries/{id}/share`
* **Path Variables:** `id` (Long)
* **Query Parameters:** - `username` (String): Tên đăng nhập của người muốn mời.

**Bắt lỗi (Error Handling):**

* `404 Not Found`: Không tìm thấy user này trong hệ thống.
* `400 Bad Request` (Validation Failed): Không thể tự share cho chính mình, hoặc user này đã được share từ trước.

### 3.2. Thu hồi quyền truy cập (Kick User)

Chủ phòng xóa một thành viên khỏi chuyến đi.

* **Endpoint:** `DELETE /api/management/itineraries/{id}/share/{username}`
* **Path Variables:** `id` (ID lộ trình), `username` (Người bị xóa).

### 3.3. Lấy danh sách thành viên trong Lộ trình (Có phân trang)

Render danh sách những người đã được share. Danh sách tự động xếp theo thời gian được mời mới nhất.

* **Endpoint:** `GET /api/management/itineraries/{id}/shared-users`
* **Query Parameters:** `page`, `size`.

**Response (200 OK):**

```json
{
  "success": true,
  "code": 200,
  "message": "Retrieve shared users list successfully",
  "data": {
    "content": [
      {
        "userId": 42,
        "username": "minhhieu",
        "avatarUrl": "[https://link-to-avatar.jpg](https://link-to-avatar.jpg)",
        "sharedAt": "2026-05-20T14:30:00"
      }
    ],
    "totalPages": 1,
    "totalElements": 1,
    "pageSize": 10,
    "pageNumber": 0,
    "empty": false
  }
}

```

### 3.4. Tìm kiếm nhanh thành viên đang trong Lộ trình

Dùng cho ô **Input Search** để chủ phòng lọc nhanh thành viên muốn kick/tìm kiếm (Không phân trang, trả thẳng mảng để FE tự render dropdown).

* **Endpoint:** `GET /api/management/itineraries/{id}/shared-users/search`
* **Query Parameters:** - `keyword` (String): Gõ tên/chữ cái đầu (vd: "hieu").

**Response (200 OK):**

```json
{
  "success": true,
  "code": 200,
  "message": "Search shared users successfully",
  "data": [
    {
      "userId": 42,
      "username": "minhhieu",
      "avatarUrl": "[https://link-to-avatar.jpg](https://link-to-avatar.jpg)",
      "sharedAt": "2026-05-20T14:30:00"
    }
  ]
}

```

> 💡 **FE Tip:** Hãy dùng kỹ thuật `Debounce` (chờ ~300ms sau khi user ngừng gõ phím) rồi mới gọi API này để tránh spam Request lên server.

### 3.5. Lấy danh sách các Lộ trình "Được chia sẻ với tôi"

Dành cho người dùng xem những chuyến đi mà họ được người khác mời vào.

* **Endpoint:** `GET /api/management/itineraries/shared-with-me`
* **Query Parameters:** `page`, `size`.

**Response:** Giống cấu trúc `1.1` (Trả về `CommonPage<ItinerarySummaryResponse>`).

---

## 🔐 Phân quyền & Quyền truy cập (Authorization Rules)

Để FE dễ dàng thiết kế luồng trải nghiệm cho Khách vãng lai (Guest) và Người dùng đã đăng nhập (Logged-in User), hệ thống API được phân rạch ròi thành 3 nhóm quyền hạn.

Đặc biệt lưu ý API **Xem chi tiết Lộ trình (`GET /api/itineraries/{id}`)**, API này hoạt động linh hoạt dựa trên trạng thái của dữ liệu.

### 🟢 Nhóm 1: Không yêu cầu Token (Public Access)
Người dùng chưa đăng nhập (Guest) vẫn có thể gọi được các API này. Frontend **không cần** truyền header `Authorization`.
* `GET /api/management/itineraries/public` (Lấy toàn bộ lộ trình public)
* `GET /api/management/itineraries/public/search` (Tìm kiếm bằng Prompt)
* `GET /api/management/itineraries/users/{userId}/public` (Lấy lộ trình public của 1 user)
* `GET /api/itineraries/{id}` (Xem chi tiết Lộ trình): **CHỈ áp dụng khi lộ trình này đang có `isPublic = true`**. Nếu gọi API này vào 1 lộ trình Private mà không có Token, hệ thống sẽ báo lỗi `401 UNAUTHORIZED`.

### 🟡 Nhóm 2: Bắt buộc Đăng nhập (Yêu cầu Token)
Bắt buộc truyền header `Authorization: Bearer {token}`. Nếu không có token hoặc token hết hạn, Backend trả về `401 UNAUTHORIZED`.
* `GET /api/management/itineraries/shared-with-me` (Lấy danh sách lộ trình được chia sẻ với tôi).
* `GET /api/itineraries/{id}` (Xem chi tiết Lộ trình): Áp dụng khi Lộ trình là **PRIVATE**. Lúc này hệ thống sẽ kiểm tra xem user có nằm trong danh sách được Share không. Nếu không, trả về `403 FORBIDDEN`.

### 🔴 Nhóm 3: Bắt buộc Đăng nhập + Chủ sở hữu (Owner Only)
Bắt buộc truyền Token. Backend sẽ kiểm tra chéo xem `userId` của token có phải là `traveler_id` (chủ lộ trình) hay không. Nếu không, trả về `403 FORBIDDEN` (Only owner can...).
* `PATCH /api/management/itineraries/{id}/public` (Bật/tắt public)
* `POST /api/management/itineraries/{id}/share` (Mời người vào chuyến đi)
* `DELETE /api/management/itineraries/{id}/share/{username}` (Kick người khỏi chuyến đi)
* `GET /api/management/itineraries/{id}/shared-users` (Lấy danh sách người được share)
* `GET /api/management/itineraries/{id}/shared-users/search` (Tìm kiếm người được share)

> 💡 **FE Tip xử lý UX/UI:**
> - **Gặp lỗi 401:** Mở Modal/Trang yêu cầu Đăng nhập.
> - **Gặp lỗi 403 (khi xem chi tiết Lộ trình):** Hiển thị màn hình rỗng kèm câu thông báo: *"Rất tiếc, lộ trình này đã được tác giả chuyển về chế độ riêng tư."*
> - **Gặp lỗi 403 (khi thao tác Share/Public):** Hiển thị Toast thông báo lỗi màu đỏ: *"Bạn không có quyền thực hiện thao tác này."*

---

## 📅 PHẦN 4: ĐỒNG BỘ GOOGLE CALENDAR

Cho phép Traveler đồng bộ `ItineraryActivity` lên Google Calendar cá nhân. Sử dụng OAuth 2.0 Progressive Consent — chỉ yêu cầu quyền Calendar khi user thực sự cần, không bắt buộc ngay lúc đăng nhập.

### Luồng tổng quan (5 bước)

```
FE: GET /me/integrations
    ↓ isGoogleLinked? hasCalendarScope?
FE: hiển thị nút "Liên kết" hay "Đồng bộ"
    ↓ User nhấn "Đồng bộ"
POST /management/itineraries/{id}/export-calendar
    ↓ nếu chưa có scope → 403 + authorizationUrl
FE: redirect user đến authorizationUrl (Google OAuth)
    ↓ User cấp quyền → Google redirect về FE với ?code=...
POST /users/me/google/calendar-callback  { code }
    ↓ 200 OK → token lưu vào DB
FE: gọi lại POST export-calendar → 200 "Calendar sync started"
    ↓ @Async: BE tạo events trên Google Calendar
WebSocket: nhận CALENDAR_SYNC_COMPLETED notification
```

---

### 4.1 Kiểm tra trạng thái tích hợp

Dùng để FE quyết định hiển thị nút "Liên kết Google" hay "Đồng bộ lịch".

- **Endpoint:** `GET /api/users/me/integrations`
- **Auth:** Required (JWT)

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Integration status fetched successfully",
  "data": {
    "isGoogleLinked": false,
    "hasCalendarScope": false
  },
  "success": true
}
```

| Trường | Ý nghĩa |
|---|---|
| `isGoogleLinked` | User đã có `user_oauth_tokens` row với provider = GOOGLE |
| `hasCalendarScope` | Token đó có chứa scope `calendar.events` |

> 💡 **FE Tip:** Gọi API này khi vào màn hình chi tiết lộ trình để quyết định trạng thái nút Calendar. Nếu `isGoogleLinked = false` → nút "Liên kết Google Calendar". Nếu `hasCalendarScope = true` → nút "Đồng bộ lên Google Calendar".

---

### 4.2 Export lộ trình lên Google Calendar

Endpoint duy nhất để trigger sync. Có hai kết quả tùy trạng thái quyền:

- **Endpoint:** `POST /api/management/itineraries/{id}/export-calendar`
- **Auth:** Required (JWT, owner only)
- **Path Variable:** `id` — ID lộ trình

**Response A — Đã có Calendar scope (200 OK):**
```json
{
  "code": 200,
  "message": "Calendar sync started",
  "data": null,
  "success": true
}
```
> Sync chạy `@Async` — response trả về ngay, events sẽ xuất hiện trong vài giây. WebSocket gửi notification khi xong.

**Response B — Chưa cấp quyền Calendar (403):**
```json
{
  "code": 403,
  "message": "Google Calendar permission required",
  "data": "https://accounts.google.com/o/oauth2/v2/auth?client_id=...&scope=...calendar.events...",
  "success": false
}
```
> FE nhận `data` là URL Google OAuth → redirect user đến URL này để cấp quyền. URL có `state={itineraryId}` để FE biết itinerary nào cần sync sau callback.

**Bắt lỗi:**
- `403 FORBIDDEN` (ownership): User không phải owner của lộ trình
- `400 BAD_REQUEST`: Lộ trình đã được sync trước đó (`calendarSyncedAt != null`)

---

### 4.3 Nhận Calendar Authorization Code (Callback)

Sau khi user cấp quyền trên Google, Google redirect về FE với `?code=...`. FE gửi code này lên BE để exchange lấy token.

- **Endpoint:** `POST /api/users/me/google/calendar-callback`
- **Auth:** Required (JWT — phải là user đang login)
- **Content-Type:** `application/json`

**Request:**
```json
{
  "code": "4/0AX4XfWi..."
}
```

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Google Calendar linked successfully",
  "data": null,
  "success": true
}
```

**Sau khi callback thành công:**
- `user_oauth_tokens` được upsert với `access_token`, `refresh_token`, `scopes`, `expires_at`
- Nếu user là LOCAL account và Google account chưa liên kết: `google_id` được ghi vào bảng `users`
- Nếu `google_id` đó đã thuộc user khác: throw 400

**Bắt lỗi:**
- `400 BAD_REQUEST`: code hết hạn, đã dùng, hoặc `google_id` xung đột với account khác

---

### 4.4 Hành vi Sync chi tiết

**Event mapping từ `ItineraryActivity`:**

| Google Calendar Field | Lấy từ |
|---|---|
| `summary` | `activity.description` (fallback: `itinerary.title + " - Activity"`) |
| `location` | `activity.poi.name` (nếu có liên kết POI) |
| `description` | `activity.note` |
| `start` / `end` | `activity.itineraryDate` + `startTime`/`endTime` (timezone: `Asia/Ho_Chi_Minh`) |

**Giá trị mặc định nếu activity thiếu time:**
- `startTime` không có → mặc định **08:00**
- `endTime` không có → `startTime + 1 giờ`

**Sync behavior:**
- Mỗi lộ trình chỉ được sync **1 lần duy nhất**
- Sau khi sync thành công → `itinerary.calendar_synced_at` được set, mọi lần gọi tiếp theo trả `400`
- Response của các API lộ trình có thêm field `calendarSyncedAt` (null = chưa sync, non-null = timestamp sync thành công)

**Auto-refresh token:**
- `GoogleCredential` tự động refresh `access_token` khi hết hạn dùng `refresh_token`
- Token mới được cập nhật vào `user_oauth_tokens` ngay sau khi refresh thành công

---

### 4.5 WebSocket Notification sau Sync

Sau khi sync hoàn tất (bất kể thành công hay thất bại), BE gửi notification qua WebSocket:

**Subscribe tại:** `/user/queue/notifications`

**Notification thành công:**
```json
{
  "title": "Đồng bộ lịch thành công",
  "message": "Lịch trình \"Khám phá Tây Bắc\" đã được đồng bộ lên Google Calendar.",
  "type": "CALENDAR_SYNC_COMPLETED",
  "targetType": "ITINERARY",
  "targetId": 42
}
```

**Notification thất bại:**
```json
{
  "title": "Đồng bộ lịch thất bại",
  "message": "Không thể đồng bộ lịch trình \"Khám phá Tây Bắc\" lên Google Calendar.",
  "type": "CALENDAR_SYNC_FAILED",
  "targetType": "ITINERARY",
  "targetId": 42
}
```

---

### 4.6 Phân quyền

| Endpoint | Auth | Role |
|---|---|---|
| `GET /api/users/me/integrations` | Required | TRAVELER |
| `POST /api/management/itineraries/{id}/export-calendar` | Required, owner only | TRAVELER |
| `POST /api/users/me/google/calendar-callback` | Required | TRAVELER |

---

### 4.7 Env Vars & Setup

```env
GOOGLE_CALENDAR_REDIRECT_URI=http://localhost:3000/calendar-callback
```

> URI này **phải được đăng ký** trong Google Cloud Console → Credentials → OAuth 2.0 Client → Authorized redirect URIs. Nếu không, Google sẽ từ chối callback với lỗi `redirect_uri_mismatch`.