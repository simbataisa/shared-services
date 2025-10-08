import { type ReactNode } from "react";
import { Navigate, useLocation } from "react-router-dom";
import { useAuth } from "@/store/auth";

interface ProtectedRouteProps {
  children: ReactNode;
  requireAuth?: boolean;
  permission?: string;
  role?: string;
  roles?: string[];
  resource?: string;
  action?: string;
  requireAll?: boolean;
  redirectTo?: string;
}

export default function ProtectedRoute({
  children,
  requireAuth = true,
  permission,
  role,
  roles,
  resource,
  action,
  requireAll = false,
  redirectTo = "/login",
}: ProtectedRouteProps) {
  const {
    isAuthenticated,
    hasPermission,
    hasRole,
    hasAnyRole,
    canAccessResource,
  } = useAuth();
  const location = useLocation();

  // Check authentication first
  if (requireAuth && !isAuthenticated) {
    return <Navigate to={redirectTo} state={{ from: location }} replace />;
  }

  // If authenticated but no specific permissions required, allow access
  if (isAuthenticated && !permission && !role && !roles && !resource) {
    return <>{children}</>;
  }

  // Check specific permission
  if (permission && !hasPermission(permission)) {
    return <Navigate to="/unauthorized" replace />;
  }

  // Check single role
  if (role && !hasRole(role)) {
    return <Navigate to="/unauthorized" replace />;
  }

  // Check multiple roles
  if (roles && roles.length > 0) {
    if (requireAll) {
      // User must have ALL specified roles
      const hasAllRoles = roles.every((r) => hasRole(r));
      if (!hasAllRoles) {
        return <Navigate to="/unauthorized" replace />;
      }
    } else {
      // User must have ANY of the specified roles
      if (!hasAnyRole(roles)) {
        return <Navigate to="/unauthorized" replace />;
      }
    }
  }

  // Check resource-based access
  if (resource && action && !canAccessResource(resource, action)) {
    return <Navigate to="/unauthorized" replace />;
  }

  return <>{children}</>;
}
