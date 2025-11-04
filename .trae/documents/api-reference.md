# Shared Services API Reference

This document provides descriptions, HTTP statuses, sample requests, and sample responses for all controller endpoints. All responses use the standardized `ApiResponse<T>` wrapper.

## Conventions
- Base URL: `http://localhost:8080` (dev) or the configured server port.
- Content-Type: `application/json`
- Authentication: JWT Bearer when security is enabled.
- Response wrapper:
  ```json
  {
    "success": true,
    "data": {},
    "message": "Meaningful message",
    "timestamp": "2025-11-04T12:00:00Z",
    "path": "/api/v1/..."
  }
  ```

---

## Authentication

### POST `/api/v1/auth/login`
- Description: Authenticates a user and returns a JWT token.
- Statuses:
  - 200 OK: Successful login
  - 400 BAD REQUEST: Invalid credentials, account locked, or inactive
  - 500 INTERNAL SERVER ERROR: Unexpected error
- Sample Request:
  ```json
  { "username": "admin", "password": "secret" }
  ```
- Sample Response (Success):
  ```json
  {
    "success": true,
    "data": { "token": "<jwt-token>" },
    "message": "Login successful",
    "timestamp": "2025-11-04T12:00:00Z",
    "path": "/api/v1/auth/login"
  }
  ```
- Sample Response (Error):
  ```json
  {
    "success": false,
    "data": null,
    "message": "Invalid username or password",
    "timestamp": "2025-11-04T12:00:00Z",
    "path": "/api/v1/auth/login"
  }
  ```

---

## Users

Base Path: `/api/v1/users`

### GET `/`
- Description: Get all active users (list).
- Statuses: 200 OK
- Sample Response:
  ```json
  {
    "success": true,
    "data": [
      { "id": 1, "username": "admin", "email": "admin@example.com", "userStatus": "ACTIVE" }
    ],
    "message": "Users retrieved successfully",
    "timestamp": "2025-11-04T12:00:00Z",
    "path": "/api/v1/users"
  }
  ```

### POST `/`
- Description: Create a new user.
- Statuses: 201 CREATED, 400 BAD REQUEST
- Sample Request:
  ```json
  {
    "username": "jane",
    "email": "jane@example.com",
    "firstName": "Jane",
    "lastName": "Doe",
    "password": "P@ssw0rd!",
    "roleIds": [1, 2],
    "userGroupIds": [10]
  }
  ```
- Sample Response:
  ```json
  {
    "success": true,
    "data": { "id": 99, "username": "jane", "email": "jane@example.com", "userStatus": "ACTIVE" },
    "message": "User created successfully",
    "timestamp": "2025-11-04T12:00:00Z",
    "path": "/api/v1/users"
  }
  ```

### GET `/{id}`
- Description: Get user by ID.
- Statuses: 200 OK, 404 NOT FOUND
- Sample Response (404):
  ```json
  { "success": false, "data": null, "message": "User not found", "timestamp": "...", "path": "/api/v1/users/999" }
  ```

### PUT `/{id}`
- Description: Update user.
- Statuses: 200 OK, 400 BAD REQUEST, 404 NOT FOUND
- Sample Request:
  ```json
  { "email": "new@example.com", "firstName": "New", "lastName": "Name", "roleIds": [1], "userGroupIds": [10] }
  ```

### DELETE `/{id}`
- Description: Soft delete user.
- Statuses: 200 OK, 404 NOT FOUND

### PATCH `/{id}/activate`
- Description: Activate user.
- Statuses: 200 OK, 404 NOT FOUND

### PATCH `/{id}/deactivate`
- Description: Deactivate user.
- Statuses: 200 OK, 404 NOT FOUND

### PATCH `/{id}/change-password`
- Description: Change user password.
- Statuses: 200 OK, 400 BAD REQUEST, 404 NOT FOUND
- Sample Request:
  ```json
  { "oldPassword": "old", "newPassword": "newStrongPassword" }
  ```

### GET `/{id}/roles`
- Description: Get user roles.
- Statuses: 200 OK, 404 NOT FOUND

### POST `/{id}/roles`
- Description: Assign roles to user.
- Statuses: 200 OK, 400 BAD REQUEST, 404 NOT FOUND
- Sample Request:
  ```json
  [1, 2, 3]
  ```

### DELETE `/{id}/roles/{roleId}`
- Description: Remove role from user.
- Statuses: 200 OK, 404 NOT FOUND

---

## User Groups

Base Path: `/api/v1/user-groups`

### GET `/`
- Description: Get paginated user groups.
- Query: `page`, `size`
- Statuses: 200 OK

### POST `/`
- Description: Create user group.
- Statuses: 201 CREATED, 400 BAD REQUEST
- Sample Request:
  ```json
  { "name": "Operations", "description": "Ops group" }
  ```
- Sample Response:
  ```json
  { "success": true, "data": { "id": 10, "name": "Operations", "description": "Ops group" }, "message": "Permission group created successfully", "timestamp": "...", "path": "/api/v1/user-groups" }
  ```

### GET `/{id}` / PUT `/{id}` / DELETE `/{id}`
- Description: Get, update, delete user group.
- Statuses:
  - 200 OK (GET/PUT)
  - 204 NO CONTENT (DELETE success) or 200 OK with message
  - 400 BAD REQUEST, 404 NOT FOUND

