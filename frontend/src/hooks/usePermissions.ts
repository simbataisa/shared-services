import { useAuth } from "@/store/auth";

export const usePermissions = () => {
  const { user, hasPermission, hasRole, hasAnyRole, canAccessResource } =
    useAuth();

  // Basic permission checks
  const canViewUsers = hasPermission("user:read");
  const canCreateUsers = hasPermission("user:create");
  const canUpdateUsers = hasPermission("user:update");
  const canDeleteUsers = hasPermission("user:delete");
  const canManageUsers = canCreateUsers || canUpdateUsers || canDeleteUsers;

  // Tenant management permissions
  const canViewTenants = hasPermission("tenants:read");
  const canCreateTenants = hasPermission("tenants:create");
  const canUpdateTenants = hasPermission("tenants:update");
  const canDeleteTenants = hasPermission("tenants:delete");
  const canManageTenants =
    canCreateTenants || canUpdateTenants || canDeleteTenants;

  // Role and permission management
  const canViewRoles = hasPermission("role:read");
  const canCreateRoles = hasPermission("role:create");
  const canUpdateRoles = hasPermission("role:update");
  const canDeleteRoles = hasPermission("role:delete");
  const canManageRoles = canCreateRoles || canUpdateRoles || canDeleteRoles;

  const canViewPermissions = hasPermission("permission:read");
  const canUpdatePermissions = hasPermission("permission:update");
  const canDeletePermissions = hasPermission("permission:delete");
  const canManagePermissions = canUpdatePermissions || canDeletePermissions;

  // Product and module permissions
  const canViewProducts = hasPermission("product:read");
  const canCreateProducts = hasPermission("product:create");
  const canUpdateProducts = hasPermission("product:update");
  const canDeleteProducts = hasPermission("product:delete");
  const canManageProducts =
    canCreateProducts || canUpdateProducts || canDeleteProducts;

  const canViewModules = hasPermission("module:read");
  const canCreateModules = hasPermission("module:create");
  const canUpdateModules = hasPermission("module:update");
  const canDeleteModules = hasPermission("module:delete");
  const canManageModules =
    canCreateModules || canUpdateModules || canDeleteModules;

  // Audit and logging
  const canViewAuditLogs = hasPermission("audit:read");
  const canExportAuditLogs = hasPermission("audit:export");

  // Admin checks
  const isAdmin = hasRole("admin");
  const isSuperAdmin = hasRole("super_admin");
  const isSystemAdmin = hasAnyRole(["admin", "super_admin", "system_admin"]);

  // Tenant-specific checks
  const isTenantAdmin = hasRole("tenant_admin");
  const canManageTenantUsers =
    isTenantAdmin || hasPermission("tenant_user:manage");

  // Multi-tenant checks
  const isMultiTenantUser = user?.tenantId !== undefined;
  const canAccessMultipleTenants = hasPermission("multi_tenant:access");

  return {
    // User permissions
    canViewUsers,
    canCreateUsers,
    canUpdateUsers,
    canDeleteUsers,
    canManageUsers,

    // Tenant permissions
    canViewTenants,
    canCreateTenants,
    canUpdateTenants,
    canDeleteTenants,
    canManageTenants,

    // Role and permission management
    canViewRoles,
    canCreateRoles,
    canUpdateRoles,
    canDeleteRoles,
    canManageRoles,
    canViewPermissions,
    canUpdatePermissions,
    canDeletePermissions,
    canManagePermissions,

    // Product and module permissions
    canViewProducts,
    canCreateProducts,
    canUpdateProducts,
    canDeleteProducts,
    canManageProducts,
    canViewModules,
    canCreateModules,
    canUpdateModules,
    canDeleteModules,
    canManageModules,

    // Audit permissions
    canViewAuditLogs,
    canExportAuditLogs,

    // Admin checks
    isAdmin,
    isSuperAdmin,
    isSystemAdmin,
    isTenantAdmin,
    canManageTenantUsers,

    // Multi-tenant
    isMultiTenantUser,
    canAccessMultipleTenants,

    // Resource access helper
    canAccessResource,
  };
};

// CRUD permissions hook for specific resources
export const useCrudPermissions = (resource: string) => {
  const { hasPermission } = useAuth();

  return {
    canView: hasPermission(`${resource}:read`),
    canCreate: hasPermission(`${resource}:create`),
    canUpdate: hasPermission(`${resource}:update`),
    canDelete: hasPermission(`${resource}:delete`),
    canManage:
      hasPermission(`${resource}:create`) ||
      hasPermission(`${resource}:update`) ||
      hasPermission(`${resource}:delete`),
  };
};

// Navigation permissions
export const useNavigationPermissions = () => {
  const {
    canViewUsers,
    canViewTenants,
    canViewRoles,
    canViewProducts,
    canViewModules,
    canViewAuditLogs,
    isSystemAdmin,
  } = usePermissions();

  return {
    canAccessDashboard: true, // Everyone can access dashboard
    canAccessUsers: canViewUsers,
    canAccessTenants: canViewTenants,
    canAccessRoles: canViewRoles,
    canAccessProducts: canViewProducts,
    canAccessModules: canViewModules,
    canAccessAuditLogs: canViewAuditLogs,
    canAccessSystemSettings: isSystemAdmin,
  };
};
