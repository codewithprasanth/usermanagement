# User Management API Documentation

## Overview
This document describes the Keycloak-based Role, Group, and User Management APIs.

### Important Notes
- **All IDs in path parameters and request bodies are UUIDs** (Keycloak generated IDs)
- **All role and privilege references use IDs, not names**
- **API paths use resource IDs** (e.g., `/api/roles/{roleId}` not `/api/roles/{roleName}`)
- **displayName fields are provided** for UI display without prefixes

### Naming Conventions
- **Roles**: Must start with `role_` (e.g., `role_admin`, `role_manager`)
- **Privileges**: Must start with `priv_` (e.g., `priv_user_management`, `priv_view_reports`)
- **Privileges are pre-defined** and cannot be created, modified, or deleted via API
- **Roles can be composite** containing multiple privileges
- **Role prefix auto-prepended**: If you send `"roleName": "manager"`, the API will auto-prepend `role_` to create `role_manager`

## Role API Endpoints

### Base Path: `/api/roles`

#### 1. Create Role
**Important**: Use privilege IDs (UUIDs) in the privilegeIds array, not privilege names.

```http
POST /api/roles
Content-Type: application/json
Authorization: Bearer {admin-token}

Request Body:
{
  "roleName": "manager",
  "description": "Manager role with specific privileges",
  "privilegeIds": ["a3b5c7d9-1234-5678-9abc-def012345678", "b4c6d8e0-2345-6789-abcd-ef0123456789"]
}

Response: 201 Created
{
  "message": "Role created successfully",
  "role": {
    "id": "65ca6853-2e06-448f-bad0-bed2995c202d",
    "name": "role_manager",
    "displayName": "manager",
    "description": "Manager role with specific privileges",
    "composite": true
  }
}

Notes:
- roleName will be auto-prepended with "role_" prefix if not already present
- privilegeIds must be valid privilege IDs (get from GET /api/roles/privileges)
- Empty privilegeIds array or null creates a non-composite role
```

#### 2. Delete Role
**Important**: Use role ID (UUID) in the path, not role name.

```http
DELETE /api/roles/{roleId}
Authorization: Bearer {admin-token}

Example:
DELETE /api/roles/65ca6853-2e06-448f-bad0-bed2995c202d

Response: 200 OK
{
  "message": "Role deleted successfully"
}

Error Response: 404 Not Found
{
  "error": "Role Not Found",
  "message": "Role with ID '65ca6853-2e06-448f-bad0-bed2995c202d' not found",
  "status": 404,
  "timestamp": "2025-12-12T14:30:00"
}

Error Response: 409 Conflict (if role is assigned to users)
{
  "error": "Role In Use",
  "message": "Cannot delete role 'role_manager'. It is currently assigned to 5 user(s). Please remove the role from all users first.",
  "status": 409,
  "timestamp": "2025-12-12T14:30:00"
}
```

#### 3. Update Role
**Important**: Use role ID (UUID) in the path and privilege IDs in the request body.

```http
PUT /api/roles/{roleId}
Content-Type: application/json
Authorization: Bearer {admin-token}

Example:
PUT /api/roles/65ca6853-2e06-448f-bad0-bed2995c202d

Request Body:
{
  "description": "Updated manager role description",
  "privilegeIdsToAdd": ["c5d7e9f1-3456-7890-bcde-f01234567890"],
  "privilegeIdsToRemove": ["b4c6d8e0-2345-6789-abcd-ef0123456789"]
}

Response: 200 OK
{
  "message": "Role updated successfully",
  "role": {
    "id": "65ca6853-2e06-448f-bad0-bed2995c202d",
    "name": "role_manager",
    "displayName": "manager",
    "description": "Updated manager role description",
    "composite": true
  }
}

Notes:
- All fields are optional - send only what you want to update
- privilegeIdsToAdd and privilegeIdsToRemove must be valid privilege IDs
- Can send empty arrays to skip adding/removing privileges
```