### PATCH `/{id}/activate` / `/{id}/deactivate`
- Description: Activate/deactivate group.
- Statuses: 200 OK, 404 NOT FOUND

### GET/POST/DELETE `/{id}/members`
- Description: Manage group members.
- Statuses: 200 OK, 201 CREATED, 404 NOT FOUND

### GET/POST/DELETE `/{id}/roles`
- Description: Manage group roles.
- Statuses: 200 OK, 201 CREATED, 404 NOT FOUND

---

## Products

Base Path: `/api/v1/products`

### GET `/`
- Description: Get all active products.
- Statuses: 200 OK

### POST `/`
- Description: Create product.
- Statuses: 201 CREATED, 400 BAD REQUEST
- Sample Request:
  ```json
  { "name": "Product Management", "code": "PRODUCT_MGMT", "description": "Manage products" }
  ```

### GET/PUT/DELETE `/{id}`
- Description: Get/update/delete product.
- Statuses: 200 OK, 201/200, 404 NOT FOUND

### PATCH `/{id}/activate` / `/{id}/deactivate`
- Description: Activate/deactivate product.
- Statuses: 200 OK, 404 NOT FOUND

---

## Modules

Base Path: `/api/v1/modules`

### GET `/`
- Description: Get all active modules.
- Statuses: 200 OK

### POST `/`
- Description: Create module.
- Statuses: 201 CREATED, 400 BAD REQUEST
- Sample Request:
  ```json
  { "productId": 1, "name": "Module Management", "code": "MODULE_MGMT", "description": "Manage modules" }
  ```

### GET/PUT/DELETE `/{id}`
- Description: Get/update/delete module.
- Statuses: 200 OK, 201/200, 404 NOT FOUND

### PATCH `/{id}/activate` / `/{id}/deactivate`
- Description: Activate/deactivate module.
- Statuses: 200 OK, 404 NOT FOUND

---

## Roles

Base Path: `/api/v1/roles`

### GET `/`
- Description: Get all active roles.
- Statuses: 200 OK

### POST `/`
- Description: Create role.
- Statuses: 201 CREATED, 400 BAD REQUEST
- Sample Request:
  ```json
  { "moduleId": 2, "name": "Admin", "description": "Module admin" }
  ```

### GET/PUT/DELETE `/{id}`
- Description: Get/update/delete role.
- Statuses: 200 OK, 201/200, 404 NOT FOUND

### PATCH `/{id}/activate` / `/{id}/deactivate`
- Description: Activate/deactivate role.
- Statuses: 200 OK, 404 NOT FOUND

---

## Permissions

Base Path: `/api/v1/permissions`

### GET `/`
- Description: Get all active permissions.
- Statuses: 200 OK

### POST `/`
- Description: Create permission.
- Statuses: 201 CREATED, 400 BAD REQUEST
- Sample Request:
  ```json
  { "resourceType": "MODULE_MGMT", "action": "read", "name": "MODULE_MGMT:read" }
  ```

### GET/PUT/DELETE `/{id}`
- Description: Get/update/delete permission.
- Statuses: 200 OK, 201/200, 404 NOT FOUND

### PATCH `/{id}/activate` / `/{id}/deactivate`
- Description: Activate/deactivate permission.
- Statuses: 200 OK, 404 NOT FOUND

---

## Tenants

Base Path: `/api/v1/tenants`

### GET `/`
- Description: Get tenants.
- Statuses: 200 OK

### GET `/search?query=...`
- Description: Search tenants by query string.
- Statuses: 200 OK

### POST `/`
- Description: Create tenant.
- Statuses: 201 CREATED, 400 BAD REQUEST
- Sample Request:
  ```json
  { "tenantCode": "ACME", "name": "Acme Inc.", "tenantStatus": "ACTIVE", "tenantType": "ENTERPRISE" }
  ```

### GET/PUT/DELETE `/{id}`
- Description: Get/update/delete tenant.
- Statuses: 200 OK, 201/200, 404 NOT FOUND

### PATCH `/{id}/activate` / `/{id}/deactivate`
- Description: Activate/deactivate tenant.
- Statuses: 200 OK, 404 NOT FOUND

---

## Dashboard

Base Path: `/api/v1/dashboard`

### GET `/stats`
- Description: Get total counts and active entities.
- Statuses: 200 OK
- Sample Response:
  ```json
  { "success": true, "data": { "users": 120, "tenants": 12 }, "message": "Stats", "timestamp": "...", "path": "/api/v1/dashboard/stats" }
  ```

### GET `/recent-activities`
- Description: Get recent changes in the system.
- Statuses: 200 OK
- Sample Response:
  ```json
  { "success": true, "data": [{ "type": "USER_CREATED", "entityId": 99 }], "message": "Recent activities", "timestamp": "...", "path": "/api/v1/dashboard/recent-activities" }
  ```

---

## HTTP Status Mapping
- 200 OK: Successful GET/PUT/PATCH/DELETE operations
- 201 CREATED: Successful POST operations
- 204 NO CONTENT: Optional for delete actions where applicable
- 400 BAD REQUEST: Validation errors, illegal arguments
- 404 NOT FOUND: Resource does not exist
- 500 INTERNAL SERVER ERROR: Unexpected server errors

## Notes
- Use JWT bearer auth when security is enabled; Swagger UI’s “Authorize” button supports it.
- Some IDs for payments use UUID, as described in the payment UUID guide.
- Permissions naming follows `{module.code}:{action}` per database schema refactoring documentation.

