import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { UserTable } from "@/components/users/UserTable";
import { usePermissions } from "@/hooks/usePermissions";
import { Shield, Plus } from "lucide-react";
import type { User, Role, UserGroup } from "@/types";
import type { TableFilter } from "@/types/components";
import httpClient from "@/lib/httpClient";
import { Skeleton } from "@/components/ui/skeleton";

const UserList: React.FC = () => {
  const navigate = useNavigate();
  const [users, setUsers] = useState<User[]>([]);
  const [roles, setRoles] = useState<Role[]>([]);
  const [userGroups, setUserGroups] = useState<UserGroup[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [statusFilter, setStatusFilter] = useState<string>("all");

  const { canViewUsers, canManageUsers } = usePermissions();

  // Define status filter configuration
  const statusFilters: TableFilter[] = [
    {
      label: "Status",
      value: statusFilter,
      onChange: setStatusFilter,
      options: [
        { value: "all", label: "All Statuses" },
        { value: "ACTIVE", label: "Active" },
        { value: "INACTIVE", label: "Inactive" },
        { value: "SUSPENDED", label: "Suspended" },
      ],
      placeholder: "Filter by status",
      width: "w-48",
    },
  ];

  useEffect(() => {
    if (canViewUsers) {
      fetchUsers();
      fetchRoles();
      fetchUserGroups();
    }
  }, [canViewUsers]);

  const fetchUsers = async () => {
    try {
      setLoading(true);
      const result = await httpClient.getUsers();
      setUsers(result || []);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to fetch users");
    } finally {
      setLoading(false);
    }
  };

  const fetchRoles = async () => {
    try {
      const roles = await httpClient.getRoles();
      setRoles(roles || []);
    } catch (err) {
      console.error("Failed to fetch roles:", err);
    }
  };

  const fetchUserGroups = async () => {
    try {
      const page = await httpClient.getUserGroups();
      setUserGroups(page?.content || []);
    } catch (err) {
      console.error("Failed to fetch user groups:", err);
    }
  };

  const handleDeleteUser = async (userId: number) => {
    if (!confirm("Are you sure you want to delete this user?")) {
      return;
    }

    try {
      await httpClient.deleteUser(userId);
      fetchUsers();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to delete user");
    }
  };

  const handleEditUser = (user: User) => {
    navigate(`/users/${user.id}/edit`);
  };

  if (!canViewUsers) {
    return (
      <div className="container mx-auto py-6 px-4 sm:px-6 lg:px-8 max-w-7xl">
        <div className="p-4 text-sm text-amber-600 bg-amber-50 border border-amber-200 rounded-md flex items-center">
          <Shield className="h-4 w-4 mr-2" />
          You don't have permission to view users.
        </div>
      </div>
    );
  }

  return (
    <div className="w-full py-6 px-4 sm:px-6 lg:px-8">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-8 space-y-4 sm:space-y-0">
        <div className="space-y-1">
          <h1 className="text-2xl sm:text-3xl font-bold tracking-tight">
            User Management
          </h1>
          <p className="text-sm sm:text-base text-muted-foreground">
            Manage users, their roles, and permissions
          </p>
        </div>
      </div>

      {error && (
        <div className="mb-6 p-4 text-sm text-red-600 bg-red-50 border border-red-200 rounded-md">
          Error: {error}
        </div>
      )}

      {/* User Table with integrated functionality */}
      <div className="w-full">
        {loading ? (
          <div className="space-y-2">
            {[...Array(5)].map((_, i) => (
              <Skeleton key={i} className="h-12 w-full" />
            ))}
          </div>
        ) : (
          <UserTable
            users={users}
            loading={loading}
            filters={statusFilters}
            actions={
              canManageUsers && (
                <Button onClick={() => navigate("/users/new")}>
                  <Plus className="mr-2 h-4 w-4" />
                  Add User
                </Button>
              )
            }
          />
        )}
      </div>
    </div>
  );
};

export default UserList;
