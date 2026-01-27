# Setup Guide

## Environment Configuration

This application uses different configuration files for local development and production environments.

### Local Development

1. **Set up environment variables:**
   ```bash
   export KAKAO_API_KEY=your_kakao_rest_api_key
   export GOOGLE_API_KEY=your_google_api_key
   ```

2. **Or run with environment variables:**
   ```bash
   KAKAO_API_KEY=your_key GOOGLE_API_KEY=your_key ./gradlew bootRun
   ```

### Configuration Files

- `application.yml` - Local development (uses H2 database)
- `application-prod.yml` - Production (uses PostgreSQL)
- `application-template.yml` - Template for developers

### Running in Production (Render)

Set these environment variables in your Render deployment:
- `KAKAO_API_KEY` - Kakao REST API Key
- `GOOGLE_API_KEY` - Google AI API Key
- `DATABASE_URL` - PostgreSQL connection URL
- `DB_USERNAME` - Database username
- `DB_PASSWORD` - Database password
- `SPRING_PROFILES_ACTIVE=prod` - Activate production profile

## Recent Changes

### WebClient Migration
- Migrated from RestTemplate to WebClient for HTTP calls
- Removed RestTemplateConfig
- Updated KakaoApiService to use reactive WebClient
- Added WebFlux dependency to build.gradle

### Security Improvements
- Configuration files are properly gitignored
- API keys use environment variables
- Production profile separate from development

### Database
- Local: H2 in-memory database
- Production: PostgreSQL