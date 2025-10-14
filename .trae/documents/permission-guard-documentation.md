# PermissionGuard Component Documentation

## Overview

The `PermissionGuard` component is a React wrapper component that provides declarative permission-based access control for UI elements. It conditionally renders its children based on user permissions, roles, and authentication status, making it easy to implement fine-grained access control throughout the application.

## Component Location

```
frontend/src/components/PermissionGuard.tsx
```

## Core Features

- **Permission-based rendering**: Show/hide content based on specific permissions
- **Role-based access control**: Support for single or multiple role checks
- **Resource-action validation**: Fine-grained resource and action-based permissions
- **Authentication checks**: Ensure user is authenticated before rendering
- **Flexible fallback content**: Custom content when access is denied
- **Multiple validation modes**: Require all permissions or any permission

## Permission Format

The component uses the standardized permission format: `MODULE_MGMT:action`

**Available Permission Modules:**
- `USER_MGMT` - User management operations
- `ROLE_MGMT` - Role management operations
- `TENANT_MGMT` - Tenant management operations
- `GROUP_MGMT` - User group management operations
- `PRODUCT_MGMT` - Product management operations
- `PERMISSION_MGMT` - Permission management operations
- `AUDIT_MGMT` - Audit log access
- `ANALYTICS_MGMT` - Analytics and reporting
- `APPROVAL_MGMT` - Approval workflow management

**Common Actions:**
- `read` - View/read access
- `create` - Create new resources
- `update` - Modify existing resources
- `delete` - Delete resources
- `assign_roles` - Assign roles to users

## Component Interface

```typescript
interface PermissionGuardProps {
  children: ReactNode           // Content to render when access is granted
  permission?: string          // Single permission to check (e.g., "USER_MGMT:read")
  role?: string               // Single role to check (e.g., "admin")
  roles?: string[]            // Multiple roles to check
  resource?: string           // Resource type for resource-based permissions
  action?: string             // Action type for resource-based permissions
  fallback?: ReactNode        // Content to render when access is denied
  requireAll?: boolean        // Whether to require ALL roles (default: false)
}
```

## Usage Patterns

### 1. Basic Permission Check

Protect content based on a specific permission:

```tsx
<PermissionGuard permission="USER_MGMT:read">
  <UserList />
</PermissionGuard>
```

### 2. Role-Based Protection

Protect content based on user roles:

```tsx
// Single role
<PermissionGuard role="admin">
  <AdminPanel />
</PermissionGuard>

// Multiple roles (user needs ANY of these roles)
<PermissionGuard roles={["admin", "manager"]}>
  <ManagementTools />
</PermissionGuard>

// Multiple roles (user needs ALL of these roles)
<PermissionGuard roles={["admin", "super_user"]} requireAll={true}>
  <SuperAdminPanel />
</PermissionGuard>
```

### 3. Resource-Action Based Protection

For fine-grained resource and action-based permissions:

```tsx
<PermissionGuard resource="user" action="delete">
  <DeleteUserButton />
</PermissionGuard>
```

### 4. Custom Fallback Content

Provide custom content when access is denied:

```tsx
<PermissionGuard 
  permission="PRODUCT_MGMT:create"
  fallback={
    <Alert>
      <Shield className="h-4 w-4" />
      <AlertDescription>
        You don't have permission to create products.
      </AlertDescription>
    </Alert>
  }
>
  <CreateProductForm />
</PermissionGuard>
```

### 5. Protecting UI Elements

Conditionally render buttons, links, and other UI elements:

```tsx
<PermissionGuard permission="PRODUCT_MGMT:create">
  <Button asChild>
    <Link to="/products/create">
      <Plus className="mr-2 h-4 w-4" />
      Create Product
    </Link>
  </Button>
</PermissionGuard>
```

## Real-World Examples

### User Management Components

```tsx
// UserList.tsx - Protect entire page
<PermissionGuard permission="USER_MGMT:read">
  <div className="container mx-auto py-10">
    {/* User list content */}
  </div>
</PermissionGuard>

// UserCreate.tsx - Protect creation form
<PermissionGuard permission="USER_MGMT:create">
  <Card>
    <CardHeader>
      <CardTitle>Create New User</CardTitle>
    </CardHeader>
    <CardContent>
      {/* Form content */}
    </CardContent>
  </Card>
</PermissionGuard>

// UserEdit.tsx - Protect edit form
<PermissionGuard permission="USER_MGMT:update">
  <form onSubmit={handleSubmit}>
    {/* Edit form content */}
  </form>
</PermissionGuard>
```

