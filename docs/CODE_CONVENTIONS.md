# TravelEz Backend - Code Conventions & Architecture Guide

## 📋 Tổng quan

Project **TravelEz Backend** là một ứng dụng Spring Boot 3.5.7 với Java 21, sử dụng kiến trúc layered architecture và domain-driven design.

---

## 🏗️ Kiến trúc hệ thống

### Package Structure (Domain-Driven)

```
com.example.travelez.backend/
├── [domain]/                    # Mỗi domain là một module độc lập
│   ├── controller/              # REST API endpoints
│   ├── service/                 # Business logic interface
│   │   └── impl/                # Service implementation
│   ├── repository/              # Data access layer
│   │   ├── specification/       # JPA Specifications (nếu cần)
│   │   └── projection/          # Query projections
│   ├── model/                   # Entity classes
│   │   └── enums/               # Domain enums
│   ├── dto/                     # Data Transfer Objects
│   │   ├── request/             # Request DTOs
│   │   └── response/            # Response DTOs
│   ├── mapper/                  # MapStruct mappers
│   ├── event/                   # Domain events (nếu có)
│   ├── listener/                # Event listeners
│   ├── handler/                 # Business handlers
│   ├── permission/              # Permission checkers
│   │   └── impl/
│   ├── config/                  # Domain-specific config
│   └── [other domain packages]
├── common/                      # Shared utilities
│   ├── api/                     # Common API responses
│   ├── dto/                     # Common DTOs
│   ├── exception/               # Exception handling
│   ├── model/                   # Base entities
│   └── utils/                   # Utility classes
├── infrastructure/              # Infrastructure concerns
│   ├── config/                  # Global configurations
│   ├── websocket/               # WebSocket support
│   ├── gemini/                  # AI integration
│   ├── filestorage/             # File storage
│   └── captcha/                 # Captcha service
├── security/                    # Security configuration
│   ├── config/                  # Security configs
│   ├── component/               # JWT filters, handlers
│   └── util/                    # Security utilities
└── config/                      # Global app configs
```

### Các Domain chính

- **users** - Quản lý người dùng, authentication
- **posts** - Bài viết du lịch
- **poi** - Points of Interest (địa điểm)
- **itinerary** - Lịch trình du lịch
- **comment** - Bình luận
- **reaction** - Tương tác (like, love, etc.)
- **review** - Đánh giá
- **social** - Follow/Following
- **chat** - Tin nhắn
- **notification** - Thông báo
- **media** - Quản lý file/media
- **report** - Báo cáo vi phạm
- **enhancement** - Cải thiện lịch trình (AI)
- **ai** - AI services

---

## 💻 Code Style & Conventions

### 1. Naming Conventions

#### Classes
- **Entity**: Số ít, PascalCase - `User`, `Posts`, `Itinerary`
- **DTO Request**: `[Entity][Action]Request` - `PostsCreateRequest`, `UserUpdateRequest`
- **DTO Response**: `[Entity]Response` hoặc `[Entity]DetailResponse` - `PostResponse`, `PostsDetailResponse`
- **Service Interface**: `[Entity]Service` - `PostsService`, `UserService`
- **Service Impl**: `[Entity]ServiceImpl` - `PostsServiceImpl`
- **Repository**: `[Entity]Repository` - `PostsRepository`
- **Controller**: `[Entity]Controller` - `PostsController`
- **Mapper**: `[Entity]Mapper` - `PostsMapper`

#### Methods
- **CRUD**: `create`, `update`, `delete`, `get`, `find`
- **Query**: `findBy...`, `existsBy...`, `countBy...`
- **Boolean**: `is...`, `has...`, `can...`
- **Service methods**: camelCase, động từ đầu tiên - `createPost()`, `getUserPosts()`

#### Variables
- camelCase - `userId`, `postId`, `currentUser`
- Constants: UPPER_SNAKE_CASE - `MAX_FILE_SIZE`, `DEFAULT_PAGE_SIZE`

### 2. Annotations

#### Entity
```java
@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Posts extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "title")
    private String title;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
```

