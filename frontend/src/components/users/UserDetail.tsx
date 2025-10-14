import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { ArrowLeft } from "lucide-react";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
// Removed inline Skeleton in favor of a reusable LoadingSpinner
import LoadingSpinner from "@/components/common/LoadingSpinner";
import { PermissionGuard } from "@/components/common/PermissionGuard";
import { StatisticsCard, DetailHeaderCard } from "@/components/common";
import { usePermissions } from "@/hooks/usePermissions";
import httpClient from "@/lib/httpClient";
import { type User, type UserStats } from "@/types";
import UserPasswordCard from "./UserPasswordCard";
import type { PasswordChangeForm } from "@/types";
import { UserInfoCard, UserRoleGroupCard, UserStatusCard } from "./index";

const UserDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { canViewUsers, canUpdateUsers, canDeleteUsers, canManageUsers } =
    usePermissions();

  const [user, setUser] = useState<User | null>(null);
  const [stats, setStats] = useState<UserStats | null>(null);
  const [updating, setUpdating] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [availableRoles, setAvailableRoles] = useState<any[]>([]);
  const [availableGroups, setAvailableGroups] = useState<any[]>([]);
  const [hasFetched, setHasFetched] = useState(false);

  useEffect(() => {
    if (!canViewUsers) {
      navigate("/unauthorized");
      return;
    }

    fetchUserData();
    fetchAvailableRolesAndGroups();
  }, [id, canViewUsers, navigate]);

  const fetchAvailableRolesAndGroups = async () => {
    try {
      const [rolesData, groupsData] = await Promise.all([
        httpClient.getRoles(),
        httpClient.getUserGroups(),
      ]);

      setAvailableRoles(rolesData || []);
      // Extract content array from paginated response and transform to match UserGroup interface
      const groups = groupsData?.content || [];
      const transformedGroups = groups.map((group: any) => ({
        id: group.userGroupId,
        userGroupId: group.userGroupId,
        name: group.name,
        description: group.description || "",
        memberCount: group.memberCount || 0,
        roleCount: group.roleCount || 0,
        userGroupStatus: group.userGroupStatus || "ACTIVE",
      }));
      setAvailableGroups(transformedGroups);
    } catch (error) {
      console.error("Error fetching available roles and groups:", error);
    }
  };

  const fetchUserData = async () => {
    try {
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
        roles:
          userData.roles?.map((role) => ({
            id: role.id,
            name: role.name,
            description: role.description || "",
            permissions: role.permissions || [],
          })) || [],
        userGroups:
          userData.userGroups?.map((group) => ({
            id: group.id || group.userGroupId,
            userGroupId: group.userGroupId,
            name: group.name,
            description: group.description || "",
            memberCount: group.memberCount || 0,
            roleCount: group.roleCount || 0,
            userGroupStatus: group.userGroupStatus || "ACTIVE",
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
      setHasFetched(true);
    }
  };

  const handleStatusUpdate = async (newStatus: "ACTIVE" | "INACTIVE") => {
    if (!user || !canUpdateUsers) return;

    try {
      setUpdating(true);

      // API call to update user status
      await httpClient.updateUserStatus(user.id, newStatus);

      setUser((prev: User | null) =>
        prev
          ? {
              ...prev,
              userStatus: newStatus,
              updatedAt: new Date().toISOString(),
            }
          : null
      );
    } catch (error) {
      console.error("Error updating user status:", error);
      setError("Failed to update user status");
    } finally {
      setUpdating(false);
    }
  };

  const handleUserInfoUpdate = async (updatedUserData: any) => {
    try {
      // Update the local user state with the new data
      setUser((prev: User | null) =>
        prev ? { ...prev, ...updatedUserData } : null
      );

      // Optionally refresh the complete user data from the server
      await fetchUserData();
    } catch (error) {
      console.error("Error refreshing user data:", error);
    }
  };

  const handlePasswordChange = async (passwordData: PasswordChangeForm) => {
    if (!user || !canUpdateUsers) return;

    try {
      setUpdating(true);

      const response = await fetch(`/api/v1/users/${user.id}/change-password`, {
        method: "PATCH",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
        body: JSON.stringify({
          newPassword: passwordData.newPassword,
        }),
      });

      if (!response.ok) {
        throw new Error("Failed to change password");
      }

      // Update user's password changed timestamp
      setUser((prev: User | null) =>
        prev
          ? {
              ...prev,
              passwordChangedAt: new Date().toISOString(),
              updatedAt: new Date().toISOString(),
            }
          : null
      );
    } catch (error) {
      console.error("Error changing password:", error);
      throw error; // Re-throw to let the component handle the error display
    } finally {
      setUpdating(false);
    }
  };

  const handleRoleAdd = async (roleId: string) => {
    try {
      setUpdating(true);
      const response = await fetch(`/api/v1/users/${id}/roles`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
        body: JSON.stringify({ roleIds: [parseInt(roleId)] }),
      });

      if (response.ok) {
        await fetchUserData();
      } else {
        console.error("Failed to add role");
      }
    } catch (error) {
      console.error("Error adding role:", error);
    } finally {
      setUpdating(false);
    }
  };

  const handleRoleRemove = async (roleId: string) => {
    try {
      setUpdating(true);
      const response = await fetch(`/api/v1/users/${id}/roles/${roleId}`, {
        method: "DELETE",
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
      });

      if (response.ok) {
        await fetchUserData();
      } else {
        console.error("Failed to remove role");
      }
    } catch (error) {
      console.error("Error removing role:", error);
    } finally {
      setUpdating(false);
    }
  };

  const handleGroupAdd = async (groupId: string) => {
    try {
      setUpdating(true);
      const response = await fetch(`/api/v1/users/${id}/user-groups`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
        body: JSON.stringify({ userGroupIds: [parseInt(groupId)] }),
      });

      if (response.ok) {
        await fetchUserData();
      } else {
        console.error("Failed to add user group");
      }
    } catch (error) {
      console.error("Error adding user group:", error);
    } finally {
      setUpdating(false);
    }
  };

  const handleGroupRemove = async (groupId: string) => {
    try {
      setUpdating(true);
      const response = await fetch(
        `/api/v1/users/${id}/user-groups/${groupId}`,
        {
          method: "DELETE",
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        }
      );

      if (response.ok) {
        await fetchUserData();
      } else {
        console.error("Failed to remove user group");
      }
    } catch (error) {
      console.error("Error removing user group:", error);
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

  // Show a loading spinner until initial fetch completes
  if (!hasFetched) {
    return <LoadingSpinner variant="page" />;
  }

  if (error || !user) {
    throw new Error(error || "User not found");
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Header */}
        <DetailHeaderCard
          title={getFullName(user)}
          description={`@${user.username} • ${user.email}`}
          breadcrumbs={[
            { label: "Users", href: "/users" },
            { label: getFullName(user) },
          ]}
        />

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Main Content */}
          <div className="lg:col-span-2 space-y-8">
            {/* User Information */}
            <UserInfoCard
              user={user}
              showExtendedInfo={true}
              canUpdate={canUpdateUsers}
              onUserUpdated={handleUserInfoUpdate}
            />

            {/* Password Management */}
            <PermissionGuard permission="USER_MGMT:update">
              <UserPasswordCard
                onPasswordChange={handlePasswordChange}
                loading={updating}
                canUpdate={canUpdateUsers}
              />
            </PermissionGuard>

            {/* Roles & Groups */}
            <UserRoleGroupCard
              user={user}
              availableRoles={availableRoles}
              availableGroups={availableGroups}
              onRoleAdd={handleRoleAdd}
              onRoleRemove={handleRoleRemove}
              onGroupAdd={handleGroupAdd}
              onGroupRemove={handleGroupRemove}
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
              <StatisticsCard
                statistics={[
                  {
                    label: "Total Roles",
                    value: stats.totalRoles,
                  },
                  {
                    label: "User Groups",
                    value: stats.totalUserGroups,
                  },
                  {
                    label: "Account Status",
                    value: stats.accountStatus,
                    className:
                      stats.accountStatus === "ACTIVE"
                        ? "text-green-600"
                        : "text-red-600",
                  },
                  ...(stats.lastLogin
                    ? [
                        {
                          label: "Last Login",
                          value: new Date(stats.lastLogin).toLocaleDateString(
                            "en-US",
                            {
                              year: "numeric",
                              month: "long",
                              day: "numeric",
                              hour: "2-digit",
                              minute: "2-digit",
                            }
                          ),
                        },
                      ]
                    : []),
                ]}
              />
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default UserDetail;
