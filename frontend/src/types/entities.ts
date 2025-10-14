// Core domain entities

export interface User {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  userStatus: "ACTIVE" | "INACTIVE";
  createdAt: string;
  updatedAt: string;
  roles: Role[];
  userGroups: UserGroup[];
  phoneNumber?: string;
  emailVerified?: boolean;
  lastLogin?: string;
  failedLoginAttempts?: number;
  passwordChangedAt?: string;
  createdBy?: string;
  updatedBy?: string;
}

export interface Permission {
  id: number;
  name: string;
  resource: string;
  action: string;
  conditions?: Record<string, any>;
  // Legacy fields for backward compatibility
  description?: string;
  isActive?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface Role {
  id: number;
  name: string;
  description?: string;
  permissions: Permission[];
  tenantId?: string;
  // Legacy fields for backward compatibility
  createdAt?: string;
  updatedAt?: string;
  status?: "DRAFT" | "ACTIVE" | "INACTIVE" | "DEPRECATED";
  roleStatus?: "DRAFT" | "ACTIVE" | "INACTIVE" | "DEPRECATED";
  moduleId?: number;
  moduleName?: string;
  isActive?: boolean;
}

export interface UserProfile {
  id: string;
  email: string;
  name: string;
  firstName?: string;
  lastName?: string;
  username?: string;
  roles: Role[];
  tenantId?: string;
  permissions: Permission[];
  lastLoginAt?: string;
  createdAt: string;
}

export interface UserGroup {
  id: number;
  userGroupId: number;
  name: string;
  description?: string;
  memberCount: number;
  roleCount: number;
  userGroupStatus: UserGroupStatus;
  roleAssignments?: RoleAssignment[];
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  updatedBy?: string;
}

export interface UserGroupDetails {
  userGroupId: number;
  name: string;
  description: string;
  memberCount: number;
  roleCount: number;
  roleAssignments?: RoleAssignment[];
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  updatedBy?: string;
}

export interface RoleAssignment {
  id: number;
  userGroupId: number;
  userGroupName: string;
  moduleId: number;
  moduleName: string;
  roleId: number;
  roleName: string;
  roleDescription: string;
  createdAt: string;
  updatedAt: string;
  createdBy?: string;
  updatedBy?: string;
}

export interface Tenant {
  id: string;
  name: string;
  code: string;
  type: "enterprise" | "standard" | "basic";
  status: "ACTIVE" | "INACTIVE" | "SUSPENDED";
  createdAt: string;
  updatedAt: string;
  // Legacy fields for backward compatibility
  description?: string;
  userCount?: number;
  roleCount?: number;
  lastActivity?: string;
  createdBy?: string;
  updatedBy?: string;
}

export type ProductStatus = "DRAFT" | "ACTIVE" | "INACTIVE";

export interface Product {
  id: number;
  name: string;
  description: string;
  code: string;
  version: string;
  productStatus: ProductStatus;
  category?: string;
  createdAt: string;
  createdBy?: string;
  updatedAt: string;
  updatedBy?: string;
  modules?: Module[];
}

export interface Module {
  id: number;
  name: string;
  code: string;
  description: string;
  moduleStatus: EntityStatus;
  createdAt: string;
  updatedAt: string;
  productId: number;
  productName: string;
  roles?: Role[] | null;
}

export const ENTITY_STATUS_MAPPINGS = {
  role: {
    DRAFT: "DRAFT",
    ACTIVE: "ACTIVE",
    INACTIVE: "INACTIVE",
    DEPRECATED: "DEPRECATED",
  },
  user: {
    ACTIVE: "ACTIVE",
    INACTIVE: "INACTIVE",
  },
  userGroup: {
    ACTIVE: "ACTIVE",
    INACTIVE: "INACTIVE",
  },
  tenant: {
    ACTIVE: "ACTIVE",
    INACTIVE: "INACTIVE",
  },
  module: {
    ACTIVE: "ACTIVE",
    INACTIVE: "INACTIVE",
  },
  product: {
    ACTIVE: "ACTIVE",
    INACTIVE: "INACTIVE",
  },
};

export const ENTITY_STATUS_MAPPINGS_1 = {
  role: {
    active: "ACTIVE",
    inactive: "INACTIVE",
  },
  user: {
    active: "ACTIVE",
    inactive: "INACTIVE",
  },
  userGroup: {
    active: "ACTIVE",
    inactive: "INACTIVE",
  },
  tenant: {
    active: "ACTIVE",
    inactive: "INACTIVE",
  },
  module: {
    active: "ACTIVE",
    inactive: "INACTIVE",
  },
  product: {
    active: "ACTIVE",
    inactive: "INACTIVE",
  },
};

export type EntityStatus =
  (typeof ENTITY_STATUS_MAPPINGS)[keyof typeof ENTITY_STATUS_MAPPINGS][keyof (typeof ENTITY_STATUS_MAPPINGS)[keyof typeof ENTITY_STATUS_MAPPINGS]];

export type RoleStatus =
  (typeof ENTITY_STATUS_MAPPINGS.role)[keyof typeof ENTITY_STATUS_MAPPINGS.role];

export type UserStatus =
  (typeof ENTITY_STATUS_MAPPINGS.user)[keyof typeof ENTITY_STATUS_MAPPINGS.user];

export type UserGroupStatus =
  (typeof ENTITY_STATUS_MAPPINGS.userGroup)[keyof typeof ENTITY_STATUS_MAPPINGS.userGroup];

export type EntityStatus1 =
  (typeof ENTITY_STATUS_MAPPINGS_1)[keyof typeof ENTITY_STATUS_MAPPINGS_1][keyof (typeof ENTITY_STATUS_MAPPINGS_1)[keyof typeof ENTITY_STATUS_MAPPINGS_1]];

export type RoleStatus1 =
  (typeof ENTITY_STATUS_MAPPINGS_1.role)[keyof typeof ENTITY_STATUS_MAPPINGS_1.role];

export type UserGroupStatus1 =
  (typeof ENTITY_STATUS_MAPPINGS_1.userGroup)[keyof typeof ENTITY_STATUS_MAPPINGS_1.userGroup];

// Extended entity types for detailed views
export interface RoleDetails {
  id: number;
  name: string;
  description: string;
  roleStatus: RoleStatus;
  permissions?: Permission[];
  userCount?: number;
  userGroupCount?: number;
  createdAt: string;
  updatedAt: string;
  createdBy?: string;
  updatedBy?: string;
  moduleId?: number;
  moduleName?: string;
}

export interface TenantDetails extends Tenant {
  userCount?: number;
  roleCount?: number;
  lastActivity?: string;
  createdBy?: string;
  updatedBy?: string;
}

// Statistics interfaces
export interface UserStats {
  totalRoles: number;
  totalUserGroups: number;
  lastLogin: string | null;
  accountStatus: string;
}

export interface RoleStats {
  totalPermissions: number;
  totalUsers: number;
  totalUserGroups: number;
  roleStatus: RoleStatus;
  lastModified: string;
}

export interface UserGroupStats {
  totalMembers: number;
  totalRoles: number;
  activeRoles: number;
  lastActivity: string | null;
}

export interface DashboardStats {
  totalUsers: number;
  activeTenants: number;
  totalRoles: number;
  recentActivities: number;
  systemHealth: "healthy" | "warning" | "critical";
  pendingApprovals: number;
}

export interface ProductStats {
  // Add product-specific statistics as needed
}

export interface ProductWithModules extends Product {
  modules: Module[];
}

// Activity tracking
export interface Activity {
  id: string;
  type: string;
  description: string;
  timestamp: string;
  user: string;
}

export interface RecentActivity {
  id: string;
  type:
    | "user_login"
    | "role_assigned"
    | "tenant_created"
    | "permission_granted";
  description: string;
  timestamp: string;
  user: string;
}
