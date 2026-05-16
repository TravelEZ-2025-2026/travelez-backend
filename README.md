# TravelEz Backend

Backend API cho nền tảng du lịch xã hội TravelEz - Kết nối người dùng với trải nghiệm du lịch thông qua AI và cộng đồng.

## 📚 Documentation

- **[AI Quick Start](docs/AI_QUICK_START.md)** - ⚡ Lệnh nhanh để bắt đầu task/feature mới
- **[System Overview](docs/SYSTEM_OVERVIEW.md)** - Tổng quan hệ thống, kiến trúc, tech stack
- **[Domain Details](docs/DOMAIN_DETAILS.md)** - Chi tiết các domain và chức năng
- **[API Reference](docs/API_REFERENCE.md)** - Danh sách API endpoints
- **[Development Guide](docs/DEVELOPMENT_GUIDE.md)** - Hướng dẫn phát triển
- **[Code Conventions](docs/CODE_CONVENTIONS.md)** - Quy ước code và best practices
- **[Working Guidelines](docs/WORKING_GUIDELINES.md)** - Quy trình làm việc với AI

## 🚀 Quick Start

### Prerequisites
- Java 21+
- Maven 3.8+
- PostgreSQL 14+ with pgvector
- Google Cloud Storage account
- Gemini API

### Setup

1. Clone repository
```bash
git clone <repository-url>
cd backend
```

2. Create `.env` file (see [Development Guide](docs/DEVELOPMENT_GUIDE.md))

3. Build and run
```bash
mvn clean install
mvn spring-boot:run
```

4. Access Swagger UI: `http://localhost:8080/swagger-ui/index.html`

## 🏗️ Tech Stack

- **Spring Boot 3.5.7** + Java 21
- **PostgreSQL** + pgvector
- **Spring Security** + JWT
- **Google Gemini AI**
- **Google Cloud Storage**
- **WebSocket** (Real-time chat)
- **MapStruct** + Lombok

## 📦 Key Features

- 🔐 Authentication (JWT + Google OAuth2)
- 📍 POI Management with semantic search
- 📝 Social posts with media
- 🗓️ AI-powered itinerary planning
- 💬 Real-time chat
- ⭐ Reviews & ratings
- 👥 Social features (follow, reactions, comments)
- 🚨 Content moderation
- 🔔 Real-time notifications

## 📖 For Developers

**Before coding, always:**
1. Read [Working Guidelines](docs/WORKING_GUIDELINES.md)
2. Follow [Code Conventions](docs/CODE_CONVENTIONS.md)
3. Create plan file before implementation
4. Update progress tracking

## 📄 License

[Add license information]

## 👥 Team

[Add team information]
