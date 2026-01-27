# AGENTS.md

## Overview

This is a Spring Boot 3.5.9 project using Java 17 with a domain-driven architecture pattern. The project name "mwo-do-shil" (무도실) appears to be related to restaurant/bar recommendations.

## Build/Test Commands

### Build Commands
```bash
# Build the entire project
./gradlew build

# Clean build
./gradlew clean build

# Run the application
./gradlew bootRun

# Build without tests
./gradlew build -x test
```

### Test Commands
```bash
# Run all tests
./gradlew test

# Run a single test class
./gradlew test --tests "com.example.mwo_do_shil.MwoDoShilApplicationTests"

# Run tests with specific pattern
./gradlew test --tests "*Store*"

# Run tests with verbose output
./gradlew test --info
```

## Code Style Guidelines

### Package Structure
- **Base Package**: `com.example.mwo_do_shil`
- **Domain Organization**: Use domain-centric structure under `domain/` package
- **Configuration**: Place configuration classes in `config/` package
- **Common Utilities**: Use `domain/common/` for shared classes

### Naming Conventions
- **Classes**: PascalCase (e.g., `StoreController`, `RecommendService`)
- **Packages**: lowercase with underscores if needed (due to project name)
- **Variables**: camelCase
- **DTOs**: End with `Dto` suffix (e.g., `RecommendResponseDto`)
- **Entities**: Singular form (e.g., `Store`, `Alcohol`)

### Import Organization
1. `jakarta.*` imports first
2. `org.springframework.*` imports
3. `com.example.mwo_do_shil.*` imports
4. `lombok.*` imports last

### Lombok Usage Patterns
```java
// Entities
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Store {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}

// Services
@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendService {
    private final SomeRepository repository;
}

// DTOs
@Getter
@Builder
@NoArgsConstructor
public class RecommendResponseDto {
    private String field;
}
```

### Architecture Patterns
- **Layered Architecture**: Controller → Service → Repository → Entity
- **Constructor Injection**: Use `@RequiredArgsConstructor` for dependency injection
- **Response Wrapper**: Use `Result<T>` for consistent API responses
- **DTO Pattern**: Separate entities from DTOs, use Builder pattern

### API Design Guidelines
- Use `Result<T>` wrapper for all API responses
- Controllers should be thin, delegate business logic to services
- Use `@RequestMapping` for base paths, specific mappings for endpoints
- Return `ResponseEntity<Result>` for proper HTTP status control

### Database Guidelines
- JPA entities with `@Entity` annotation
- Use `GenerationType.IDENTITY` for primary keys
- Protected no-args constructors for entities
- Repository pattern for data access

### Error Handling
- Use `@Slf4j` for logging
- Wrap external API calls in try-catch blocks
- Log errors with appropriate context
- Return meaningful error messages in `Result<T>`

### External API Integration
- Use WebClient for HTTP calls (configured as bean)
- Store API keys in environment variables with `@Value`
- Create specific DTOs for external API responses
- Handle API failures gracefully with logging

### Configuration Management
- Environment variables in `application.properties` using `${VAR:default}` syntax
- Sensitive data (API keys) should use environment variables
- Configuration classes annotated with `@Configuration`

## Development Workflow

### Adding New Features
1. Create domain package if needed
2. Add Entity class with proper JPA annotations
3. Create Repository interface
4. Implement Service class with business logic
5. Create Controller with proper mapping
6. Add DTOs for request/response
7. Wrap responses in `Result<T>`

### Testing Guidelines
- Use JUnit 5 (Jupiter)
- Place tests in `src/test/java/com/example/mwo_do_shil/`
- Follow same package structure as main code
- Use `@SpringBootTest` for integration tests
- Test service layer business logic separately

## Important Notes

- **Package Typo**: The `recommand` package contains a typo - should be `recommend` (keep existing for backward compatibility)
- **Java Version**: Strictly Java 17 with toolchain configuration
- **Build System**: Gradle with Spring Boot plugin
- **Database**: PostgreSQL
- **Reactive**: WebFlux included for WebClient usage

## Environment Setup

### Required Environment Variables
```bash
KAKAO_REST_API_KEY=your_kakao_rest_api_key
```

### IDE Configuration
- IntelliJ IDEA recommended (.idea/ directory included in git)
- Lombok plugin required for proper IDE support
- Ensure annotation processing is enabled

## Code Quality

Currently no formal linting tools configured. Recommended additions:
- SpotBugs for static analysis
- Checkstyle for code formatting consistency
- Consider adding SonarQube for quality gates

## Security Considerations

- Never commit API keys or sensitive data
- Use environment variables for all external service credentials
- Implement proper input validation in controllers
- Consider Spring Security for authentication/authorization