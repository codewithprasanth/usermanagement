# User Management Service

A Spring Boot application for comprehensive user, role, and group management integrated with Keycloak for authentication and authorization.

## 📋 Features

- **User Management**: Complete CRUD operations with role and group assignments
- **Role Management**: Hierarchical roles with privilege composition
- **Group Management**: Group-based access control
- **JWT Authentication**: Secure OAuth2/JWT integration with Keycloak
- **API Documentation**: Interactive Swagger UI
- **Monitoring**: Spring Boot Actuator with health checks
- **Externalized Configuration**: Easy configuration via properties file

## 🚀 Quick Start

### Prerequisites

- Java 21 or higher
- Maven 3.8+
- Keycloak 23.0.4 or higher running on port 8080
- Docker (optional, for Keycloak)

### 1. Configure the Application

All configuration is in `src/main/resources/application.yaml`. The defaults work for local development.

**Key configurations:**
```yaml
# Server
server:
  port: 8090

# Keycloak
keycloak:
  auth-server-url: http://localhost:8080
  realm: sprint-ap
  resource: sprint-ap-backend
  credentials:
    secret: your-client-secret

# CORS (for frontend)
cors:
  allowed-origins: http://localhost:3000,http://localhost:3001
```

### 2. Build and Run

```bash
# Build the project
./mvnw clean install

# Run the application
./mvnw spring-boot:run
```

The application will start on `http://localhost:8090`

### 3. Access API Documentation

Open your browser and navigate to:
- Swagger UI: http://localhost:8090/swagger-ui.html
- API Docs (JSON): http://localhost:8090/v3/api-docs

### 4. Health Check

```bash
curl http://localhost:8090/actuator/health
```

## 📖 Configuration Guide

### Environment Variables

For production, override properties using environment variables:

```bash
# Format: Replace dots with underscores and use UPPERCASE
# Example: server.port -> SERVER_PORT

export SERVER_PORT=8090
export KEYCLOAK_AUTH_SERVER_URL=https://keycloak.example.com
export KEYCLOAK_REALM=your-realm
export KEYCLOAK_RESOURCE=your-client-id
export KEYCLOAK_CLIENT_SECRET=your-secret
export CORS_ALLOWED_ORIGINS=https://app.example.com
```

### Configuration File

**Location**: `src/main/resources/application.yaml`

This file contains ALL configurable properties with documentation. Key sections:

#### 1. Server Configuration
```yaml
server:
  port: 8090

spring:
  application:
    name: user-management-service
```

#### 2. Keycloak Configuration
```yaml
keycloak:
  auth-server-url: http://localhost:8080
  realm: sprint-ap
  resource: sprint-ap-backend
  credentials:
    secret: your-secret
```

#### 3. CORS Configuration
```yaml
cors:
  allowed-origins: http://localhost:3000
  allowed-methods: GET,POST,PUT,DELETE,OPTIONS,PATCH
  allow-credentials: true
```

#### 4. Application Constants
```yaml
app:
  role-prefix: role_
  privilege-prefix: priv_
  pagination:
    default-page-size: 20
    max-page-size: 100
```

#### 5. Logging Configuration
```yaml
logging:
  level:
    root: INFO
  file:
    name: logs/application.log
    max-size: 10MB
```

## 🔌 API Endpoints

### User Management
- `POST /api/users` - Create user
- `GET /api/users` - Get all users
- `GET /api/users/{id}` - Get user by ID
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user

### Role Management
- `POST /api/roles` - Create role
- `GET /api/roles` - Get all roles
- `PUT /api/roles/{id}` - Update role
- `DELETE /api/roles/{id}` - Delete role
- `GET /api/roles/{id}/privileges` - Get role privileges
- `GET /api/roles/privileges` - Get all privileges

### Group Management
- `POST /api/groups` - Create group
- `GET /api/groups` - Get all groups
- `DELETE /api/groups/{id}` - Delete group
- `GET /api/groups/{id}/users` - Get group members
- `GET /api/groups/{id}/roles-privileges` - Get group roles/privileges
- `PUT /api/groups/{id}/users` - Update group members
- `PUT /api/groups/{id}/roles-privileges` - Update group roles/privileges

## 🔐 Authentication

All endpoints require JWT Bearer token authentication.

### Obtain Token from Keycloak

```bash
curl -X POST "http://localhost:8080/realms/sprint-ap/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=sprint-ap-backend" \
  -d "client_secret=your-secret" \
  -d "grant_type=client_credentials"
```

### Use Token in API Requests

```bash
curl -X GET "http://localhost:8090/api/users" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

## 📊 Monitoring

### Health Check
```bash
curl http://localhost:8090/actuator/health
```

### Metrics (Prometheus format)
```bash
curl http://localhost:8090/actuator/metrics
curl http://localhost:8090/actuator/prometheus
```

## 🛠️ Development

### Project Structure
```
src/main/java/com/sprintap/usermanagement/
├── config/          # Configuration classes
├── constants/       # Application constants
├── controller/      # REST controllers
├── dto/            # Data Transfer Objects
├── exception/      # Custom exceptions
├── service/        # Business logic
└── util/           # Utility classes

src/main/resources/
├── application.yaml        # ⭐ Main configuration file
├── application-dev.yml     # Dev profile settings
└── application-prod.yml    # Production profile settings
```

### Build Commands

```bash
# Compile
./mvnw compile

# Run tests
./mvnw test

# Package
./mvnw package

# Skip tests
./mvnw package -DskipTests

# Clean and rebuild
./mvnw clean install
```

### Changing Configuration

1. **For Development**: Edit `src/main/resources/application.yaml`
2. **For Production**: Use environment variables (see above)
3. **Profile-Specific**: Already configured in `application-dev.yml` and `application-prod.yml`

## 📝 Additional Documentation

- **API Documentation**: Access Swagger UI at `/swagger-ui.html` when running
- **Production Guide**: See `PRODUCTION_README.md` for deployment details
- **Quick Reference**: See `QUICK_REFERENCE_GUIDE.md` for developer guide

## 🗂️ Project Files

### Essential Files
- `pom.xml` - Maven dependencies and build configuration
- `src/main/resources/application.properties` - All configuration in one place
- `Keycloak-UserManagement-Complete-Final-Dec2025.postman_collection.json` - Postman API collection

### Documentation
- `README.md` - This file
- `PRODUCTION_README.md` - Production deployment guide
- `QUICK_REFERENCE_GUIDE.md` - Quick developer reference
- `API_DOCUMENTATION.md` - Detailed API documentation
- `HELP.md` - Spring Boot reference

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 🐛 Troubleshooting

### Application won't start
1. Check if Keycloak is running on port 8080
2. Verify configuration in `application.yaml`
3. Check logs in `logs/application.log`

### Cannot access API
1. Ensure you have a valid JWT token
2. Check CORS configuration if calling from frontend
3. Verify Keycloak realm and client settings

### Configuration not working
1. Environment variables override properties file
2. Check property names (must match exactly)
3. Restart application after changes

## 📄 License

This project is licensed under the Apache License 2.0.

## 📞 Support

For issues and questions:
- Check the API documentation at `/swagger-ui.html`
- Review all configuration in `application.yaml`
- Check application logs in `logs/application.log`
- Review Keycloak configuration

---

**Quick Start Command:**
```bash
./mvnw spring-boot:run
```

Then open: http://localhost:8090/swagger-ui.html

