# 📚 API Documentation Index

## Overview
This index provides quick access to all API documentation files for the User Management System.

**Last Updated:** December 8, 2025  
**Status:** ✅ Fully Updated and Accurate

---

## 📖 Documentation Files

### 1. **FRONTEND_API_DOCUMENTATION.md** (PRIMARY)
**Purpose:** Complete API reference for frontend integration  
**Status:** ✅ Fully updated and accurate  
**Use For:**
- Complete API endpoint reference
- Request/response format examples
- Data model definitions
- Error handling guide
- Frontend integration examples (React/TypeScript)

**Start Here:** This is the main documentation file you should reference.

---

### 2. **CRITICAL_API_CHANGES.md**
**Purpose:** Before/after comparison of critical fixes  
**Status:** ✅ New document  
**Use For:**
- Quick migration guide
- Understanding what changed and why
- Code examples showing incorrect vs correct usage
- Quick reference checklist

**Start Here If:** You're migrating existing frontend code.

---

### 3. **API_DOCUMENTATION_UPDATES_SUMMARY.md**
**Purpose:** Detailed explanation of all changes  
**Status:** ✅ New document  
**Use For:**
- Understanding the complete scope of changes
- Technical details of each fix
- Testing checklist
- Migration guide with step-by-step instructions

**Start Here If:** You need to understand the technical details of what was fixed.

---

### 4. **API_DOCUMENTATION.md** (LEGACY)
**Purpose:** Original backend API documentation  
**Status:** ⚠️ May contain outdated information  
**Use For:**
- Historical reference only

**Note:** Use FRONTEND_API_DOCUMENTATION.md instead.

---

### 5. **Keycloak-UserManagement-Complete-Final-Dec2025.postman_collection.json**
**Purpose:** Postman collection for API testing  
**Status:** ✅ Up to date  
**Use For:**
- Testing API endpoints
- Generating sample requests
- Validating responses

---

## 🚀 Quick Start Guide

### For New Frontend Developers

1. Read **CRITICAL_API_CHANGES.md** (5 min) - Understand the key points
2. Reference **FRONTEND_API_DOCUMENTATION.md** (ongoing) - Your main reference
3. Import Postman collection for testing

### For Existing Frontend Developers (Migration)

1. Read **CRITICAL_API_CHANGES.md** (10 min) - See what changed
2. Follow migration checklist in **API_DOCUMENTATION_UPDATES_SUMMARY.md** (30 min)
3. Test changes against **FRONTEND_API_DOCUMENTATION.md** (ongoing)
4. Use Postman collection to verify new formats

---

## ⚠️ Most Critical Changes (Summary)

### 1. GroupDTO Has No `path` Field
```typescript
// ❌ WRONG
interface GroupDTO { id: string; name: string; path: string; }

// ✅ CORRECT
interface GroupDTO { id: string; name: string; }
```

### 2. All API Paths Use UUIDs
```
❌ DELETE /api/roles/role_admin
✅ DELETE /api/roles/650e8400-e29b-41d4-a716-446655440001
```

### 3. All Request Bodies Use IDs
```json
❌ { "roleNames": ["role_admin"] }
✅ { "roleIds": ["650e8400-e29b-41d4-a716-446655440001"] }
```

### 4. No Username Field
```json
❌ { "username": "john", "email": "john@example.com" }
✅ { "email": "john@example.com" }
```

### 5. Arrays Required (Not Null)
```json
❌ { "privilegeIdsToAdd": null }
✅ { "privilegeIdsToAdd": [] }
```

---

## 📋 API Endpoint Quick Reference

### Authentication
- `POST /realms/{realm}/protocol/openid-connect/token` - Get JWT token (Keycloak)

### Users
- `POST /api/users` - Create user
- `GET /api/users` - Get all users
- `GET /api/users/{userId}` - Get user by ID
- `PUT /api/users/{userId}` - Update user
- `DELETE /api/users/{userId}` - Delete user

### Roles
- `POST /api/roles` - Create role
- `GET /api/roles` - Get all roles
- `GET /api/roles/{roleId}/privileges` - Get role privileges
- `GET /api/roles/privileges` - Get all privileges
- `PUT /api/roles/{roleId}` - Update role
- `DELETE /api/roles/{roleId}` - Delete role

### Groups
- `POST /api/groups` - Create group
- `GET /api/groups` - Get all groups
- `GET /api/groups/{groupId}/roles-privileges` - Get group roles & privileges
- `GET /api/groups/{groupId}/users` - Get users in group
- `PUT /api/groups/{groupId}/users` - Update group users
- `PUT /api/groups/{groupId}/roles-privileges` - Update group roles & privileges
- `DELETE /api/groups/{groupId}` - Delete group

