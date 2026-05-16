# Archive Directory

Thư mục này chứa tài liệu của các features/tasks đã hoàn thành.

## Cấu trúc

Mỗi module có thư mục riêng, mỗi feature/task là 1 file `.md`:

```
archive/
├── users/
│   ├── google-oauth-integration.md
│   ├── profile-enhancement.md
│   └── avatar-upload.md
├── poi/
│   ├── semantic-search.md
│   └── location-based-search.md
├── posts/
│   ├── media-upload.md
│   └── post-moderation.md
├── itinerary/
│   ├── ai-generation.md
│   └── shared-itinerary.md
├── chat/
│   ├── real-time-messaging.md
│   └── notification-fix.md
└── [other-modules]/
    └── [feature-name].md
```

## Quy tắc đặt tên

- **Thư mục module:** Tên module (lowercase): `users`, `poi`, `posts`, `itinerary`, `chat`
- **File feature:** kebab-case: `google-oauth-integration.md`, `semantic-search.md`

## Nội dung Archive Document

Mỗi file archive bao gồm:

1. **Tóm tắt** - Mô tả feature/task
2. **Chức năng** - Use cases, business logic
3. **Implementation Details** - Files created/modified, database changes
4. **API Documentation** - Endpoints, request/response, cách sử dụng
5. **Notes** - Lưu ý, known issues, future improvements
6. **Testing** - Test cases, how to test
7. **Deployment Notes** - Lưu ý khi deploy

Xem template chi tiết trong `docs/WORKING_GUIDELINES.md`

## Mục đích

- 📚 Tài liệu tham khảo cho developers
- 🔍 Dễ dàng tìm kiếm và hiểu features đã có
- 📖 Hướng dẫn sử dụng API
- 🧠 Knowledge base cho AI và team members
- 🚀 Onboarding cho developers mới