#### 4. Get All Roles
```http
GET /api/roles
Authorization: Bearer {admin-token}

Response: 200 OK
[
  {
    "id": "65ca6853-2e06-448f-bad0-bed2995c202d",
    "name": "role_admin",
    "displayName": "admin",
    "description": "Administrator role",
    "composite": true
  },
  {
    "id": "468bdbcc-ae15-47f6-a529-4c104f589010",
    "name": "role_manager",
    "displayName": "manager",
    "description": "Manager role",
    "composite": true
  }
]

Notes:
- Returns only roles (filtered by "role_" prefix)
- displayName is the name without "role_" prefix for UI display
```

#### 5. Get Privileges for Role
**Important**: Use role ID (UUID) in the path.

```http
GET /api/roles/{roleId}/privileges
Authorization: Bearer {admin-token}

Example:
GET /api/roles/65ca6853-2e06-448f-bad0-bed2995c202d/privileges

Response: 200 OK
[
  {
    "id": "a3b5c7d9-1234-5678-9abc-def012345678",
    "name": "priv_user_management",
    "displayName": "user_management",
    "description": "User management privilege"
  },
  {
    "id": "b4c6d8e0-2345-6789-abcd-ef0123456789",
    "name": "priv_view_reports",
    "displayName": "view_reports",
    "description": "View reports privilege"
  }
]

Notes:
- Returns privileges assigned to the specified role
- displayName is the name without "priv_" prefix for UI display
```

#### 6. Get All Privileges
```http
GET /api/roles/privileges
Authorization: Bearer {admin-token}

Response: 200 OK
[
  {
    "id": "a3b5c7d9-1234-5678-9abc-def012345678",
    "name": "priv_user_management",
    "displayName": "user_management",
    "description": "User management privilege"
  },
  {
    "id": "b4c6d8e0-2345-6789-abcd-ef0123456789",
    "name": "priv_view_reports",
    "displayName": "view_reports",
    "description": "View reports privilege"
  }
]

Notes:
- Returns all available privileges (filtered by "priv_" prefix)
- Use these IDs when creating/updating roles
- Privileges cannot be created, modified, or deleted via API
```

---

## Group API Endpoints

### Base Path: `/api/groups`

#### 1. Create Group
**Important**: Use role IDs and privilege IDs (UUIDs), not names.

```http
POST /api/groups
Content-Type: application/json
Authorization: Bearer {admin-token}

Request Body:
{
  "groupName": "Engineering Team",
  "roleIds": ["65ca6853-2e06-448f-bad0-bed2995c202d", "468bdbcc-ae15-47f6-a529-4c104f589010"],
  "privilegeIds": ["a3b5c7d9-1234-5678-9abc-def012345678"]
}

Response: 201 Created
{
  "message": "Group created successfully",
  "group": {
    "id": "f8e7d6c5-4321-0987-bcde-f01234567890",
    "name": "Engineering Team",
    "userCount": 0
  }
}

Notes:
- roleIds and privilegeIds are optional arrays
- roleIds must be valid role IDs (get from GET /api/roles)
- privilegeIds must be valid privilege IDs (get from GET /api/roles/privileges)
- userCount is always returned (0 for new groups)
```

#### 2. Delete Group
```http
DELETE /api/groups/{groupId}
Authorization: Bearer {admin-token}

Example:
DELETE /api/groups/f8e7d6c5-4321-0987-bcde-f01234567890

Response: 200 OK
{
  "message": "Group deleted successfully"
}

Error Response: 404 Not Found
{
  "error": "Group Not Found",
  "message": "Group with ID 'f8e7d6c5-4321-0987-bcde-f01234567890' not found",
  "status": 404,
  "timestamp": "2025-12-12T14:30:00"
}
```

#### 3. Get All Groups
**Important**: Response includes userCount field.

