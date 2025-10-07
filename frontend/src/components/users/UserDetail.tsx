import React, { useState, useEffect } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import { ArrowLeft, Edit, Trash2 } from "lucide-react";
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
import { PermissionGuard } from "../PermissionGuard";
import { StatusBadge } from "../StatusBadge";
import { usePermissions } from "@/hooks/usePermissions";
import { normalizeEntityStatus } from "@/lib/status-colors";
import httpClient from "@/lib/httpClient";
import {
  UserInfoCard,
  UserStatusCard,
  UserRoleGroupCard,
  type User,
  type UserStats,
} from "./index";

const UserDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const {
    canViewUsers,
    canUpdateUsers,
    canDeleteUsers,
    canManageUsers,
  } = usePermissions();

  const [user, setUser] = useState<User | null>(null);
  const [stats, setStats] = useState<UserStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [updating, setUpdating] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!canViewUsers) {
      navigate("/unauthorized");
      return;
    }

    fetchUserData();
  }, [id, canViewUsers, navigate]);

  const fetchUserData = async () => {
    try {
      setLoading(true);

      // Fetch user data from API
      const userData = await httpClient.getUserById(Number(id));

      // Transform user data to match frontend interface
      const transformedUser: User = {
        id: userData.id,
        username: userData.username,
        email: userData.email,
        firstName: userData.firstName,
        lastName: userData.lastName,
        userStatus: userData.userStatus as "ACTIVE" | "INACTIVE",
        createdAt: userData.createdAt,
        updatedAt: userData.updatedAt,
        roles: userData.roles?.map(role => ({
          id: role.id,
          name: role.name,
          description: role.description || ""
        })) || [],
        userGroups: userData.userGroups?.map(group => ({
          userGroupId: group.userGroupId,
          name: group.name,
          description: group.description || ""
        })) || [],
        phoneNumber: userData.phoneNumber,
        emailVerified: userData.emailVerified,
        lastLogin: userData.lastLogin,
        failedLoginAttempts: userData.failedLoginAttempts,
        createdBy: userData.createdBy || "system",
        updatedBy: userData.updatedBy || "system",
      };

      // Calculate stats
      const userStats: UserStats = {
        totalRoles: transformedUser.roles.length,
        totalUserGroups: transformedUser.userGroups.length,
        lastLogin: transformedUser.lastLogin || null,
        accountStatus: transformedUser.userStatus,
      };

      setUser(transformedUser);
      setStats(userStats);
    } catch (error) {
      console.error("Error fetching user data:", error);
      setError("Failed to load user data");
    } finally {
      setLoading(false);
    }
  };

  const handleStatusUpdate = async (newStatus: "ACTIVE" | "INACTIVE") => {
    if (!user || !canUpdateUsers) return;

    try {
      setUpdating(true);

      // API call to update user status
      await httpClient.updateUserStatus(user.id, newStatus);

      setUser((prev) =>
        prev
          ? { ...prev, userStatus: newStatus, updatedAt: new Date().toISOString() }
          : null
      );
    } catch (error) {
      console.error("Error updating user status:", error);
      setError("Failed to update user status");
    } finally {
      setUpdating(false);
    }
  };

  const handleDeleteUser = async () => {
    if (!user || !canDeleteUsers) return;

    if (
      !window.confirm(
        `Are you sure you want to delete the user "${user.username}"? This action cannot be undone.`
      )
    ) {
      return;
    }

    try {
      setUpdating(true);

      // API call to delete user
      await httpClient.deleteUser(user.id);

      navigate("/users");
    } catch (error) {
      console.error("Error deleting user:", error);
      setError("Failed to delete user");
      setUpdating(false);
    }
  };

  const getFullName = (user: User) => {
    return `${user.firstName} ${user.lastName}`.trim();
  };

  if (!canViewUsers) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <Card className="w-full max-w-md">
          <CardHeader>
            <CardTitle>Access Denied</CardTitle>
            <CardDescription>
              You don't have permission to view users.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <Button onClick={() => navigate("/dashboard")} className="w-full">
              <ArrowLeft className="mr-2 h-4 w-4" />
              Back to Dashboard
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 py-8">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="space-y-6">
            <Skeleton className="h-8 w-64" />
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              <Skeleton className="h-32" />
              <Skeleton className="h-32" />
              <Skeleton className="h-32" />
            </div>
            <Skeleton className="h-64" />
          </div>
        </div>
      </div>
    );
  }

  if (error || !user) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <Card className="w-full max-w-md">
          <CardHeader>
            <CardTitle>User Not Found</CardTitle>
            <CardDescription>
              {error || "The requested user could not be found."}
            </CardDescription>
          </CardHeader>
          <CardContent>
            <Button onClick={() => navigate("/users")} className="w-full">
              <ArrowLeft className="mr-2 h-4 w-4" />
              Back to Users
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
                  <Link to="/users">Users</Link>
                </BreadcrumbLink>
              </BreadcrumbItem>
              <BreadcrumbSeparator />
              <BreadcrumbItem>
                <BreadcrumbPage>{getFullName(user)}</BreadcrumbPage>
              </BreadcrumbItem>
            </BreadcrumbList>
          </Breadcrumb>

          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-3xl font-bold text-gray-900">
                {getFullName(user)}
              </h1>
              <p className="mt-2 text-gray-600">@{user.username}</p>
              <p className="text-gray-600">{user.email}</p>
            </div>

            <div className="flex items-center space-x-3">
              <StatusBadge
                status={normalizeEntityStatus("user", user.userStatus)}
              />

              <PermissionGuard permission="user:update">
                <div className="flex space-x-2">
                  {user.userStatus === "ACTIVE" ? (
                    <Button
                      onClick={() => handleStatusUpdate("INACTIVE")}
                      disabled={updating}
                      variant="destructive"
                      size="sm"
                    >
                      Deactivate
                    </Button>
                  ) : (
                    <Button
                      onClick={() => handleStatusUpdate("ACTIVE")}
                      disabled={updating}
                      variant="default"
                      size="sm"
                      className="bg-green-600 hover:bg-green-700"
                    >
                      Activate
                    </Button>
                  )}

                  <Button asChild size="sm">
                    <Link to={`/users/${user.id}/edit`}>
                      <Edit className="mr-2 h-4 w-4" />
                      Edit
                    </Link>
                  </Button>
                </div>
              </PermissionGuard>

              <PermissionGuard permission="user:delete">
                <Button
                  onClick={handleDeleteUser}
                  disabled={updating}
                  variant="destructive"
                  size="sm"
                >
                  <Trash2 className="mr-2 h-4 w-4" />
                  Delete
                </Button>
              </PermissionGuard>
            </div>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Main Content */}
          <div className="lg:col-span-2 space-y-8">
            {/* User Information */}
            <UserInfoCard user={user} showExtendedInfo={true} />

            {/* Roles & Groups */}
            <UserRoleGroupCard 
              user={user}
              canUpdate={canManageUsers}
              loading={updating}
            />
          </div>

          {/* Sidebar */}
          <div className="space-y-6">
            {/* User Status Management */}
            <UserStatusCard
              user={user}
              onStatusChange={handleStatusUpdate}
              loading={updating}
              canUpdate={canUpdateUsers}
            />

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
                    <span className="text-sm text-gray-600">Total Roles</span>
                    <span className="text-sm font-medium text-gray-900">
                      {stats.totalRoles}
                    </span>
                  </div>

                  <div className="flex justify-between">
                    <span className="text-sm text-gray-600">User Groups</span>
                    <span className="text-sm font-medium text-gray-900">
                      {stats.totalUserGroups}
                    </span>
                  </div>

                  <div className="flex justify-between">
                    <span className="text-sm text-gray-600">Account Status</span>
                    <span className={`text-sm font-medium ${
                      stats.accountStatus === "ACTIVE" ? "text-green-600" : "text-red-600"
                    }`}>
                      {stats.accountStatus}
                    </span>
                  </div>

                  {stats.lastLogin && (
                    <div className="pt-4 border-t border-gray-200">
                      <span className="text-sm text-gray-600">Last Login</span>
                      <p className="text-sm font-medium text-gray-900">
                        {new Date(stats.lastLogin).toLocaleDateString("en-US", {
                          year: "numeric",
                          month: "long",
                          day: "numeric",
                          hour: "2-digit",
                          minute: "2-digit",
                        })}
                      </p>
                    </div>
                  )}
                </CardContent>
              </Card>
            )}

            {/* Quick Actions */}
            <Card>
              <CardHeader>
                <CardTitle className="text-lg font-semibold text-gray-900">
                  Quick Actions
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-3">
                <PermissionGuard permission="user:update">
                  <Button asChild className="w-full" size="sm">
                    <Link to={`/users/${user.id}/edit`}>
                      <Edit className="mr-2 h-4 w-4" />
                      Edit User
                    </Link>
                  </Button>
                </PermissionGuard>

                <Button
                  asChild
                  className="w-full"
                  variant="outline"
                  size="sm"
                >
                  <Link to="/users">
                    <ArrowLeft className="mr-2 h-4 w-4" />
                    Back to Users
                  </Link>
                </Button>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </div>
  );
};

export default UserDetail;