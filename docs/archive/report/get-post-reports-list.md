# Get Post Reports List Feature

**Module:** Report
**Completed:** 2026-05-18

---

## 📋 Tóm tắt

Tạo API endpoint cho admin để xem danh sách tất cả các report của một bài post cụ thể, bao gồm tổng số report, số report chưa xử lý (PENDING), và danh sách chi tiết các report với phân trang.

---

## 🎯 Chức năng

Admin có thể:
- Xem tất cả reports của một post cụ thể
- Lọc reports theo status (PENDING, RESOLVED, REJECTED)
- Xem tổng số report và số report chưa xử lý
- Phân trang danh sách reports
- Xem thông tin chi tiết của từng report (reporter, reason, reason detail, status, timestamps)

**Use cases:**
- Admin kiểm tra các report của một post bị báo cáo
- Admin xem lịch sử xử lý report của một post
- Admin lọc chỉ xem các report đang chờ xử lý

---

## 🔧 Implementation Details

### Files Created
- `src/main/java/com/example/travelez/backend/report/dto/response/PostReportsResponse.java` - Wrapper response chứa summary và paginated reports
- `src/main/java/com/example/travelez/backend/report/dto/response/PostReportDetailResponse.java` - Detail response cho mỗi report

### Files Modified
- `src/main/java/com/example/travelez/backend/report/repository/ReportRepository.java` - Thêm query methods:
  - `findByPostId()` - Lấy tất cả reports của post
  - `findByPostIdAndStatus()` - Lấy reports theo status
  - `countByPostId()` - Đếm tổng reports
  - `countByPostIdAndStatus()` - Đếm reports theo status
- `src/main/java/com/example/travelez/backend/report/mapper/ReportMapper.java` - Thêm mapping method `toPostReportDetailResponse()`
- `src/main/java/com/example/travelez/backend/report/service/AdminReportService.java` - Thêm method interface `getPostReports()`
- `src/main/java/com/example/travelez/backend/report/service/impl/AdminReportServiceImpl.java` - Implement business logic
- `src/main/java/com/example/travelez/backend/report/controller/AdminReportController.java` - Thêm endpoint `GET /api/admin/reports/posts/{postId}`

### Database Changes
Không có thay đổi database schema. Sử dụng existing `reports` table.

---

## 📡 API Documentation

### Endpoint: Get Post Reports

**URL:** `GET /api/admin/reports/posts/{postId}`

**Authentication:** Required (JWT)

**Authorization:** ADMIN only

**Path Parameters:**
- `postId` (Long, required) - ID của post cần xem reports

**Query Parameters:**
- `status` (ReportStatus, optional) - Filter theo status: PENDING, RESOLVED, REJECTED
- `page` (Integer, optional, default: 0) - Page number
- `size` (Integer, optional, default: 10) - Page size
- `sort` (String, optional, default: createdAt,DESC) - Sort field và direction

**Request:**
```bash
# Get all reports of post
GET /api/admin/reports/posts/123

# Get only pending reports
GET /api/admin/reports/posts/123?status=PENDING

# Get with pagination
GET /api/admin/reports/posts/123?page=0&size=20&sort=createdAt,DESC
```

**Response (Success - 200):**
```json
{
  "code": 200,
  "message": "Post reports fetched successfully",
  "success": true,
  "data": {
    "totalReports": 15,
    "pendingReports": 8,
    "reports": {
      "content": [
        {
          "id": 1,
          "reporter": {
            "userId": 123,
            "username": "user123",
            "fullName": "John Doe"
          },
          "reason": "SPAM",
          "reasonDetail": "This post contains spam content",
          "status": "PENDING",
          "createdAt": "2026-05-18T10:30:00",
          "updatedAt": "2026-05-18T10:30:00"
        },
        {
          "id": 2,
          "reporter": {
            "userId": 456,
            "username": "user456",
            "fullName": "Jane Smith"
          },
          "reason": "INAPPROPRIATE_CONTENT",
          "reasonDetail": "Contains offensive language",
          "status": "RESOLVED",
          "createdAt": "2026-05-17T15:20:00",
          "updatedAt": "2026-05-18T09:00:00"
        }
      ],
      "page": 0,
      "size": 10,
      "totalElements": 15,
      "totalPages": 2,
      "empty": false
    }
  }
}
```