**Quy tắc:**
- Luôn dùng `@Table(name = "...")` với tên bảng snake_case
- Dùng `@Column(name = "...")` cho các field
- Entity kế thừa `AuditableEntity` để có `createdAt`, `updatedAt`
- Dùng Lombok: `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`

#### DTO
```java
@Data
public class PostsCreateRequest {
    @NotBlank(message = "Content must not be blank")
    private String content;
    
    @NotNull(message = "Post status is required")
    @Enumerated(EnumType.STRING)
    private PostStatus status;
    
    private List<MultipartFile> files;
}
```

```java
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private UserSummaryResponse author;
    private List<MediaBaseResponse> medias;
}
```

**Quy tắc:**
- Request DTO: `@Data` + validation annotations
- Response DTO: `@Data`, `@SuperBuilder`, `@NoArgsConstructor`, `@AllArgsConstructor`
- Dùng `@Valid` trong controller để validate

#### Controller
```java
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Tag(name = "Posts", description = "Posts endpoints")
public class PostsController {
    private final PostsService postsService;
    
    @PostMapping
    public ResponseEntity<BaseResponse<Void>> createPosts(
            @ModelAttribute @Valid PostsCreateRequest request) {
        postsService.createPost(request);
        return BaseResponse.success(null, ResultCode.CREATED, "Posts created successfully");
    }
    
    @GetMapping("/{postId}")
    public ResponseEntity<BaseResponse<PostsDetailResponse>> getPostById(
            @PathVariable Long postId) {
        PostsDetailResponse response = postsService.getPostDetail(postId);
        return BaseResponse.success(response, ResultCode.SUCCESS, "Post fetched successfully");
    }
}
```

**Quy tắc:**
- `@RestController` + `@RequestMapping("/api/[domain]")`
- `@RequiredArgsConstructor` cho dependency injection
- `@Tag` cho Swagger documentation
- Luôn return `ResponseEntity<BaseResponse<T>>`
- Dùng `@Valid` cho validation
- HTTP methods: `@GetMapping`, `@PostMapping`, `@PatchMapping`, `@DeleteMapping`

#### Service
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class PostsServiceImpl implements PostsService {
    private final PostsRepository postsRepository;
    private final PostsMapper postsMapper;
    private final ApplicationEventPublisher eventPublisher;
    
    @Override
    public void createPost(PostsCreateRequest request) {
        Posts post = postsMapper.toPosts(request, SecurityUtils.getCurrentUserId(), request.getPoiId());
        Posts savedPost = postsRepository.save(post);
        eventPublisher.publishEvent(new PostsCreatedEvent(...));
    }
}
```

**Quy tắc:**
- `@Service` + `@RequiredArgsConstructor` + `@Slf4j`
- Implement interface
- Inject dependencies qua constructor (final fields)
- Dùng `@Transactional` khi cần transaction
- Log với `log.info()`, `log.error()`, `log.debug()`

#### Repository
```java
public interface PostsRepository extends JpaRepository<Posts, Long>, JpaSpecificationExecutor<Posts> {
    
    @EntityGraph(attributePaths = {"user", "poi"})
    Optional<Posts> findByIdAndUserId(Long postId, Long userId);
    
    @Query("SELECT p FROM Posts p WHERE p.status = 'PUBLISHED' ORDER BY p.id DESC")
    Slice<Posts> findAllPostsFirstPage(Pageable pageable);
    
