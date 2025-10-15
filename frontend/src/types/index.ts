// Centralized type exports for better reusability and maintenance

// Core entities
export * from './entities';

// API types
export * from './api';

// Form types
export * from './forms';

// Component props
export * from './components';

// Store types
export * from './store';

// Error types
export * from './errors';

// Re-export commonly used types for convenience
export type {
  User,
  Role,
  Permission,
  UserGroup,
  UserGroupDetails,
  RoleAssignment,
  Tenant,
  Module,
  Product,
  DashboardStats,
  Activity,
  RecentActivity
} from './entities';

export type {
  CreateUserRequest,
  UpdateUserRequest,
  CreateRoleRequest,
  UpdateRoleRequest,
  LoginRequest,
  LoginResponse,
  ApiResponse
} from './api';

export type {
  CreateUserForm,
  EditUserForm,
  RoleFormData,
  UserGroupFormData
} from './forms';

export type {
  UserDetailProps,
  RoleDetailProps,
  UserGroupDetailProps,
  ProtectedRouteProps,
  PermissionGuardProps
} from './components';