```http
GET /api/groups
Authorization: Bearer {admin-token}

Response: 200 OK
[
  {
    "id": "f8e7d6c5-4321-0987-bcde-f01234567890",
    "name": "Engineering Team",
    "userCount": 15
  },
  {
    "id": "e7d6c5b4-3210-9876-abcd-e01234567890",
    "name": "Sales Team",
    "userCount": 8
  }
]

Notes:
- userCount shows the number of users in each group
- This is a real-time count fetched from Keycloak
```

#### 4. Get Roles and Privileges for Group
```http
GET /api/groups/{groupId}/roles-privileges
Authorization: Bearer {admin-token}

Example:
GET /api/groups/f8e7d6c5-4321-0987-bcde-f01234567890/roles-privileges

Response: 200 OK
{
  "roles": [
    {
      "id": "65ca6853-2e06-448f-bad0-bed2995c202d",
      "name": "role_developer",
      "displayName": "developer",
      "description": "Developer role",
      "composite": true
    }
  ],
  "privileges": [
    {
      "id": "a3b5c7d9-1234-5678-9abc-def012345678",
      "name": "priv_code_review",
      "displayName": "code_review",
      "description": "Code review privilege"
    }
  ]
}

Notes:
- Returns roles and privileges assigned to the group
- displayName fields are provided without prefixes for UI display
```

#### 5. Get Users in Group
```http
GET /api/groups/{groupId}/users
Authorization: Bearer {admin-token}

Example:
GET /api/groups/f8e7d6c5-4321-0987-bcde-f01234567890/users

Response: 200 OK
[
  {
    "id": "0580fded-cb94-4304-9be1-7cc54b806b92",
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
        "roleId": "65ca6853-2e06-448f-bad0-bed2995c202d",
        "roleName": "role_developer",
        "roleDisplayName": "developer"
      }
    ],
    "groups": [
      {
        "groupId": "f8e7d6c5-4321-0987-bcde-f01234567890",
        "groupName": "Engineering Team"
      }
    ]
  }
]

Notes:
- Returns full user details including roles and groups
- Roles include roleId, roleName, and roleDisplayName for convenience
- Groups include groupId and groupName
```

#### 6. Update Group Users
**Important**: Use user IDs (UUIDs).

```http
PUT /api/groups/{groupId}/users
Content-Type: application/json
Authorization: Bearer {admin-token}

Example:
PUT /api/groups/f8e7d6c5-4321-0987-bcde-f01234567890/users

Request Body:
{
  "userIdsToAdd": ["0580fded-cb94-4304-9be1-7cc54b806b92", "c52e3794-3992-42a8-ab22-307df4427850"],
  "userIdsToRemove": ["e213eb0d-67c5-4567-8fb8-c6e9a6f5172c"]
}

Response: 200 OK
{
  "message": "Group users updated successfully"
}

Notes:
- Both arrays are required but can be empty
- userIdsToAdd: Add users to the group
- userIdsToRemove: Remove users from the group
- Invalid user IDs will throw 404 error
```

#### 7. Update Group Roles and Privileges
**Important**: Use role IDs and privilege IDs (UUIDs).

```http
PUT /api/groups/{groupId}/roles-privileges
Content-Type: application/json
Authorization: Bearer {admin-token}

Example:
PUT /api/groups/f8e7d6c5-4321-0987-bcde-f01234567890/roles-privileges

Request Body:
{
  "roleIdsToAdd": ["65ca6853-2e06-448f-bad0-bed2995c202d"],
  "roleIdsToRemove": ["468bdbcc-ae15-47f6-a529-4c104f589010"],
  "privilegeIdsToAdd": ["a3b5c7d9-1234-5678-9abc-def012345678"],
  "privilegeIdsToRemove": ["b4c6d8e0-2345-6789-abcd-ef0123456789"]
}

Response: 200 OK
{
  "message": "Group roles and privileges updated successfully"
}

Notes:
- All four arrays are required but can be empty
- roleIdsToAdd/roleIdsToRemove: Manage role assignments
- privilegeIdsToAdd/privilegeIdsToRemove: Manage privilege assignments
- Invalid role or privilege IDs will throw 404 error
```

