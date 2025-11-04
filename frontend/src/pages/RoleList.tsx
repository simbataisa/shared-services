import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Skeleton } from "@/components/ui/skeleton";
import { usePermissions } from "@/hooks/usePermissions";
import { Plus, Edit, Trash2, Shield } from "lucide-react";
import RoleDialog from "@/components/role/RoleDialog";
import RoleTable from "@/components/role/RoleTable";
import httpClient from "@/lib/httpClient";
import type {
  Role,
  Permission,
  CreateRoleRequest,
  RoleListProps,
  RoleSearchFilters,
} from "@/types";

const RoleList: React.FC<RoleListProps> = ({
  onRoleSelect,
  selectedRoleId,
  showActions = true,
}) => {
  const [roles, setRoles] = useState<Role[]>([]);
  const [permissions, setPermissions] = useState<Permission[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState("all");
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false);
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false);
  const [selectedRole, setSelectedRole] = useState<Role | null>(null);
  const [saving, setSaving] = useState(false);

  const { canViewRoles, canManageRoles } = usePermissions();
  const navigate = useNavigate();

  // Event handlers
  const handleSearchChange = (newSearchTerm: string) => {
    setSearchTerm(newSearchTerm);
  };

  const handleCreateRole = async (form: CreateRoleRequest) => {
    try {
      setSaving(true);
      await httpClient.createRole(form);
      setIsCreateDialogOpen(false);
      await fetchRoles();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to create role");
    } finally {
      setSaving(false);
    }
  };

  const handleEditRole = (role: Role) => {
    setSelectedRole(role);
    setIsEditDialogOpen(true);
    onRoleSelect?.(role);
  };

  const handleUpdateRole = async (form: CreateRoleRequest) => {
    if (!selectedRole) return;

    try {
      setSaving(true);
      await httpClient.updateRole(selectedRole.id, form);
      setIsEditDialogOpen(false);
      setSelectedRole(null);
      await fetchRoles();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to update role");
    } finally {
      setSaving(false);
    }
  };

  const handleDeleteRole = async (roleId: number) => {
    if (!confirm("Are you sure you want to delete this role?")) {
      return;
    }

    try {
      await httpClient.deleteRole(roleId);
      await fetchRoles();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to delete role");
    }
  };

  const handleViewRole = (role: Role) => {
    navigate(`/roles/${role.id}`);
    onRoleSelect?.(role);
  };

  // API calls
  const fetchRoles = async () => {
    try {
      setLoading(true);
      const data = await httpClient.getRoles();
      setRoles(data || []);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to fetch roles");
    } finally {
      setLoading(false);
    }
  };

  const fetchPermissions = async () => {
    try {
      const data = await httpClient.getPermissions();
      setPermissions(data || []);
    } catch (err) {
      console.error("Failed to fetch permissions:", err);
    }
  };

  // Effects
  useEffect(() => {
    if (canViewRoles) {
      fetchRoles();
      fetchPermissions();
    }
  }, [canViewRoles]);

  // Computed values
  const filteredRoles = roles.filter((role) => {
    const matchesSearch =
      role.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      role.description?.toLowerCase().includes(searchTerm.toLowerCase());

    const matchesStatus =
      statusFilter === "all" || (role.status || "ACTIVE") === statusFilter;

    return matchesSearch && matchesStatus;
  });

  // Render method
  if (!canViewRoles) {
    return (
      <div className="container mx-auto py-10">
        <Alert>
          <Shield className="h-4 w-4" />
          <AlertDescription>
            You don't have permission to view roles.
          </AlertDescription>
        </Alert>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="w-full py-6 px-4 sm:px-6 lg:px-8">
        <div className="space-y-2">
          {[...Array(5)].map((_, i) => (
            <Skeleton key={i} className="h-12 w-full" />
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="w-full py-6 px-4 sm:px-6 lg:px-8">
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Role Management</h1>
          <p className="text-muted-foreground">
            Manage roles and their associated permissions
          </p>
        </div>
      </div>

      {/* Edit Dialog */}
      {canManageRoles && (
        <RoleDialog
          open={isEditDialogOpen}
          onOpenChange={setIsEditDialogOpen}
          role={selectedRole}
          onSave={handleUpdateRole}
          permissions={permissions}
          loading={saving}
        />
      )}

      {error && (
        <Alert className="mb-6">
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      <RoleTable
        data={filteredRoles}
        selectedRoleId={selectedRoleId}
        showActions={showActions}
        canManageRoles={canManageRoles}
        onViewRole={handleViewRole}
        searchTerm={searchTerm}
        onSearchChange={setSearchTerm}
        searchPlaceholder="Search roles by name or description..."
        filters={[
          {
            label: "Status",
            value: statusFilter,
            onChange: setStatusFilter,
            options: [
              { value: "all", label: "All Status" },
              { value: "ACTIVE", label: "Active" },
              { value: "INACTIVE", label: "Inactive" },
              { value: "DEPRECATED", label: "Deprecated" },
            ],
          },
        ]}
        actions={
          canManageRoles &&
          showActions && (
            <>
              <Button onClick={() => navigate("/roles/new")}>
                <Plus className="mr-2 h-4 w-4" />
                Add Role
              </Button>
              <RoleDialog
                open={isCreateDialogOpen}
                onOpenChange={setIsCreateDialogOpen}
                onSave={handleCreateRole}
                permissions={permissions}
                loading={saving}
              />
            </>
          )
        }
      />
    </div>
  );
};

export default RoleList;
