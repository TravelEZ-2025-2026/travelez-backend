# TravelEz Backend - System Overview

## 📋 Tổng quan hệ thống

**TravelEz** là một nền tảng du lịch xã hội (Social Travel Platform) giúp người dùng:
- Khám phá và chia sẻ địa điểm du lịch
- Tạo và quản lý lịch trình du lịch với AI
- Tương tác xã hội (posts, comments, reactions, follow)
- Chat real-time
- Đánh giá và review địa điểm

---

## 🏗️ Kiến trúc

### Architecture Pattern
- **Layered Architecture** (Controller → Service → Repository → Entity)
- **Domain-Driven Design** (DDD) - Mỗi domain là module độc lập
- **Event-Driven Architecture** - Sử dụng Spring Events cho loose coupling

### Tech Stack

#### Backend Framework
- **Spring Boot 3.5.7** - Core framework
- **Java 21** - Programming language
- **Maven** - Build tool

#### Database & ORM
- **PostgreSQL** - Primary database
- **Spring Data JPA** - ORM framework
- **Hibernate 6.3** - JPA implementation
- **pgvector** - Vector storage cho AI embeddings

#### Security
- **Spring Security** - Authentication & Authorization
- **JWT (jjwt 0.12.6)** - Token-based authentication
- **Google OAuth2** - Social login
- **Google reCAPTCHA** - Bot protection

#### AI & Machine Learning
- **Spring AI 1.1.4** - AI integration framework
- **Google Gemini API** - AI model (gemini-2.5-pro, gemini-2.5-flash)
- **Gemini Embedding** - Text embeddings (768 dimensions)
- **Vector Search** - Semantic search với pgvector

#### File Storage
- **Google Cloud Storage (GCS)** - Cloud file storage
- **Apache Tika** - File type detection

#### Real-time Communication
- **WebSocket** - Real-time messaging
- **Spring WebSocket** - WebSocket support
- **STOMP** - Messaging protocol

#### Utilities
- **Lombok** - Boilerplate reduction
- **MapStruct 1.5.5** - Object mapping
- **SpringDoc OpenAPI** - API documentation (Swagger)
- **Spring Dotenv** - Environment variables management

---

## 📦 Package Structure

```
com.example.travelez.backend/
├── users/              # User management & authentication
├── posts/              # Travel posts & social content
├── poi/                # Points of Interest (địa điểm)
├── itinerary/          # Travel itinerary planning
├── comment/            # Comments on posts
├── reaction/           # Reactions (like, love, etc.)
├── review/             # POI reviews & ratings
├── social/             # Follow/Following relationships
├── chat/               # Real-time messaging
├── notification/       # User notifications
├── media/              # File & media management
├── report/             # Content reporting & moderation
├── enhancement/        # AI itinerary enhancement
├── ai/                 # AI services & pipelines
├── common/             # Shared utilities & DTOs
├── infrastructure/     # Infrastructure services
│   ├── websocket/      # WebSocket configuration
│   ├── gemini/         # Gemini AI integration
│   ├── filestorage/    # File storage service
│   └── captcha/        # Captcha verification
├── security/           # Security configuration
└── config/             # Global configurations
```

---

## 🎯 Core Features Overview

### 1. User Management (`users`)
- User registration & authentication
- Google OAuth2 login
- JWT token management
- User profiles (avatar, cover, bio)
- User roles: TRAVELER, PROVIDER, ADMIN

### 2. Points of Interest (`poi`)
- POI database (restaurants, hotels, attractions)
- Google Places integration
- POI search & filtering
- Location-based search
- Opening hours & contact info
- Admin POI management

### 3. Posts (`posts`)
- Create travel posts with media
- Post status: DRAFT, PUBLISHED, BANNED
- Topic tags & POI tagging
- Post moderation (admin)
- Post status history tracking

### 4. Itinerary Planning (`itinerary`)
- Create & manage travel itineraries
- AI-powered itinerary generation
- Budget estimation
- Activity scheduling
- Shared itineraries (collaborative planning)
- Itinerary enhancement with AI

### 5. Social Features
- **Comments** (`comment`): Comment on posts with media, nested replies
- **Reactions** (`reaction`): React to posts/comments (LIKE, LOVE, HAHA, WOW, SAD, ANGRY)
- **Follow** (`social`): Follow/unfollow users
- **Notifications** (`notification`): Real-time notifications