---

## User API Endpoints

### Base Path: `/api/users`

#### 1. Create User
**Important**: Use role IDs and group IDs (UUIDs), not names. Username is auto-generated from email.

```http
POST /api/users
Content-Type: application/json
Authorization: Bearer {admin-token}

Request Body:
{
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "password": "SecurePassword123!",
  "enabled": true,
  "emailVerified": false,
  "entityCode": "ENT001",
  "countryCode": "US",
  "roleIds": ["65ca6853-2e06-448f-bad0-bed2995c202d"],
  "groupIds": ["f8e7d6c5-4321-0987-bcde-f01234567890"]
}

Response: 201 Created
{
  "message": "User created successfully",
  "user": {
    "id": "0580fded-cb94-4304-9be1-7cc54b806b92",
    "username": "john.doe@example.com",
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "enabled": true,
    "emailVerified": false,
    "createdTimestamp": 1701780000000,
    "attributes": {
      "entity_code": ["ENT001"],
      "country_code": ["US"]
    },
    "roles": [
      {
        "roleId": "65ca6853-2e06-448f-bad0-bed2995c202d",
        "roleName": "role_developer",
        "roleDisplayName": "developer"
      }
    ],
    "groups": [
      {
        "groupId": "f8e7d6c5-4321-0987-bcde-f01234567890",
        "groupName": "Engineering Team"
      }
    ]
  }
}

Notes:
- email is required and must be valid
- password is required (minimum requirements apply)
- username is auto-generated from email
- enabled defaults to true if not provided
- emailVerified defaults to false if not provided
- entityCode and countryCode are optional custom attributes
- roleIds and groupIds are optional arrays (can be empty or null)
- roleIds must be valid role IDs (get from GET /api/roles)
- groupIds must be valid group IDs (get from GET /api/groups)
```

#### 2. Delete User
```http
DELETE /api/users/{userId}
Authorization: Bearer {admin-token}

Example:
DELETE /api/users/0580fded-cb94-4304-9be1-7cc54b806b92

Response: 200 OK
{
  "message": "User deleted successfully"
}

Error Response: 404 Not Found
{
  "error": "User Not Found",
  "message": "User with ID '0580fded-cb94-4304-9be1-7cc54b806b92' not found",
  "status": 404,
  "timestamp": "2025-12-12T14:30:00"
}
```

#### 3. Get All Users
```http
GET /api/users
Authorization: Bearer {admin-token}

Response: 200 OK
[
  {
    "id": "0580fded-cb94-4304-9be1-7cc54b806b92",
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
        "roleId": "65ca6853-2e06-448f-bad0-bed2995c202d",
        "roleName": "role_developer",
        "roleDisplayName": "developer"
      },
      {
        "roleId": "468bdbcc-ae15-47f6-a529-4c104f589010",
        "roleName": "role_tester",
        "roleDisplayName": "tester"
      }
    ],
    "groups": [
      {
        "groupId": "f8e7d6c5-4321-0987-bcde-f01234567890",
        "groupName": "Engineering Team"
      }
    ]
  }
]

Notes:
- Returns all users with complete details
- roles array includes roleId, roleName, and roleDisplayName
- groups array includes groupId and groupName
- attributes is a map where values are arrays
```

#### 4. Get User by ID
```http
GET /api/users/{userId}
Authorization: Bearer {admin-token}

Example:
GET /api/users/0580fded-cb94-4304-9be1-7cc54b806b92

Response: 200 OK
{
  "id": "0580fded-cb94-4304-9be1-7cc54b806b92",
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
      "roleId": "65ca6853-2e06-448f-bad0-bed2995c202d",
      "roleName": "role_developer",
      "roleDisplayName": "developer"
    }
  ],
  "groups": [
    {
      "groupId": "f8e7d6c5-4321-0987-bcde-f01234567890",
      "groupName": "Engineering Team"
    }
  ]
}

Error Response: 404 Not Found
{
  "error": "User Not Found",
  "message": "User with ID '0580fded-cb94-4304-9be1-7cc54b806b92' not found",
  "status": 404,
  "timestamp": "2025-12-12T14:30:00"
}
```

