# TravelEz Backend - API Reference

## 🌐 API Endpoints Summary

Base URL: `http://localhost:8080/api`

Swagger UI: `http://localhost:8080/swagger-ui/index.html`

---

## Authentication

### Public Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/register` | Register new user |
| POST | `/auth/login` | Login with email/password |
| POST | `/auth/google` | Google OAuth login |
| POST | `/auth/refresh` | Refresh JWT token |

### Protected Endpoints
All other endpoints require JWT token in header:
```
Authorization: Bearer <jwt_token>
```

---

## Users

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| GET | `/users/me` | Any | Get current user profile |
| PATCH | `/users/me` | Any | Update current user profile |
| GET | `/users/{userId}` | Any | Get user by ID |
| GET | `/users/search` | Any | Search users |
| PATCH | `/users/me/avatar` | Any | Update avatar |
| PATCH | `/users/me/cover` | Any | Update cover photo |

---

## POI (Points of Interest)

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| GET | `/pois` | Any | List POIs with filters |
| GET | `/pois/{poiId}` | Any | Get POI details |
| GET | `/pois/search` | Any | Search POIs (text + semantic) |
| GET | `/pois/nearby` | Any | Nearby POIs (lat/lng) |
| GET | `/places` | Any | List places (cities) |
| GET | `/places/{placeId}/wards` | Any | Get wards in place |

### Admin POI Management
| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | `/admin/pois` | Admin | Create POI |
| PATCH | `/admin/pois/{poiId}` | Admin | Update POI |
| DELETE | `/admin/pois/{poiId}` | Admin | Delete POI |
| POST | `/admin/pois/{poiId}/approve` | Admin | Approve POI |
| POST | `/admin/pois/{poiId}/reject` | Admin | Reject POI |

---

## Posts

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | `/posts` | Traveler | Create post |
| GET | `/posts` | Any | List posts (cursor pagination) |
| GET | `/posts/{postId}` | Any | Get post details |
| PATCH | `/posts/{postId}` | Traveler | Update own post |
| DELETE | `/posts/{postId}` | Traveler | Delete own post |
| GET | `/posts/search` | Any | Search posts |
| GET | `/posts/user/{userId}` | Any | Get user's posts |

### Admin Post Management
| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| GET | `/admin/posts` | Admin | List all posts |
| POST | `/admin/posts/{postId}/ban` | Admin | Ban post |
| POST | `/admin/posts/{postId}/unban` | Admin | Unban post |

---

## Itineraries

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | `/itineraries` | Traveler | Create itinerary |
| GET | `/itineraries` | Traveler | List own itineraries |
| GET | `/itineraries/{itineraryId}` | Traveler | Get itinerary details |
| PATCH | `/itineraries/{itineraryId}` | Traveler | Update itinerary |
| DELETE | `/itineraries/{itineraryId}` | Traveler | Delete itinerary |
| POST | `/itineraries/{itineraryId}/share` | Traveler | Share itinerary |
| POST | `/itineraries/{itineraryId}/enhance` | Traveler | AI enhance itinerary |

### Itinerary Activities
| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | `/itineraries/{itineraryId}/activities` | Traveler | Add activity |
| PATCH | `/itineraries/{itineraryId}/activities/{activityId}` | Traveler | Update activity |
| DELETE | `/itineraries/{itineraryId}/activities/{activityId}` | Traveler | Delete activity |
| POST | `/itineraries/{itineraryId}/activities/reorder` | Traveler | Reorder activities |

---

## Comments

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | `/posts/{postId}/comments` | Traveler | Create comment |
| GET | `/posts/{postId}/comments` | Any | List post comments |
| POST | `/comments/{commentId}/replies` | Traveler | Reply to comment |
| PATCH | `/comments/{commentId}` | Traveler | Update own comment |
| DELETE | `/comments/{commentId}` | Traveler | Delete own comment |

---

## Reactions

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | `/reactions` | Traveler | Add/update reaction |
| DELETE | `/reactions` | Traveler | Remove reaction |
| GET | `/posts/{postId}/reactions` | Any | Get post reactions |
| GET | `/comments/{commentId}/reactions` | Any | Get comment reactions |

**Request Body (POST):**
```json
{
  "targetId": 123,
  "targetType": "POST",
  "reactionType": "LIKE"
}
```

---

## Reviews

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | `/pois/{poiId}/reviews` | Traveler | Create review |
| GET | `/pois/{poiId}/reviews` | Any | List POI reviews |
| GET | `/reviews/{reviewId}` | Any | Get review details |
| PATCH | `/reviews/{reviewId}` | Traveler | Update own review |
| DELETE | `/reviews/{reviewId}` | Traveler | Delete own review |