    @Modifying
    @Query(value = "DELETE FROM posts WHERE id = :postId", nativeQuery = true)
    void deletePostById(Long postId);
}
```

**Quy tắc:**
- Extend `JpaRepository<Entity, ID>`
- Thêm `JpaSpecificationExecutor<Entity>` nếu cần dynamic query (search/filter với nhiều điều kiện tùy chọn)
- Dùng `@EntityGraph` để tránh N+1 query
- Dùng `@Query` cho custom query
- Dùng `@Modifying` cho UPDATE/DELETE query

**Specification — Tách ra class riêng trong `repository/specification/`:**

> ❌ **SAI** — Viết Specification inline trong Service:
> ```java
> // PostsServiceImpl.java ← KHÔNG làm thế này
> Specification<Posts> spec = (root, query, cb) -> {
>     var predicates = new ArrayList<Predicate>();
>     if (request.getStatus() != null) {
>         predicates.add(cb.equal(root.get("status"), request.getStatus()));
>     }
>     return cb.and(predicates.toArray(new Predicate[0]));
> };
> ```

> ✅ **ĐÚNG** — Tạo class `[Entity]Specification` riêng trong `repository/specification/`:
> ```java
> // repository/specification/PostsSpecification.java
> public class PostsSpecification {
>     public static Specification<Posts> filterByStatus(PostStatus status) {
>         return (root, query, cb) ->
>             status == null ? null : cb.equal(root.get("status"), status);
>     }
>     public static Specification<Posts> filterByUserId(Long userId) {
>         return (root, query, cb) ->
>             userId == null ? null : cb.equal(root.get("user").get("id"), userId);
>     }
> }
> 
> // PostsServiceImpl.java ← Chỉ gọi các hàm filterBy, kết hợp bằng Specification.allOf
> List<Specification<Posts>> specs = new ArrayList<>();
> specs.add(PostsSpecification.filterByStatus(request.getStatus()));
> specs.add(PostsSpecification.filterByUserId(request.getUserId()));
> 
> Page<Posts> page = repository.findAll(Specification.allOf(specs), pageable);
> ```

#### Mapper (MapStruct)
```java
@Mapper(componentModel = "spring", uses = {MediaMapper.class, UserMapper.class})
public interface PostsMapper {
    
    @Mapping(target = "user.id", source = "userId")
    @Mapping(target = "poi.id", source = "poiId")
    Posts toPosts(PostsCreateRequest request, Long userId, Long poiId);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updatePostsFromRequest(PostsUpdateRequest request, @MappingTarget Posts posts);
    
    @Mapping(target = "author", source = "posts.user")
    PostResponse toPostResponse(Posts posts, List<Media> medias, Long commentCount);
}
```

**Quy tắc:**
- `@Mapper(componentModel = "spring")`
- Dùng `uses = {...}` để reference mappers khác
- Dùng `@Mapping` để map field khác tên
- Dùng `@BeanMapping(nullValuePropertyMappingStrategy = IGNORE)` cho update

### 3. Response Pattern

#### BaseResponse
```java
@Data
@Builder
public class BaseResponse<T> {
    private long code;
    private String message;
    private T data;
    private boolean success;
}
```

**Usage:**
```java
// Success
return BaseResponse.success(data, ResultCode.SUCCESS, "Message");
return BaseResponse.success(null, ResultCode.CREATED, "Created successfully");

// Error (handled by GlobalExceptionHandler)
throw new ApiException(ResultCode.NOT_FOUND, "Post not found");
```

#### Pagination
```java
// CommonPage - offset-based pagination
CommonPage<PostResponse> response = postsService.searchPosts(searchRequest, pageable);

// CursorResponse - cursor-based pagination
CursorResponse<PostResponse> response = postsService.getAllPosts(request);
```

### 4. Exception Handling

```java
// Throw exception
throw new ApiException(ResultCode.NOT_FOUND, "Resource not found");
throw new ApiException(ResultCode.FORBIDDEN, "Access denied");

// GlobalExceptionHandler sẽ tự động catch và return BaseResponse
```

**Common ResultCodes:**
- `SUCCESS` (200)
- `CREATED` (201)
- `BAD_REQUEST` (400)
- `UNAUTHORIZED` (401)
- `FORBIDDEN` (403)
- `NOT_FOUND` (404)
- `INTERNAL_SERVER_ERROR` (500)

### 5. Security & Authentication

```java
// Get current user ID
Long userId = SecurityUtils.getCurrentUserId();

// Get current user details
UserPrinciple currentUser = (UserPrinciple) SecurityContextHolder
    .getContext()
    .getAuthentication()
    .getPrincipal();
```

### 6. Event-Driven Pattern

```java
// 1. Define Event
public record PostsCreatedEvent(PostsCreatedPayload payload) {}

// 2. Publish Event
eventPublisher.publishEvent(new PostsCreatedEvent(payload));

