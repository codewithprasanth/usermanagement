# Frontend Integration Quick Reference

## 🚨 Critical: Use IDs, Not Names!

### Path Parameters - Always Use UUIDs

```javascript
// ❌ WRONG
DELETE /api/roles/role_manager
PUT /api/roles/role_manager
GET /api/roles/role_manager/privileges

// ✅ CORRECT
DELETE /api/roles/65ca6853-2e06-448f-bad0-bed2995c202d
PUT /api/roles/65ca6853-2e06-448f-bad0-bed2995c202d
GET /api/roles/65ca6853-2e06-448f-bad0-bed2995c202d/privileges
```

---

## Request Body Field Names

### Creating a Role

```javascript
// ❌ WRONG
{
  "roleName": "manager",
  "privilegeNames": ["priv_user_management"]
}

// ✅ CORRECT
{
  "roleName": "manager",
  "privilegeIds": ["a3b5c7d9-1234-5678-9abc-def012345678"]
}
```

### Updating a Role

```javascript
// ❌ WRONG
{
  "privilegesToAdd": ["priv_new"],
  "privilegesToRemove": ["priv_old"]
}

// ✅ CORRECT
{
  "privilegeIdsToAdd": ["uuid1"],
  "privilegeIdsToRemove": ["uuid2"]
}
```

### Creating a Group

```javascript
// ❌ WRONG
{
  "groupName": "Engineering",
  "roleNames": ["role_developer"],
  "privilegeNames": ["priv_code_review"]
}

// ✅ CORRECT
{
  "groupName": "Engineering",
  "roleIds": ["65ca6853-2e06-448f-bad0-bed2995c202d"],
  "privilegeIds": ["a3b5c7d9-1234-5678-9abc-def012345678"]
}
```

### Updating Group Roles/Privileges

```javascript
// ❌ WRONG
{
  "rolesToAdd": ["role_new"],
  "privilegesToAdd": ["priv_new"]
}

// ✅ CORRECT
{
  "roleIdsToAdd": ["uuid1"],
  "roleIdsToRemove": ["uuid2"],
  "privilegeIdsToAdd": ["uuid3"],
  "privilegeIdsToRemove": ["uuid4"]
}
```

### Creating a User

```javascript
// ❌ WRONG
{
  "email": "john@example.com",
  "roleNames": ["role_developer"],
  "groupIds": ["group-uuid"]
}

// ✅ CORRECT
{
  "email": "john@example.com",
  "roleIds": ["65ca6853-2e06-448f-bad0-bed2995c202d"],
  "groupIds": ["f8e7d6c5-4321-0987-bcde-f01234567890"]
}
```

### Updating a User

```javascript
// ❌ WRONG
{
  "roleNamesToAdd": ["role_senior"],
  "roleNamesToRemove": ["role_junior"]
}

// ✅ CORRECT
{
  "roleIdsToAdd": ["uuid1"],
  "roleIdsToRemove": ["uuid2"],
  "groupIdsToAdd": ["uuid3"],
  "groupIdsToRemove": ["uuid4"]
}
```

---

## Response Structures

### Role Response

```javascript
{
  "id": "65ca6853-2e06-448f-bad0-bed2995c202d",  // ✅ Use for API calls
  "name": "role_manager",
  "displayName": "manager",  // ✅ Use for UI display
  "description": "Manager role",
  "composite": true
}
```

### Privilege Response

```javascript
{
  "id": "a3b5c7d9-1234-5678-9abc-def012345678",  // ✅ Use for API calls
  "name": "priv_user_management",
  "displayName": "user_management",  // ✅ Use for UI display
  "description": "User management privilege"
}
```

### Group Response

```javascript
{
  "id": "f8e7d6c5-4321-0987-bcde-f01234567890",
  "name": "Engineering Team",
  "userCount": 15  // ✅ Display this!
}
```

### User Response

```javascript
{
  "id": "0580fded-cb94-4304-9be1-7cc54b806b92",
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "roles": [
    {
      "roleId": "65ca6853-2e06-448f-bad0-bed2995c202d",  // ✅ Use for API
      "roleName": "role_developer",
      "roleDisplayName": "developer"  // ✅ Use for display
    }
  ],
  "groups": [
    {
      "groupId": "f8e7d6c5-4321-0987-bcde-f01234567890",  // ✅ Use for API
      "groupName": "Engineering Team"  // ✅ Use for display
    }
  ]
}
```

---

## Common Frontend Patterns

### Pattern 1: Fetch and Display Roles

```javascript
// Fetch roles
const roles = await fetch('/api/roles', {
  headers: { 'Authorization': `Bearer ${token}` }
}).then(r => r.json());

// Display using displayName
roles.map(role => (
  <option key={role.id} value={role.id}>
    {role.displayName}  {/* Show "manager" not "role_manager" */}
  </option>
));
```

