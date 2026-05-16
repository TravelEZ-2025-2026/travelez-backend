# TravelEz Backend - Domain Details

## 📚 Chi tiết các Domain

### 1. Users Domain

#### Entities
- **User** (base class)
  - `Traveler` - Người dùng thông thường
  - `Provider` - Nhà cung cấp dịch vụ
  - `Admin` - Quản trị viên
- **UserOAuthToken** - OAuth tokens
- **UserProfileVector** - User preference embeddings

#### Key Features
- User registration (email/password)
- Google OAuth2 login
- JWT authentication
- Profile management (avatar, cover, bio)
- User search & filtering
- Follow/follower counts

#### Endpoints
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login
- `POST /api/auth/google` - Google OAuth login
- `GET /api/users/me` - Get current user
- `PATCH /api/users/me` - Update profile
- `GET /api/users/{userId}` - Get user by ID
- `GET /api/users/search` - Search users

---

### 2. POI (Points of Interest) Domain

#### Entities
- **Poi** - Place of Interest
- **Place** - City/Province
- **Ward** - District/Ward
- **PoiHour** - Opening hours
- **OpeningHours** (JSONB) - Detailed hours
- **ReviewDistributions** (JSONB) - Rating distribution

#### Key Features
- POI database with Google Places integration
- Location-based search (lat/lng radius)
- Semantic search (vector similarity)
- POI filtering (type, rating, status)
- Opening hours management
- Admin POI management (approve/reject)

#### POI Types
- RESTAURANT, CAFE, BAR
- HOTEL, HOSTEL, RESORT
- ATTRACTION, MUSEUM, PARK
- SHOPPING, MARKET
- ENTERTAINMENT, NIGHTLIFE

#### Endpoints
- `GET /api/pois` - List POIs with filters
- `GET /api/pois/{poiId}` - Get POI details
- `GET /api/pois/search` - Search POIs
- `GET /api/pois/nearby` - Nearby POIs (location-based)
- `POST /api/admin/pois` - Create POI (admin)
- `PATCH /api/admin/pois/{poiId}` - Update POI (admin)

---

### 3. Posts Domain

#### Entities
- **Posts** - Travel posts
- **PostStatusHistory** - Status change tracking

#### Post Status
- `DRAFT` - Not published
- `PUBLISHED` - Public
- `BANNED` - Moderated/removed

#### Key Features
- Create posts with text, media, POI tag
- Topic tags for categorization
- Post status management
- Cursor-based pagination (infinite scroll)
- Search & filter posts
- Admin moderation

#### Events
- `PostsCreatedEvent` - Trigger notifications
- `PostsUpdateEvent` - Update related data
- `PostsDeleteEvent` - Cleanup
- `PostsStatusChangedEvent` - Status tracking

#### Endpoints
- `POST /api/posts` - Create post
- `GET /api/posts` - List posts (cursor pagination)
- `GET /api/posts/{postId}` - Get post details
- `PATCH /api/posts/{postId}` - Update post
- `DELETE /api/posts/{postId}` - Delete post
- `GET /api/posts/search` - Search posts
- `POST /api/admin/posts/{postId}/ban` - Ban post (admin)
- `POST /api/admin/posts/{postId}/unban` - Unban post (admin)

---

### 4. Itinerary Domain

#### Entities
- **Itinerary** - Travel plan
- **ItineraryActivity** - Daily activities
- **ItinerarySharedUser** - Shared access

#### Itinerary Status
- `DRAFT` - In planning
- `CONFIRMED` - Finalized
- `COMPLETED` - Trip finished
- `CANCELLED` - Cancelled

#### Key Features
- Create itineraries with AI assistance
- Multi-day activity planning
- Budget estimation (total, transportation, food, accommodation, activities)
- Shared itineraries (collaborative planning)
- Activity management (add/edit/delete/reorder)
- Itinerary templates

#### Budget Breakdown
- `estimatedTotalPrice`
- `estimatedTransportationPrice`
- `estimatedActivityPrice`
- `estimatedFoodAndDrinkPrice`
- `estimatedAccommodationPrice`

#### Endpoints
- `POST /api/itineraries` - Create itinerary
- `GET /api/itineraries` - List user itineraries
- `GET /api/itineraries/{itineraryId}` - Get details
- `PATCH /api/itineraries/{itineraryId}` - Update
- `DELETE /api/itineraries/{itineraryId}` - Delete
- `POST /api/itineraries/{itineraryId}/activities` - Add activity
- `PATCH /api/itineraries/{itineraryId}/activities/{activityId}` - Update activity
- `DELETE /api/itineraries/{itineraryId}/activities/{activityId}` - Delete activity
- `POST /api/itineraries/{itineraryId}/share` - Share with users

---

### 5. Comment Domain

#### Entities
- **Comment** - Post comments

#### Key Features
- Comment on posts
- Nested replies (parent-child)
- Media attachments
- Soft delete
- Comment count tracking

#### Endpoints
- `POST /api/posts/{postId}/comments` - Create comment
- `GET /api/posts/{postId}/comments` - List comments
- `POST /api/comments/{commentId}/replies` - Reply to comment
- `PATCH /api/comments/{commentId}` - Update comment
- `DELETE /api/comments/{commentId}` - Delete comment

---

### 6. Reaction Domain

#### Entities
- **Reaction** - User reactions

#### Reaction Types
- `LIKE` 👍
- `LOVE` ❤️
- `HAHA` 😂
- `WOW` 😮
- `SAD` 😢
- `ANGRY` 😠

#### Target Types
- `POST` - React to posts
- `COMMENT` - React to comments

