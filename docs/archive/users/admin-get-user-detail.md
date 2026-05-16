# Admin - Get User Detail API

**Module:** Users (Admin Management)
**Completed:** 2026-05-16
**Developer:** AI Assistant

---

## 📋 Tóm tắt

API cho phép admin xem thông tin chi tiết của 1 tài khoản người dùng có role TRAVELER, bao gồm thông tin cơ bản, media (avatar, cover), thông tin chi tiết, thống kê và OAuth.

---

## 🎯 Chức năng

- Admin có thể xem thông tin chi tiết của user có role TRAVELER
- Hiển thị đầy đủ thông tin: cơ bản, media, chi tiết, thống kê, OAuth, timestamps
- Validate chỉ cho phép xem user TRAVELER (không xem được admin khác)
- Return 404 nếu user không tồn tại
- Return 403 nếu user không phải TRAVELER

---

## 🔧 Implementation Details

### Files Created
- `src/main/java/com/example/travelez/backend/users/dto/response/UserAdminDetailResponse.java` - Response DTO với đầy đủ thông tin user
- `src/main/java/com/example/travelez/backend/users/service/AdminUserService.java` - Service interface cho admin user management
- `src/main/java/com/example/travelez/backend/users/service/impl/AdminUserServiceImpl.java` - Service implementation

### Files Modified
- `src/main/java/com/example/travelez/backend/users/controller/AdminUserController.java` - Thêm endpoint GET /{userId}
- `src/main/java/com/example/travelez/backend/users/mapper/UserMapper.java` - Thêm mapping method toUserAdminDetailResponse

### Key Components

#### UserAdminDetailResponse
```java
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserAdminDetailResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private GenderType gender;
    private LocalDateTime dob;
    private UserStatus status;
    private RoleType role;
    private MediaBaseResponse avatar;
    private MediaBaseResponse cover;
    private Long followerCount;
    private Long followingCount;
    private String googleId;
    private AuthProvider authProvider;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

#### AdminUserServiceImpl Logic
- Sử dụng `userRepository.findUserProfileById(userId)` với `@EntityGraph` để eager load avatar và cover
- Validate user.getRole() == RoleType.TRAVELER
- Throw ApiException(NOT_FOUND) nếu user không tồn tại
- Throw ApiException(FORBIDDEN) nếu không phải TRAVELER
- Map User entity sang UserAdminDetailResponse qua UserMapper

---

## 📡 API Documentation

### Endpoint: Get User Detail

**URL:** `GET /api/admin/users/{userId}`

**Authentication:** Required (JWT)

**Authorization:** ADMIN

**Path Parameters:**
- `userId` (Long, required) - ID của user cần xem

**Response (Success - 200):**
```json
{
  "code": 200,
  "message": "User detail fetched successfully",
  "data": {
    "id": 1,
    "username": "traveler123",
    "email": "traveler@example.com",
    "fullName": "John Doe",
    "gender": "MALE",
    "dob": "1990-01-15T00:00:00",
    "status": "ACTIVE",
    "role": "TRAVELER",
    "avatar": {
      "id": 10,
      "url": "https://storage.googleapis.com/bucket/users/1/avatar.jpg",
      "type": "IMAGE"
    },
    "cover": {
      "id": 11,
      "url": "https://storage.googleapis.com/bucket/users/1/cover.jpg",
      "type": "IMAGE"
    },
    "followerCount": 150,
    "followingCount": 200,
    "googleId": "1234567890",
    "authProvider": "GOOGLE",
    "createdAt": "2025-01-01T10:00:00",
    "updatedAt": "2026-05-15T14:30:00"
  },
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
  "message": "Can only view TRAVELER users",
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
curl -X GET http://localhost:8080/api/admin/users/1 \
  -H "Authorization: Bearer <admin_jwt_token>"
```

---

## 📝 Notes

### Lưu ý quan trọng
- Chỉ admin mới có thể gọi API này (require ADMIN role)
- Chỉ có thể xem thông tin user có role TRAVELER
- Không thể xem thông tin của admin khác (bảo mật)
- Media (avatar, cover) được eager load qua @EntityGraph để tránh N+1 query
- MapStruct tự động map Media entity sang MediaBaseResponse

### Technical Details
- Sử dụng `AdminUserService` riêng thay vì `UserService` để tách biệt logic admin
- Repository method `findUserProfileById` có `@EntityGraph(attributePaths = {"avatar", "cover"})`
- Validation ở service layer, không cần thêm validation ở controller

### Known Issues
- None

### Future Improvements
- Có thể thêm thống kê chi tiết hơn (số bài viết, số review, số report)
- Có thể thêm filter/search trong list users
- Có thể thêm action buttons (ban user, activate user) trong tương lai

### Lessons Learned
- Tách service riêng cho admin giúp code dễ maintain và mở rộng
- Sử dụng @EntityGraph giúp tối ưu query khi cần load relationships
- Validation role ở service layer giúp bảo mật tốt hơn

---

## 🧪 Testing

### Test Cases
1. ✅ Admin xem thông tin user TRAVELER hợp lệ → Success 200
2. ✅ Admin xem user không tồn tại → Error 404
3. ✅ Admin cố xem thông tin admin khác → Error 403
4. ✅ User không phải admin gọi API → Error 401/403
5. ✅ Media (avatar, cover) được load đúng

### How to Test
```bash
# 1. Login as admin to get JWT token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 2. Get user detail (replace {userId} and {token})
curl -X GET http://localhost:8080/api/admin/users/{userId} \
  -H "Authorization: Bearer {token}"

# 3. Test with Swagger UI
# Navigate to: http://localhost:8080/swagger-ui/index.html
# Find "Admin - User Management" section
# Click "Authorize" and enter JWT token
# Try "GET /api/admin/users/{userId}" endpoint
```

---

## 🚀 Deployment Notes

- Không cần thêm environment variables
- Không cần migration scripts (sử dụng table và columns hiện có)
- Build thành công với `mvn clean compile`
- Swagger documentation tự động generate

---

## 📚 Related Features

- Feature 1: Get all users (list) - `/api/admin/users` (GET)
- Future: Update user status (ban/activate)
- Future: View user activity logs
