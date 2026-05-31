
```markdown
# Tài liệu Tích hợp API Module Lịch trình (Itinerary - M1)

Tài liệu này mô tả chi tiết các endpoint thuộc module quản lý lịch trình. Tất cả API đều tuân thủ chuẩn RESTful và trả về định dạng `BaseResponse` thống nhất.

## 1. Cấu trúc Phản hồi Chuẩn (Base Response)

Tất cả các API trả về HTTP Status Code 200 kèm theo cấu trúc JSON sau. Frontend bóc tách dữ liệu từ trường `data`.

```json
{
  "code": 200,
  "message": "Thông báo từ server (ví dụ: Success)",
  "data": { ... } // Payload thực tế của từng API
}
```

---

## 2. Object Dữ liệu Cốt lõi (Shared Models)

### ItineraryResponse (Kết quả lịch trình từ AI)
Được trả về trong các API: Generate, Replan, Get Temp, và Get Detail.

```json
{
  "tempId": "uuid-string-1234",
  "tripTitle": "Tên chuyến đi",
  "destinationCities": ["ho_chi_minh"],
  "reasoningSummary": "Mô tả lý do gợi ý...",
  "estimatedBudget": {
    "total": 5000000.0,
    "transportation": 1000000.0,
    "activity": 2000000.0,
    "foodAndDrink": 1000000.0,
    "accommodation": 1000000.0,
    "currency": "VND"
  },
  "days": [
    {
      "dayIndex": 1,
      "date": "2025-11-11",
      "activities": [
        {
          "id": 101, // Dùng để định danh POI nếu cần xoá (Replan)
          "title": "Dinh Độc Lập",
          "activityName": "Tham quan di tích lịch sử",
          "activityType": "ATTRACTION",
          "startTime": "08:00",
          "endTime": "10:00",
          "address": "135 Nam Kỳ Khởi Nghĩa...",
          "price": 40000.0,
          "aiTip": "Nên đi sớm để tránh đông...",
          "image": "[https://link-anh.jpg](https://link-anh.jpg)",
          "lat": 10.7768,
          "lng": 106.6953
        }
      ]
    }
  ]
}
```

---

## 3. Danh sách Endpoint Chi tiết

### 3.1. Tạo mới lịch trình tự động (Generate)
- **Endpoint:** `/api/itineraries/generate`
- **Method:** `POST`
- **Mô tả:** Gửi form thông tin ban đầu để hệ thống AI sinh ra kịch bản lịch trình nháp. API này có thời gian chờ xử lý lâu.

**Request Body (`ItineraryCreationRequest`):**
```json
{
  "destinationCities": ["ho_chi_minh"],
  "budget": 5000000, 
  "startDate": "2025-11-11",
  "endDate": "2025-11-14",
  "styles": ["Food Tourism", "Photography"],
  "companion": "Family Expedition",
  "hasKids": true,
  "hasPets": false,
  "specialNotes": "Gia đình không đi bộ nhiều được"
}
```

**Response Data:** Object `ItineraryResponse` (Chứa `tempId` dùng cho việc lưu hoặc phục hồi).

---

### 3.2. Chỉnh sửa lịch trình (Replan)
- **Endpoint:** `/api/itineraries/replan`
- **Method:** `POST`
- **Mô tả:** Tái tạo lại lịch trình dựa trên bản nháp hiện tại kèm theo phản hồi của người dùng.

> **Sự khác biệt Request Payload giữa Replan và Generate:**
> `ItineraryReplanRequest` kế thừa toàn bộ các field của `ItineraryCreationRequest` (để AI giữ lại bối cảnh chuyến đi), nhưng yêu cầu truyền thêm 3 trường dữ liệu đặc thù để xử lý cập nhật:
> 1. `feedbackNotes`: Dạng text mô tả yêu cầu sửa đổi (VD: "Thêm quán cafe").
> 2. `rejectedPoiIds`: Mảng chứa danh sách ID các địa điểm người dùng muốn xoá (Frontend có nhiệm vụ cộng dồn mảng này qua các lần replan).
> 3. `previousItinerary`: Toàn bộ cục dữ liệu `ItineraryResponse` của lần sinh gần nhất.

**Request Body (`ItineraryReplanRequest`):**
```json
{
  "destinationCities": ["ho_chi_minh"],
  "budget": 5000000, 
  "startDate": "2025-11-11",
  "endDate": "2025-11-14",
  "styles": ["Food Tourism", "Photography"],
  "companion": "Family Expedition",
  "hasKids": true,
  "hasPets": false,
  "specialNotes": "Gia đình không đi bộ nhiều được",
  
  // Trường dữ liệu mới dành riêng cho Replan
  "feedbackNotes": "Tôi muốn thêm 1 quán ăn chay vào buổi tối ngày thứ 2",
  "rejectedPoiIds": [101, 250],
  "previousItinerary": { 
     // Toàn bộ Object ItineraryResponse đang hiển thị trên UI
  }
}
```

**Response Data:** Object `ItineraryResponse` mới đã được AI cập nhật, cấp kèm một `tempId` mới.

---

### 3.3. Phục hồi lịch trình nháp (Get Temp Itinerary)
- **Endpoint:** `/api/itineraries/temp/{tempId}`
- **Method:** `GET`
- **Mô tả:** Lấy lại dữ liệu lịch trình chưa lưu (trong bộ nhớ tạm) thông qua mã `tempId`. Hữu ích khi người dùng tải lại trang Preview.

**Response Data:** Object `ItineraryResponse`.

---

### 3.4. Lưu lịch trình chính thức (Save Itinerary)
- **Endpoint:** `/api/itineraries/save`
- **Method:** `POST`
- **Mô tả:** Lưu vĩnh viễn dữ liệu vào cơ sở dữ liệu sau khi người dùng chốt kịch bản.

**Request Body (`ItinerarySaveRequest`):**
```json
{
  "createRequest": { 
    // Data form ban đầu (ItineraryCreationRequest)
  },
  "aiResult": { 
    // Data lịch trình chốt hạ (ItineraryResponse)
  }
}
```

**Response Data:** `Long` (ID thực tế của lịch trình trong hệ thống. Ví dụ: `42`).

---

### 3.5. Lấy danh sách lịch trình (Get Itinerary Summary)
- **Endpoint:** `/api/itineraries`
- **Method:** `GET`
- **Mô tả:** Lấy danh sách phân trang các lịch trình thuộc sở hữu của người dùng đang đăng nhập.
- **Query Params:**
    - `page` (Integer): Số thứ tự trang (Mặc định: 0)
    - `size` (Integer): Số phần tử trên mỗi trang (Mặc định: 10)

**Response Data:**
```json
{
  "content": [
    {
      "id": 42,
      "title": "Chuyến đi Sài Gòn",
      "destinationCities": ["ho_chi_minh"],
      "styles": ["Food Tourism"],
      "startDate": "2025-11-11",
      "endDate": "2025-11-14",
      "status": "DRAFT",
      "createdAt": "2024-04-24T10:00:00",
      "ownerUsername": "hieuvm",
      "isPublic": false
    }
  ],
  "totalPages": 1,
  "totalElements": 1,
  "pageSize": 10,
  "pageNumber": 0,
  "empty": false
}
```

---

### 3.6. Xem chi tiết lịch trình (Get Detail)
- **Endpoint:** `/api/itineraries/{id}`
- **Method:** `GET`
- **Mô tả:** Lấy toàn bộ thông tin chi tiết của một lịch trình đã lưu. API có kiểm tra quyền truy cập (Chỉ chủ sở hữu hoặc người được share mới có thể xem).

**Response Data (`ItineraryDetailResponse`):** Bao gồm toàn bộ cấu trúc của `ItineraryResponse` và bổ sung thêm các trường thông tin cấu hình ban đầu:
```json
{
  "id": 42,
  "userId": 1,
  "isPublic": true,
  "tripTitle": "Chuyến đi Sài Gòn",
  "hasKids": true,
  "hasPets": false,
  "companion": "Family Expedition",
  "styles": ["Food Tourism"],
  "specialNotes": "Gia đình không đi bộ nhiều được"
}

```

---

### 3.7. Xoá lịch trình (Delete)
- **Endpoint:** `/api/itineraries/{id}`
- **Method:** `DELETE`
- **Mô tả:** Xoá vĩnh viễn một lịch trình khỏi hệ thống.

**Response Data:** `null` (Dựa vào `code` 200 ở BaseResponse để xác định thành công).
```