**Query Parameters:**
- `rating` - Filter by rating (1-5)
- `sortField` - Sort field (default: id)
- `sortDirection` - ASC/DESC
- `page` - Page number
- `size` - Page size

---

## Social (Follow)

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | `/follow/{userId}` | Traveler | Follow user |
| DELETE | `/follow/{userId}` | Traveler | Unfollow user |
| GET | `/users/{userId}/followers` | Any | Get followers list |
| GET | `/users/{userId}/following` | Any | Get following list |
| GET | `/follow/status/{userId}` | Traveler | Check follow status |

---

## Chat

### REST Endpoints
| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | `/conversations` | Traveler | Create conversation |
| GET | `/conversations` | Traveler | List conversations |
| GET | `/conversations/{conversationId}` | Traveler | Get conversation details |
| GET | `/conversations/{conversationId}/messages` | Traveler | Get messages |
| POST | `/messages/{messageId}/recall` | Traveler | Recall message |

### WebSocket Endpoints
- **Connect**: `ws://localhost:8080/ws`
- **Send message**: `/app/chat.send`
- **Receive messages**: `/user/queue/messages`
- **Receive notifications**: `/user/queue/notifications`

**WebSocket Message Format:**
```json
{
  "conversationId": 123,
  "content": "Hello!",
  "mediaIds": [1, 2, 3]
}
```

---

## Notifications

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| GET | `/notifications` | Traveler | List notifications |
| PATCH | `/notifications/{notificationId}/read` | Traveler | Mark as read |
| PATCH | `/notifications/read-all` | Traveler | Mark all as read |
| DELETE | `/notifications/{notificationId}` | Traveler | Delete notification |
| GET | `/notifications/unread-count` | Traveler | Get unread count |

---

## Reports

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | `/reports` | Traveler | Submit report |
| GET | `/reports/my-reports` | Traveler | Get own reports |

### Admin Report Management
| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| GET | `/admin/reports` | Admin | List all reports |
| GET | `/admin/reports/{reportId}` | Admin | Get report details |
| POST | `/admin/reports/{reportId}/approve` | Admin | Approve report |
| POST | `/admin/reports/{reportId}/reject` | Admin | Reject report |

**Report Request:**
```json
{
  "targetId": 123,
  "targetType": "POST",
  "reportType": "SPAM",
  "reason": "This is spam content"
}
```

---

## Media Upload

Media upload is handled via multipart/form-data in respective endpoints:
- Posts: `POST /posts` with `files` field
- Comments: `POST /posts/{postId}/comments` with `files` field
- Reviews: `POST /pois/{poiId}/reviews` with `files` field
- Messages: WebSocket with `mediaIds` (upload first, then send)

**File Limits:**
- Max file size: 20MB
- Max request size: 100MB
- Supported formats: jpg, png, gif, webp, mp4, mov

---

## Common Query Parameters

### Pagination (Offset-based)
- `page` - Page number (default: 0)
- `size` - Page size (default: 10)
- `sortField` - Sort field (default: id)
- `sortDirection` - ASC/DESC (default: ASC)

### Pagination (Cursor-based)
- `cursor` - Last item ID
- `limit` - Number of items (default: 10)

### Filtering
- Varies by endpoint, check Swagger docs

---

## Response Format

### Success Response
```json
{
  "code": 200,
  "message": "Success message",
  "data": { ... },
  "success": true
}
```

### Error Response
```json
{
  "code": 400,
  "message": "Error message",
  "data": null,
  "success": false
}
```

### Paginated Response (Offset)
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "content": [...],
    "page": 0,
    "size": 10,
    "totalElements": 100,
    "totalPages": 10,
    "last": false
  },
  "success": true
}
```

### Paginated Response (Cursor)
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "items": [...],
    "nextCursor": "123",
    "hasMore": true
  },
  "success": true
}
```

---

## HTTP Status Codes

| Code | Meaning | Usage |
|------|---------|-------|
| 200 | OK | Successful GET, PATCH, DELETE |
| 201 | Created | Successful POST |
| 400 | Bad Request | Validation error |
| 401 | Unauthorized | Missing/invalid token |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource not found |
| 500 | Internal Server Error | Server error |

---

## Rate Limiting

Currently not implemented. Consider adding in production.

---

## CORS Configuration

Allowed origins configured via environment variable:
```
ALLOWED_ORIGINS=http://localhost:5173,https://travelez.com
```

---

_For detailed request/response schemas, refer to Swagger UI_