#### 5. Update User
**Important**: Use role IDs and group IDs (UUIDs), not names.

```http
PUT /api/users/{userId}
Content-Type: application/json
Authorization: Bearer {admin-token}

Example:
PUT /api/users/0580fded-cb94-4304-9be1-7cc54b806b92

Request Body:
{
  "email": "john.new@example.com",
  "firstName": "John",
  "lastName": "Doe Updated",
  "enabled": true,
  "emailVerified": true,
  "entityCode": "ENT002",
  "countryCode": "UK",
  "roleIdsToAdd": ["468bdbcc-ae15-47f6-a529-4c104f589010"],
  "roleIdsToRemove": ["65ca6853-2e06-448f-bad0-bed2995c202d"],
  "groupIdsToAdd": ["e7d6c5b4-3210-9876-abcd-e01234567890"],
  "groupIdsToRemove": ["f8e7d6c5-4321-0987-bcde-f01234567890"]
}

Response: 200 OK
{
  "message": "User updated successfully",
  "user": {
    "id": "0580fded-cb94-4304-9be1-7cc54b806b92",
    "username": "john.new@example.com",
    "email": "john.new@example.com",
    "firstName": "John",
    "lastName": "Doe Updated",
    "enabled": true,
    "emailVerified": true,
    "createdTimestamp": 1701780000000,
    "attributes": {
      "entity_code": ["ENT002"],
      "country_code": ["UK"]
    },
    "roles": [
      {
        "roleId": "468bdbcc-ae15-47f6-a529-4c104f589010",
        "roleName": "role_tester",
        "roleDisplayName": "tester"
      }
    ],
    "groups": [
      {
        "groupId": "e7d6c5b4-3210-9876-abcd-e01234567890",
        "groupName": "Sales Team"
      }
    ]
  }
}

Notes:
- email, firstName, and lastName are required
- enabled and emailVerified are optional
- entityCode and countryCode are optional
- roleIdsToAdd, roleIdsToRemove, groupIdsToAdd, groupIdsToRemove are optional
- All ID arrays can be empty but not null if provided
- Username is automatically updated when email changes
- Invalid role or group IDs will throw 404 error
```

---

## Error Responses

All endpoints follow a consistent error response format:

```json
{
  "error": "Error Type",
  "message": "Detailed error message",
  "status": 400,
  "timestamp": "2025-12-12T14:30:00"
}
```

### Common Error Codes
- **400 Bad Request**: Invalid input or validation failure
- **401 Unauthorized**: Missing or invalid authentication token
- **403 Forbidden**: Insufficient permissions
- **404 Not Found**: Resource not found (Role, Group, User, or Privilege)
- **409 Conflict**: Resource conflict (e.g., role in use, duplicate resource)
- **500 Internal Server Error**: Server error

### Specific Error Types

#### 1. Role Not Found
```json
{
  "error": "Role Not Found",
  "message": "Role with ID '65ca6853-2e06-448f-bad0-bed2995c202d' not found",
  "status": 404,
  "timestamp": "2025-12-12T14:30:00"
}
```

#### 2. Privilege Not Found
```json
{
  "error": "Privilege Not Found",
  "message": "Privilege with ID 'a3b5c7d9-1234-5678-9abc-def012345678' not found",
  "status": 404,
  "timestamp": "2025-12-12T14:30:00"
}
```

#### 3. Group Not Found
```json
{
  "error": "Group Not Found",
  "message": "Group with ID 'f8e7d6c5-4321-0987-bcde-f01234567890' not found",
  "status": 404,
  "timestamp": "2025-12-12T14:30:00"
}
```