### Auth Endpoints (Testing)
- `GET /api/public/hello` - Public endpoint (no auth)
- `GET /api/user/profile` - Get user profile
- `GET /api/admin/dashboard` - Admin dashboard

---

## 🔍 Finding What You Need

### I need to...

**Create a new user**
→ FRONTEND_API_DOCUMENTATION.md → User Management APIs → Create User

**Update role privileges**
→ FRONTEND_API_DOCUMENTATION.md → Role Management APIs → Update Role

**Understand GroupDTO structure**
→ FRONTEND_API_DOCUMENTATION.md → Data Models → GroupDTO

**Handle API errors**
→ FRONTEND_API_DOCUMENTATION.md → Error Handling

**Migrate existing code**
→ CRITICAL_API_CHANGES.md → Complete Migration Checklist

**Understand what changed**
→ API_DOCUMENTATION_UPDATES_SUMMARY.md

**See correct TypeScript interfaces**
→ CRITICAL_API_CHANGES.md → TypeScript Interfaces

**Test with Postman**
→ Import Keycloak-UserManagement-Complete-Final-Dec2025.postman_collection.json

---

## 💡 Important Concepts

### UUID vs Name
- **UUIDs** are used in API paths and request body arrays
- **Names** are only used for display and initial creation
- Example: `"650e8400-e29b-41d4-a716-446655440001"` is a UUID

### Role vs Privilege
- **Roles** start with `role_` (e.g., `role_admin`)
- **Privileges** start with `priv_` (e.g., `priv_user_management`)
- Roles can be assigned to users
- Privileges cannot be directly assigned to users
- Privileges are assigned to roles, users inherit them

### Display Names
- Display names have prefixes removed: `role_admin` → `admin`
- Display names are lowercase: `user_management` not `User Management`
- Format them in your UI layer if needed

### Timestamps
- `createdTimestamp` in UserDTO: Unix epoch milliseconds (number)
- `timestamp` in responses: ISO 8601 format (string)

### Validation
- All IDs (roleIds, groupIds, privilegeIds) are validated BEFORE creating/updating
- Invalid IDs cause immediate 404 errors
- This prevents partial operations

---

## 🧪 Testing Your Integration

### Checklist
- [ ] All API calls use UUIDs in paths
- [ ] No `username` field in user requests
- [ ] No references to `group.path`
- [ ] All update requests send arrays (not null)
- [ ] Display names formatted correctly
- [ ] Error handling for all status codes
- [ ] Loading states for async operations
- [ ] JWT token management working

### Test Scenarios
1. Create user with roles and groups
2. Update user: add/remove roles and groups
3. Create role with privileges
4. Update role: add/remove privileges
5. Create group with roles and privileges
6. Update group users
7. Delete operations with confirmation
8. Error handling (404, 400, 409)

---

## 📞 Support & Resources

### Backend Source Code
```
src/main/java/com/sprintap/usermanagement/
├── controller/          # REST endpoints
├── dto/                # Request/response models
├── service/            # Business logic
└── exception/          # Error handling
```

### Configuration
- **Backend URL**: `http://localhost:8090`
- **Keycloak URL**: `http://localhost:8080`
- **Realm**: `sprint-ap`
- **Client ID**: `sprint-ap-backend`

### Common Issues
1. **404 Not Found** → Check you're using UUID, not name
2. **400 Bad Request** → Check for null values, should be []
3. **401 Unauthorized** → Check JWT token is valid
4. **409 Conflict** → Role is in use, cannot delete

---

## 📝 Document Versions

| Document | Version | Date | Status |
|----------|---------|------|--------|
| FRONTEND_API_DOCUMENTATION.md | 1.0 | Dec 8, 2025 | ✅ Current |
| CRITICAL_API_CHANGES.md | 1.0 | Dec 8, 2025 | ✅ Current |
| API_DOCUMENTATION_UPDATES_SUMMARY.md | 1.0 | Dec 8, 2025 | ✅ Current |
| API_DOCUMENTATION.md | - | - | ⚠️ Legacy |

---

## 🎯 Next Steps

1. **Review** CRITICAL_API_CHANGES.md for quick overview
2. **Reference** FRONTEND_API_DOCUMENTATION.md during development
3. **Test** using Postman collection
4. **Migrate** existing code using checklists
5. **Validate** all endpoints are working correctly

---

**Need help?** Check the detailed documentation files or examine the backend source code.

**Found an issue?** The backend code is the source of truth. If documentation conflicts with actual API behavior, the API is correct.

---

**Last Updated:** December 8, 2025  
**Maintained By:** Development Team  
**Priority:** Critical - Accurate documentation required for successful frontend integration

