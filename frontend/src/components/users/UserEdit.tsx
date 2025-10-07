import React, { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import RoleAssignment from "./UserRoleAssignment";
import UserGroupAssignment from "./UserGroupAssignment";
import { usePermissions } from "@/hooks/usePermissions";
import {
  User as UserIcon,
  AlertTriangle,
  CheckCircle,
  Shield,
} from "lucide-react";
import httpClient from "@/lib/httpClient";
import {
  UserStatusCard,
  UserPasswordCard,
  type User,
  type PasswordChangeForm,
  type UserEditProps,
} from "./index";
import UserEditInfoCard from "./UserEditInfoCard";

const UserEdit: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const userId = id ? parseInt(id, 10) : null;
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(false);
  const [fetchLoading, setFetchLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState("profile");

  const [passwordForm, setPasswordForm] = useState<PasswordChangeForm>({
    newPassword: "",
    confirmPassword: "",
  });

  const { canUpdateUsers } = usePermissions();

  const fetchUser = async () => {
    if (!userId) return;

    try {
      setFetchLoading(true);
      setError(null);
      const userData = await httpClient.getUserById(Number(userId));
      // Transform the user data to match the component's User type
      const user: User = {
        ...userData,
        userStatus: userData.userStatus as "ACTIVE" | "INACTIVE",
        roles:
          userData.roles?.map((role) => ({
            id: role.id,
            name: role.name,
            description: role.description || "",
          })) || [],
        userGroups:
          userData.userGroups?.map((group) => ({
            userGroupId: group.userGroupId,
            name: group.name,
            description: group.description || "",
          })) || [],
      };
      setUser(user);
    } catch (err) {
      setError("Failed to fetch user data");
      console.error("Error fetching user:", err);
    } finally {
      setFetchLoading(false);
    }
  };

  useEffect(() => {
    fetchUser();
  }, [userId]);

  useEffect(() => {
    if (user) {
      setPasswordForm({ newPassword: "", confirmPassword: "" });
      setError(null);
      setSuccess(null);
    }
  }, [user]);

  const handleUserUpdated = async () => {
    if (!userId) return;

    try {
      // Refresh user data after update
      await fetchUser();
    } catch (err) {
      console.error("Error refreshing user data:", err);
    }
  };

  const handleStatusChange = async (newStatus: "ACTIVE" | "INACTIVE") => {
    if (!user) return;

    try {
      setLoading(true);
      setError(null);

      const endpoint = newStatus === "ACTIVE" ? "activate" : "deactivate";
      const response = await fetch(`/api/v1/users/${user.id}/${endpoint}`, {
        method: "PUT",
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
      });

      if (!response.ok) {
        throw new Error(`Failed to ${newStatus.toLowerCase()} user`);
      }

      setSuccess(`User ${newStatus.toLowerCase()}d successfully`);
      handleUserUpdated();
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : `Failed to ${newStatus.toLowerCase()} user`
      );
    } finally {
      setLoading(false);
    }
  };

  const handlePasswordChange = async () => {
    if (!user) return;

    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      setError("Passwords do not match");
      return;
    }

    if (passwordForm.newPassword.length < 6) {
      setError("Password must be at least 6 characters long");
      return;
    }

    try {
      setLoading(true);
      setError(null);

      const response = await fetch(`/api/v1/users/${user.id}/change-password`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
        body: JSON.stringify({
          newPassword: passwordForm.newPassword,
        }),
      });

      if (!response.ok) {
        throw new Error("Failed to change password");
      }

      setSuccess("Password changed successfully");
      setPasswordForm({ newPassword: "", confirmPassword: "" });
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "Failed to change password"
      );
    } finally {
      setLoading(false);
    }
  };

  if (!userId) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="text-center">
          <UserIcon className="mx-auto h-12 w-12 text-gray-400" />
          <p className="mt-2 text-gray-500">No user selected.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto p-6 space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900 flex items-center gap-2">
            <UserIcon className="h-8 w-8" />
            {fetchLoading
              ? "Loading..."
              : user
              ? `Edit User: ${user.firstName} ${user.lastName}`
              : "User Not Found"}
          </h1>
          {user && (
            <>
              <p className="mt-2 text-gray-600">
                @{user.username} • {user.email}
              </p>
              <p className="text-sm text-gray-500">
                Manage user status, password, roles, and permissions
              </p>
            </>
          )}
        </div>
      </div>

      {/* Loading State */}
      {fetchLoading && (
        <div className="flex items-center justify-center py-12">
          <div className="text-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto mb-4"></div>
            <p className="text-lg text-muted-foreground">
              Loading user data...
            </p>
          </div>
        </div>
      )}

      {/* Error State */}
      {!fetchLoading && !user && (
        <div className="text-center py-12">
          <UserIcon className="mx-auto h-16 w-16 text-gray-400" />
          <p className="mt-4 text-lg text-gray-500">
            User not found or access denied.
          </p>
        </div>
      )}

      {/* Main Content */}
      {!fetchLoading && user && (
        <>
          {/* Alerts */}
          {error && (
            <Alert variant="destructive">
              <AlertTriangle className="h-4 w-4" />
              <AlertDescription>{error}</AlertDescription>
            </Alert>
          )}

          {success && (
            <Alert>
              <CheckCircle className="h-4 w-4" />
              <AlertDescription>{success}</AlertDescription>
            </Alert>
          )}

          {/* Tabs */}
          <Tabs
            value={activeTab}
            onValueChange={setActiveTab}
            className="w-full"
          >
            <TabsList className="grid w-full grid-cols-4">
              <TabsTrigger value="profile">Profile</TabsTrigger>
              <TabsTrigger value="status">Status</TabsTrigger>
              <TabsTrigger value="password">Password</TabsTrigger>
              <TabsTrigger value="permissions">Permissions</TabsTrigger>
            </TabsList>

            <div className="mt-6 space-y-6">
              <TabsContent value="profile" className="space-y-4 mt-0">
                <UserEditInfoCard 
                  user={user} 
                  onUserUpdated={handleUserUpdated}
                  canUpdate={canUpdateUsers}
                />
              </TabsContent>

              <TabsContent value="status" className="space-y-4 mt-0">
                <UserStatusCard
                  user={user}
                  onStatusChange={handleStatusChange}
                  loading={loading}
                  canUpdate={canUpdateUsers}
                />
              </TabsContent>

              <TabsContent value="password" className="space-y-4 mt-0">
                <UserPasswordCard
                  onPasswordChange={handlePasswordChange}
                  loading={loading}
                  canUpdate={canUpdateUsers}
                />
              </TabsContent>

              <TabsContent value="permissions" className="space-y-4 mt-0">
                <div className="space-y-6">
                  {/* Roles Card */}
                  <Card>
                    <CardHeader>
                      <CardTitle className="flex items-center text-lg font-semibold text-gray-900">
                        <Shield className="mr-2 h-5 w-5" />
                        Role Assignment
                      </CardTitle>
                      <CardDescription>
                        Manage user roles and permissions
                      </CardDescription>
                    </CardHeader>
                    <CardContent>
                      <RoleAssignment
                        userId={user.id}
                        currentRoles={user.roles || []}
                        onRolesUpdated={handleUserUpdated}
                        loading={loading}
                      />
                    </CardContent>
                  </Card>

                  {/* User Groups Card */}
                  <Card>
                    <CardHeader>
                      <CardTitle className="flex items-center text-lg font-semibold text-gray-900">
                        <UserIcon className="mr-2 h-5 w-5" />
                        User Group Assignment
                      </CardTitle>
                      <CardDescription>
                        Manage user group memberships
                      </CardDescription>
                    </CardHeader>
                    <CardContent>
                      <UserGroupAssignment
                        userId={user.id}
                        currentUserGroups={user.userGroups || []}
                        onUserGroupsUpdated={handleUserUpdated}
                        loading={loading}
                      />
                    </CardContent>
                  </Card>
                </div>
              </TabsContent>
            </div>
          </Tabs>
        </>
      )}
    </div>
  );
};

export default UserEdit;
