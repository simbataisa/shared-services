import React, { useState, useEffect } from "react";
import { useNavigate, Link } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Alert, AlertDescription } from "@/components/ui/alert";
import {
  Breadcrumb,
  BreadcrumbItem,
  BreadcrumbLink,
  BreadcrumbList,
  BreadcrumbPage,
  BreadcrumbSeparator,
} from "@/components/ui/breadcrumb";
import { PermissionGuard } from "@/components/common/PermissionGuard";
import { usePermissions } from "@/hooks/usePermissions";
import httpClient from "@/lib/httpClient";
import type { CreateUserRequest, CreateUserForm } from "@/types";
import UserInfoFormCard from "./UserInfoFormCard";
import UserPasswordFormCard from "./UserPasswordFormCard";
import UserRoleGroupFormCard from "./UserRoleGroupFormCard";

interface Role {
  id: number;
  name: string;
  description?: string;
}

interface UserGroup {
  id: number;
  userGroupId: number;
  name: string;
  description?: string;
}

const UserCreate: React.FC = () => {
  const navigate = useNavigate();
  const { canCreateUsers } = usePermissions();

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [roles, setRoles] = useState<Role[]>([]);
  const [userGroups, setUserGroups] = useState<UserGroup[]>([]);

  const [createForm, setCreateForm] = useState<CreateUserForm>({
    username: "",
    email: "",
    firstName: "",
    lastName: "",
    password: "",
    roleIds: [],
    userGroupIds: [],
  });

  // Redirect if user doesn't have permission
  useEffect(() => {
    if (!canCreateUsers) {
      navigate("/unauthorized");
    }
  }, [canCreateUsers, navigate]);

  // Fetch roles and user groups
  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        const [rolesData, userGroupsData] = await Promise.all([
          httpClient.getRoles(),
          httpClient.getUserGroups(),
        ]);
        setRoles(Array.isArray(rolesData) ? rolesData : []);
        setUserGroups(Array.isArray(userGroupsData) ? userGroupsData : []);
      } catch (error) {
        console.error("Error fetching data:", error);
        setError("Failed to load roles and user groups");
        // Ensure arrays are set even on error
        setRoles([]);
        setUserGroups([]);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  const handleCreateUser = async (e: React.FormEvent) => {
    e.preventDefault();

    try {
      setLoading(true);
      setError(null);

      const userData: CreateUserRequest = {
        username: createForm.username,
        email: createForm.email,
        firstName: createForm.firstName,
        lastName: createForm.lastName,
        password: createForm.password,
        roleIds: createForm.roleIds,
        userGroupIds: createForm.userGroupIds,
      };

      await httpClient.createUser(userData);
      navigate("/users");
    } catch (error) {
      console.error("Error creating user:", error);
      setError("Failed to create user");
    } finally {
      setLoading(false);
    }
  };

  const handleFormChange = (
    field: keyof CreateUserForm,
    value: string | number[]
  ) => {
    setCreateForm((prev) => ({
      ...prev,
      [field]: value,
    }));
  };

  const handleRoleToggle = (roleId: number, checked: boolean) => {
    setCreateForm((prev) => ({
      ...prev,
      roleIds: checked
        ? [...prev.roleIds, roleId]
        : prev.roleIds.filter((id) => id !== roleId),
    }));
  };

  const handleUserGroupToggle = (userGroupId: number, checked: boolean) => {
    setCreateForm((prev) => ({
      ...prev,
      userGroupIds: checked
        ? [...prev.userGroupIds, userGroupId]
        : prev.userGroupIds.filter((id) => id !== userGroupId),
    }));
  };

  if (loading && roles.length === 0) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-lg">Loading...</div>
      </div>
    );
  }

  return (
    <PermissionGuard permission="USER_MGMT:create">
      <div className="container mx-auto py-4 px-4 sm:py-6 sm:px-6 lg:px-8 space-y-4 sm:space-y-6">
        {/* Breadcrumb */}
        <Breadcrumb>
          <BreadcrumbList>
            <BreadcrumbItem>
              <BreadcrumbLink asChild>
                <Link to="/">Dashboard</Link>
              </BreadcrumbLink>
            </BreadcrumbItem>
            <BreadcrumbSeparator />
            <BreadcrumbItem>
              <BreadcrumbLink asChild>
                <Link to="/users">Users</Link>
              </BreadcrumbLink>
            </BreadcrumbItem>
            <BreadcrumbSeparator />
            <BreadcrumbPage>Create User</BreadcrumbPage>
          </BreadcrumbList>
        </Breadcrumb>

        {/* Header */}
        <div className="flex flex-col sm:flex-row sm:items-center gap-2 sm:gap-4">
          <div className="flex-1">
            <h1 className="text-2xl sm:text-3xl font-bold tracking-tight">
              Create New User
            </h1>
            <p className="text-sm sm:text-base text-muted-foreground mt-1">
              Add a new user to the system with roles and permissions.
            </p>
          </div>
        </div>

        {error && (
          <Alert variant="destructive">
            <AlertDescription>{error}</AlertDescription>
          </Alert>
        )}

        <form onSubmit={handleCreateUser} className="space-y-4 sm:space-y-6">
          <div className="grid grid-cols-1 xl:grid-cols-2 gap-4 sm:gap-6">
            {/* User Information Card */}
            <div className="xl:col-span-2 order-1">
              <UserInfoFormCard
                formData={createForm}
                onFormChange={handleFormChange}
              />
            </div>

            {/* Password Card */}
            <div className="xl:col-span-2 order-2">
              <UserPasswordFormCard
                formData={createForm}
                onFormChange={handleFormChange}
              />
            </div>

            {/* Roles and User Groups */}
            <div className="xl:col-span-2 order-3">
              <UserRoleGroupFormCard
                availableRoles={roles}
                availableGroups={userGroups}
                selectedRoleIds={createForm.roleIds.map((id) => id.toString())}
                selectedGroupIds={createForm.userGroupIds.map((id) =>
                  id.toString()
                )}
                onRoleToggle={handleRoleToggle}
                onGroupToggle={handleUserGroupToggle}
                loading={loading}
                className="xl:col-span-1 order-3"
              />
            </div>
          </div>

          {/* Action Buttons */}
          <div className="flex flex-col sm:flex-row gap-3 sm:gap-4 pt-4 sm:pt-6">
            <Button
              type="submit"
              disabled={loading}
              className="w-full sm:w-auto order-2 sm:order-1"
            >
              {loading ? "Creating..." : "Create User"}
            </Button>
            <Button
              type="button"
              variant="outline"
              onClick={() => navigate("/users")}
              className="w-full sm:w-auto order-1 sm:order-2"
            >
              Cancel
            </Button>
          </div>
        </form>
      </div>
    </PermissionGuard>
  );
};

export default UserCreate;