### Dashboard Statistics

```tsx
// Dashboard.tsx - Conditional statistics display
<PermissionGuard permission="ANALYTICS_MGMT:read">
  <Card>
    <CardHeader>
      <CardTitle>System Statistics</CardTitle>
    </CardHeader>
    <CardContent>
      <div className="grid grid-cols-2 gap-4">
        <div>Total Users: {stats.totalUsers}</div>
        <div>Active Sessions: {stats.activeSessions}</div>
      </div>
    </CardContent>
  </Card>
</PermissionGuard>

<PermissionGuard permission="AUDIT_MGMT:read">
  <Card>
    <CardHeader>
      <CardTitle>Recent Activities</CardTitle>
    </CardHeader>
    <CardContent>
      {/* Activity list */}
    </CardContent>
  </Card>
</PermissionGuard>
```

## Best Practices

### 1. Use Descriptive Permission Names

Follow a consistent naming convention for permissions:

```tsx
// Good: Clear resource:action pattern
<PermissionGuard permission="user:create">
<PermissionGuard permission="product:update">
<PermissionGuard permission="report:read">

// Avoid: Vague or inconsistent names
<PermissionGuard permission="canEdit">
<PermissionGuard permission="admin_access">
```

### 2. Wrap Entire Components

For page-level protection, wrap the entire component content:

```tsx
const ModuleList: React.FC = () => {
  return (
    <PermissionGuard permission="module:read">
      <div className="container mx-auto py-10">
        {/* All component content */}
      </div>
    </PermissionGuard>
  )
}
```

### 3. Granular UI Element Protection

Protect individual UI elements for better user experience:

```tsx
<div className="flex space-x-2">
  <Button variant="ghost" size="sm" asChild>
    <Link to={`/modules/${module.id}`}>
      <Eye className="h-4 w-4" />
    </Link>
  </Button>
  
  <PermissionGuard permission="module:update">
    <Button variant="ghost" size="sm" asChild>
      <Link to={`/modules/${module.id}/edit`}>
        <Edit className="h-4 w-4" />
      </Link>
    </Button>
  </PermissionGuard>
  
  <PermissionGuard permission="USER_MGMT:delete">
    <Button
      variant="ghost"
      size="sm"
      onClick={() => handleDelete(user.id)}
    >
      <Trash2 className="h-4 w-4" />
    </Button>
  </PermissionGuard>
</div>
```

### 4. Provide Meaningful Fallbacks

When appropriate, provide informative fallback content:

```tsx
<PermissionGuard 
  permission="ANALYTICS_MGMT:read"
  fallback={
    <Card>
      <CardContent className="p-6 text-center">
        <Shield className="h-12 w-12 mx-auto mb-4 text-muted-foreground" />
        <h3 className="text-lg font-semibold mb-2">Premium Feature</h3>
        <p className="text-muted-foreground">
          Upgrade your plan to access advanced analytics.
        </p>
      </CardContent>
    </Card>
  }
>
  <AdvancedAnalyticsPanel />
</PermissionGuard>
```

### 5. Combine with Loading States

Handle loading states appropriately:

```tsx
const UserDetail: React.FC = () => {
  const [loading, setLoading] = useState(true)
  
  return (
    <PermissionGuard permission="USER_MGMT:read">
      {loading ? (
        <div className="space-y-4">
          <Skeleton className="h-8 w-64" />
          <Skeleton className="h-32 w-full" />
        </div>
      ) : (
        <div>
          {/* User detail content */}
        </div>
      )}
    </PermissionGuard>
  )
}
```

## Common Permission Patterns

### CRUD Operations

```tsx
// Create
<PermissionGuard permission="PRODUCT_MGMT:create">
  <CreateButton />
</PermissionGuard>

// Read
<PermissionGuard permission="PRODUCT_MGMT:read">
  <ProductList />
</PermissionGuard>

// Update
<PermissionGuard permission="PRODUCT_MGMT:update">
  <EditButton />
</PermissionGuard>

// Delete
<PermissionGuard permission="PRODUCT_MGMT:delete">
  <DeleteButton />
</PermissionGuard>
```