#### Key Features
- React to posts/comments
- Change reaction type
- Remove reaction
- Reaction count by type
- User's reaction status

#### Endpoints
- `POST /api/reactions` - Add/update reaction
- `DELETE /api/reactions` - Remove reaction
- `GET /api/posts/{postId}/reactions` - Get post reactions
- `GET /api/comments/{commentId}/reactions` - Get comment reactions

---

### 7. Review Domain

#### Entities
- **Review** - POI reviews

#### Review Status
- `APPROVED` - Visible
- `PENDING` - Under review
- `REJECTED` - Hidden

#### Key Features
- Review POIs with rating (1-5 stars)
- Upload review photos
- Crawled reviews from Google Maps
- Review filtering (rating, date)
- Review statistics

#### Endpoints
- `POST /api/pois/{poiId}/reviews` - Create review
- `GET /api/pois/{poiId}/reviews` - List POI reviews
- `GET /api/reviews/{reviewId}` - Get review details
- `PATCH /api/reviews/{reviewId}` - Update review
- `DELETE /api/reviews/{reviewId}` - Delete review

---

### 8. Social Domain

#### Entities
- **Follow** - Follow relationships

#### Key Features
- Follow/unfollow users
- Follower/following lists
- Follow counts
- Follow status check

#### Endpoints
- `POST /api/follow/{userId}` - Follow user
- `DELETE /api/follow/{userId}` - Unfollow user
- `GET /api/users/{userId}/followers` - Get followers
- `GET /api/users/{userId}/following` - Get following
- `GET /api/follow/status/{userId}` - Check follow status

---

### 9. Chat Domain

#### Entities
- **Conversation** - Chat conversations
- **ConversationMember** - Conversation participants
- **Message** - Chat messages

#### Conversation Types
- `DIRECT` - 1-on-1 chat
- `GROUP` - Group chat

#### Key Features
- Real-time messaging via WebSocket
- Create conversations
- Send messages with media
- Message recall/delete
- Unread message count
- Auto cleanup old messages (scheduled task)

#### WebSocket Endpoints
- `/app/chat.send` - Send message
- `/user/queue/messages` - Receive messages

#### REST Endpoints
- `POST /api/conversations` - Create conversation
- `GET /api/conversations` - List conversations
- `GET /api/conversations/{conversationId}/messages` - Get messages
- `POST /api/messages/{messageId}/recall` - Recall message

---

### 10. Notification Domain

#### Entities
- **Notification** - User notifications

#### Notification Types
- `POST_CREATED` - New post from followed user
- `POST_REACTION` - Someone reacted to your post
- `POST_COMMENT` - Someone commented on your post
- `COMMENT_REPLY` - Reply to your comment
- `FOLLOW` - New follower
- `SYSTEM` - System notifications

#### Key Features
- Real-time notifications via WebSocket
- Mark as read
- Notification history
- Notification preferences

#### Endpoints
- `GET /api/notifications` - List notifications
- `PATCH /api/notifications/{notificationId}/read` - Mark as read
- `PATCH /api/notifications/read-all` - Mark all as read
- `DELETE /api/notifications/{notificationId}` - Delete notification

---

### 11. Report Domain

#### Entities
- **Report** - Content reports

#### Report Types
- `SPAM` - Spam content
- `HARASSMENT` - Harassment/bullying
- `INAPPROPRIATE` - Inappropriate content
- `MISINFORMATION` - False information
- `OTHER` - Other reasons

#### Report Status
- `PENDING` - Under review
- `APPROVED` - Action taken
- `REJECTED` - No action needed

#### Key Features
- Report posts/comments/reviews
- Admin review queue
- Auto-ban on threshold (configurable)
- Report history

#### Endpoints
- `POST /api/reports` - Submit report
- `GET /api/admin/reports` - List reports (admin)
- `POST /api/admin/reports/{reportId}/approve` - Approve report (admin)
- `POST /api/admin/reports/{reportId}/reject` - Reject report (admin)

---

### 12. Enhancement Domain

#### Entities
- **ItineraryEnhancementHistory** - Enhancement history
- **ItineraryEnhancementContext** - Enhancement context

#### Key Features
- AI-powered itinerary enhancement
- 4-phase enhancement pipeline
- Personalized recommendations
- Enhancement history tracking

#### Enhancement Pipeline
1. **Phase 1 - Extraction**: Extract itinerary context
2. **Phase 2 - Matching**: Match POIs with activities
3. **Phase 3 - Insight Pool**: Build recommendation pool
4. **Phase 4 - Analysis**: Generate enhancement suggestions

#### Endpoints
- `POST /api/itineraries/{itineraryId}/enhance` - Enhance itinerary
- `GET /api/itineraries/{itineraryId}/enhancements` - Get enhancement history

---

### 13. Media Domain

#### Entities
- **Media** - File metadata

#### Media Types
- `IMAGE` - Images (jpg, png, gif, webp)
- `VIDEO` - Videos (mp4, mov, avi)
- `DOCUMENT` - Documents (pdf, doc)

#### Key Features
- Upload to Google Cloud Storage
- File type validation
- Media associations (posts, comments, reviews, messages, POI)
- Public URL generation
- File size limits (20MB per file, 100MB per request)

#### Storage Structure
```
gs://{bucket}/
  ├── posts/{userId}/{filename}
  ├── comments/{userId}/{filename}
  ├── reviews/{userId}/{filename}
  ├── messages/{userId}/{filename}
  ├── avatars/{userId}/{filename}
  └── covers/{userId}/{filename}
```

---

_End of Domain Details_
