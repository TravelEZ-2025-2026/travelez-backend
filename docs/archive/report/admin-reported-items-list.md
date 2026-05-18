# Admin - Get Reported Items List

**Module:** Report
**Completed:** 2026-05-16

---

## 📋 Tóm tắt

Tạo API cho Admin để lấy danh sách các posts bị report, hiển thị thông tin post và số lượng report. Hỗ trợ filter theo trạng thái report (PENDING/RESOLVED/REJECTED), sort theo số lượng report, và pagination.

---

## 🎯 Chức năng

Admin có thể:
- Xem danh sách tất cả posts đã bị report
- Xem **2 số đếm riêng biệt**:
  - **reportCount**: Tổng số report (tất cả trạng thái)
  - **pendingReportCount**: Số report PENDING (chưa xử lý)
- **Filter 2 loại:**
  - **Mặc định (`showAll=false`)**: Chỉ hiển thị posts có **pendingReportCount >= 1**
  - **Show All (`showAll=true`)**: Hiển thị posts có **reportCount >= 1** (bất kể trạng thái)
- Sort theo số lượng report (nhiều → ít) hoặc thời gian
- Pagination với offset-based

**Use cases:**
- Admin cần xem posts nào đang có report chưa xử lý (PENDING) để ưu tiên review
- Admin cần biết tổng số report và số report pending của mỗi post
- Admin cần xem posts nào bị report nhiều nhất (bất kể đã xử lý hay chưa)
- Admin cần xem lịch sử tất cả posts từng bị report

---

## 🔧 Implementation Details

### Files Created
- `src/main/java/com/example/travelez/backend/report/dto/request/ReportedItemsFilterRequest.java` - Filter request DTO với status, sortBy, sortDirection
- `src/main/java/com/example/travelez/backend/report/dto/response/ReportedPostResponse.java` - Response DTO chứa thông tin post và report count
- `src/main/java/com/example/travelez/backend/report/repository/projection/ReportedPostProjection.java` - Projection interface cho query hiệu quả

### Files Modified
- `src/main/java/com/example/travelez/backend/report/repository/ReportRepository.java` - Thêm custom query `findReportedPosts` với GROUP BY để đếm reports
- `src/main/java/com/example/travelez/backend/report/service/AdminReportService.java` - Thêm method `getReportedItems`
- `src/main/java/com/example/travelez/backend/report/service/impl/AdminReportServiceImpl.java` - Implement logic query và mapping
- `src/main/java/com/example/travelez/backend/report/controller/AdminReportController.java` - Thêm GET endpoint `/reported-items`

### Database Changes
Không có thay đổi database schema. Sử dụng bảng `reports` và `posts` hiện có.

### Technical Approach
- **JPA Query với GROUP BY**: Đếm số lượng report cho mỗi post trong một query duy nhất
- **Projection**: Sử dụng interface projection để chỉ lấy fields cần thiết, tránh load toàn bộ entity
- **Filter động**: Support filter theo status (null = ALL)
- **Pagination**: Offset-based với Spring Data Pageable

---

## 📡 API Documentation

### Endpoint: Get Reported Items

**URL:** `GET /api/admin/reports/reported-items`

**Authentication:** Required (JWT)

**Authorization:** ADMIN only

**Query Parameters:**
- `showAll` (optional): Boolean
  - `false` (default): Only posts with at least 1 PENDING report
  - `true`: All reported posts regardless of report status
- `sortBy` (optional): `reportCount` or `createdAt`. Default: `reportCount`
- `sortDirection` (optional): `ASC` or `DESC`. Default: `DESC`
- `page` (optional): Page number (0-indexed). Default: `0`
- `size` (optional): Page size. Default: `10`

**Response (Success - 200):**
```json
{
  "code": 200,
  "message": "Reported items fetched successfully",
  "success": true,
  "data": {
    "content": [
      {
        "postId": 123,
        "title": "Amazing trip to Da Nang",
        "content": "This is the post content...",
        "authorId": 456,
        "authorName": "John Doe",
        "reportCount": 5,
        "pendingReportCount": 2,
        "firstReportedAt": "2026-05-16T10:30:00",
        "latestReportedAt": "2026-05-16T15:20:00",
        "postStatus": "PUBLISHED"
      },
      {
        "postId": 124,
        "title": "Another post",
        "content": "Content here...",
        "authorId": 457,
        "authorName": "Jane Smith",
        "reportCount": 3,
        "pendingReportCount": 3,
        "firstReportedAt": "2026-05-15T14:20:00",
        "latestReportedAt": "2026-05-16T09:10:00",
        "postStatus": "PUBLISHED"
      }
    ],
    "totalPages": 3,
    "totalElements": 25,
    "size": 10,
    "page": 0,
    "empty": false
  }
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

**Response (Error - 403):**
```json
{
  "code": 403,
  "message": "Access denied",
  "data": null,
  "success": false
}
```

**Cách sử dụng:**

```bash
# Get posts with PENDING reports (default)
curl -X GET "http://localhost:8080/api/admin/reports/reported-items" \
  -H "Authorization: Bearer <admin_token>"

# Get posts with PENDING reports (explicit)
curl -X GET "http://localhost:8080/api/admin/reports/reported-items?showAll=false" \
  -H "Authorization: Bearer <admin_token>"

