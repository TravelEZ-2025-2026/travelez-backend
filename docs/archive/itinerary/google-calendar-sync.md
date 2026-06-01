# Google Calendar Sync

**Module:** itinerary, users, infrastructure/googlecalendar
**Completed:** 2026-06-01
**Developer:** Claude (AI)

---

## Tóm tắt

Tính năng cho phép Traveler đồng bộ lịch trình du lịch lên Google Calendar cá nhân. Hỗ trợ OAuth 2.0 Progressive Consent, auto-refresh token qua `GoogleCredential`, lưu Google Event ID để update/delete sau, và thông báo realtime qua WebSocket khi sync xong.

---

## Chức năng

- **Phase 1 — Kiểm tra trạng thái:** FE gọi `GET /api/users/me/integrations` → biết hiển thị nút "Liên kết" hay "Đồng bộ"
- **Phase 2 — Progressive Consent:** `POST /api/management/itineraries/{id}/export-calendar` → nếu chưa có Calendar scope trả HTTP 403 + `authorizationUrl`
- **Phase 3 — Callback:** `POST /api/users/me/google/calendar-callback` → exchange code, validate unique googleId, lưu token + scopes
- **Phase 4 — Sync @Async (1 lần):** Tạo Google Calendar events từ `ItineraryActivity`, set `itinerary.calendar_synced_at` sau khi thành công. Nếu đã sync rồi → 400.
- **Phase 5 — Auto-refresh:** `UserCredentials` tự refresh access token khi hết hạn, cập nhật DB

---

## Implementation Details

### Files Created
- `src/main/resources/db/migration/V26__add_token_scopes_and_expiry.sql`
- `src/main/resources/db/migration/V27__create_calendar_sync_history_table.sql` *(dropped via V28)*
- `src/main/resources/db/migration/V28__refactor_calendar_sync.sql` *(ADD calendar_synced_at, DROP calendar_sync_history)*
- `src/main/java/.../users/dto/response/IntegrationStatusResponse.java`
- `src/main/java/.../infrastructure/googlecalendar/GoogleCalendarService.java`
- `src/main/java/.../infrastructure/googlecalendar/impl/GoogleCalendarServiceImpl.java`
- `src/main/java/.../infrastructure/config/AsyncConfig.java`

### Files Modified
- `pom.xml` — thêm `google-api-services-calendar:v3-rev411-1.25.0`
- `application.yaml` — thêm `google.calendar-redirect-uri`
- `GoogleOAuthConfig.java` — thêm `calendarRedirectUri`
- `UserOAthToken.java` — thêm `scopes`, `expiresAt`
- `UserService.java` / `UserServiceImpl.java` — thêm `getIntegrationStatus()`
- `UserController.java` — thêm 2 endpoint mới
- `OAuth2Service.java` / `OAuth2ServiceImpl.java` — thêm `processCalendarCallback()`
- `UserRepository.java` — thêm `findByGoogleId()`
- `ItineraryManagementService.java` — đổi `exportToGoogleCalendar` return type → `String`
- `ItineraryManagementServiceImpl.java` — implement đầy đủ
- `ItineraryManagementController.java` — xử lý auth URL 403 response
- `NotificationType.java` — thêm `CALENDAR_SYNC_COMPLETED`, `CALENDAR_SYNC_FAILED`
- `NotificationTargetType.java` — thêm `ITINERARY`

### Database Changes
- `user_oauth_tokens`: thêm `scopes TEXT`, `expires_at TIMESTAMPTZ`
- `itinerary`: thêm `calendar_synced_at TIMESTAMPTZ NULL` (null = chưa sync)

### Env Vars cần thêm
```
GOOGLE_CALENDAR_REDIRECT_URI=http://localhost:3000/calendar-callback
```
(Phải đăng ký URI này trong Google Cloud Console → Authorized redirect URIs)

---

## API Documentation

### GET /api/users/me/integrations
**Auth:** Required (JWT)

**Response 200:**
```json
{
  "code": 200,
  "message": "Integration status fetched successfully",
  "data": { "isGoogleLinked": true, "hasCalendarScope": false },
  "success": true
}
```

---

### POST /api/management/itineraries/{id}/export-calendar
**Auth:** Required (JWT, owner only)

**Response 200 — sync started:**
```json
{ "code": 200, "message": "Calendar sync started", "data": null, "success": true }
```

**Response 403 — cần cấp quyền:**
```json
{
  "code": 403,
  "message": "Google Calendar permission required",
  "data": "https://accounts.google.com/o/oauth2/v2/auth?...",
  "success": false
}
```

---

### POST /api/users/me/google/calendar-callback
**Auth:** Required (JWT)

**Request:**
```json
{ "code": "4/0AX4XfWi..." }
```

**Response 200:**
```json
{ "code": 200, "message": "Google Calendar linked successfully", "data": null, "success": true }
```

---

## Testing

### Test theo thứ tự

1. **Tạo user LOCAL** → login lấy JWT
2. **GET /api/users/me/integrations** → expect `isGoogleLinked: false`
3. **POST /api/management/itineraries/{id}/export-calendar** → expect 403 + `authorizationUrl`
4. Mở `authorizationUrl` trong browser → đăng nhập Google → copy `code` từ redirect URL
5. **POST /api/users/me/google/calendar-callback** với `code` → expect 200
6. **GET /api/users/me/integrations** → expect `isGoogleLinked: true, hasCalendarScope: true`
7. **POST /api/management/itineraries/{id}/export-calendar** lại → expect 200 "Calendar sync started"
8. Kiểm tra Google Calendar của user → thấy events được tạo
9. Kiểm tra WebSocket notification tại `/topic/user.{userId}` → thấy `CALENDAR_SYNC_COMPLETED`
10. **POST lần 2** → events phải được UPDATE (không tạo mới) do logic upsert trong `CalendarSyncHistory`

### Test auto-refresh
- Xóa `access_token` trong DB, giữ `refresh_token`
- Gọi lại endpoint sync → GoogleCredential tự refresh → token mới được lưu vào DB

---

## Notes

- Sync chỉ được thực hiện **1 lần duy nhất** — endpoint trả 400 nếu `itinerary.calendar_synced_at != null`
- Sync là `@Async` → API trả về 200 ngay, notification WebSocket sẽ đến sau
- Sau sync thành công → `itinerary.calendar_synced_at` được set; `ItinerarySummaryResponse` expose field này cho FE
- Event timezone cố định `Asia/Ho_Chi_Minh`; nếu activity không có `startTime` thì mặc định 08:00, `endTime` mặc định +1h
- Dùng `UserCredentials` (google-auth-library) thay vì `GoogleCredential` (deprecated)