// 3. Listen to Event
@Component
@RequiredArgsConstructor
public class PostsEventListener {
    @EventListener
    public void handlePostsCreated(PostsCreatedEvent event) {
        // Handle event
    }
}
```

### 7. File Upload Pattern

```java
@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<BaseResponse<Void>> createPosts(
        @ModelAttribute @Valid PostsCreateRequest request) {
    // request.getFiles() -> List<MultipartFile>
    postsService.createPost(request);
    return BaseResponse.success(null, ResultCode.CREATED, "Created");
}
```

---

## 🗄️ Database Conventions

### Table Names
- snake_case, số nhiều - `posts`, `users`, `itinerary_activities`

### Column Names
- snake_case - `user_id`, `created_at`, `post_status`

### Foreign Keys
- `[referenced_table]_id` - `user_id`, `poi_id`

### Timestamps
- Mọi table có `created_at`, `updated_at` (qua `AuditableEntity`)

---

## 📦 Dependencies chính

- **Spring Boot 3.5.7** - Framework
- **Java 21** - Language
- **PostgreSQL** - Database
- **Spring Data JPA** - ORM
- **Spring Security** - Authentication/Authorization
- **JWT (jjwt 0.12.6)** - Token-based auth
- **Lombok** - Boilerplate reduction
- **MapStruct 1.5.5** - Object mapping
- **SpringDoc OpenAPI** - API documentation
- **Google Cloud Storage** - File storage
- **Spring AI + Gemini** - AI integration
- **WebSocket** - Real-time communication

---

## 🔧 Configuration

### application.yaml
- Dùng environment variables: `${DB_URL}`, `${JWT_SECRET}`
- Profile-based config: `spring.profiles.active`

### .env file
- Chứa sensitive data
- Không commit vào git

---

## ✅ Best Practices

### DO ✓
- Luôn validate input với `@Valid`
- Dùng `@Transactional` cho operations có nhiều bước
- Log errors với `log.error()`
- Throw `ApiException` với message rõ ràng
- Dùng `@EntityGraph` để tránh N+1
- Dùng MapStruct cho mapping
- Tách business logic vào Service layer
- Dùng events cho loose coupling
- Return `BaseResponse` từ controller

### DON'T ✗
- Không để business logic trong Controller
- Không dùng `@Autowired` (dùng constructor injection)
- Không catch exception trong Service (để GlobalExceptionHandler xử lý)
- Không hardcode values (dùng config/constants)
- Không expose Entity trực tiếp (dùng DTO)
- Không dùng `SELECT *` trong query
- Không commit sensitive data

---

## 📝 Code Example Template

### Tạo một feature mới

#### 1. Entity
```java
@Entity
@Table(name = "example_entities")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ExampleEntity extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
```

#### 2. DTOs
```java
// Request
@Data
public class ExampleCreateRequest {
    @NotBlank(message = "Name is required")
    private String name;
}

// Response
@Data @SuperBuilder @NoArgsConstructor @AllArgsConstructor
public class ExampleResponse {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
}
```

#### 3. Repository
```java
// Không có dynamic filter → không cần JpaSpecificationExecutor
public interface ExampleRepository extends JpaRepository<ExampleEntity, Long> {
    Optional<ExampleEntity> findByIdAndUserId(Long id, Long userId);
}

// Có dynamic filter (search) → thêm JpaSpecificationExecutor
public interface ExampleRepository extends JpaRepository<ExampleEntity, Long>,
        JpaSpecificationExecutor<ExampleEntity> {
    Optional<ExampleEntity> findByIdAndUserId(Long id, Long userId);
}
```

#### 3a. Specification (chỉ tạo khi có dynamic filter)

Tạo file `repository/specification/ExampleSpecification.java`:

```java
public class ExampleSpecification {