### 6. Reviews (`review`)
- Review POIs with ratings (1-5 stars)
- Upload review photos
- Crawled reviews from Google
- Review filtering & sorting

### 7. Chat (`chat`)
- Real-time messaging via WebSocket
- Conversation management
- Message media attachments
- Message recall/delete
- Auto cleanup old messages

### 8. Content Moderation (`report`)
- Report posts/comments/reviews
- Admin review & action
- Auto-ban on threshold
- Report status tracking

### 9. AI Enhancement (`enhancement`, `ai`)
- AI-powered itinerary enhancement
- Multi-phase enhancement pipeline
- Semantic search for POIs
- Personalized recommendations

### 10. Media Management (`media`)
- Upload to Google Cloud Storage
- Image/video support
- Media associations (posts, comments, reviews, messages)
- File type validation

---

## 🔐 Security & Authentication

### Authentication Flow
1. User login → JWT token issued
2. Token stored in client
3. Subsequent requests include token in `Authorization: Bearer <token>` header
4. JWT filter validates token
5. User principal loaded into SecurityContext

### Authorization
- Role-based access control (RBAC)
- Roles: `TRAVELER`, `PROVIDER`, `ADMIN`
- Method-level security with `@PreAuthorize`
- Resource ownership checks (permission checkers)

### OAuth2
- Google OAuth2 integration
- Auto user creation on first login
- Token management

---

## 🌐 API Design

### Response Format
All APIs return standardized `BaseResponse<T>`:
```json
{
  "code": 200,
  "message": "Success message",
  "data": { ... },
  "success": true
}
```

### Pagination
- **Offset-based**: `CommonPage<T>` (page, size, sort)
- **Cursor-based**: `CursorResponse<T>` (cursor, limit) - for infinite scroll

### Error Handling
- Global exception handler (`GlobalExceptionHandler`)
- Standardized error responses
- HTTP status codes aligned with `ResultCode`

---

## 📡 Real-time Features

### WebSocket
- Endpoint: `/ws`
- STOMP protocol
- Destinations:
  - `/app/chat.send` - Send message
  - `/user/queue/messages` - Receive messages
  - `/user/queue/notifications` - Receive notifications

### Events
- Spring Application Events for decoupling
- Event listeners for async processing
- Examples:
  - `PostsCreatedEvent` → Notification
  - `MessageSaveEvent` → Persist message
  - `ReportProcessedEvent` → Update post status

---

## 🤖 AI Integration

### Gemini AI
- **Models**:
  - `gemini-2.5-pro` - Complex reasoning
  - `gemini-2.5-flash` - Fast responses
- **Use cases**:
  - Itinerary generation
  - Itinerary enhancement
  - POI recommendations
  - Content analysis

### Vector Search
- **Embedding model**: `gemini-embedding-001` (768 dimensions)
- **Vector store**: pgvector (PostgreSQL extension)
- **Distance metric**: Cosine similarity
- **Use cases**:
  - Semantic POI search
  - Similar itinerary matching
  - User preference matching

### Enhancement Pipeline
4-phase enhancement process:
1. **Phase 1**: Extract itinerary context
2. **Phase 2**: Match POIs with activities
3. **Phase 3**: Build insight pool
4. **Phase 4**: Generate enhancement suggestions

---

## 📊 Database Design

### Key Tables
- `users` - User accounts (inheritance: traveler, provider, admin)
- `place_of_interest` - POI database
- `posts` - Travel posts
- `itinerary` - Travel plans
- `itinerary_activity` - Itinerary activities
- `comment` - Post comments
- `review` - POI reviews
- `reaction` - User reactions
- `follow` - Follow relationships
- `conversation` - Chat conversations
- `message` - Chat messages
- `notification` - User notifications
- `report` - Content reports
- `media` - File metadata

### Relationships
- Many-to-Many: `media_posts`, `media_comment`, `media_review`, `media_message`, `media_poi`
- Soft deletes: `deleted_at` column (POI, Comment)
- Audit fields: `created_at`, `updated_at` (via `AuditableEntity`)

---

_Continued in next sections..._