### Administrative Functions

```tsx
// User management
<PermissionGuard permission="USER_MGMT:read">
  <UserManagementPanel />
</PermissionGuard>

// System settings
<PermissionGuard role="admin">
  <SystemSettings />
</PermissionGuard>

// Audit logs
<PermissionGuard permission="audit:read">
  <AuditLogViewer />
</PermissionGuard>
```

## Integration with Authentication

The `PermissionGuard` integrates with the application's authentication system through the `useAuth` hook:

```typescript
const { hasPermission, hasRole, hasAnyRole, canAccessResource, isAuthenticated } = useAuth()
```

### Authentication Flow

1. **Authentication Check**: First verifies if user is authenticated
2. **Permission Validation**: Checks specific permissions if provided
3. **Role Validation**: Validates user roles if specified
4. **Resource Access**: Checks resource-action permissions if provided
5. **Render Decision**: Renders children or fallback based on validation results

## Companion Components

### RequireAuth

For simple authentication checks without permission validation:

```tsx
<RequireAuth fallback={<LoginPrompt />}>
  <AuthenticatedContent />
</RequireAuth>
```

### ConditionalRender

For custom conditional rendering logic:

```tsx
<ConditionalRender 
  condition={user.isPremium}
  fallback={<UpgradePrompt />}
>
  <PremiumFeatures />
</ConditionalRender>
```

## Migration from Manual Permission Checks

### Before (Manual Checks)

```tsx
const UserList: React.FC = () => {
  const { canViewUsers } = usePermissions()
  const navigate = useNavigate()
  
  useEffect(() => {
    if (!canViewUsers) {
      navigate('/unauthorized')
      return
    }
  }, [canViewUsers, navigate])
  
  if (!canViewUsers) {
    return (
      <Alert>
        <Shield className="h-4 w-4" />
        <AlertDescription>
          You don't have permission to view users.
        </AlertDescription>
      </Alert>
    )
  }
  
  return <div>{/* Component content */}</div>
}
```

### After (PermissionGuard)

```tsx
const UserList: React.FC = () => {
  return (
    <PermissionGuard permission="USER_MGMT:read">
      <div>{/* Component content */}</div>
    </PermissionGuard>
  )
}
```

## Benefits

1. **Declarative**: Clear, readable permission declarations
2. **Consistent**: Uniform permission handling across the application
3. **Maintainable**: Centralized permission logic
4. **Flexible**: Multiple validation modes and fallback options
5. **Type-safe**: Full TypeScript support
6. **Performance**: Efficient rendering with minimal re-renders
7. **Testable**: Easy to unit test permission scenarios

## Testing

Example test cases for components using PermissionGuard:

```typescript
describe('UserList with PermissionGuard', () => {
  it('renders content when user has permission', () => {
    // Mock user with USER_MGMT:read permission
    render(<UserList />)
    expect(screen.getByText('User Management')).toBeInTheDocument()
  })
  
  it('renders nothing when user lacks permission', () => {
    // Mock user without USER_MGMT:read permission
    render(<UserList />)
    expect(screen.queryByText('User Management')).not.toBeInTheDocument()
  })
  
  it('renders fallback when provided', () => {
    // Test custom fallback content
    render(
      <PermissionGuard 
        permission="USER_MGMT:read" 
        fallback={<div>Access Denied</div>}
      >
        <div>Protected Content</div>
      </PermissionGuard>
    )
    expect(screen.getByText('Access Denied')).toBeInTheDocument()
  })
})
```

## Migration from Old Format

The permission format has been updated from `module:action` to `MODULE_MGMT:action` for better consistency and clarity:

**Old Format:**
- `user:read` → `USER_MGMT:read`
- `role:create` → `ROLE_MGMT:create`
- `tenant:update` → `TENANT_MGMT:update`
- `product:delete` → `PRODUCT_MGMT:delete`

**Migration Steps:**
1. Update all `PermissionGuard` components with new permission format
2. Update `ProtectedRoute` components in routing configuration
3. Update permission checks in hooks and utilities
4. Test all permission-protected components

This documentation provides a comprehensive guide for effectively using the `PermissionGuard` component to implement secure, maintainable access control throughout your React application.