# Get all reported posts (regardless of status)
curl -X GET "http://localhost:8080/api/admin/reports/reported-items?showAll=true" \
  -H "Authorization: Bearer <admin_token>"

# Get with pagination and sorting
curl -X GET "http://localhost:8080/api/admin/reports/reported-items?showAll=false&page=0&size=20&sortBy=reportCount&sortDirection=DESC" \
  -H "Authorization: Bearer <admin_token>"
```

---

## 📝 Notes

### Lưu ý quan trọng
- Endpoint chỉ dành cho ADMIN, được bảo vệ bởi `@PreAuthorize("hasRole('ADMIN')")`
- **Mặc định (showAll=false)**: Chỉ hiển thị posts có ít nhất 1 report PENDING
- **showAll=true**: Hiển thị tất cả posts bị report (bất kể PENDING/RESOLVED/REJECTED)
- Query sử dụng 2 methods riêng biệt để tối ưu performance
- Projection giúp tránh N+1 query problem

### Giới hạn
- Hiện tại chỉ support POST, chưa support REVIEW (theo design hiện tại)
- Không có filter theo loại vi phạm (ReportReason)
- Không có filter theo khoảng thời gian

### Future Improvements
- Thêm filter theo ReportReason (SPAM, HARASSMENT, etc.)
- Thêm filter theo date range (firstReportedAt, latestReportedAt)
- Thêm filter theo author
- Thêm bulk actions (approve/reject multiple posts)
- Mở rộng cho REVIEW khi cần

### Lessons Learned
- Sử dụng Projection interface giúp query hiệu quả hơn việc load full entity
- GROUP BY trong JPQL cần alias rõ ràng cho các aggregate functions
- CommonPage constructor cần 6 parameters đầy đủ
- Import đúng package `common.api` không phải `common.dto`

---

## 🧪 Testing

### Test Cases
1. ✅ Get pending reported posts (default behavior)
2. ✅ Get all reported posts (status=null)
3. ✅ Get resolved reported posts
4. ✅ Get rejected reported posts
5. ✅ Sort by report count DESC (most reported first)
6. ✅ Pagination works correctly
7. ✅ Only ADMIN can access (403 for non-admin)
8. ✅ Unauthenticated request returns 401

### How to Test

1. **Build project:**
```bash
mvn clean compile
```

2. **Run application:**
```bash
mvn spring-boot:run
```

3. **Test with Swagger UI:**
   - Navigate to `http://localhost:8080/swagger-ui/index.html`
   - Find "Admin Reports" section
   - Click "GET /api/admin/reports/reported-items"
   - Click "Try it out"
   - Enter parameters and execute

4. **Test with curl:**
```bash
# Login as admin first to get token
TOKEN="your_admin_jwt_token"

# Test default (pending reports)
curl -X GET "http://localhost:8080/api/admin/reports/reported-items" \
  -H "Authorization: Bearer $TOKEN"

# Test with filters
curl -X GET "http://localhost:8080/api/admin/reports/reported-items?status=RESOLVED&page=0&size=5" \
  -H "Authorization: Bearer $TOKEN"
```

---

## 🚀 Deployment Notes

- Không cần migration scripts (sử dụng schema hiện có)
- Không cần environment variables mới
- Build thành công với `mvn clean compile`
- Swagger documentation tự động generate

---

## 📊 Query Performance

**Query 1: Posts with PENDING reports (default - showAll=false)**
```sql
SELECT p.id AS postId,
       p.title AS title,
       p.content AS content,
       u.id AS authorId,
       u.fullName AS authorName,
       COUNT(r.id) AS reportCount,
       SUM(CASE WHEN r.status = 'PENDING' THEN 1 ELSE 0 END) AS pendingReportCount,
       MIN(r.createdAt) AS firstReportedAt,
       MAX(r.createdAt) AS latestReportedAt,
       p.status AS postStatus
FROM Report r
JOIN r.post p
JOIN p.user u
GROUP BY p.id, p.title, p.content, u.id, u.fullName, p.status
HAVING SUM(CASE WHEN r.status = 'PENDING' THEN 1 ELSE 0 END) >= 1
```

**Query 2: All reported posts (showAll=true)**
```sql
SELECT p.id AS postId,
       p.title AS title,
       p.content AS content,
       u.id AS authorId,
       u.fullName AS authorName,
       COUNT(r.id) AS reportCount,
       SUM(CASE WHEN r.status = 'PENDING' THEN 1 ELSE 0 END) AS pendingReportCount,
       MIN(r.createdAt) AS firstReportedAt,
       MAX(r.createdAt) AS latestReportedAt,
       p.status AS postStatus
FROM Report r
JOIN r.post p
JOIN p.user u
GROUP BY p.id, p.title, p.content, u.id, u.fullName, p.status
HAVING COUNT(r.id) >= 1
```

**Performance characteristics:**
- 2 separate queries tối ưu cho từng use case
- Single query với GROUP BY thay vì N+1 queries
- Sử dụng `SUM(CASE WHEN...)` để đếm conditional trong cùng 1 query
- Cả 2 queries đều trả về cả `reportCount` và `pendingReportCount`
- Projection chỉ lấy fields cần thiết
- Index trên `reports.status` và `reports.post_id` giúp query nhanh hơn