    // Mỗi điều kiện là 1 public static method riêng — null-safe (trả null → bỏ qua predicate đó)
    public static Specification<ExampleEntity> filterByName(String name) {
        return (root, query, cb) -> name == null || name.isBlank()
                ? null
                : cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<ExampleEntity> filterByStatus(ExampleStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<ExampleEntity> filterByCreatedAfter(LocalDateTime from) {
        return (root, query, cb) ->
                from == null ? null : cb.greaterThanOrEqualTo(root.get("createdAt"), from);
    }
}
```

**Quy tắc:**
- Tên class: `[Entity]Specification`
- Vị trí: `repository/specification/[Entity]Specification.java`
- Mỗi filter điều kiện → 1 `public static` method riêng bắt đầu bằng `filterBy...`
- Tất cả method phải **null-safe** (trả `null` thay vì throw exception khi input null)
- **Service** sẽ chịu trách nhiệm kết hợp các filter bằng `Specification.allOf()`:
  ```java
  List<Specification<ExampleEntity>> specs = new ArrayList<>();
  specs.add(ExampleSpecification.filterByName(request.getName()));
  specs.add(ExampleSpecification.filterByStatus(request.getStatus()));
  
  repository.findAll(Specification.allOf(specs), pageable);
  ```

#### 4. Mapper
```java
@Mapper(componentModel = "spring")
public interface ExampleMapper {
    ExampleEntity toEntity(ExampleCreateRequest request);
    ExampleResponse toResponse(ExampleEntity entity);
}
```

#### 5. Service
```java
public interface ExampleService {
    void create(ExampleCreateRequest request);
    ExampleResponse getById(Long id);
}

@Service
@RequiredArgsConstructor
@Slf4j
public class ExampleServiceImpl implements ExampleService {
    private final ExampleRepository repository;
    private final ExampleMapper mapper;
    
    @Override
    public void create(ExampleCreateRequest request) {
        ExampleEntity entity = mapper.toEntity(request);
        entity.setUser(getCurrentUser());
        repository.save(entity);
    }
    
    @Override
    public ExampleResponse getById(Long id) {
        ExampleEntity entity = repository.findById(id)
            .orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND, "Not found"));
        return mapper.toResponse(entity);
    }
}
```

#### 6. Controller
```java
@RestController
@RequestMapping("/api/examples")
@RequiredArgsConstructor
@Tag(name = "Example", description = "Example endpoints")
public class ExampleController {
    private final ExampleService service;
    
    @PostMapping
    public ResponseEntity<BaseResponse<Void>> create(
            @RequestBody @Valid ExampleCreateRequest request) {
        service.create(request);
        return BaseResponse.success(null, ResultCode.CREATED, "Created successfully");
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<ExampleResponse>> getById(@PathVariable Long id) {
        ExampleResponse response = service.getById(id);
        return BaseResponse.success(response, ResultCode.SUCCESS, "Fetched successfully");
    }
}
```

---

## 🎯 Summary

**Kiến trúc:** Layered Architecture + Domain-Driven Design
**Pattern:** Controller → Service → Repository → Entity
**Response:** Luôn dùng `BaseResponse<T>`
**Exception:** Throw `ApiException`, để `GlobalExceptionHandler` xử lý
**Mapping:** Dùng MapStruct
**Validation:** `@Valid` + Bean Validation
**Security:** JWT + Spring Security
**Logging:** Slf4j với Lombok `@Slf4j`
**DI:** Constructor injection với `@RequiredArgsConstructor`

---

## 📚 API Documentation Standards

Project sử dụng **SpringDoc OpenAPI 3** (Swagger) để tự động generate API documentation.

### Swagger Configuration

```java
@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TravelEZ System API")
                        .version("1.0")
                        .description("API documentation with JWT authentication"))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