#### 4. User Not Found
```json
{
  "error": "User Not Found",
  "message": "User with ID '0580fded-cb94-4304-9be1-7cc54b806b92' not found",
  "status": 404,
  "timestamp": "2025-12-12T14:30:00"
}
```

#### 5. Role In Use (Cannot Delete)
```json
{
  "error": "Role In Use",
  "message": "Cannot delete role 'role_manager'. It is currently assigned to 5 user(s). Please remove the role from all users first.",
  "status": 409,
  "timestamp": "2025-12-12T14:30:00"
}
```

#### 6. Invalid Operation (Privilege Modification)
```json
{
  "error": "Invalid Operation",
  "message": "Cannot delete privilege 'priv_user_management'. Privileges are pre-defined and cannot be deleted.",
  "status": 400,
  "timestamp": "2025-12-12T14:30:00"
}
```

#### 7. Validation Errors
```json
{
  "error": "Validation Failed",
  "message": "Please check the input fields",
  "fieldErrors": {
    "roleName": "Role name is required",
    "email": "Email must be valid"
  },
  "status": 400,
  "timestamp": "2025-12-12T14:30:00"
}
```

---

## Security

All endpoints require:
1. Valid JWT token in Authorization header
2. ROLE_ADMIN role assigned to the authenticated user

Example:
```
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

## Integration Notes for React Frontend

### Critical Frontend Integration Points

#### 1. Use IDs, Not Names
**IMPORTANT**: All API endpoints use resource IDs (UUIDs), not names.

```javascript
// ❌ WRONG - Using role names
const request = {
  roleNames: ["role_admin", "role_manager"]
};

// ✅ CORRECT - Using role IDs
const request = {
  roleIds: ["65ca6853-2e06-448f-bad0-bed2995c202d", "468bdbcc-ae15-47f6-a529-4c104f589010"]
};
```

#### 2. Display Names for UI
Use the `displayName` fields (without prefixes) for UI display:

```javascript
// Role object from API
const role = {
  id: "65ca6853-2e06-448f-bad0-bed2995c202d",
  name: "role_admin",
  displayName: "admin"  // ✅ Use this for display
};

// Privilege object from API
const privilege = {
  id: "a3b5c7d9-1234-5678-9abc-def012345678",
  name: "priv_user_management",
  displayName: "user_management"  // ✅ Use this for display
};
```

#### 3. Group User Count
Groups always include a `userCount` field:

```javascript
const group = {
  id: "f8e7d6c5-4321-0987-bcde-f01234567890",
  name: "Engineering Team",
  userCount: 15  // ✅ Display this to show group size
};
```

#### 4. User Roles and Groups Structure
Users have nested role and group information:

```javascript
const user = {
  id: "0580fded-cb94-4304-9be1-7cc54b806b92",
  email: "john.doe@example.com",
  roles: [
    {
      roleId: "65ca6853-2e06-448f-bad0-bed2995c202d",  // ✅ Use for API calls
      roleName: "role_developer",
      roleDisplayName: "developer"  // ✅ Use for display
    }
  ],
  groups: [
    {
      groupId: "f8e7d6c5-4321-0987-bcde-f01234567890",  // ✅ Use for API calls
      groupName: "Engineering Team"  // ✅ Use for display
    }
  ]
};
```

### Example Frontend Workflows

#### Workflow 1: Create Role with Privileges
```javascript
// Step 1: Fetch all privileges
const privilegesResponse = await fetch('http://localhost:8090/api/roles/privileges', {
  headers: { 'Authorization': `Bearer ${token}` }
});
const privileges = await privilegesResponse.json();

// Step 2: Let user select privileges (display using displayName)
// User selects: "user_management" and "view_reports"

// Step 3: Extract IDs from selected privileges
const selectedPrivilegeIds = selectedPrivileges.map(p => p.id);

