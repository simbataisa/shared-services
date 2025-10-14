import React, { useState, useEffect } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import { ArrowLeft } from "lucide-react";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { DetailHeaderCard } from "@/components/common";
import { RoleStatusCard } from "./RoleStatusCard";
import { RoleInfoCard } from "./RoleInfoCard";
import { RoleStatsCard } from "./RoleStatsCard";
import { RolePermissionCard } from "./RolePermissionCard";
import { usePermissions } from "@/hooks/usePermissions";
import httpClient from "@/lib/httpClient";
import {
  type RoleDetails,
  type RoleStats,
  type RoleDetailProps,
  type RoleStatus,
  type Permission,
  ENTITY_STATUS_MAPPINGS,
} from "@/types";

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
        roleStatus: roleData.roleStatus || ENTITY_STATUS_MAPPINGS.role.ACTIVE,
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

  const handleStatusUpdate = async (roleId: number, newStatus: RoleStatus) => {
    if (!role) return;

    try {
      setUpdating(true);
      const updatedRole = await httpClient.updateRoleStatus(roleId, newStatus);

      setRole(updatedRole);
      if (stats) {
        setStats({ ...stats, roleStatus: newStatus });
      }
    } catch (err: any) {
      console.error("Error updating role status:", err);
      setError(err.response?.data?.message || "Failed to update role status");
    } finally {
      setUpdating(false);
    }
  };

  const handleRoleUpdate = async (updatedData: { name: string; description: string }) => {
    if (!role) return;

    try {
      setUpdating(true);
      await httpClient.updateRole(role.id, updatedData);

      // Refetch role details to get the complete RoleDetails object
      await fetchRoleDetails();
      setError(null);
    } catch (err: any) {
      console.error("Error updating role:", err);
      setError(err.response?.data?.message || "Failed to update role");
      throw err; // Re-throw to let RoleInfoCard handle the error
    } finally {
      setUpdating(false);
    }
  };

  const handlePermissionsUpdate = async (updatedPermissions: Permission[]) => {
    if (!role) return;

    try {
      // Update the role state with new permissions
      setRole({ ...role, permissions: updatedPermissions });
      
      // Update stats
      if (stats) {
        setStats({ ...stats, totalPermissions: updatedPermissions.length });
      }
      
      setError(null);
    } catch (err: any) {
      console.error("Error updating permissions:", err);
      setError(err.response?.data?.message || "Failed to update permissions");
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
        <DetailHeaderCard
          title={role.name}
          description={role.description}
          breadcrumbs={[
            { label: "Roles", href: "/roles" },
            { label: role.name }
          ]}
        />

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Main Content */}
          <div className="lg:col-span-2 space-y-8">
            {/* Role Information */}
            <RoleInfoCard 
              role={role} 
              onUpdate={handleRoleUpdate}
              updating={updating}
            />

            {/* Permissions */}
            <RolePermissionCard
              roleId={role.id}
              permissions={role.permissions}
              title="Permissions"
              defaultExpanded={false}
              emptyMessage="No permissions assigned to this role."
              onPermissionsUpdate={handlePermissionsUpdate}
              updating={updating}
            />
          </div>

          <div className="space-y-6">
            {/* Role Status Management */}
            <RoleStatusCard
              role={role}
              onStatusUpdate={handleStatusUpdate}
              updating={updating}
            />

            {/* Statistics */}
            <RoleStatsCard stats={stats} />
          </div>
        </div>
      </div>
    </div>
  );
};

export default RoleDetail;