### Pattern 2: Create Role with Selected Privileges

```javascript
// 1. Fetch all privileges
const privileges = await fetch('/api/roles/privileges', {
  headers: { 'Authorization': `Bearer ${token}` }
}).then(r => r.json());

// 2. User selects privileges (track by id)
const selectedPrivilegeIds = selectedPrivileges.map(p => p.id);

// 3. Create role with privilege IDs
await fetch('/api/roles', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  },
  body: JSON.stringify({
    roleName: "manager",
    description: "Manager role",
    privilegeIds: selectedPrivilegeIds  // ✅ IDs not names
  })
});
```

### Pattern 3: Display Groups with User Count

```javascript
const groups = await fetch('/api/groups', {
  headers: { 'Authorization': `Bearer ${token}` }
}).then(r => r.json());

groups.map(group => (
  <div key={group.id}>
    <h3>{group.name}</h3>
    <span>{group.userCount} members</span>  {/* ✅ Show count */}
  </div>
));
```

### Pattern 4: Update User Roles

```javascript
// Current user roles (from GET /api/users/{userId})
const currentRoleIds = user.roles.map(r => r.roleId);

// New selected role IDs (from role picker)
const newRoleIds = ["uuid1", "uuid2", "uuid3"];

// Calculate additions and removals
const roleIdsToAdd = newRoleIds.filter(id => !currentRoleIds.includes(id));
const roleIdsToRemove = currentRoleIds.filter(id => !newRoleIds.includes(id));

// Update user
await fetch(`/api/users/${userId}`, {
  method: 'PUT',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  },
  body: JSON.stringify({
    email: user.email,
    firstName: user.firstName,
    lastName: user.lastName,
    roleIdsToAdd,    // ✅ IDs not names
    roleIdsToRemove  // ✅ IDs not names
  })
});
```

---

## Error Handling

### Check for Specific Error Types

```javascript
try {
  const response = await fetch(url, options);
  
  if (!response.ok) {
    const error = await response.json();
    
    // Check error.error field for specific types
    if (error.error === "Role Not Found") {
      alert("Role not found. It may have been deleted.");
    } else if (error.error === "Privilege Not Found") {
      alert("Invalid privilege selected. Please refresh.");
    } else if (error.error === "Role In Use") {
      alert("Cannot delete: Role is assigned to users.");
    }
  }
} catch (err) {
  console.error("API Error:", err);
}
```

---

## Testing Checklist

Before deploying frontend changes:

- [ ] All role/privilege selections use IDs internally
- [ ] Display names shown to users (without prefixes)
- [ ] API paths use UUIDs not names
- [ ] Request bodies use correct field names (`roleIds` not `roleNames`)
- [ ] Group user count displayed correctly
- [ ] User roles shown with roleId, roleName, roleDisplayName
- [ ] User groups shown with groupId, groupName
- [ ] Error messages handled appropriately
- [ ] "Role not found" errors resolved (using IDs now)
- [ ] Privilege validation errors resolved (using IDs now)

---

## Quick Lookup: Field Name Mapping

| Old (WRONG) | New (CORRECT) |
|-------------|---------------|
| `privilegeNames` | `privilegeIds` |
| `roleNames` | `roleIds` |
| `privilegesToAdd` | `privilegeIdsToAdd` |
| `privilegesToRemove` | `privilegeIdsToRemove` |
| `rolesToAdd` | `roleIdsToAdd` |
| `rolesToRemove` | `roleIdsToRemove` |
| `roleNamesToAdd` | `roleIdsToAdd` |
| `roleNamesToRemove` | `roleIdsToRemove` |

---

## Base URL

```
http://localhost:8090
```

## Common Endpoints

```
GET    /api/roles
POST   /api/roles
DELETE /api/roles/{roleId}
PUT    /api/roles/{roleId}
GET    /api/roles/{roleId}/privileges
GET    /api/roles/privileges

GET    /api/groups
POST   /api/groups
DELETE /api/groups/{groupId}
GET    /api/groups/{groupId}/roles-privileges
GET    /api/groups/{groupId}/users
PUT    /api/groups/{groupId}/users
PUT    /api/groups/{groupId}/roles-privileges

GET    /api/users
POST   /api/users
GET    /api/users/{userId}
DELETE /api/users/{userId}
PUT    /api/users/{userId}
```

---

## Need Help?

1. Check `API_DOCUMENTATION.md` for complete details
2. Check `API_DOCUMENTATION_FIXES_SUMMARY.md` for all changes
3. Review application logs for detailed error messages
4. Verify you're using UUIDs (36-character strings with hyphens)

