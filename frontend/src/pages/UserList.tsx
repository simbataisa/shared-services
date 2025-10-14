import React, { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Alert, AlertDescription } from "@/components/ui/alert";
import SearchAndFilter from "@/components/common/SearchAndFilter";
import { UserTable } from "@/components/users/UserTable";
import { usePermissions } from "@/hooks/usePermissions";
import { Shield } from "lucide-react";
import { PermissionGuard } from "@/components/common/PermissionGuard";
import type { User, Role, UserGroup } from "@/types";
import httpClient from "@/lib/httpClient";

const UserList: React.FC = () => {
  const navigate = useNavigate();
  const [users, setUsers] = useState<User[]>([]);
  const [roles, setRoles] = useState<Role[]>([]);
  const [userGroups, setUserGroups] = useState<UserGroup[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState<string>("all");

  const { canViewUsers, canManageUsers } = usePermissions();

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
      const response = await fetch("/api/v1/users", {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
      });

      if (!response.ok) {
        throw new Error("Failed to fetch users");
      }

      const data = await response.json();
      setUsers(data.data || []);
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
      const response = await fetch("/api/v1/user-groups", {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
      });

      if (response.ok) {
        const data = await response.json();
        setUserGroups(data.data || []);
      }
    } catch (err) {
      console.error("Failed to fetch user groups:", err);
    }
  };

  const handleDeleteUser = async (userId: number) => {
    if (!confirm("Are you sure you want to delete this user?")) {
      return;
    }

    try {
      const response = await fetch(`/api/v1/users/${userId}`, {
        method: "DELETE",
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
      });

      if (!response.ok) {
        throw new Error("Failed to delete user");
      }

      fetchUsers();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to delete user");
    }
  };

  const handleEditUser = (user: User) => {
    navigate(`/users/${user.id}/edit`);
  };

  const filteredUsers = users.filter((user) => {
    const matchesSearch =
      user.username.toLowerCase().includes(searchTerm.toLowerCase()) ||
      user.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
      user.firstName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      user.lastName.toLowerCase().includes(searchTerm.toLowerCase());

    const matchesStatus =
      statusFilter === "all" || user.userStatus === statusFilter;

    return matchesSearch && matchesStatus;
  });

  if (!canViewUsers) {
    return (
      <div className="container mx-auto py-10">
        <Alert>
          <Shield className="h-4 w-4" />
          <AlertDescription>
            You don't have permission to view users.
          </AlertDescription>
        </Alert>
      </div>
    );
  }

  return (
    <div className="container mx-auto py-10">
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">User Management</h1>
          <p className="text-muted-foreground">
            Manage users, their roles, and permissions
          </p>
        </div>
      </div>

      {error && (
        <Alert className="mb-6">
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      <SearchAndFilter
        searchTerm={searchTerm}
        onSearchChange={setSearchTerm}
        searchPlaceholder="Search users by username, email, or name..."
        filters={[
          {
            label: "Status",
            value: statusFilter,
            onChange: setStatusFilter,
            options: [
              { value: "all", label: "All Status" },
              { value: "ACTIVE", label: "Active" },
              { value: "INACTIVE", label: "Inactive" },
            ],
            width: "w-40",
          },
        ]}
        actions={[
          <PermissionGuard key="create-user" permission="USER_MGMT:create">
            <Button asChild>
              <Link to="/users/create">Create New User</Link>
            </Button>
          </PermissionGuard>,
        ]}
        className="mb-6"
      />

      <Card>
        <CardContent>
          <UserTable users={filteredUsers} loading={loading} />
        </CardContent>
      </Card>
    </div>
  );
};

export default UserList;
