import React, { useState, useEffect } from "react";
import { useNavigate, useParams, Link } from "react-router-dom";
import {
  Users,
  ArrowLeft,
  Edit,
  Shield,
  Activity,
  CheckCircle,
  XCircle,
  Clock,
  Calendar,
  User,
  Trash2,
  Settings,
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
import { Badge } from "@/components/ui/badge";
import { PermissionGuard } from "@/components/PermissionGuard";
import { usePermissions } from "@/hooks/usePermissions";
import api from "@/lib/api";

interface RoleAssignment {
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
}

interface UserGroupDetails {
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

interface UserGroupStats {
  totalMembers: number;
  totalRoles: number;
  activeRoles: number;
  lastActivity: string | null;
}

export default function UserGroupDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { canViewUsers, canManageUsers } = usePermissions();

  const [userGroup, setUserGroup] = useState<UserGroupDetails | null>(null);
  const [stats, setStats] = useState<UserGroupStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [updating, setUpdating] = useState(false);
  const [error, setError] = useState<string | null>(null);

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

  const handleDeleteUserGroup = async () => {
    if (!userGroup || !canManageUsers) return;

    if (
      !window.confirm(
        `Are you sure you want to delete the user group "${userGroup.name}"? This action cannot be undone.`
      )
    ) {
      return;
    }

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
            {getStatusIcon(userGroup.memberCount)}
            <PermissionGuard permission="user-groups:update">
              <Button asChild size="sm">
                <Link to={`/user-groups/${userGroup.userGroupId}/edit`}>
                  <Edit className="h-4 w-4 mr-2" />
                  Edit
                </Link>
              </Button>
            </PermissionGuard>
            <PermissionGuard permission="user-groups:delete">
              <Button
                variant="destructive"
                size="sm"
                onClick={handleDeleteUserGroup}
                disabled={updating}
              >
                <Trash2 className="h-4 w-4 mr-2" />
                Delete
              </Button>
            </PermissionGuard>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column - Main Information */}
        <div className="lg:col-span-2 space-y-6">
          {/* Basic Information */}
          <Card>
            <CardHeader>
              <CardTitle>Basic Information</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="space-y-2">
                  <Label>Group Name</Label>
                  <div className="text-sm">{userGroup.name}</div>
                </div>

                <div className="space-y-2">
                  <Label>Member Count</Label>
                  <div className="flex items-center gap-2">
                    <Users className="h-4 w-4 text-muted-foreground" />
                    <Badge variant="secondary">
                      {userGroup.memberCount}{" "}
                      {userGroup.memberCount === 1 ? "member" : "members"}
                    </Badge>
                  </div>
                </div>

                <div className="space-y-2 md:col-span-2">
                  <Label>Description</Label>
                  <div className="text-sm text-muted-foreground">
                    {userGroup.description || "No description provided"}
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Role Assignments */}
          <Card>
            <CardHeader>
              <CardTitle>Role Assignments</CardTitle>
              <CardDescription>
                Roles assigned to this user group
              </CardDescription>
            </CardHeader>
            <CardContent>
              {userGroup.roleAssignments && userGroup.roleAssignments.length > 0 ? (
                <div className="space-y-4">
                  {userGroup.roleAssignments.map((assignment) => (
                    <div
                      key={assignment.id}
                      className="border border-gray-200 rounded-lg p-4"
                    >
                      <div className="flex items-center justify-between">
                        <div className="flex-1">
                          <div className="flex items-center space-x-3">
                            <h3 className="text-lg font-medium text-gray-900">
                              {assignment.roleName}
                            </h3>
                            <Badge variant="outline">{assignment.moduleName}</Badge>
                          </div>
                          <p className="mt-1 text-sm text-gray-600">
                            {assignment.roleDescription}
                          </p>
                        </div>
                        <PermissionGuard permission="user-groups:manage-roles">
                          <Button variant="ghost" size="sm">
                            <Settings className="h-4 w-4" />
                          </Button>
                        </PermissionGuard>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="text-center py-8">
                  <Shield className="mx-auto h-12 w-12 text-gray-400" />
                  <p className="mt-2 text-gray-500">
                    No roles assigned to this user group.
                  </p>
                  <PermissionGuard permission="user-groups:manage-roles">
                    <Button variant="outline" className="mt-4">
                      <Shield className="h-4 w-4 mr-2" />
                      Assign Roles
                    </Button>
                  </PermissionGuard>
                </div>
              )}
            </CardContent>
          </Card>

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
                      {userGroup.createdAt ? formatDate(userGroup.createdAt) : "N/A"}
                    </div>
                  </div>

                  <div className="space-y-2">
                    <Label>Updated At</Label>
                    <div className="flex items-center gap-2 text-sm text-muted-foreground">
                      <Calendar className="h-4 w-4" />
                      {userGroup.updatedAt ? formatDate(userGroup.updatedAt) : "N/A"}
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
        </div>

        {/* Right Column - Actions & Statistics */}
        <div className="space-y-6">
          {/* Status Management */}
          <Card>
            <CardHeader>
              <CardTitle>Status Management</CardTitle>
              <CardDescription>
                Manage the user group's operational status
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-3">
              <PermissionGuard permission="user-groups:activate">
                <Button
                  variant={userGroup.memberCount > 0 ? "default" : "outline"}
                  className="w-full justify-start"
                  disabled={updating}
                >
                  <CheckCircle className="h-4 w-4 mr-2" />
                  Active ({userGroup.memberCount} members)
                </Button>
              </PermissionGuard>

              <PermissionGuard permission="user-groups:deactivate">
                <Button
                  variant={userGroup.memberCount === 0 ? "destructive" : "outline"}
                  className="w-full justify-start"
                  disabled={updating}
                >
                  <XCircle className="h-4 w-4 mr-2" />
                  Empty Group
                </Button>
              </PermissionGuard>
            </CardContent>
          </Card>

          {/* Statistics */}
          {stats && (
            <Card>
              <CardHeader>
                <CardTitle>Statistics</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                  <div className="text-center">
                    <div className="bg-blue-100 p-3 rounded-full w-12 h-12 flex items-center justify-center mx-auto mb-2">
                      <Users className="h-6 w-6 text-blue-600" />
                    </div>
                    <p className="text-2xl font-bold">{stats.totalMembers}</p>
                    <p className="text-sm text-muted-foreground">Members</p>
                  </div>

                  <div className="text-center">
                    <div className="bg-purple-100 p-3 rounded-full w-12 h-12 flex items-center justify-center mx-auto mb-2">
                      <Shield className="h-6 w-6 text-purple-600" />
                    </div>
                    <p className="text-2xl font-bold">{stats.totalRoles}</p>
                    <p className="text-sm text-muted-foreground">Roles</p>
                  </div>

                  <div className="text-center">
                    <div className="bg-green-100 p-3 rounded-full w-12 h-12 flex items-center justify-center mx-auto mb-2">
                      <Activity className="h-6 w-6 text-green-600" />
                    </div>
                    <p className="text-2xl font-bold">
                      {stats.totalMembers > 0 ? "Active" : "Empty"}
                    </p>
                    <p className="text-sm text-muted-foreground">Status</p>
                  </div>
                </div>
              </CardContent>
            </Card>
          )}

          {/* Quick Actions */}
          <Card>
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
                <Button
                  variant="outline"
                  className="w-full justify-start"
                >
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
          </Card>
        </div>
      </div>
    </div>
  );
}