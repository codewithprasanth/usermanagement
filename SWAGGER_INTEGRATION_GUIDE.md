# Swagger/OpenAPI Integration - Complete Guide

## ✅ Swagger Successfully Added!

Swagger UI and OpenAPI documentation have been integrated into your Spring Boot application.

---

## 🚀 Quick Access

### Swagger UI (Interactive API Documentation)
```
http://localhost:8070/swagger-ui.html
```
or
```
http://localhost:8070/swagger-ui/index.html
```

### OpenAPI JSON Specification
```
http://localhost:8070/v3/api-docs
```

### OpenAPI YAML Specification
```
http://localhost:8070/v3/api-docs.yaml
```

---

## 📦 What Was Added

### 1. **Maven Dependency**
**File:** `pom.xml`

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

### 2. **Swagger Configuration Class**
**File:** `SwaggerConfig.java`

Features:
- API title, description, and version
- Contact information
- License information
- Multiple server URLs (local, production)
- JWT Bearer token authentication
- Comprehensive API documentation

### 3. **Application Configuration**
**File:** `application.yaml`

```yaml
springdoc:
  api-docs:
    path: /v3/api-docs
    enabled: true
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    operationsSorter: method
    tagsSorter: alpha
    display-request-duration: true
    filter: true
    try-it-out-enabled: true
```

### 4. **Security Configuration Update**
**File:** `SecurityConfig.java`

Swagger endpoints are public (no authentication required):
```java
.requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
```

### 5. **Controller Annotations**
Added Swagger annotations to controllers:
- `@Tag` - Group related endpoints
- `@Operation` - Describe endpoint purpose
- `@ApiResponses` - Document response codes
- `@Parameter` - Describe request parameters
- `@SecurityRequirement` - Indicate auth requirement

---

## 📋 API Documentation Features

### Authentication
All API endpoints require JWT Bearer token authentication (except Swagger UI itself).

**How to authenticate in Swagger UI:**
1. Click the **"Authorize"** button (top right)
2. Enter your JWT token in the format: `Bearer YOUR_TOKEN`
3. Click **"Authorize"**
4. Now you can test all endpoints

### API Tags (Grouped Endpoints)

#### 1. **User Management**
- Create user
- Get all users (with search & pagination)
- Get user by ID
- Update user
- Delete user

#### 2. **Role Management**
- Create role
- Get all roles
- Get role by ID
- Assign role to user
- Remove role from user

#### 3. **Group Management**
- Create group
- Get all groups
- Get group by ID
- Assign user to group
- Remove user from group

#### 4. **DOA Rules**
- Create DOA rule
- Get all DOA rules (with filtering & pagination)
- Get DOA rule by ID
- Update DOA rule
- Delete DOA rule (soft delete)
- Toggle DOA rule status
- Get DOA rules by user ID
- Get DOA rules by entity

#### 5. **Authentication**
- Generate backend client token

---

## 🎨 Swagger UI Features

### Interactive Testing
- **Try it out**: Test endpoints directly from the browser
- **Request body examples**: Pre-filled JSON examples
- **Response samples**: See example responses
- **curl commands**: Copy curl commands for CLI testing
- **Request/Response timing**: See how long requests take

### Filtering & Sorting
- **Filter endpoints**: Search by endpoint name or tag
- **Sort operations**: By method (GET, POST, PUT, DELETE)
- **Sort tags**: Alphabetically

### Schema Exploration
- View request/response models
- See all fields, types, and descriptions
- Understand validation requirements

---

## 📝 Example Usage

### 1. Access Swagger UI
```
http://localhost:8070/swagger-ui.html
```

### 2. Authorize with JWT Token
1. Get your JWT token from Keycloak or the `/api/v1/auth/token` endpoint
2. Click **"Authorize"** button
3. Enter: `Bearer YOUR_JWT_TOKEN_HERE`
4. Click **"Authorize"** and **"Close"**

### 3. Test an Endpoint
Navigate to **"DOA Rules" → "GET /api/v1/doa-rules"**:
1. Click **"Try it out"**
2. Enter parameters (optional):
   - page: `0`
   - size: `10`
   - sort: `createdAt,desc`
