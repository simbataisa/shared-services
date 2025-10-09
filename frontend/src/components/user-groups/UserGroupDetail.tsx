import React, { useState, useEffect } from "react";
import { useNavigate, useParams, Link } from "react-router-dom";
import {
  Users,
  ArrowLeft,
  Shield,
  Activity,
  CheckCircle,
  XCircle,
  Calendar,
  User,
  Trash2,
} from "lucide-react";
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
import { Label } from "@/components/ui/label";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from "@/components/ui/alert-dialog";
import { PermissionGuard } from "@/components/common/PermissionGuard";
import { StatisticsCard } from "@/components/common";
import { usePermissions } from "@/hooks/usePermissions";
import { BasicInformationCard } from "./BasicInformationCard";
import { RoleAssignmentsCard } from "./RoleAssignmentsCard";
import UserGroupStatusCard from "./UserGroupStatusCard";
import type { UserGroupDetails, UserGroupStats, Module, Role } from "@/types";
import api from "@/lib/api";

export default function UserGroupDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { canViewUsers, canManageUsers } = usePermissions();

  const [userGroup, setUserGroup] = useState<UserGroupDetails | null>(null);
  const [stats, setStats] = useState<UserGroupStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [updating, setUpdating] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Role management state
  const [availableModules, setAvailableModules] = useState<Module[]>([]);
  const [availableRoles, setAvailableRoles] = useState<Role[]>([]);
  const [roleLoading, setRoleLoading] = useState(false);

  // Redirect if user doesn't have permission to view user groups
  useEffect(() => {
    if (!canViewUsers) {
      navigate("/unauthorized");
      return;
    }
  }, [canViewUsers, navigate]);

  useEffect(() => {
    if (id && canViewUsers) {
      fetchUserGroupDetails();
      fetchModulesAndRoles();
    }
  }, [id, canViewUsers]);

  const fetchUserGroupDetails = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await api.get(`/v1/user-groups/${id}`);
      const groupData = response.data.data; // Access the actual data from ApiResponse wrapper

      setUserGroup(groupData);

      // Calculate stats
      const groupStats: UserGroupStats = {
        totalMembers: groupData.memberCount || 0,
        totalRoles: groupData.roleAssignments?.length || 0,
        activeRoles: groupData.roleAssignments?.length || 0,
        lastActivity: groupData.updatedAt || null,
      };

      setStats(groupStats);
    } catch (error) {
      console.error("Failed to fetch user group details:", error);
      setError("Failed to load user group details");
    } finally {
      setLoading(false);
    }
  };

  const fetchModulesAndRoles = async () => {
    try {
      setRoleLoading(true);
      const [modulesResponse, rolesResponse] = await Promise.all([
        api.get("/v1/modules"),
        api.get("/v1/roles"),
      ]);

      const modulesData = Array.isArray(modulesResponse.data)
        ? modulesResponse.data
        : modulesResponse.data.data || [];
      const rolesData = Array.isArray(rolesResponse.data)
        ? rolesResponse.data
        : rolesResponse.data.data || [];

      setAvailableModules(modulesData);
      setAvailableRoles(rolesData);
    } catch (error) {
      console.error("Error fetching modules and roles:", error);
    } finally {
      setRoleLoading(false);
    }
  };

  const handleBasicInfoUpdate = async (data: {
    name: string;
    description: string;
  }) => {
    if (!userGroup) return;

    try {
      setUpdating(true);
      const response = await api.put(
        `/v1/user-groups/${userGroup.userGroupId}`,
        {
          name: data.name,
          description: data.description,
        }
      );

      if (response.data) {
        await fetchUserGroupDetails();
      }
    } catch (error) {
      console.error("Failed to update user group:", error);
      setError("Failed to update user group");
    } finally {
      setUpdating(false);
    }
  };

  const handleAssignRoles = async (roleIds: number[]) => {
    if (!userGroup || roleIds.length === 0) return;

    try {
      setUpdating(true);
      await api.post(`/v1/user-groups/${userGroup.userGroupId}/roles`, {
        roleIds: roleIds,
      });

      await fetchUserGroupDetails();
    } catch (error) {
      console.error("Failed to assign roles:", error);
      setError("Failed to assign roles");
    } finally {
      setUpdating(false);
    }
  };

  const handleRemoveRoleAssignment = async (assignmentId: number) => {
    if (!userGroup) return;

    try {
      setUpdating(true);
      await api.delete(
        `/v1/user-groups/${userGroup.userGroupId}/role-assignments/${assignmentId}`
      );
      await fetchUserGroupDetails();
    } catch (error) {
      console.error("Failed to remove role assignment:", error);
      setError("Failed to remove role assignment");
    } finally {
      setUpdating(false);
    }
  };

  const handleDeleteUserGroup = async () => {
    if (!userGroup || !canManageUsers) return;

    try {
      setUpdating(true);
      await api.delete(`/v1/user-groups/${userGroup.userGroupId}`);
      navigate("/user-groups");
    } catch (error) {
      console.error("Failed to delete user group:", error);
      setError("Failed to delete user group");
      setUpdating(false);
    }
  };

  const handleStatusUpdate = async (action: "activate" | "deactivate") => {
    if (!userGroup) return;

    try {
      setUpdating(true);
      // For now, this is a placeholder - the actual API endpoint would depend on your backend
      // You might need to implement specific endpoints for activating/deactivating user groups
      console.log(`${action} user group:`, userGroup.userGroupId);

      // Refresh the user group details after status update
      await fetchUserGroupDetails();
    } catch (error) {
      console.error(`Failed to ${action} user group:`, error);
      setError(`Failed to ${action} user group`);
    } finally {
      setUpdating(false);
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("en-US", {
      year: "numeric",
      month: "long",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  const getStatusIcon = (memberCount: number) => {
    if (memberCount > 0) {
      return <CheckCircle className="h-5 w-5 text-green-600" />;
    }
    return <XCircle className="h-5 w-5 text-gray-600" />;
  };

  const getStatusVariant = (memberCount: number) => {
    if (memberCount > 0) {
      return "success";
    }
    return "secondary";
  };

  if (loading) {
    return (
      <div className="p-6">
        <div className="flex items-center gap-4">
          <Skeleton className="h-10 w-10" />
          <div className="space-y-2">
            <Skeleton className="h-8 w-48" />
            <Skeleton className="h-4 w-32" />
          </div>
        </div>
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mt-6">
          <div className="lg:col-span-2 space-y-6">
            <Card>
              <CardHeader>
                <Skeleton className="h-6 w-32" />
              </CardHeader>
              <CardContent>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  {[...Array(4)].map((_, i) => (
                    <div key={i} className="space-y-2">
                      <Skeleton className="h-4 w-20" />
                      <Skeleton className="h-8 w-full" />
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    );
  }

  if (error || !userGroup) {
    return (
      <div className="p-6">
        <div className="text-center py-12">
          <Users className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
          <h3 className="text-lg font-semibold mb-2">
            {error || "User group not found"}
          </h3>
          <p className="text-muted-foreground mb-4">
            {error ||
              "The user group you're looking for doesn't exist or has been removed."}
          </p>
          <Button onClick={() => navigate("/user-groups")}>
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back to User Groups
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="p-6">
      {/* Header */}
      <div className="mb-8">
        <Breadcrumb className="mb-4">
          <BreadcrumbList>
            <BreadcrumbItem>
              <BreadcrumbLink asChild>
                <Link to="/user-groups">User Groups</Link>
              </BreadcrumbLink>
            </BreadcrumbItem>
            <BreadcrumbSeparator />
            <BreadcrumbItem>
              <BreadcrumbPage>{userGroup.name}</BreadcrumbPage>
            </BreadcrumbItem>
          </BreadcrumbList>
        </Breadcrumb>

        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold">{userGroup.name}</h1>
            <p className="text-muted-foreground">
              {userGroup.description || "No description provided"}
            </p>
          </div>
          <div className="flex items-center gap-3">
            <PermissionGuard permission="user-groups:delete">
              <AlertDialog>
                <AlertDialogTrigger asChild>
                  <Button variant="destructive" size="sm" disabled={updating}>
                    <Trash2 className="h-4 w-4 mr-2" />
                    Delete
                  </Button>
                </AlertDialogTrigger>
                <AlertDialogContent>
                  <AlertDialogHeader>
                    <AlertDialogTitle>Delete User Group</AlertDialogTitle>
                    <AlertDialogDescription>
                      Are you sure you want to delete the user group "
                      {userGroup.name}"? This action cannot be undone.
                    </AlertDialogDescription>
                  </AlertDialogHeader>
                  <AlertDialogFooter>
                    <AlertDialogCancel>Cancel</AlertDialogCancel>
                    <AlertDialogAction
                      className="bg-destructive text-white"
                      onClick={handleDeleteUserGroup}
                    >
                      Delete
                    </AlertDialogAction>
                  </AlertDialogFooter>
                </AlertDialogContent>
              </AlertDialog>
            </PermissionGuard>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column - Main Information */}
        <div className="lg:col-span-2 space-y-6">
          {/* Basic Information */}
          <BasicInformationCard
            userGroup={{
              id: userGroup.userGroupId,
              userGroupId: userGroup.userGroupId,
              name: userGroup.name,
              description: userGroup.description,
              memberCount: userGroup.memberCount,
            }}
            onUpdate={handleBasicInfoUpdate}
            updating={updating}
          />

          {/* Role Assignments */}
          <RoleAssignmentsCard
            roleAssignments={userGroup.roleAssignments || []}
            availableModules={availableModules}
            availableRoles={availableRoles}
            onAssignRoles={handleAssignRoles}
            onRemoveRoleAssignment={handleRemoveRoleAssignment}
            updating={updating}
          />
        </div>

        {/* Right Column - Actions & Statistics */}
        <div className="space-y-6">
          {/* Status Management */}
          <UserGroupStatusCard
            memberCount={userGroup.memberCount}
            onStatusUpdate={handleStatusUpdate}
            updating={updating}
          />

          {/* Statistics */}
          {stats && (
            <StatisticsCard
              title="Statistics"
              layout="grid"
              statistics={[
                {
                  label: "Members",
                  value: stats.totalMembers,
                  icon: <Users className="h-6 w-6 text-blue-600" />,
                  className: "bg-blue-100",
                },
                {
                  label: "Roles",
                  value: stats.totalRoles,
                  icon: <Shield className="h-6 w-6 text-purple-600" />,
                  className: "bg-purple-100",
                },
                {
                  label: "Status",
                  value: stats.totalMembers > 0 ? "Active" : "Empty",
                  icon: <Activity className="h-6 w-6 text-green-600" />,
                  className: "bg-green-100",
                },
              ]}
            />
          )}

          {/* Audit Information */}
          <PermissionGuard permission="audit:read">
            <Card>
              <CardHeader>
                <CardTitle>Audit Information</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div className="space-y-2">
                    <Label>Created At</Label>
                    <div className="flex items-center gap-2 text-sm text-muted-foreground">
                      <Calendar className="h-4 w-4" />
                      {userGroup.createdAt
                        ? formatDate(userGroup.createdAt)
                        : "N/A"}
                    </div>
                  </div>

                  <div className="space-y-2">
                    <Label>Updated At</Label>
                    <div className="flex items-center gap-2 text-sm text-muted-foreground">
                      <Calendar className="h-4 w-4" />
                      {userGroup.updatedAt
                        ? formatDate(userGroup.updatedAt)
                        : "N/A"}
                    </div>
                  </div>

                  <div className="space-y-2">
                    <Label>Created By</Label>
                    <div className="flex items-center gap-2 text-sm text-muted-foreground">
                      <User className="h-4 w-4" />
                      {userGroup.createdBy || "System"}
                    </div>
                  </div>

                  <div className="space-y-2">
                    <Label>Updated By</Label>
                    <div className="flex items-center gap-2 text-sm text-muted-foreground">
                      <User className="h-4 w-4" />
                      {userGroup.updatedBy || "System"}
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>
          </PermissionGuard>

          {/* Quick Actions */}
          {/* <Card>
            <CardHeader>
              <CardTitle>Quick Actions</CardTitle>
              <CardDescription>
                Manage group members and view activity
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-3">
              <PermissionGuard permission="users:read">
                <Button
                  variant="outline"
                  className="w-full justify-start"
                  asChild
                >
                  <Link to={`/users?group=${userGroup.userGroupId}`}>
                    <Users className="h-4 w-4 mr-2" />
                    View Members ({userGroup.memberCount})
                  </Link>
                </Button>
              </PermissionGuard>

              <PermissionGuard permission="user-groups:manage-roles">
                <Button variant="outline" className="w-full justify-start">
                  <Shield className="h-4 w-4 mr-2" />
                  Manage Roles ({userGroup.roleAssignments?.length || 0})
                </Button>
              </PermissionGuard>

              <PermissionGuard permission="audit:read">
                <Button
                  variant="outline"
                  className="w-full justify-start"
                  asChild
                >
                  <Link to={`/activity?group=${userGroup.userGroupId}`}>
                    <Activity className="h-4 w-4 mr-2" />
                    View Activity
                  </Link>
                </Button>
              </PermissionGuard>
            </CardContent>
          </Card> */}
        </div>
      </div>
    </div>
  );
}