```

### Controller Documentation

#### 1. Controller Level
```java
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Review", description = "Review endpoints")
public class ReviewController {
    // ...
}
```

**Quy tắc:**
- `@Tag(name = "...", description = "...")` - Nhóm endpoints theo domain
- `name`: Tên ngắn gọn, PascalCase
- `description`: Mô tả chi tiết hơn

#### 2. Endpoint Level

```java
@Operation(
    summary = "Get list review by poi id", 
    description = "Get list review by poi id with optional filters"
)
@ApiBaseResponses
@ApiResponse(responseCode = "200", description = "Reviews fetched successfully")
@GetMapping("/pois/{poiId}/reviews")
public ResponseEntity<BaseResponse<CommonPage<ReviewBaseResponse>>> getReviewByPoiId(
        @PathVariable Long poiId,
        @RequestParam(required = false) Double rating) {
    // ...
}
```

**Annotations:**
- `@Operation` - Mô tả endpoint
  - `summary`: Mô tả ngắn (hiển thị trong list)
  - `description`: Mô tả chi tiết (hiển thị khi expand)
- `@ApiResponse` - Mô tả response cụ thể
  - `responseCode`: HTTP status code
  - `description`: Mô tả response
- `@ApiBaseResponses` - Common error responses (400, 500)
- `@ApiAuthResponses` - Auth error responses (401, 403)
- `@ApiNotFoundResponses` - Not found response (404)

#### 3. Parameter Documentation

```java
@GetMapping
public ResponseEntity<BaseResponse<CommonPage<PostResponse>>> getPosts(
        @ParameterObject PostsSearchRequest searchRequest,
        @PathVariable Long id,
        @RequestParam(required = false, defaultValue = "10") Integer size) {
    // ...
}
```

**Quy tắc:**
- `@ParameterObject` - Dùng cho complex request objects (tự động expand fields)
- `@PathVariable` - Path parameters
- `@RequestParam` - Query parameters (luôn set `required` và `defaultValue`)
- `@RequestBody` - Request body
- `@ModelAttribute` - Form data / multipart

#### 4. Request Body Documentation

```java
@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<BaseResponse<Void>> createReview(
        @PathVariable Long poiId,
        @ModelAttribute @Valid ReviewCreateRequest request) {
    // ...
}
```

### DTO Documentation

#### Request DTO
```java
@Data
public class ReviewCreateRequest {
    
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Double rating;
    
    @NotBlank(message = "Content is required")
    @Size(min = 10, max = 1000, message = "Content must be between 10 and 1000 characters")
    private String content;
    
    private List<MultipartFile> files;
}
```

**Quy tắc:**
- Validation annotations tự động hiển thị trong Swagger
- Message trong validation sẽ hiển thị khi validation fail

#### Response DTO
```java
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewBaseResponse {
    private Long id;
    private Double rating;
    private String content;
    private LocalDateTime createdAt;
    private UserSummaryResponse author;
    private List<MediaBaseResponse> medias;
}
```

### Custom Response Annotations

Project có sẵn các custom annotations để tái sử dụng:

#### @ApiBaseResponses
```java
@ApiBaseResponses  // Tự động thêm 400, 500 responses
@GetMapping
public ResponseEntity<BaseResponse<T>> getResource() {
    // ...
}
```

Equivalent to:
```json
{
  "400": {
    "description": "Bad request",
    "content": {
      "application/json": {
        "schema": { "$ref": "#/components/schemas/BaseResponse" },
        "example": {
          "code": 400,
          "message": "Validation failed",
          "data": null,
          "success": false
        }
      }
    }
  },
  "500": {
    "description": "Internal server error",
    "content": {
      "application/json": {
        "schema": { "$ref": "#/components/schemas/BaseResponse" },
        "example": {
          "code": 500,
          "message": "Internal Server Error",
          "data": null,
          "success": false
        }
      }
    }
  }
}
```

#### @ApiAuthResponses
```java
@PreAuthorize("hasRole('TRAVELER')")
@ApiAuthResponses  // Tự động thêm 401, 403 responses
@PostMapping
public ResponseEntity<BaseResponse<T>> createResource() {
    // ...
}
```

Equivalent to:
```json
{
  "401": {
    "description": "Authentication required",
    "example": {
      "code": 401,
      "message": "Authentication required",
      "data": null,
      "success": false
    }
  },
  "403": {
    "description": "Access denied",
    "example": {
      "code": 403,
      "message": "Access denied",
      "data": null,
      "success": false
    }
  }
}
```

#### @ApiNotFoundResponses
```java
@ApiNotFoundResponses  // Tự động thêm 404 response
@GetMapping("/{id}")
public ResponseEntity<BaseResponse<T>> getById(@PathVariable Long id) {
    // ...
}
```

### Complete Example

```java
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Review", description = "Review management endpoints")
public class ReviewController {
    
    private final ReviewService reviewService;
    