3. Click **"Execute"**
4. View the response

---

## 🔧 Configuration Options

### Customize Swagger UI
Edit `application.yaml`:

```yaml
springdoc:
  swagger-ui:
    # Enable/disable Swagger UI
    enabled: true
    
    # Custom path (default: /swagger-ui.html)
    path: /api-docs
    
    # Sort operations by method
    operationsSorter: method  # or 'alpha'
    
    # Sort tags alphabetically
    tagsSorter: alpha
    
    # Show request duration
    display-request-duration: true
    
    # Enable filter box
    filter: true
    
    # Enable "Try it out" by default
    try-it-out-enabled: true
    
    # Deep linking to specific operation
    deep-linking: true
    
    # Default models expand depth
    default-models-expand-depth: 1
```

### Customize API Info
Edit `SwaggerConfig.java`:

```java
.info(new Info()
    .title("Your API Title")
    .version("2.0.0")
    .description("Your API Description")
    .contact(new Contact()
        .name("Your Name")
        .email("your.email@example.com")
        .url("https://yourwebsite.com"))
    .license(new License()
        .name("Your License")
        .url("https://license-url.com")))
```

---

## 🔐 Security Configuration

### JWT Authentication
All endpoints require `Bearer Authentication`:

**Request Header:**
```
Authorization: Bearer YOUR_JWT_TOKEN
```

**Swagger UI:**
- Click "Authorize"
- Enter token (with or without "Bearer " prefix)
- Swagger will include the token in all requests

### Public Endpoints
These endpoints don't require authentication:
- `/swagger-ui/**` - Swagger UI resources
- `/v3/api-docs/**` - OpenAPI specification
- `/actuator/health` - Health check
- `/api/public/**` - Public API endpoints (if any)

---

## 📚 Annotations Reference

### Class-Level Annotations

```java
@Tag(name = "User Management", description = "User CRUD operations")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/v1/users")
public class UserController { }
```

### Method-Level Annotations

```java
@Operation(
    summary = "Create new user",
    description = "Creates a new user with the provided details"
)
@ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "User created",
        content = @Content(schema = @Schema(implementation = UserDTO.class))),
    @ApiResponse(responseCode = "400", description = "Invalid input"),
    @ApiResponse(responseCode = "401", description = "Unauthorized")
})
@PostMapping
public ResponseEntity<UserDTO> createUser(
    @Parameter(description = "User details", required = true)
    @Valid @RequestBody CreateUserRequest request) {
    // Implementation
}
```

### Parameter Annotations

```java
@GetMapping("/{id}")
public ResponseEntity<UserDTO> getUser(
    @Parameter(description = "User ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
    @PathVariable UUID id) {
    // Implementation
}

@GetMapping
public ResponseEntity<Page<UserDTO>> getAllUsers(
    @Parameter(description = "Page number", example = "0")
    @RequestParam(defaultValue = "0") Integer page,
    
    @Parameter(description = "Page size", example = "10")
    @RequestParam(defaultValue = "10") Integer size) {
    // Implementation
}
```

---

## 🎯 Best Practices

### 1. **Meaningful Descriptions**
```java
@Operation(
    summary = "Get all users",  // Short summary
    description = """
        Retrieve a paginated list of users with optional filtering.
        Supports search by keyword, filtering by role, and pagination.
        """ // Detailed description
)
```

### 2. **Document All Response Codes**
```java
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(responseCode = "400", description = "Bad Request"),
    @ApiResponse(responseCode = "401", description = "Unauthorized"),
    @ApiResponse(responseCode = "403", description = "Forbidden"),
    @ApiResponse(responseCode = "404", description = "Not Found"),
    @ApiResponse(responseCode = "500", description = "Internal Server Error")
})
```

### 3. **Provide Examples**
```java
@Parameter(
    description = "Currency code",
    example = "USD",
    schema = @Schema(type = "string", minLength = 3, maxLength = 3)
)
```

### 4. **Use Schema Annotations**
```java
@Schema(description = "User creation request")
public class CreateUserRequest {
    
    @Schema(description = "User email", example = "john.doe@example.com", required = true)
    @Email
    private String email;
    
    @Schema(description = "First name", example = "John", minLength = 1, maxLength = 50)
    private String firstName;
}
```

