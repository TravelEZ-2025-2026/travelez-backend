# Admin - Ban/Unban User API

**Module:** Users (Admin Management)
**Completed:** 2026-05-16
**Developer:** AI Assistant

---

## 📋 Tóm tắt

API cho phép admin khóa/hủy khóa tài khoản người dùng có role TRAVELER, với lý do và tự động ghi log vào database. Sử dụng Strategy Pattern và Event-Driven Architecture.

---

## 🎯 Chức năng

- Admin có thể khóa (BAN) hoặc hủy khóa (UNBAN) user TRAVELER
- Lý do khóa/hủy khóa là optional
- Tự động ghi log vào bảng `user_action_logs`
- Gửi notification cho user (TODO)
- Ignore silently nếu status không thay đổi (BAN user đã BANNED, UNBAN user đã ACTIVE)
- Chỉ ADMIN có quyền thực hiện
- Không thể khóa user không phải TRAVELER

---

## 🔧 Implementation Details

### Files Created
- `src/main/java/com/example/travelez/backend/users/model/enums/ActionType.java` - Enum BAN/UNBAN
- `src/main/java/com/example/travelez/backend/users/model/UserActionLog.java` - Entity cho action logs
- `src/main/java/com/example/travelez/backend/users/repository/UserActionLogRepository.java` - Repository
- `src/main/java/com/example/travelez/backend/users/dto/request/UserStatusUpdateRequest.java` - Request DTO
- `src/main/java/com/example/travelez/backend/users/handler/UserStatusHandler.java` - Strategy interface
- `src/main/java/com/example/travelez/backend/users/handler/impl/BanUserHandler.java` - BAN handler
- `src/main/java/com/example/travelez/backend/users/handler/impl/UnbanUserHandler.java` - UNBAN handler
- `src/main/java/com/example/travelez/backend/users/event/UserStatusChangedEvent.java` - Domain event
- `src/main/java/com/example/travelez/backend/users/listener/UserStatusChangedListener.java` - Event listener

### Files Modified
- `src/main/java/com/example/travelez/backend/users/service/AdminUserService.java` - Thêm method updateUserStatus
- `src/main/java/com/example/travelez/backend/users/service/impl/AdminUserServiceImpl.java` - Implement logic
- `src/main/java/com/example/travelez/backend/users/controller/AdminUserController.java` - Thêm endpoint PATCH

### Architecture Patterns

#### Strategy Pattern
```java
// Interface
public interface UserStatusHandler {
    void handle(User user, String reason);
    boolean canHandle(User user);
}

// BanUserHandler: ACTIVE → BANNED
// UnbanUserHandler: BANNED → ACTIVE
```

**Benefits:**
- Dễ dàng thêm action mới (SUSPEND, WARNING, etc.)
- Tránh if-else phức tạp
- Single Responsibility Principle

#### Event-Driven Architecture
```
Service → Update Status → Publish Event
                              ↓
                         Listener
                         ├─ Ghi log
                         └─ Gửi notification (TODO)
```

**Benefits:**
- Decouple logic: service chỉ update status
- Listener xử lý side effects (log, notification)
- Dễ dàng thêm listener mới

### Key Logic Flow

1. **Validate:** User tồn tại và có role TRAVELER
2. **Select Handler:** Chọn BanUserHandler hoặc UnbanUserHandler theo action
3. **Check canHandle:** Ignore nếu status không đổi
4. **Execute:** Handler update user status
5. **Save:** Persist user vào database
6. **Publish Event:** Trigger UserStatusChangedEvent
7. **Listener:** Ghi log + gửi notification

---

## 📡 API Documentation

### Endpoint: Update User Status (Ban/Unban)

**URL:** `PATCH /api/admin/users/{userId}/status`

**Authentication:** Required (JWT)

**Authorization:** ADMIN

**Path Parameters:**
- `userId` (Long, required) - ID của user cần khóa/hủy khóa

**Request Body:**
```json
{
  "action": "BAN",
  "reason": "Spam content"
}
```

**Fields:**
- `action` (ActionType, required) - BAN hoặc UNBAN
- `reason` (String, optional) - Lý do khóa/hủy khóa

**Response (Success - 200):**
```json
{
  "code": 200,
  "message": "User status updated successfully",
  "data": null,
  "success": true
}
```

**Response (Error - 404):**
```json
{
  "code": 404,
  "message": "User not found",
  "data": null,
  "success": false
}
```

**Response (Error - 403):**
```json
{
  "code": 403,
  "message": "Can only update TRAVELER users",
  "data": null,
  "success": false
}
```

