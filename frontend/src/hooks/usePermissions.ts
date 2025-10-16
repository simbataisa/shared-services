import { useAuth } from "@/store/auth";

export const usePermissions = () => {
  const { user, hasPermission, hasRole, hasAnyRole, canAccessResource } =
    useAuth();

  // User management permissions
  const canViewUsers = hasPermission("USER_MGMT:read");
  const canCreateUsers = hasPermission("USER_MGMT:create");
  const canUpdateUsers = hasPermission("USER_MGMT:update");
  const canDeleteUsers = hasPermission("USER_MGMT:delete");
  const canAdminUsers = hasPermission("USER_MGMT:admin");
  const canAssignRoles = hasPermission("USER_MGMT:assign_roles");
  const canAssignGroups = hasPermission("USER_MGMT:assign_groups");
  const canManageUsers = canCreateUsers || canUpdateUsers || canDeleteUsers || canAdminUsers;

  // Tenant management permissions
  const canViewTenants = hasPermission("TENANT_MGMT:read");
  const canCreateTenants = hasPermission("TENANT_MGMT:create");
  const canUpdateTenants = hasPermission("TENANT_MGMT:update");
  const canDeleteTenants = hasPermission("TENANT_MGMT:delete");
  const canAdminTenants = hasPermission("TENANT_MGMT:admin");
  const canManageTenants =
    canCreateTenants || canUpdateTenants || canDeleteTenants || canAdminTenants;

  // Role management permissions
  const canViewRoles = hasPermission("ROLE_MGMT:read");
  const canCreateRoles = hasPermission("ROLE_MGMT:create");
  const canUpdateRoles = hasPermission("ROLE_MGMT:update");
  const canDeleteRoles = hasPermission("ROLE_MGMT:delete");
  const canAdminRoles = hasPermission("ROLE_MGMT:admin");
  const canManageRoles = canCreateRoles || canUpdateRoles || canDeleteRoles || canAdminRoles;

  // Permission management permissions
  const canViewPermissions = hasPermission("PERMISSION_MGMT:read");
  const canCreatePermissions = hasPermission("PERMISSION_MGMT:create");
  const canUpdatePermissions = hasPermission("PERMISSION_MGMT:update");
  const canDeletePermissions = hasPermission("PERMISSION_MGMT:delete");
  const canAdminPermissions = hasPermission("PERMISSION_MGMT:admin");
  const canManagePermissions = canCreatePermissions || canUpdatePermissions || canDeletePermissions || canAdminPermissions;

  // Product and module permissions
  const canViewProducts = hasPermission("PRODUCT_MGMT:read");
  const canCreateProducts = hasPermission("PRODUCT_MGMT:create");
  const canUpdateProducts = hasPermission("PRODUCT_MGMT:update");
  const canDeleteProducts = hasPermission("PRODUCT_MGMT:delete");
  const canAdminProducts = hasPermission("PRODUCT_MGMT:admin");
  const canManageProducts =
    canCreateProducts || canUpdateProducts || canDeleteProducts || canAdminProducts;

  const canViewModules = hasPermission("MODULE_MGMT:read");
  const canCreateModules = hasPermission("MODULE_MGMT:create");
  const canUpdateModules = hasPermission("MODULE_MGMT:update");
  const canDeleteModules = hasPermission("MODULE_MGMT:delete");
  const canAdminModules = hasPermission("MODULE_MGMT:admin");
  const canConfigModules = hasPermission("MODULE_MGMT:config");
  const canManageModules =
    canCreateModules || canUpdateModules || canDeleteModules || canAdminModules;

  // Group management permissions
  const canViewGroups = hasPermission("GROUP_MGMT:read");
  const canCreateGroups = hasPermission("GROUP_MGMT:create");
  const canUpdateGroups = hasPermission("GROUP_MGMT:update");
  const canDeleteGroups = hasPermission("GROUP_MGMT:delete");
  const canAdminGroups = hasPermission("GROUP_MGMT:admin");
  const canManageGroups =
    canCreateGroups || canUpdateGroups || canDeleteGroups || canAdminGroups;

  // Analytics permissions
  const canViewAnalytics = hasPermission("ANALYTICS_USER:read");
  const canCreateAnalytics = hasPermission("ANALYTICS_USER:create");
  const canAdminAnalytics = hasPermission("ANALYTICS_USER:admin");

  // Audit and logging permissions
  const canViewAuditLogs = hasPermission("CORE_AUDIT:read");
  const canAdminAuditLogs = hasPermission("CORE_AUDIT:admin");

  // Payment management permissions
  const canViewPayments = hasPermission("PAYMENT_MGMT:read");
  const canCreatePayments = hasPermission("PAYMENT_MGMT:create");
  const canUpdatePayments = hasPermission("PAYMENT_MGMT:update");
  const canDeletePayments = hasPermission("PAYMENT_MGMT:delete");
  const canAdminPayments = hasPermission("PAYMENT_MGMT:admin");
  const canProcessPayments = hasPermission("PAYMENT_MGMT:process");
  const canRefundPayments = hasPermission("PAYMENT_MGMT:refund");
  const canManagePayments =
    canCreatePayments || canUpdatePayments || canDeletePayments || canAdminPayments;

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
    canAdminUsers,
    canAssignRoles,
    canAssignGroups,
    canManageUsers,

    // Tenant permissions
    canViewTenants,
    canCreateTenants,
    canUpdateTenants,
    canDeleteTenants,
    canAdminTenants,
    canManageTenants,

    // Role management permissions
    canViewRoles,
    canCreateRoles,
    canUpdateRoles,
    canDeleteRoles,
    canAdminRoles,
    canManageRoles,

    // Permission management permissions
    canViewPermissions,
    canCreatePermissions,
    canUpdatePermissions,
    canDeletePermissions,
    canAdminPermissions,
    canManagePermissions,

    // Product and module permissions
    canViewProducts,
    canCreateProducts,
    canUpdateProducts,
    canDeleteProducts,
    canAdminProducts,
    canManageProducts,
    canViewModules,
    canCreateModules,
    canUpdateModules,
    canDeleteModules,
    canAdminModules,
    canConfigModules,
    canManageModules,

    // Group management permissions
    canViewGroups,
    canCreateGroups,
    canUpdateGroups,
    canDeleteGroups,
    canAdminGroups,
    canManageGroups,

    // Analytics permissions
    canViewAnalytics,
    canCreateAnalytics,
    canAdminAnalytics,

    // Audit permissions
    canViewAuditLogs,
    canAdminAuditLogs,

    // Payment permissions
    canViewPayments,
    canCreatePayments,
    canUpdatePayments,
    canDeletePayments,
    canAdminPayments,
    canProcessPayments,
    canRefundPayments,
    canManagePayments,

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

  // Map old resource names to new MODULE_MGMT format
  const getModulePermission = (action: string) => {
    switch (resource.toLowerCase()) {
      case 'user':
      case 'users':
        return `USER_MGMT:${action}`;
      case 'tenant':
      case 'tenants':
        return `TENANT_MGMT:${action}`;
      case 'product':
      case 'products':
        return `PRODUCT_MGMT:${action}`;
      case 'module':
      case 'modules':
        return `MODULE_MGMT:${action}`;
      case 'role':
      case 'roles':
        return `ROLE_MGMT:${action}`;
      case 'permission':
      case 'permissions':
        return `PERMISSION_MGMT:${action}`;
      case 'group':
      case 'groups':
        return `GROUP_MGMT:${action}`;
      case 'analytics':
        return `ANALYTICS_USER:${action}`;
      case 'audit':
        return `CORE_AUDIT:${action}`;
      default:
        // Fallback to old format for unknown resources
        return `${resource}:${action}`;
    }
  };

  return {
    canView: hasPermission(getModulePermission('read')),
    canCreate: hasPermission(getModulePermission('create')),
    canUpdate: hasPermission(getModulePermission('update')),
    canDelete: hasPermission(getModulePermission('delete')),
    canAdmin: hasPermission(getModulePermission('admin')),
    canManage:
      hasPermission(getModulePermission('create')) ||
      hasPermission(getModulePermission('update')) ||
      hasPermission(getModulePermission('delete')) ||
      hasPermission(getModulePermission('admin')),
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
    canViewPayments,
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
    canAccessPayments: canViewPayments,
    canAccessSystemSettings: isSystemAdmin,
  };
};
