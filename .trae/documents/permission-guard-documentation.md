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

## Component Interface

```typescript
interface PermissionGuardProps {
  children: ReactNode           // Content to render when access is granted
  permission?: string          // Single permission to check (e.g., "module:read")
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
<PermissionGuard permission="module:read">
  <ModuleList />
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
  permission="module:create"
  fallback={
    <Alert>
      <Shield className="h-4 w-4" />
      <AlertDescription>
        You don't have permission to create modules.
      </AlertDescription>
    </Alert>
  }
>
  <CreateModuleForm />
</PermissionGuard>
```

### 5. Protecting UI Elements

Conditionally render buttons, links, and other UI elements:

```tsx
<PermissionGuard permission="product:create">
  <Button asChild>
    <Link to="/products/create">
      <Plus className="mr-2 h-4 w-4" />
      Create Product
    </Link>
  </Button>
</PermissionGuard>
```

## Real-World Examples

### Module Management Components

```tsx
// ModuleList.tsx - Protect entire page
<PermissionGuard permission="module:read">
  <div className="container mx-auto py-10">
    {/* Module list content */}
  </div>
</PermissionGuard>

// ModuleCreate.tsx - Protect creation form
<PermissionGuard permission="module:create">
  <Card>
    <CardHeader>
      <CardTitle>Create New Module</CardTitle>
    </CardHeader>
    <CardContent>
      {/* Form content */}
    </CardContent>
  </Card>
</PermissionGuard>

// ModuleEdit.tsx - Protect edit form
<PermissionGuard permission="module:update">
  <form onSubmit={handleSubmit}>
    {/* Edit form content */}
  </form>
</PermissionGuard>
```

### Dashboard Statistics

```tsx
// Dashboard.tsx - Conditional statistics display
<PermissionGuard permission="analytics:read">
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

<PermissionGuard permission="audit:read">
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
  
  <PermissionGuard permission="module:delete">
    <Button
      variant="ghost"
      size="sm"
      onClick={() => handleDelete(module.id)}
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
  permission="advanced:features"
  fallback={
    <Card>
      <CardContent className="p-6 text-center">
        <Shield className="h-12 w-12 mx-auto mb-4 text-muted-foreground" />
        <h3 className="text-lg font-semibold mb-2">Premium Feature</h3>
        <p className="text-muted-foreground">
          Upgrade your plan to access advanced features.
        </p>
      </CardContent>
    </Card>
  }
>
  <AdvancedFeaturePanel />
</PermissionGuard>
```

### 5. Combine with Loading States

Handle loading states appropriately:

```tsx
const ModuleDetail: React.FC = () => {
  const [loading, setLoading] = useState(true)
  
  return (
    <PermissionGuard permission="module:read">
      {loading ? (
        <div className="space-y-4">
          <Skeleton className="h-8 w-64" />
          <Skeleton className="h-32 w-full" />
        </div>
      ) : (
        <div>
          {/* Module detail content */}
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
<PermissionGuard permission="resource:create">
  <CreateButton />
</PermissionGuard>

// Read
<PermissionGuard permission="resource:read">
  <ResourceList />
</PermissionGuard>

// Update
<PermissionGuard permission="resource:update">
  <EditButton />
</PermissionGuard>

// Delete
<PermissionGuard permission="resource:delete">
  <DeleteButton />
</PermissionGuard>
```

### Administrative Functions

```tsx
// User management
<PermissionGuard permission="user:manage">
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
const ModuleList: React.FC = () => {
  const { canViewModules } = usePermissions()
  const navigate = useNavigate()
  
  useEffect(() => {
    if (!canViewModules) {
      navigate('/unauthorized')
      return
    }
  }, [canViewModules, navigate])
  
  if (!canViewModules) {
    return (
      <Alert>
        <Shield className="h-4 w-4" />
        <AlertDescription>
          You don't have permission to view modules.
        </AlertDescription>
      </Alert>
    )
  }
  
  return <div>{/* Component content */}</div>
}
```

### After (PermissionGuard)

```tsx
const ModuleList: React.FC = () => {
  return (
    <PermissionGuard permission="module:read">
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
describe('ModuleList with PermissionGuard', () => {
  it('renders content when user has permission', () => {
    // Mock user with module:read permission
    render(<ModuleList />)
    expect(screen.getByText('Module Management')).toBeInTheDocument()
  })
  
  it('renders nothing when user lacks permission', () => {
    // Mock user without module:read permission
    render(<ModuleList />)
    expect(screen.queryByText('Module Management')).not.toBeInTheDocument()
  })
  
  it('renders fallback when provided', () => {
    // Test custom fallback content
    render(
      <PermissionGuard 
        permission="module:read" 
        fallback={<div>Access Denied</div>}
      >
        <div>Protected Content</div>
      </PermissionGuard>
    )
    expect(screen.getByText('Access Denied')).toBeInTheDocument()
  })
})
```

This documentation provides a comprehensive guide for effectively using the `PermissionGuard` component to implement secure, maintainable access control throughout your React application.