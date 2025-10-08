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

export interface Role {
  id: number;
  name: string;
  description: string;
  createdAt?: string;
  updatedAt?: string;
  permissions?: Permission[];
  status?: "active" | "inactive" | "draft" | "deprecated";
  moduleId?: number;
  moduleName?: string;
  isActive?: boolean;
}

export interface Permission {
  id: number;
  name: string;
  description: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface UserGroup {
  id: number;
  userGroupId: number;
  name: string;
  description?: string;
  memberCount: number;
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
  id: number;
  name: string;
  description?: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface Module {
  id: number;
  name: string;
  description?: string;
  code?: string;
  isActive: boolean;
  productId?: number;
  productName?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface Product {
  id: number;
  name: string;
  description?: string;
  code: string;
  status: string;
  version: string;
}

export const ENTITY_STATUS_MAPPINGS = {
  role: {
    ACTIVE: "ACTIVE",
    INACTIVE: "INACTIVE",
  },
  user: {
    ACTIVE: "ACTIVE",
    INACTIVE: "INACTIVE",
  },
  tenant: {
    ACTIVE: "ACTIVE",
    INACTIVE: "INACTIVE",
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

export const ENTITY_STATUS_MAPPINGS_1 = {
  role: {
    active: "ACTIVE",
    inactive: "INACTIVE",
  },
  user: {
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

export type EntityStatus1 =
  (typeof ENTITY_STATUS_MAPPINGS_1)[keyof typeof ENTITY_STATUS_MAPPINGS_1][keyof (typeof ENTITY_STATUS_MAPPINGS_1)[keyof typeof ENTITY_STATUS_MAPPINGS_1]];

export type RoleStatus1 =
  (typeof ENTITY_STATUS_MAPPINGS_1.role)[keyof typeof ENTITY_STATUS_MAPPINGS_1.role];

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
  code?: string;
  type?: "enterprise" | "standard" | "basic";
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
