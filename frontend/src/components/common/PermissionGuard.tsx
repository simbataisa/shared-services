import { type ReactNode } from "react";
import { useAuth } from "@/store/auth";

interface PermissionGuardProps {
  children: ReactNode;
  permission?: string;
  role?: string;
  roles?: string[];
  resource?: string;
  action?: string;
  fallback?: ReactNode;
  requireAll?: boolean;
}

export function PermissionGuard({
  children,
  permission,
  role,
  roles,
  resource,
  action,
  fallback = null,
  requireAll = false,
}: PermissionGuardProps) {
  const {
    hasPermission,
    hasRole,
    hasAnyRole,
    canAccessResource,
    isAuthenticated,
  } = useAuth();

  if (!isAuthenticated) {
    return <>{fallback}</>;
  }

  // Check specific permission
  if (permission && !hasPermission(permission)) {
    return <>{fallback}</>;
  }

  // Check single role
  if (role && !hasRole(role)) {
    return <>{fallback}</>;
  }

  // Check multiple roles
  if (roles && roles.length > 0) {
    if (requireAll) {
      // User must have ALL specified roles
      const hasAllRoles = roles.every((r) => hasRole(r));
      if (!hasAllRoles) {
        return <>{fallback}</>;
      }
    } else {
      // User must have ANY of the specified roles
      if (!hasAnyRole(roles)) {
        return <>{fallback}</>;
      }
    }
  }

  // Check resource-based access
  if (resource && action && !canAccessResource(resource, action)) {
    return <>{fallback}</>;
  }

  return <>{children}</>;
}

interface RequireAuthProps {
  children: ReactNode;
  fallback?: ReactNode;
}

export function RequireAuth({ children, fallback = null }: RequireAuthProps) {
  const { isAuthenticated } = useAuth();

  if (!isAuthenticated) {
    return <>{fallback}</>;
  }

  return <>{children}</>;
}

interface ConditionalRenderProps {
  children: ReactNode;
  condition: boolean;
  fallback?: ReactNode;
}

export function ConditionalRender({
  children,
  condition,
  fallback = null,
}: ConditionalRenderProps) {
  return condition ? <>{children}</> : <>{fallback}</>;
}