// Step 4: Create role with privilege IDs
const createRoleResponse = await fetch('http://localhost:8090/api/roles', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  },
  body: JSON.stringify({
    roleName: "manager",  // Will become "role_manager"
    description: "Manager role",
    privilegeIds: selectedPrivilegeIds  // ✅ Use IDs
  })
});
```

#### Workflow 2: Update User Roles
```javascript
// Step 1: Fetch all roles
const rolesResponse = await fetch('http://localhost:8090/api/roles', {
  headers: { 'Authorization': `Bearer ${token}` }
});
const roles = await rolesResponse.json();

// Step 2: Display roles using displayName, track selected using id
const selectedRoleIds = selectedRoles.map(r => r.id);

// Step 3: Update user
const updateUserResponse = await fetch(`http://localhost:8090/api/users/${userId}`, {
  method: 'PUT',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  },
  body: JSON.stringify({
    email: user.email,
    firstName: user.firstName,
    lastName: user.lastName,
    roleIdsToAdd: newRoleIds,  // ✅ Use IDs
    roleIdsToRemove: removedRoleIds  // ✅ Use IDs
  })
});
```

#### Workflow 3: Display Group with User Count
```javascript
// Fetch groups
const groupsResponse = await fetch('http://localhost:8090/api/groups', {
  headers: { 'Authorization': `Bearer ${token}` }
});
const groups = await groupsResponse.json();

// Display in UI
groups.map(group => (
  <div key={group.id}>
    <h3>{group.name}</h3>
    <span>Members: {group.userCount}</span>  {/* ✅ Show user count */}
  </div>
));
```

### Error Handling Best Practices

```javascript
const apiCall = async (url, options) => {
  try {
    const response = await fetch(url, options);
    
    if (!response.ok) {
      const error = await response.json();
      
      // Handle specific error types
      switch (error.status) {
        case 404:
          if (error.error === "Role Not Found") {
            showNotification("Role not found. It may have been deleted.");
          } else if (error.error === "Privilege Not Found") {
            showNotification("Invalid privilege selected. Please refresh and try again.");
          }
          break;
        case 409:
          if (error.error === "Role In Use") {
            showNotification("Cannot delete role: It is assigned to users.");
          }
          break;
        case 400:
          if (error.fieldErrors) {
            // Show validation errors
            Object.entries(error.fieldErrors).forEach(([field, msg]) => {
              showFieldError(field, msg);
            });
          }
          break;
        default:
          showNotification(error.message);
      }
      
      throw error;
    }
    
    return await response.json();
  } catch (error) {
    console.error('API call failed:', error);
    throw error;
  }
};
```

### Required Request Body Fields Summary

| Endpoint | Required Fields | Optional Fields |
|----------|----------------|-----------------|
| POST /api/roles | roleName | description, privilegeIds |
| PUT /api/roles/{roleId} | - | description, privilegeIdsToAdd, privilegeIdsToRemove |
| POST /api/groups | groupName | roleIds, privilegeIds |
| PUT /api/groups/{groupId}/users | userIdsToAdd, userIdsToRemove | - |
| PUT /api/groups/{groupId}/roles-privileges | roleIdsToAdd, roleIdsToRemove, privilegeIdsToAdd, privilegeIdsToRemove | - |
| POST /api/users | email, firstName, lastName, password | enabled, emailVerified, entityCode, countryCode, roleIds, groupIds |
| PUT /api/users/{userId} | email, firstName, lastName | enabled, emailVerified, entityCode, countryCode, roleIdsToAdd, roleIdsToRemove, groupIdsToAdd, groupIdsToRemove |

### Testing Checklist for Frontend

- [ ] All API calls use IDs (UUIDs) not names
- [ ] Display names shown to users without prefixes
- [ ] Group user count displayed correctly
- [ ] User roles show as nested objects with roleId, roleName, roleDisplayName
- [ ] User groups show as nested objects with groupId, groupName
- [ ] Error messages from API displayed appropriately
- [ ] 404 errors handled (role not found, etc.)
- [ ] 409 errors handled (role in use)
- [ ] Validation errors shown for specific fields
- [ ] Loading states during API calls
- [ ] Confirmation dialogs before delete operations

