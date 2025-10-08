import React, { useState, useEffect } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import { ArrowLeft, Shield, Activity } from "lucide-react";
import {
  Breadcrumb,
  BreadcrumbItem,
  BreadcrumbLink,
  BreadcrumbList,
  BreadcrumbPage,
  BreadcrumbSeparator,
} from "@/components/ui/breadcrumb";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { StatusBadge } from "@/components/common/StatusBadge";
import { PermissionGuard } from "@/components/common/PermissionGuard";
import { PermissionsCard } from "@/components/common";
import { usePermissions } from "@/hooks/usePermissions";
import { normalizeEntityStatus } from "@/lib/status-colors";
import httpClient from "@/lib/httpClient";
import type { RoleDetails, RoleStats, RoleDetailProps } from "@/types";

const RoleDetail: React.FC<RoleDetailProps> = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { canViewRoles, canManageRoles } = usePermissions();

  const [role, setRole] = useState<RoleDetails | null>(null);
  const [stats, setStats] = useState<RoleStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [updating, setUpdating] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Redirect if user doesn't have permission to view roles
  useEffect(() => {
    if (!canViewRoles) {
      navigate("/unauthorized");
      return;
    }
  }, [canViewRoles, navigate]);

  useEffect(() => {
    if (id && canViewRoles) {
      fetchRoleDetails();
    }
  }, [id, canViewRoles]);

  const fetchRoleDetails = async () => {
    try {
      setLoading(true);
      setError(null);

      const roleData: RoleDetails = await httpClient.getRoleDetails(Number(id));
      console.log("roleData:", roleData);
      setRole(roleData);

      // Calculate stats
      const roleStats: RoleStats = {
        totalPermissions: roleData.permissions?.length || 0,
        totalUsers: roleData.userCount || 0,
        totalUserGroups: roleData.userGroupCount || 0,
        status: roleData.roleStatus || "ACTIVE",
        lastModified: roleData.updatedAt || roleData.createdAt,
      };

      setStats(roleStats);
    } catch (err: any) {
      console.error("Error fetching role details:", err);
      setError(err.response?.data?.message || "Failed to fetch role details");
    } finally {
      setLoading(false);
    }
  };

  const handleStatusUpdate = async (newStatus: "ACTIVE" | "INACTIVE") => {
    if (!role) return;

    try {
      setUpdating(true);
      const updatedRole = await httpClient.updateRoleStatus(role.id, newStatus);

      setRole(updatedRole);
      if (stats) {
        setStats({ ...stats, status: newStatus });
      }
    } catch (err: any) {
      console.error("Error updating role status:", err);
      setError(err.response?.data?.message || "Failed to update role status");
    } finally {
      setUpdating(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 py-8">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="mb-8">
            <Skeleton className="h-6 w-48 mb-4" />
            <Skeleton className="h-8 w-64 mb-2" />
            <Skeleton className="h-4 w-32" />
          </div>
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            <div className="lg:col-span-2 space-y-8">
              <Skeleton className="h-64 w-full" />
              <Skeleton className="h-48 w-full" />
            </div>
            <div className="space-y-6">
              <Skeleton className="h-32 w-full" />
              <Skeleton className="h-48 w-full" />
            </div>
          </div>
        </div>
      </div>
    );
  }

  if (error || !role) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <Card className="w-full max-w-md">
          <CardHeader>
            <CardTitle>Role Not Found</CardTitle>
            <CardDescription>
              {error || "The requested role could not be found."}
            </CardDescription>
          </CardHeader>
          <CardContent>
            <Button onClick={() => navigate("/roles")} className="w-full">
              <ArrowLeft className="mr-2 h-4 w-4" />
              Back to Roles
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Header */}
        <div className="mb-8">
          <Breadcrumb className="mb-4">
            <BreadcrumbList>
              <BreadcrumbItem>
                <BreadcrumbLink asChild>
                  <Link to="/roles">Roles</Link>
                </BreadcrumbLink>
              </BreadcrumbItem>
              <BreadcrumbSeparator />
              <BreadcrumbItem>
                <BreadcrumbPage>{role.name}</BreadcrumbPage>
              </BreadcrumbItem>
            </BreadcrumbList>
          </Breadcrumb>

          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-3xl font-bold text-gray-900">{role.name}</h1>
              <p className="mt-2 text-gray-600">{role.description}</p>
            </div>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Main Content */}
          <div className="lg:col-span-2 space-y-8">
            {/* Role Information */}
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center">
                  <Shield className="mr-2 h-5 w-5" />
                  Role Information
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="text-sm font-medium text-gray-500">
                      Role Name
                    </label>
                    <p className="mt-1 text-sm text-gray-900">{role.name}</p>
                  </div>
                  <div>
                    <label className="text-sm font-medium text-gray-500">
                      Status
                    </label>
                    <div className="mt-1">
                      <StatusBadge
                        status={normalizeEntityStatus("role", role.roleStatus)}
                      />
                    </div>
                  </div>
                  <div className="md:col-span-2">
                    <label className="text-sm font-medium text-gray-500">
                      Description
                    </label>
                    <p className="mt-1 text-sm text-gray-900">
                      {role.description || "No description provided"}
                    </p>
                  </div>
                  <div>
                    <label className="text-sm font-medium text-gray-500">
                      Created At
                    </label>
                    <p className="mt-1 text-sm text-gray-900">
                      {new Date(role.createdAt).toLocaleDateString("en-US", {
                        year: "numeric",
                        month: "long",
                        day: "numeric",
                      })}
                    </p>
                  </div>
                  <div>
                    <label className="text-sm font-medium text-gray-500">
                      Last Updated
                    </label>
                    <p className="mt-1 text-sm text-gray-900">
                      {new Date(role.updatedAt).toLocaleDateString("en-US", {
                        year: "numeric",
                        month: "long",
                        day: "numeric",
                      })}
                    </p>
                  </div>
                </div>
              </CardContent>
            </Card>

            {/* Permissions */}
            <PermissionsCard
              permissions={role.permissions}
              title="Permissions"
              emptyMessage="No permissions assigned to this role."
            />
          </div>

          {/* Sidebar */}
          <div className="space-y-6">
            {/* Role Status Management */}
            <PermissionGuard permission="role:update">
              <Card>
                <CardHeader>
                  <CardTitle className="text-lg font-semibold text-gray-900">
                    Status Management
                  </CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-gray-600">
                      Current Status
                    </span>
                    <StatusBadge
                      status={normalizeEntityStatus("role", role.roleStatus)}
                    />
                  </div>
                  <div className="space-y-2">
                    {role.roleStatus === "ACTIVE" ? (
                      <Button
                        onClick={() => handleStatusUpdate("INACTIVE")}
                        disabled={updating}
                        variant="outline"
                        size="sm"
                        className="w-full"
                      >
                        Deactivate Role
                      </Button>
                    ) : (
                      <Button
                        onClick={() => handleStatusUpdate("ACTIVE")}
                        disabled={updating}
                        variant="outline"
                        size="sm"
                        className="w-full"
                      >
                        Activate Role
                      </Button>
                    )}
                  </div>
                </CardContent>
              </Card>
            </PermissionGuard>

            {/* Statistics */}
            {stats && (
              <Card>
                <CardHeader>
                  <CardTitle className="text-lg font-semibold text-gray-900">
                    Statistics
                  </CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="flex justify-between">
                    <span className="text-sm text-gray-600">
                      Total Permissions
                    </span>
                    <span className="text-sm font-medium text-gray-900">
                      {stats.totalPermissions}
                    </span>
                  </div>

                  <div className="flex justify-between">
                    <span className="text-sm text-gray-600">
                      Users with Role
                    </span>
                    <span className="text-sm font-medium text-gray-900">
                      {stats.totalUsers}
                    </span>
                  </div>

                  <div className="flex justify-between">
                    <span className="text-sm text-gray-600">User Groups</span>
                    <span className="text-sm font-medium text-gray-900">
                      {stats.totalUserGroups}
                    </span>
                  </div>

                  <div className="flex justify-between">
                    <span className="text-sm text-gray-600">Status</span>
                    <StatusBadge
                      status={normalizeEntityStatus("role", stats.status)}
                    />
                  </div>

                  {stats.lastModified && (
                    <div className="pt-4 border-t border-gray-200">
                      <span className="text-sm text-gray-600">
                        Last Modified
                      </span>
                      <p className="text-sm font-medium text-gray-900">
                        {new Date(stats.lastModified).toLocaleDateString(
                          "en-US",
                          {
                            year: "numeric",
                            month: "long",
                            day: "numeric",
                            hour: "2-digit",
                            minute: "2-digit",
                          }
                        )}
                      </p>
                    </div>
                  )}
                </CardContent>
              </Card>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default RoleDetail;