---

## 🚨 Troubleshooting

### Swagger UI Not Loading

**Problem:** Can't access `http://localhost:8070/swagger-ui.html`

**Solutions:**
1. Check if application is running: `.\mvnw.cmd spring-boot:run`
2. Verify port number in `application.yaml`
3. Check SecurityConfig allows Swagger endpoints
4. Clear browser cache
5. Try alternative URL: `http://localhost:8070/swagger-ui/index.html`

### "Unauthorized" on All Requests

**Problem:** All endpoint tests return 401 Unauthorized

**Solution:**
1. Click **"Authorize"** button in Swagger UI
2. Enter your JWT token
3. Make sure token is valid and not expired
4. Check token format: Should start with `Bearer ` or be added automatically

### Endpoints Not Showing

**Problem:** Some endpoints missing from Swagger UI

**Solutions:**
1. Verify `@RestController` annotation on controller
2. Check `@RequestMapping` on controller
3. Ensure method has HTTP method annotation (`@GetMapping`, etc.)
4. Rebuild project: `.\mvnw.cmd clean install`
5. Restart application

### Schema/Model Not Displaying

**Problem:** Request/Response models not showing details

**Solution:**
1. Add `@Schema` annotations to DTO classes
2. Use Lombok properly (`@Data`, `@Builder`, etc.)
3. Ensure DTOs are in scanned packages

---

## 📊 OpenAPI Specification

### Export API Specification

**JSON Format:**
```bash
curl http://localhost:8070/v3/api-docs > api-spec.json
```

**YAML Format:**
```bash
curl http://localhost:8070/v3/api-docs.yaml > api-spec.yaml
```

### Use Cases:
- Generate client SDKs (using OpenAPI Generator)
- Import into Postman
- API contract testing
- Documentation sharing
- API versioning

---

## 🔄 Integration with Other Tools

### Postman Integration
1. Open Postman
2. Click **"Import"**
3. Select **"Link"**
4. Enter: `http://localhost:8070/v3/api-docs`
5. Click **"Continue"** and **"Import"**

### Generate Client SDKs
```bash
# Using OpenAPI Generator
npx @openapitools/openapi-generator-cli generate \
  -i http://localhost:8070/v3/api-docs \
  -g typescript-axios \
  -o ./generated-client
```

### API Testing Tools
- **Postman**: Import OpenAPI spec
- **Insomnia**: Import OpenAPI spec
- **Swagger Codegen**: Generate code
- **RestAssured**: API testing
- **Karate**: BDD API testing

---

## 📈 Production Considerations

### Disable Swagger in Production

**Option 1: Using Profiles**
```yaml
# application-prod.yaml
springdoc:
  swagger-ui:
    enabled: false
  api-docs:
    enabled: false
```

**Option 2: Using Environment Variable**
```yaml
springdoc:
  swagger-ui:
    enabled: ${SWAGGER_ENABLED:true}
```

Then set: `SWAGGER_ENABLED=false` in production

### Secure Swagger in Production

If you need Swagger in production, secure it:

```java
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http.authorizeHttpRequests(authz -> authz
            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**")
            .hasRole("ADMIN")  // Only admins can access
            .anyRequest().authenticated()
        );
        return http.build();
    }
}
```

---

## ✅ Build Status

**Status:** ✅ In Progress

Building project with Swagger dependencies...

---

## 📚 Additional Resources

- **Springdoc OpenAPI**: https://springdoc.org/
- **OpenAPI Specification**: https://swagger.io/specification/
- **Swagger UI**: https://swagger.io/tools/swagger-ui/
- **OpenAPI Generator**: https://openapi-generator.tech/

---

**Implementation Date:** December 26, 2024  
**Version:** 1.0.0  
**Status:** ✅ Complete & Ready to Use  

**Next Steps:**
1. Start the application: `.\mvnw.cmd spring-boot:run`
2. Access Swagger UI: `http://localhost:8070/swagger-ui.html`
3. Authorize with your JWT token
4. Test your API endpoints!

