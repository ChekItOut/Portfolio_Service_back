# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Portfolio Archive Backend is a Spring Boot REST API for managing user portfolios with image galleries, descriptions, and skills. It features OAuth2 Google authentication with JWT token support and AWS S3 integration for image storage.

**Tech Stack:**
- Java 17 with Spring Boot 4.0.2
- Gradle build system
- MySQL database (Hibernate/Spring Data JPA)
- OAuth2 (Google) + JWT authentication
- AWS S3 for image storage
- Lombok for boilerplate reduction

## Build & Development Commands

```bash
# Build the project
./gradlew build

# Run the application (requires MySQL running on localhost:3306)
./gradlew bootRun

# Run tests
./gradlew test

# Run a single test
./gradlew test --tests ClassName

# Clean build
./gradlew clean build

# Run with debug output
./gradlew bootRun --debug
```

## Project Structure

```
src/main/java/pard/server/com/portfolioarchiveback/
├── config/                    # Security, OAuth2, JWT configuration
│   ├── WebOAuthSecurityConfig.java  # Main security configuration
│   ├── jwt/                   # JWT token provider and token refresh logic
│   ├── oauth/                 # OAuth2 handlers and custom user service
│   └── TokenAuthenticationFilter.java # Token validation filter
├── portfolio/                 # Portfolio domain (main feature)
│   ├── Portfolio.java         # JPA entity
│   ├── PortfolioDTO.java      # Data transfer objects (Req1, Req2, Res1, Res2)
│   ├── PortfolioService.java  # Business logic
│   ├── PortfolioRepository.java # Data access
│   └── PortfolioController.java # REST endpoints (/portfolios/*)
├── image/                     # Image management with S3 integration
├── description/               # Portfolio descriptions
├── skill/                      # Portfolio skills
├── user/                       # User management and authorization
├── s3/                        # AWS S3 service configuration
├── util/                      # Utilities (CookieUtil, etc.)
└── baseEntity/                # Base JPA entity with timestamps
```

## Architecture Patterns

### Authentication Flow
1. **OAuth2 + JWT Hybrid Approach:**
   - Initial login: OAuth2 Google authorization → `OAuth2SuccessHandler` creates JWT tokens
   - AccessToken: Sent in `Authorization: Bearer` header for API requests
   - RefreshToken: Stored in HTTP-only cookie + database for token rotation
   - Subsequent requests: `TokenAuthenticationFilter` validates JWT from header

2. **Authorization Pattern:**
   - Uses `AuthorizeUserId.getAuthorizedUserId()` throughout codebase to get current user
   - Securely extracts user ID from JWT principal in `TokenAuthenticationFilter`

3. **Service/Repository Pattern:**
   - Controllers → Services (business logic) → Repositories (data access)
   - DTO pattern for request/response objects (no direct entity exposure)
   - Service classes handle cross-cutting concerns (image deletion, etc.)

### API Endpoints

**Portfolio Management:**
- `GET /portfolios/hero` - Get all portfolios with titles for hero section
- `GET /portfolios/detail/{portfolioId}` - Get portfolio detail page with descriptions/skills/images
- `POST /portfolios/posts` - Create portfolio (multipart: request + image files)
- `PATCH /portfolios/text/{portfolioId}` - Update portfolio text fields only
- `PATCH /portfolios/imageReorder/{portfolioId}` - Replace all portfolio images
- `POST /portfolios/imageAdd/{portfolioId}` - Add new images to portfolio

**Authentication:**
- `POST /api/token` - Create new access token from refresh token (public endpoint)
- OAuth2 endpoints handled by Spring Security

## Database Configuration

**Local Development:**
- MySQL on `localhost:3306`
- Database: `portfolio_backend`
- Default credentials: root/5991 (configured in `application.yaml`)
- Hibernate `ddl-auto: update` - schema auto-creates tables on startup

**Entity Relationships:**
- `Portfolio` (user-owned with title)
  - `1-to-many` Description
  - `1-to-many` Skill
  - `1-to-many` Image
- `User` (OAuth2 user from Google)

## Key Configuration

**CORS Configuration:**
- Frontend: `http://localhost:3000` (Next.js frontend)
- Methods: GET, POST, PUT, PATCH, DELETE
- Credentials enabled for cookie-based refresh token

**Security Configuration:**
- Session-less (STATELESS) - all state in JWT/cookies
- CSRF disabled (token-based auth)
- OAuth2 requests and `/api/token` are public endpoints
- All other endpoints require authentication

**S3 Configuration:**
- Bucket: `portfolio-archive-server`
- Region: `ap-northeast-2`
- Images stored with multipart file upload support

## Development Notes

1. **JWT Secret:** Keep `jwt.secret_key` in `application.yaml` secure - never commit to public repos
2. **Multipart Upload:** Max file size 50MB per configuration
3. **Lazy Loading:** `enable_lazy_load_no_trans: true` allows lazy loading outside transactions
4. **Image Service:** Handles S3 upload/delete via `AwsS3Service`
5. **Sensitive Data:** Google OAuth2 credentials and AWS keys in `application.yaml` - use environment variables in production