**Response (Error - 401):**
```json
{
  "code": 401,
  "message": "Authentication required",
  "data": null,
  "success": false
}
```

**Cách sử dụng:**

```bash
# Ban user
curl -X PATCH http://localhost:8080/api/admin/users/1/status \
  -H "Authorization: Bearer <admin_jwt_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "action": "BAN",
    "reason": "Violating community guidelines"
  }'

# Unban user
curl -X PATCH http://localhost:8080/api/admin/users/1/status \
  -H "Authorization: Bearer <admin_jwt_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "action": "UNBAN",
    "reason": "Appeal accepted"
  }'
```

---

## 📝 Notes

### Lưu ý quan trọng
- Chỉ admin mới có thể gọi API này (require ADMIN role)
- Chỉ có thể khóa/hủy khóa user có role TRAVELER
- Không thể khóa admin khác (bảo mật)
- Lý do (reason) là optional, không giới hạn độ dài
- Ignore silently nếu status không thay đổi (không throw error)
- User bị khóa không thể login và không xem được nội dung
- User bị khóa chưa bị logout ngay lập tức

### Database Schema
```sql
CREATE TABLE user_action_logs (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    action_type action_type NOT NULL,  -- BAN, UNBAN
    reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
```

### Technical Details
- **Strategy Pattern:** Dễ dàng mở rộng với action mới
- **Event-Driven:** Decouple logic, dễ maintain
- **Transaction:** @Transactional đảm bảo consistency
- **Logging:** Tự động ghi vào user_action_logs qua listener
- **Notification:** TODO - sẽ implement sau

### Known Issues
- Notification chưa được implement (có TODO trong listener)
- User bị ban chưa bị logout ngay (cần implement JWT blacklist hoặc check status khi validate token)

### Future Improvements
- Implement notification khi user bị ban/unban
- Thêm action mới: SUSPEND (tạm khóa), WARNING (cảnh báo)
- Thêm thời gian khóa (ban duration)
- Thêm API để xem lịch sử action logs của user
- Implement JWT blacklist để logout user ngay khi bị ban
- Thêm bulk ban/unban (khóa nhiều user cùng lúc)

### Lessons Learned
- Strategy Pattern giúp code clean và dễ mở rộng
- Event-Driven giúp tách biệt concerns
- Ignore silently thay vì throw error giúp API idempotent
- Validation ở service layer giúp bảo mật tốt hơn

---

## 🧪 Testing

### Test Cases
1. ✅ Admin ban user TRAVELER ACTIVE → Success, status = BANNED, log được ghi
2. ✅ Admin unban user TRAVELER BANNED → Success, status = ACTIVE, log được ghi
3. ✅ Admin ban user đã BANNED → Success (ignore), không ghi log
4. ✅ Admin unban user đã ACTIVE → Success (ignore), không ghi log
5. ✅ Admin ban user không tồn tại → Error 404
6. ✅ Admin ban user không phải TRAVELER → Error 403
7. ✅ User không phải admin gọi API → Error 401/403
8. ✅ Request không có action → Error 400 (validation)
9. ✅ Request có reason → Log ghi đúng reason
10. ✅ Request không có reason → Log ghi reason = null

### How to Test

```bash
# 1. Login as admin to get JWT token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 2. Ban user (replace {userId} and {token})
curl -X PATCH http://localhost:8080/api/admin/users/{userId}/status \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"action":"BAN","reason":"Test ban"}'

# 3. Check database
SELECT * FROM user_action_logs WHERE user_id = {userId};

# 4. Unban user
curl -X PATCH http://localhost:8080/api/admin/users/{userId}/status \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"action":"UNBAN","reason":"Test unban"}'

# 5. Test with Swagger UI
# Navigate to: http://localhost:8080/swagger-ui/index.html
# Find "Admin - User Management" section
# Click "Authorize" and enter JWT token
# Try "PATCH /api/admin/users/{userId}/status" endpoint
```

---

## 🚀 Deployment Notes

- Không cần thêm environment variables
- Không cần migration scripts (bảng user_action_logs đã có trong schema)
- Build thành công với `mvn clean compile`
- Swagger documentation tự động generate

---

## 📚 Related Features

- Feature 1: Get all users (list) - `/api/admin/users` (GET)
- Feature 2: Get user detail - `/api/admin/users/{userId}` (GET)
- Future: View user action logs history
- Future: Implement notification for ban/unban
- Future: JWT blacklist for immediate logout
