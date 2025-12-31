# User Management API - Complete Frontend Integration Guide

## 📋 Table of Contents
- [Overview](#overview)
- [Base Configuration](#base-configuration)
- [Authentication](#authentication)
- [API Endpoints](#api-endpoints)
  - [Auth APIs](#auth-apis)
  - [User Management APIs](#user-management-apis)
  - [Role Management APIs](#role-management-apis)
  - [Group Management APIs](#group-management-apis)
- [Data Models](#data-models)
- [Error Handling](#error-handling)
- [Frontend Integration Guide](#frontend-integration-guide)

---

## 🔧 Overview

This API provides comprehensive user, role, and group management capabilities integrated with Keycloak. All endpoints use JWT-based authentication and follow RESTful conventions.

**Technology Stack:**
- Spring Boot 3.x
- Keycloak Integration
- OAuth2/JWT Authentication
- RESTful API Design

**Last Updated:** December 11, 2025  
**API Version:** 1.1  
**Latest Fix:** Role deletion now correctly accepts roleId (UUID) instead of roleName

---

## 🚨 CRITICAL FIX - December 11, 2025

### Role Deletion Issue RESOLVED ✅

**Problem:** The `DELETE /api/roles/{roleId}` endpoint was receiving a `roleId` (UUID) as a path parameter but the backend service was incorrectly treating it as a `roleName`, causing **"role not found"** errors when valid UUIDs were passed.

**Root Cause:** The `RoleService.deleteRole()` method parameter was named `roleName` instead of `roleId`, and it was directly using the value as a role name without converting the UUID to the actual role name first.

**Solution:** Updated the `RoleService.deleteRole()` method to:
1. Accept `roleId` (UUID) as parameter
2. First resolve the UUID to the actual role name using `getRoleById()`
3. Then proceed with deletion using the resolved role name

**Impact:** 
- ✅ `DELETE /api/roles/{roleId}` - Now works correctly with UUID
- ✅ Consistent with all other role endpoints that use roleId (UUID)
- ✅ Frontend can now use the role UUID directly from the role list
- ✅ No more "role not found" errors when passing valid role UUIDs

**Correct Usage:**
```http
DELETE /api/roles/650e8400-e29b-41d4-a716-446655440001
Authorization: Bearer <token>

Response: 200 OK
{
  "message": "Role deleted successfully",
  "timestamp": "2025-12-11T19:45:00.123"
}
```

---

## 🎯 Critical API Format Notes

### ⚠️ Important Changes & Clarifications

#### 1. **All IDs are UUIDs, NOT Names**
- Use `roleId` (UUID) not `roleName` in API paths
- Use `groupId` (UUID) not `groupName` in request bodies
- Use `userId` (UUID) not `username` in API paths
- Example: `DELETE /api/roles/{roleId}` where roleId = `"650e8400-e29b-41d4-a716-446655440001"`

#### 2. **Username is Automatically Generated**
- Username is automatically set to the email address
- Do NOT send a `username` field in create/update user requests
- The backend uses email as the username

#### 3. **GroupDTO Structure with userCount**
- GroupDTO has `id`, `name`, and `userCount` fields
- **`userCount`** shows the number of users in each group (Integer)
- There is **NO** `path` field in the response
- `userCount` is automatically calculated when fetching groups
- `userCount` shows the number of users in each group
- Frontend should not expect or use a `path` field

#### 4. **Role and Privilege Display Names**
- `displayName` fields have the prefix removed (lowercase)
- Example: `role_admin` → displayName: `"admin"`
- Example: `priv_user_management` → displayName: `"user_management"`

#### 5. **All Success Responses Include Timestamps**
- Create/Update/Delete operations return timestamps in ISO format
- Example: `"timestamp": "2025-12-08T10:30:00.123"`

#### 6. **Validation Happens Before Creation**
- All IDs (roles, groups, privileges) are validated BEFORE creating/updating resources
- Invalid IDs will cause immediate error responses (404 Not Found)
- This prevents partial operations and inconsistent states

#### 7. **User Response Structure**
- User responses always include:
  - Complete role information: `{ roleId, roleName, roleDisplayName }`
  - Complete group information: `{ groupId, groupName }`
  - Attributes as a map: `{ "entity_code": ["ENT001"], "country_code": ["US"] }`
  - Timestamp in milliseconds (epoch): `1733655000000`

---

## ⚙️ Base Configuration

### API Base URL
```
Development: http://localhost:8090
Production: [Your Production URL]
```

### CORS Configuration
The API supports CORS for the following origins:
- `http://localhost:3000`
- `http://localhost:3001`
- `http://127.0.0.1:3000`

### Content Type
All requests should use:
```
Content-Type: application/json
```

---

## 🔐 Authentication

All protected endpoints require a valid JWT Bearer token obtained from Keycloak.

### Token Format
```http
Authorization: Bearer <your-jwt-token>
```

### Required Roles
Most management endpoints require:
- `ROLE_ADMIN` - For user/role/group management operations

### Getting a Token
Use Keycloak's token endpoint:
```http
POST http://localhost:8080/realms/sprint-ap/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

grant_type=password&
client_id=sprint-ap-backend&
client_secret=YOUR_CLIENT_SECRET&
username=<username>&
password=<password>
```

**Response:**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expires_in": 300,
  "refresh_expires_in": 1800,
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer"
}
```

---

## 📡 API Endpoints

### Auth APIs

#### 1. Public Hello (No Auth Required)
```http
GET /api/public/hello
```

**Response:**
```json
{
  "message": "This is a public endpoint"
}
```

---

#### 2. Get User Profile
```http
GET /api/user/profile
Authorization: Bearer <token>
```

**Response:**
```json
{
  "username": "john.doe",
  "email": "john@example.com",
  "name": "John Doe",
  "roles": [
    "ROLE_ADMIN",
    "PRIV_USER_MANAGEMENT"
  ]
}
```

---

#### 3. Get User Data (Requires PRIV_USER_MANAGEMENT)
```http
GET /api/user/data
Authorization: Bearer <token>
```

**Response:**
```json
{
  "message": "This endpoint requires USER role",
  "data": "Some user data"
}
```

---

#### 4. Admin Dashboard (Requires ROLE_ADMIN)
```http
GET /api/admin/dashboard
Authorization: Bearer <token>
```

**Response:**
```json
{
  "message": "This endpoint requires ADMIN role",
  "data": "Admin dashboard data"
}
```

---

#### 5. Manager Reports (Requires ROLE_ADMIN or ROLE_MANAGER)
```http
GET /api/manager/reports
Authorization: Bearer <token>
```

**Response:**
```json
{
  "message": "This endpoint requires ADMIN or MANAGER role",
  "data": "Reports data"
}
```

---

#### 6. Combined Access (Requires Multiple Roles)
```http
GET /api/secure/combined
Authorization: Bearer <token>
```

**Response:**
```json
{
  "message": "This endpoint requires both USER and PREMIUM roles",
  "data": "Premium content"
}
```

---

### User Management APIs

#### 1. Create User
```http
POST /api/users
Authorization: Bearer <token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "password": "SecurePassword123!",
  "enabled": true,
  "emailVerified": false,
  "entityCode": "ENT001",
  "countryCode": "US",
  "roleIds": ["role-uuid-1", "role-uuid-2"],
  "groupIds": ["group-uuid-1"]
}
```

**Validation Rules:**
- `email`: **Required**, must be valid email format (will be used as username automatically)
- `firstName`: **Required**
- `lastName`: **Required**
- `password`: **Required**
- `enabled`: Optional, defaults to `true`
- `emailVerified`: Optional, defaults to `false`
- `entityCode`: Optional, stored in user attributes
- `countryCode`: Optional, stored in user attributes
- `roleIds`: Optional array of role UUIDs (validates IDs before creating user)
- `groupIds`: Optional array of group UUIDs (validates IDs before creating user)

**Important Notes:**
- Username is automatically set to the email address
- Role IDs must be valid UUIDs of existing roles (NOT role names)
- Group IDs must be valid UUIDs of existing groups (NOT group names)
- Only roles starting with `role_` can be assigned (privileges cannot be directly assigned to users)

**Response:** `201 Created`
```json
{
  "message": "User created successfully",
  "timestamp": "2025-12-08T10:30:00.123",
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "username": "john.doe@example.com",
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "enabled": true,
    "emailVerified": false,
    "createdTimestamp": 1733655000000,
    "attributes": {
      "entity_code": ["ENT001"],
      "country_code": ["US"]
    },
    "roles": [
      {
        "roleId": "650e8400-e29b-41d4-a716-446655440001",
        "roleName": "role_developer",
        "roleDisplayName": "developer"
      }
    ],
    "groups": [
      {
        "groupId": "750e8400-e29b-41d4-a716-446655440002",
        "groupName": "Engineering Team"
      }
    ]
  }
}
```

---

#### 2. Get All Users
```http
GET /api/users
Authorization: Bearer <token>
```

**Response:** `200 OK`
```json
[
  {
    "id": "user-uuid-1",
    "username": "john.doe@example.com",
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "enabled": true,
    "emailVerified": true,
    "createdTimestamp": 1701780000000,
    "attributes": {
      "entity_code": ["ENT001"],
      "country_code": ["US"]
    },
    "roles": [
      {
        "roleId": "role-uuid-1",
        "roleName": "role_developer",
        "roleDisplayName": "Developer"
      }
    ],
    "groups": [
      {
        "groupId": "group-uuid-1",
        "groupName": "Engineering Team"
      }
    ]
  }
]
```

---

#### 3. Get User by ID
```http
GET /api/users/{userId}
Authorization: Bearer <token>
```

**Path Parameters:**
- `userId` (string, required): The UUID of the user

**Response:** `200 OK`
```json
{
  "id": "user-uuid-1",
  "username": "john.doe@example.com",
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "enabled": true,
  "emailVerified": true,
  "createdTimestamp": 1701780000000,
  "attributes": {
    "entity_code": ["ENT001"],
    "country_code": ["US"]
  },
  "roles": [
    {
      "roleId": "role-uuid-1",
      "roleName": "role_developer",
      "roleDisplayName": "Developer"
    }
  ],
  "groups": [
    {
      "groupId": "group-uuid-1",
      "groupName": "Engineering Team"
    }
  ]
}
```

---

#### 4. Update User
```http
PUT /api/users/{userId}
Authorization: Bearer <token>
Content-Type: application/json
```

**Path Parameters:**
- `userId` (string, required): The UUID of the user

**Request Body:**
```json
{
  "email": "john.updated@example.com",
  "firstName": "John",
  "lastName": "Doe Updated",
  "enabled": true,
  "emailVerified": true,
  "entityCode": "ENT002",
  "countryCode": "UK",
  "roleIdsToAdd": ["role-uuid-3"],
  "roleIdsToRemove": ["role-uuid-1"],
  "groupIdsToAdd": ["group-uuid-2"],
  "groupIdsToRemove": ["group-uuid-1"]
}
```

**Validation Rules:**
- `email`: Required, must be valid email format
- `firstName`: Required
- `lastName`: Required
- `enabled`: Optional
- `emailVerified`: Optional
- `entityCode`: Optional
- `countryCode`: Optional
- `roleIdsToAdd`: Optional array of role UUIDs to add
- `roleIdsToRemove`: Optional array of role UUIDs to remove
- `groupIdsToAdd`: Optional array of group UUIDs to add
- `groupIdsToRemove`: Optional array of group UUIDs to remove

**Response:** `200 OK`
```json
{
  "message": "User updated successfully",
  "timestamp": "2025-12-08T10:30:00.123",
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "username": "john.updated@example.com",
    "email": "john.updated@example.com",
    "firstName": "John",
    "lastName": "Doe Updated",
    "enabled": true,
    "emailVerified": true,
    "createdTimestamp": 1733655000000,
    "attributes": {
      "entity_code": ["ENT002"],
      "country_code": ["UK"]
    },
    "roles": [
      {
        "roleId": "650e8400-e29b-41d4-a716-446655440002",
        "roleName": "role_manager",
        "roleDisplayName": "manager"
      },
      {
        "roleId": "750e8400-e29b-41d4-a716-446655440003",
        "roleName": "role_senior_developer",
        "roleDisplayName": "senior_developer"
      }
    ],
    "groups": [
      {
        "groupId": "850e8400-e29b-41d4-a716-446655440004",
        "groupName": "Management Team"
      }
    ]
  }
}
```

---

#### 5. Delete User
```http
DELETE /api/users/{userId}
Authorization: Bearer <token>
```

**Path Parameters:**
- `userId` (string, required): The UUID of the user

**Response:** `200 OK`
```json
{
  "message": "User deleted successfully",
  "timestamp": "2025-12-08T10:30:00.123"
}
```

---

### Role Management APIs

#### 1. Create Role
```http
POST /api/roles
Authorization: Bearer <token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "roleName": "role_manager",
  "description": "Manager role with specific privileges",
  "privilegeIds": ["priv-uuid-1", "priv-uuid-2"]
}
```

**Validation Rules:**
- `roleName`: Required, must start with `role_`
- `description`: Optional
- `privilegeIds`: Optional array of privilege UUIDs

**Response:** `201 Created`
```json
{
  "message": "Role created successfully",
  "timestamp": "2025-12-08T10:30:00.123",
  "role": {
    "id": "650e8400-e29b-41d4-a716-446655440001",
    "name": "role_manager",
    "displayName": "manager",
    "description": "Manager role with specific privileges",
    "composite": true
  }
}
```

---

#### 2. Get All Roles
```http
GET /api/roles
Authorization: Bearer <token>
```

**Response:** `200 OK`
```json
[
  {
    "id": "650e8400-e29b-41d4-a716-446655440001",
    "name": "role_admin",
    "displayName": "admin",
    "description": "Administrator role",
    "composite": true
  },
  {
    "id": "750e8400-e29b-41d4-a716-446655440002",
    "name": "role_manager",
    "displayName": "manager",
    "description": "Manager role",
    "composite": true
  }
]
```

---

#### 3. Get Privileges for Role
```http
GET /api/roles/{roleId}/privileges
Authorization: Bearer <token>
```

**Path Parameters:**
- `roleId` (string, required): The UUID of the role

**Response:** `200 OK`
```json
[
  {
    "id": "850e8400-e29b-41d4-a716-446655440001",
    "name": "priv_user_management",
    "displayName": "user_management",
    "description": "User management privilege"
  },
  {
    "id": "950e8400-e29b-41d4-a716-446655440002",
    "name": "priv_view_reports",
    "displayName": "view_reports",
    "description": "View reports privilege"
  }
]
```

---

#### 4. Get All Privileges
```http
GET /api/roles/privileges
Authorization: Bearer <token>
```

**Response:** `200 OK`
```json
[
  {
    "id": "850e8400-e29b-41d4-a716-446655440001",
    "name": "priv_user_management",
    "displayName": "user_management",
    "description": "User management privilege"
  },
  {
    "id": "950e8400-e29b-41d4-a716-446655440002",
    "name": "priv_view_reports",
    "displayName": "view_reports",
    "description": "View reports privilege"
  },
  {
    "id": "a50e8400-e29b-41d4-a716-446655440003",
    "name": "priv_code_review",
    "displayName": "code_review",
    "description": "Code review privilege"
  }
]
```

---

#### 5. Update Role
```http
PUT /api/roles/{roleId}
Authorization: Bearer <token>
Content-Type: application/json
```

**Path Parameters:**
- `roleId` (string, required): The UUID of the role

**Request Body:**
```json
{
  "description": "Updated role description",
  "privilegeIdsToAdd": ["a50e8400-e29b-41d4-a716-446655440003"],
  "privilegeIdsToRemove": ["850e8400-e29b-41d4-a716-446655440001"]
}
```

**Validation Rules:**
- `description`: Optional
- `privilegeIdsToAdd`: Required (can be empty array)
- `privilegeIdsToRemove`: Required (can be empty array)

**Response:** `200 OK`
```json
{
  "message": "Role updated successfully",
  "timestamp": "2025-12-08T10:30:00.123",
  "role": {
    "id": "650e8400-e29b-41d4-a716-446655440001",
    "name": "role_manager",
    "displayName": "manager",
    "description": "Updated role description",
    "composite": true
  }
}
```

---

#### 6. Delete Role
```http
DELETE /api/roles/{roleId}
Authorization: Bearer <token>
```

**Path Parameters:**
- `roleId` (string, required): The UUID of the role

**Response:** `200 OK`
```json
{
  "message": "Role deleted successfully",
  "timestamp": "2025-12-08T10:30:00.123"
}
```

**Error Response (Role in Use):** `409 Conflict`
```json
{
  "error": "Role In Use",
  "message": "Cannot delete role 'role_manager'. It is currently assigned to 5 user(s)...",
  "status": 409,
  "timestamp": "2025-12-08T10:30:00"
}
```

---

### Group Management APIs

#### 1. Create Group
```http
POST /api/groups
Authorization: Bearer <token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "groupName": "Engineering Team",
  "roleIds": ["role-uuid-1", "role-uuid-2"],
  "privilegeIds": ["priv-uuid-1"]
}
```

**Validation Rules:**
- `groupName`: Required
- `roleIds`: Optional array of role UUIDs
- `privilegeIds`: Optional array of privilege UUIDs

**Response:** `201 Created`
```json
{
  "message": "Group created successfully",
  "timestamp": "2025-12-08T10:30:00.123",
  "group": {
    "id": "750e8400-e29b-41d4-a716-446655440002",
    "name": "Engineering Team",
    "userCount": 0
  }
}
```

**Important Notes:**
- Group IDs are UUIDs generated by Keycloak
- Role IDs must be valid UUIDs (validates before creating group)
- Privilege IDs must be valid UUIDs (validates before creating group)
- The response does NOT include a `path` field

---

#### 2. Get All Groups
```http
GET /api/groups
Authorization: Bearer <token>
```

**Response:** `200 OK`
```json
[
  {
    "id": "750e8400-e29b-41d4-a716-446655440002",
    "name": "Engineering Team",
    "userCount": 5
  },
  {
    "id": "850e8400-e29b-41d4-a716-446655440003",
    "name": "Sales Team",
    "userCount": 3
  }
]
```

---

#### 3. Get Roles and Privileges for Group
```http
GET /api/groups/{groupId}/roles-privileges
Authorization: Bearer <token>
```

**Path Parameters:**
- `groupId` (string, required): The UUID of the group

**Response:** `200 OK`
```json
{
  "roles": [
    {
      "id": "role-uuid-1",
      "name": "role_developer",
      "displayName": "Developer",
      "description": "Developer role",
      "composite": true
    }
  ],
  "privileges": [
    {
      "id": "priv-uuid-1",
      "name": "priv_code_review",
      "displayName": "Code Review",
      "description": "Code review privilege"
    }
  ]
}
```

---

#### 4. Get Users in Group
```http
GET /api/groups/{groupId}/users
Authorization: Bearer <token>
```

**Path Parameters:**
- `groupId` (string, required): The UUID of the group

**Response:** `200 OK`
```json
[
  {
    "id": "user-uuid-1",
    "username": "john.doe@example.com",
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "enabled": true,
    "emailVerified": true,
    "createdTimestamp": 1701780000000,
    "attributes": {
      "entity_code": ["ENT001"],
      "country_code": ["US"]
    },
    "roles": [
      {
        "roleId": "role-uuid-1",
        "roleName": "role_developer",
        "roleDisplayName": "Developer"
      }
    ],
    "groups": [
      {
        "groupId": "group-uuid-1",
        "groupName": "Engineering Team"
      }
    ]
  }
]
```

---

#### 5. Update Group Users
```http
PUT /api/groups/{groupId}/users
Authorization: Bearer <token>
Content-Type: application/json
```

**Path Parameters:**
- `groupId` (string, required): The UUID of the group

**Request Body:**
```json
{
  "userIdsToAdd": ["user-uuid-1", "user-uuid-2"],
  "userIdsToRemove": ["user-uuid-3"]
}
```

**Validation Rules:**
- `userIdsToAdd`: Optional array of user UUIDs to add to the group
- `userIdsToRemove`: Optional array of user UUIDs to remove from the group

**Response:** `200 OK`
```json
{
  "message": "Group users updated successfully",
  "timestamp": "2025-12-08T10:30:00.123"
}
```

---

#### 6. Update Group Roles and Privileges
```http
PUT /api/groups/{groupId}/roles-privileges
Authorization: Bearer <token>
Content-Type: application/json
```

**Path Parameters:**
- `groupId` (string, required): The UUID of the group

**Request Body:**
```json
{
  "roleIdsToAdd": ["750e8400-e29b-41d4-a716-446655440003"],
  "roleIdsToRemove": ["650e8400-e29b-41d4-a716-446655440001"],
  "privilegeIdsToAdd": ["950e8400-e29b-41d4-a716-446655440002"],
  "privilegeIdsToRemove": ["850e8400-e29b-41d4-a716-446655440001"]
}
```

**Validation Rules:**
- `roleIdsToAdd`: Required (can be empty array)
- `roleIdsToRemove`: Required (can be empty array)
- `privilegeIdsToAdd`: Required (can be empty array)
- `privilegeIdsToRemove`: Required (can be empty array)

**Response:** `200 OK`
```json
{
  "message": "Group roles and privileges updated successfully",
  "timestamp": "2025-12-08T10:30:00.123"
}
```

---

#### 7. Delete Group
```http
DELETE /api/groups/{groupId}
Authorization: Bearer <token>
```

**Path Parameters:**
- `groupId` (string, required): The UUID of the group

**Response:** `200 OK`
```json
{
  "message": "Group deleted successfully",
  "timestamp": "2025-12-08T10:30:00.123"
}
```

---

## 📊 Data Models

### UserDTO
```typescript
interface UserDTO {
  id: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  enabled: boolean;
  emailVerified: boolean;
  createdTimestamp: number;
  attributes: {
    [key: string]: string[];
  };
  roles: UserRoleInfo[];
  groups: UserGroupInfo[];
}
```

### UserRoleInfo
```typescript
interface UserRoleInfo {
  roleId: string;
  roleName: string;
  roleDisplayName: string;  // Name without 'role_' prefix for UI display
}
```

### UserGroupInfo
```typescript
interface UserGroupInfo {
  groupId: string;
  groupName: string;
}
```

### RoleDTO
```typescript
interface RoleDTO {
  id: string;
  name: string;
  displayName: string;  // Name without 'role_' prefix for UI display
  description: string;
  composite: boolean;
}
```

### PrivilegeDTO
```typescript
interface PrivilegeDTO {
  id: string;
  name: string;
  displayName: string;  // Name without 'priv_' prefix for UI display
  description: string;
}
```

### GroupDTO
```typescript
interface GroupDTO {
  id: string;
  name: string;
  userCount: number;  // Number of users in the group
}
```

### GroupRolesPrivilegesDTO
```typescript
interface GroupRolesPrivilegesDTO {
  roles: RoleDTO[];
  privileges: PrivilegeDTO[];
}
```

---

## ⚠️ Error Handling

### Error Response Format
All API errors follow this consistent format:

```json
{
  "error": "Error Type",
  "message": "Detailed error message",
  "status": 400,
  "timestamp": "2025-12-08T10:30:00"
}
```

### Common HTTP Status Codes

| Code | Description | Example |
|------|-------------|---------|
| 200 | Success | Request completed successfully |
| 201 | Created | Resource created successfully |
| 400 | Bad Request | Invalid input or validation failure |
| 401 | Unauthorized | Missing or invalid authentication token |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource not found |
| 409 | Conflict | Resource conflict (e.g., role in use) |
| 500 | Internal Server Error | Server error |

### Validation Error Response
```json
{
  "error": "Validation Failed",
  "message": "Please check the input fields",
  "fieldErrors": {
    "email": "Email is required",
    "firstName": "First name is required"
  },
  "status": 400,
  "timestamp": "2025-12-08T10:30:00"
}
```

### Common Error Scenarios

#### 1. Unauthorized Access
```json
{
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "status": 401,
  "timestamp": "2025-12-08T10:30:00"
}
```

#### 2. Forbidden Access
```json
{
  "error": "Forbidden",
  "message": "Access denied. Insufficient permissions.",
  "status": 403,
  "timestamp": "2025-12-08T10:30:00"
}
```

#### 3. Resource Not Found
```json
{
  "error": "Not Found",
  "message": "User with ID 'user-uuid-123' not found",
  "status": 404,
  "timestamp": "2025-12-08T10:30:00"
}
```

#### 4. Conflict (Role in Use)
```json
{
  "error": "Role In Use",
  "message": "Cannot delete role 'role_manager'. It is currently assigned to 5 user(s). Please remove the role from all users before deletion.",
  "status": 409,
  "timestamp": "2025-12-08T10:30:00"
}
```

---

## 🚀 Frontend Integration Guide

### React/TypeScript Example

#### 1. API Service Setup

```typescript
// api/config.ts
export const API_CONFIG = {
  baseURL: 'http://localhost:8090',
  timeout: 30000,
};

// api/auth.ts
export const getAuthHeader = () => {
  const token = localStorage.getItem('access_token');
  return token ? { Authorization: `Bearer ${token}` } : {};
};
```

#### 2. User Service Example

```typescript
// services/userService.ts
import { API_CONFIG, getAuthHeader } from './config';

export const userService = {
  // Get all users
  getAllUsers: async (): Promise<UserDTO[]> => {
    const response = await fetch(`${API_CONFIG.baseURL}/api/users`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        ...getAuthHeader(),
      },
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Failed to fetch users');
    }

    return response.json();
  },

  // Create user
  createUser: async (userData: CreateUserRequest): Promise<any> => {
    const response = await fetch(`${API_CONFIG.baseURL}/api/users`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...getAuthHeader(),
      },
      body: JSON.stringify(userData),
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Failed to create user');
    }

    return response.json();
  },

  // Update user
  updateUser: async (userId: string, userData: UpdateUserRequest): Promise<any> => {
    const response = await fetch(`${API_CONFIG.baseURL}/api/users/${userId}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        ...getAuthHeader(),
      },
      body: JSON.stringify(userData),
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Failed to update user');
    }

    return response.json();
  },

  // Delete user
  deleteUser: async (userId: string): Promise<void> => {
    const response = await fetch(`${API_CONFIG.baseURL}/api/users/${userId}`, {
      method: 'DELETE',
      headers: {
        ...getAuthHeader(),
      },
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Failed to delete user');
    }
  },
};
```

#### 3. React Component Example

```typescript
// components/UserList.tsx
import React, { useEffect, useState } from 'react';
import { userService } from '../services/userService';

interface UserDTO {
  id: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  enabled: boolean;
}

export const UserList: React.FC = () => {
  const [users, setUsers] = useState<UserDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadUsers();
  }, []);

  const loadUsers = async () => {
    try {
      setLoading(true);
      const data = await userService.getAllUsers();
      setUsers(data);
      setError(null);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An error occurred');
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteUser = async (userId: string) => {
    if (!window.confirm('Are you sure you want to delete this user?')) {
      return;
    }

    try {
      await userService.deleteUser(userId);
      await loadUsers(); // Reload list
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to delete user');
    }
  };

  if (loading) return <div>Loading...</div>;
  if (error) return <div>Error: {error}</div>;

  return (
    <div>
      <h2>Users</h2>
      <table>
        <thead>
          <tr>
            <th>Email</th>
            <th>Name</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {users.map((user) => (
            <tr key={user.id}>
              <td>{user.email}</td>
              <td>{`${user.firstName} ${user.lastName}`}</td>
              <td>{user.enabled ? 'Active' : 'Inactive'}</td>
              <td>
                <button onClick={() => handleDeleteUser(user.id)}>
                  Delete
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};
```

#### 4. Axios Alternative (If you prefer Axios)

```typescript
// services/axiosClient.ts
import axios from 'axios';

const axiosClient = axios.create({
  baseURL: 'http://localhost:8090',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add token
axiosClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('access_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor for error handling
axiosClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Handle unauthorized - redirect to login
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default axiosClient;

// services/userService.ts (Axios version)
import axiosClient from './axiosClient';

export const userService = {
  getAllUsers: () => axiosClient.get('/api/users'),
  getUserById: (userId: string) => axiosClient.get(`/api/users/${userId}`),
  createUser: (data: CreateUserRequest) => axiosClient.post('/api/users', data),
  updateUser: (userId: string, data: UpdateUserRequest) => 
    axiosClient.put(`/api/users/${userId}`, data),
  deleteUser: (userId: string) => axiosClient.delete(`/api/users/${userId}`),
};
```

#### 5. Error Handling Hook

```typescript
// hooks/useApiError.ts
import { useState } from 'react';

interface ApiError {
  error: string;
  message: string;
  status: number;
  fieldErrors?: { [key: string]: string };
}

export const useApiError = () => {
  const [error, setError] = useState<ApiError | null>(null);

  const handleError = (err: any) => {
    if (err.response?.data) {
      setError(err.response.data);
    } else if (err instanceof Error) {
      setError({
        error: 'Error',
        message: err.message,
        status: 500,
      });
    } else {
      setError({
        error: 'Unknown Error',
        message: 'An unknown error occurred',
        status: 500,
      });
    }
  };

  const clearError = () => setError(null);

  return { error, handleError, clearError };
};
```

### Best Practices

#### 1. Token Management
```typescript
// Store token after login
const storeToken = (token: string) => {
  localStorage.setItem('access_token', token);
};

// Clear token on logout
const clearToken = () => {
  localStorage.removeItem('access_token');
};

// Check if token exists
const isAuthenticated = () => {
  return !!localStorage.getItem('access_token');
};
```

#### 2. Loading States
```typescript
const [loading, setLoading] = useState(false);

const fetchData = async () => {
  setLoading(true);
  try {
    const data = await apiCall();
    // Handle success
  } catch (error) {
    // Handle error
  } finally {
    setLoading(false);
  }
};
```

#### 3. Confirmation Dialogs
```typescript
const handleDelete = async (id: string) => {
  const confirmed = window.confirm(
    'Are you sure you want to delete this item?'
  );
  
  if (confirmed) {
    await deleteItem(id);
  }
};
```

#### 4. Form Validation
```typescript
const validateForm = (data: CreateUserRequest) => {
  const errors: { [key: string]: string } = {};

  if (!data.email) {
    errors.email = 'Email is required';
  } else if (!/\S+@\S+\.\S+/.test(data.email)) {
    errors.email = 'Email is invalid';
  }

  if (!data.firstName) {
    errors.firstName = 'First name is required';
  }

  if (!data.password || data.password.length < 8) {
    errors.password = 'Password must be at least 8 characters';
  }

  return errors;
};
```

---

## 📝 Notes

### Important Naming Conventions
- **Roles**: Must start with `role_` (e.g., `role_admin`, `role_manager`)
- **Privileges**: Must start with `priv_` (e.g., `priv_user_management`, `priv_view_reports`)
- **Privileges are pre-defined** and cannot be created, modified, or deleted via API
- **Roles can be composite** containing multiple privileges

### Security Considerations
1. Always use HTTPS in production
2. Store JWT tokens securely (consider using httpOnly cookies for production)
3. Implement token refresh logic
4. Handle token expiration gracefully
5. Never log sensitive data (passwords, tokens)
6. Validate all user inputs on both client and server side

### Performance Tips
1. Implement pagination for large lists (if backend supports it)
2. Use debouncing for search inputs
3. Cache frequently accessed data
4. Implement optimistic UI updates where appropriate
5. Use loading skeletons for better UX

### Testing Recommendations
1. Test all error scenarios
2. Test with expired tokens
3. Test with missing required fields
4. Test concurrent operations
5. Test network failures

---

## 📞 Support

For issues or questions:
- Check the application logs at `logs/application.log`
- Review Keycloak admin console at `http://localhost:8080`
- Verify CORS settings if facing cross-origin issues

---

## 📋 Quick Reference Guide

### API Endpoint Summary

#### User Management
| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| POST | `/api/users` | Create user | CreateUserRequest | UserDTO with message & timestamp |
| GET | `/api/users` | Get all users | - | UserDTO[] |
| GET | `/api/users/{userId}` | Get user by ID | - | UserDTO |
| PUT | `/api/users/{userId}` | Update user | UpdateUserRequest | UserDTO with message & timestamp |
| DELETE | `/api/users/{userId}` | Delete user | - | Message & timestamp |

#### Role Management
| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| POST | `/api/roles` | Create role | CreateRoleRequest | RoleDTO with message & timestamp |
| GET | `/api/roles` | Get all roles | - | RoleDTO[] |
| GET | `/api/roles/{roleId}/privileges` | Get role privileges | - | PrivilegeDTO[] |
| GET | `/api/roles/privileges` | Get all privileges | - | PrivilegeDTO[] |
| PUT | `/api/roles/{roleId}` | Update role | UpdateRoleRequest | RoleDTO with message & timestamp |
| DELETE | `/api/roles/{roleId}` | Delete role | - | Message & timestamp |

#### Group Management
| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| POST | `/api/groups` | Create group | CreateGroupRequest | GroupDTO with message & timestamp |
| GET | `/api/groups` | Get all groups | - | GroupDTO[] |
| GET | `/api/groups/{groupId}/roles-privileges` | Get group roles & privileges | - | GroupRolesPrivilegesDTO |
| GET | `/api/groups/{groupId}/users` | Get users in group | - | UserDTO[] |
| PUT | `/api/groups/{groupId}/users` | Update group users | UpdateGroupUsersRequest | Message & timestamp |
| PUT | `/api/groups/{groupId}/roles-privileges` | Update group roles & privileges | UpdateGroupRolesPrivilegesRequest | Message & timestamp |
| DELETE | `/api/groups/{groupId}` | Delete group | - | Message & timestamp |

### Request Body Structures

#### CreateUserRequest
```json
{
  "email": "user@example.com",          // Required (used as username)
  "firstName": "John",                   // Required
  "lastName": "Doe",                     // Required
  "password": "password123",             // Required
  "enabled": true,                       // Optional (default: true)
  "emailVerified": false,                // Optional (default: false)
  "entityCode": "ENT001",               // Optional
  "countryCode": "US",                  // Optional
  "roleIds": ["uuid1", "uuid2"],        // Optional (UUIDs only)
  "groupIds": ["uuid3"]                 // Optional (UUIDs only)
}
```

#### UpdateUserRequest
```json
{
  "email": "user@example.com",          // Required
  "firstName": "John",                   // Required
  "lastName": "Doe",                     // Required
  "enabled": true,                       // Optional
  "emailVerified": true,                 // Optional
  "entityCode": "ENT002",               // Optional
  "countryCode": "UK",                  // Optional
  "roleIdsToAdd": ["uuid4"],            // Optional
  "roleIdsToRemove": ["uuid1"],         // Optional
  "groupIdsToAdd": ["uuid5"],           // Optional
  "groupIdsToRemove": ["uuid3"]         // Optional
}
```

#### CreateRoleRequest
```json
{
  "roleName": "role_manager",           // Required (auto-prepends "role_" if missing)
  "description": "Manager role",        // Optional
  "privilegeIds": ["uuid1", "uuid2"]   // Optional (UUIDs only)
}
```

#### UpdateRoleRequest
```json
{
  "description": "Updated description",         // Optional
  "privilegeIdsToAdd": ["uuid3"],              // Required (can be empty [])
  "privilegeIdsToRemove": ["uuid1"]            // Required (can be empty [])
}
```

#### CreateGroupRequest
```json
{
  "groupName": "Engineering Team",      // Required
  "roleIds": ["uuid1", "uuid2"],       // Optional (UUIDs only)
  "privilegeIds": ["uuid3"]            // Optional (UUIDs only)
}
```

#### UpdateGroupUsersRequest
```json
{
  "userIdsToAdd": ["uuid1", "uuid2"],          // Required (can be empty [])
  "userIdsToRemove": ["uuid3"]                 // Required (can be empty [])
}
```

#### UpdateGroupRolesPrivilegesRequest
```json
{
  "roleIdsToAdd": ["uuid1"],                   // Required (can be empty [])
  "roleIdsToRemove": ["uuid2"],                // Required (can be empty [])
  "privilegeIdsToAdd": ["uuid3"],              // Required (can be empty [])
  "privilegeIdsToRemove": ["uuid4"]            // Required (can be empty [])
}
```

### Common Pitfalls to Avoid

1. **❌ DON'T** use role/group names in API paths - use UUIDs
   - Wrong: `DELETE /api/roles/role_admin`
   - Correct: `DELETE /api/roles/650e8400-e29b-41d4-a716-446655440001`

2. **❌ DON'T** include `username` field in user requests
   - The backend automatically uses email as username

3. **❌ DON'T** expect a `path` field in GroupDTO responses
   - GroupDTO only has `id` and `name`

4. **❌ DON'T** send null values for required arrays
   - Send empty arrays `[]` instead of `null` for "IdsToAdd" and "IdsToRemove" fields

5. **❌ DON'T** try to assign privileges directly to users
   - Only roles can be assigned to users
   - Privileges are assigned to roles, users inherit them

6. **❌ DON'T** use role names in request bodies
   - Always use role/group/privilege UUIDs

### Validation Examples

#### ✅ Valid Create User Request
```json
{
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "password": "SecurePass123!",
  "roleIds": ["650e8400-e29b-41d4-a716-446655440001"],
  "groupIds": []
}
```

#### ❌ Invalid Create User Request
```json
{
  "email": "invalid-email",              // Invalid email format
  "firstName": "",                       // Empty string not allowed
  "password": "123",                     // Too short
  "roleIds": ["role_admin"],             // Should be UUID, not name
  "groupIds": null                       // Should be [] not null
}
```

### HTTP Status Code Reference

| Code | Meaning | Common Causes |
|------|---------|---------------|
| 200 | OK | Successful GET, PUT, DELETE operations |
| 201 | Created | Successful POST operations (resource created) |
| 400 | Bad Request | Validation failed, invalid input, null instead of empty array |
| 401 | Unauthorized | Missing or invalid JWT token |
| 403 | Forbidden | Valid token but insufficient permissions |
| 404 | Not Found | User/Role/Group/Privilege with given ID not found |
| 409 | Conflict | Role is in use and cannot be deleted, duplicate email |
| 500 | Internal Server Error | Server-side error |

---

**Last Updated:** December 8, 2025  
**API Version:** 1.0  
**Backend:** Spring Boot 3.x + Keycloak