    // GET endpoint with filters
    @Operation(
        summary = "Get reviews by POI",
        description = "Retrieve paginated list of reviews for a specific POI with optional rating filter"
    )
    @ApiBaseResponses
    @ApiResponse(responseCode = "200", description = "Reviews fetched successfully")
    @GetMapping("/pois/{poiId}/reviews")
    public ResponseEntity<BaseResponse<CommonPage<ReviewBaseResponse>>> getReviewByPoiId(
            @PathVariable Long poiId,
            @RequestParam(required = false) Double rating,
            @RequestParam(required = false, defaultValue = "id") String sortField,
            @RequestParam(required = false, defaultValue = "ASC") Sort.Direction sortDirection,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        
        final PaginationRequest request = new PaginationRequest(page, size, sortField, sortDirection);
        ReviewFilterRequest filter = ReviewFilterRequest.builder()
                .poiId(poiId)
                .rating(rating)
                .build();
        
        CommonPage<ReviewBaseResponse> response = reviewService.getReviewByPoiId(
            filter, 
            PaginationUtils.getPageable(request)
        );
        
        return BaseResponse.success(response, ResultCode.SUCCESS, "Reviews fetched successfully");
    }
    
    // POST endpoint with authentication
    @PreAuthorize("hasRole('TRAVELER')")
    @Operation(
        summary = "Create review",
        description = "Create a new review for a POI. Requires TRAVELER role."
    )
    @ApiBaseResponses
    @ApiAuthResponses
    @ApiResponse(responseCode = "201", description = "Review created successfully")
    @PostMapping(
        value = "/pois/{poiId}/reviews",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<BaseResponse<ReviewBaseResponse>> createReview(
            @PathVariable Long poiId,
            @ModelAttribute @Valid ReviewCreateRequest request) {
        
        ReviewBaseResponse response = reviewService.createReview(poiId, request, request.getFiles());
        return BaseResponse.success(response, ResultCode.CREATED, "Review created successfully");
    }
    
    // DELETE endpoint
    @PreAuthorize("hasAnyRole('TRAVELER', 'ADMIN')")
    @Operation(
        summary = "Delete review",
        description = "Delete a review. User can only delete their own reviews. Admin can delete any review."
    )
    @ApiBaseResponses
    @ApiAuthResponses
    @ApiNotFoundResponses
    @ApiResponse(responseCode = "200", description = "Review deleted successfully")
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<BaseResponse<Void>> deleteReview(@PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
        return BaseResponse.success(null, ResultCode.SUCCESS, "Review deleted successfully");
    }
}
```

### Swagger UI Access

- **Local**: `http://localhost:8080/swagger-ui/index.html`
- **API Docs JSON**: `http://localhost:8080/v3/api-docs`

### Best Practices

#### DO ✓
- Luôn thêm `@Tag` ở controller level
- Luôn thêm `@Operation` với `summary` và `description`
- Dùng custom annotations (`@ApiBaseResponses`, `@ApiAuthResponses`, `@ApiNotFoundResponses`)
- Thêm `@ApiResponse` cho success case
- Dùng `@ParameterObject` cho complex request objects
- Set `required` và `defaultValue` cho `@RequestParam`
- Thêm validation annotations trong DTO (tự động hiển thị trong Swagger)
- Mô tả rõ ràng về authentication/authorization requirements

#### DON'T ✗
- Không bỏ qua documentation cho public APIs
- Không dùng generic descriptions như "Get data", "Create resource"
- Không quên document error responses
- Không hardcode examples (dùng actual DTOs)
- Không document internal/private endpoints

### Security Documentation

Endpoints có authentication:
```java
@PreAuthorize("hasRole('TRAVELER')")
@ApiAuthResponses  // Document 401, 403 responses
@PostMapping
public ResponseEntity<BaseResponse<T>> securedEndpoint() {
    // ...
}
```

Swagger UI sẽ tự động thêm "Authorize" button để nhập JWT token.

---

**Lưu ý:** Tài liệu này được tạo dựa trên phân tích codebase hiện tại. Khi có thay đổi lớn về architecture hoặc conventions, cần cập nhật tài liệu này.