**Response (Error - 404):**
```json
{
  "code": 404,
  "message": "Post not found",
  "data": null,
  "success": false
}
```

**Response (Error - 403):**
```json
{
  "code": 403,
  "message": "Access denied",
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
# Get all reports
curl -X GET http://localhost:8080/api/admin/reports/posts/123 \
  -H "Authorization: Bearer <admin_token>"

# Get only pending reports
curl -X GET "http://localhost:8080/api/admin/reports/posts/123?status=PENDING" \
  -H "Authorization: Bearer <admin_token>"

# Get with pagination and sorting
curl -X GET "http://localhost:8080/api/admin/reports/posts/123?page=0&size=20&sort=createdAt,DESC" \
  -H "Authorization: Bearer <admin_token>"
```

---

## 📝 Notes

### Lưu ý quan trọng
- Chỉ ADMIN mới có quyền truy cập endpoint này
- Post phải tồn tại trong database, nếu không sẽ trả về 404
- Không tính report của post đã bị xóa (soft delete)
- Sử dụng `@EntityGraph` để eager load reporter, tránh N+1 query problem
- Mặc định sort theo `createdAt DESC` (report mới nhất trước)

### Performance Considerations
- Repository sử dụng `@EntityGraph(attributePaths = {"reporter"})` để tối ưu query
- Pagination giúp giảm tải khi post có nhiều reports
- Count queries được tách riêng để tối ưu performance

### Business Rules
- `totalReports`: Tổng số tất cả reports (bất kể status)
- `pendingReports`: Chỉ đếm reports có status = PENDING
- Filter `status` là optional, không truyền sẽ lấy tất cả reports
- Empty list được trả về khi post không có report nào

---

## 🧪 Testing

### Test Cases
1. **Get all reports of a post** - Verify trả về đúng tổng số và danh sách
2. **Filter by PENDING status** - Verify chỉ trả về pending reports
3. **Filter by RESOLVED status** - Verify chỉ trả về resolved reports
4. **Filter by REJECTED status** - Verify chỉ trả về rejected reports
5. **Pagination** - Verify phân trang hoạt động đúng
6. **Post not found** - Verify trả về 404
7. **Post without reports** - Verify trả về empty list với counts = 0
8. **Unauthorized access** - Verify non-admin không truy cập được
9. **Sort order** - Verify sort theo createdAt DESC

### How to Test
```bash
# 1. Start application
mvn spring-boot:run

# 2. Get admin token (login as admin)
# 3. Test endpoint
curl -X GET "http://localhost:8080/api/admin/reports/posts/{postId}" \
  -H "Authorization: Bearer <admin_token>"

# 4. Test with filters
curl -X GET "http://localhost:8080/api/admin/reports/posts/{postId}?status=PENDING" \
  -H "Authorization: Bearer <admin_token>"

# 5. Test pagination
curl -X GET "http://localhost:8080/api/admin/reports/posts/{postId}?page=0&size=5" \
  -H "Authorization: Bearer <admin_token>"
```

---

## 🚀 Deployment Notes

- Không cần migration scripts (sử dụng existing schema)
- Không cần environment variables mới
- Endpoint tự động được thêm vào Swagger UI: `/swagger-ui/index.html`
- Verify admin role được config đúng trong Spring Security

---

## 🔗 Related Features

- **Get Reported Items** (`GET /api/admin/reports/reported-items`) - Lấy danh sách posts bị report
- **Reject Reports** (`POST /api/admin/reports/reject`) - Từ chối reports
- Feature này bổ sung cho admin workflow: xem danh sách posts bị report → xem chi tiết reports của post → xử lý (reject/resolve)

---

## ✅ Verification

Build status: ✅ SUCCESS
- Compiled successfully with `mvn clean compile`
- No compilation errors
- MapStruct generated implementation correctly
- All dependencies resolved
