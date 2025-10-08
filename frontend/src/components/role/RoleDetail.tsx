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
import { PermissionsCard } from "@/components/common";
import { RoleStatusCard } from "./RoleStatusCard";
import { RoleInfoCard } from "./RoleInfoCard";
import { RoleStatsCard } from "./RoleStatsCard";
import { usePermissions } from "@/hooks/usePermissions";
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
            <RoleInfoCard role={role} />

            {/* Permissions */}
            <PermissionsCard
              permissions={role.permissions}
              title="Permissions"
              defaultExpanded={false}
              emptyMessage="No permissions assigned to this role."
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
