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
import { Badge } from "@/components/ui/badge";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Skeleton } from "@/components/ui/skeleton";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { usePermissions } from "@/hooks/usePermissions";
import { Plus, Edit, Trash2, Shield, Eye } from "lucide-react";
import RoleDialog from "@/components/role/RoleDialog";
import SearchAndFilter from "@/components/SearchAndFilter";
import { StatusBadge } from "@/components/StatusBadge";
import { normalizeEntityStatus } from "@/lib/status-colors";
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
  const [searchFilters, setSearchFilters] = useState<RoleSearchFilters>({
    searchTerm: "",
    status: "all",
  });
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false);
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false);
  const [selectedRole, setSelectedRole] = useState<Role | null>(null);
  const [saving, setSaving] = useState(false);

  const { canViewRoles, canManageRoles } = usePermissions();
  const navigate = useNavigate();

  // Event handlers
  const handleSearchChange = (searchTerm: string) => {
    setSearchFilters((prev: RoleSearchFilters) => ({ ...prev, searchTerm }));
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
      role.name
        .toLowerCase()
        .includes(searchFilters.searchTerm.toLowerCase()) ||
      role.description
        ?.toLowerCase()
        .includes(searchFilters.searchTerm.toLowerCase());

    const matchesStatus =
      searchFilters.status === "all" ||
      (role.status || "active") === searchFilters.status;

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

  return (
    <div className="container mx-auto py-10">
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

      {/* Search and Filters */}
      <SearchAndFilter
        searchTerm={searchFilters.searchTerm}
        onSearchChange={handleSearchChange}
        searchPlaceholder="Search roles by name or description..."
        actions={
          canManageRoles &&
          showActions && (
            <>
              <Button onClick={() => setIsCreateDialogOpen(true)}>
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

      <Card>
        <CardHeader>
          <CardTitle>Roles</CardTitle>
          <CardDescription>A list of all roles in the system</CardDescription>
        </CardHeader>
        <CardContent>
          {loading ? (
            <div className="space-y-2">
              {[...Array(5)].map((_, i) => (
                <Skeleton key={i} className="h-12 w-full" />
              ))}
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Name</TableHead>
                  <TableHead>Description</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Permissions</TableHead>
                  <TableHead>Created</TableHead>
                  {showActions && <TableHead>Actions</TableHead>}
                </TableRow>
              </TableHeader>
              <TableBody>
                {filteredRoles.map((role) => (
                  <TableRow
                    key={role.id}
                    className={selectedRoleId === role.id ? "bg-muted/50" : ""}
                  >
                    <TableCell className="font-medium">{role.name}</TableCell>
                    <TableCell>{role.description}</TableCell>
                    <TableCell>
                      <StatusBadge
                        status={normalizeEntityStatus(
                          "role",
                          role.status || "ACTIVE"
                        )}
                      />
                    </TableCell>
                    <TableCell>
                      <div className="flex flex-wrap gap-1">
                        {role.permissions
                          ?.slice(0, 3)
                          .map((permission: Permission) => (
                            <Badge
                              key={permission.id}
                              variant="outline"
                              className="text-xs"
                            >
                              {permission.name}
                            </Badge>
                          ))}
                        {role.permissions && role.permissions.length > 3 && (
                          <Badge variant="outline" className="text-xs">
                            +{role.permissions.length - 3} more
                          </Badge>
                        )}
                      </div>
                    </TableCell>
                    <TableCell>
                      {role.createdAt
                        ? new Date(role.createdAt).toLocaleDateString()
                        : ""}
                    </TableCell>
                    {showActions && (
                      <TableCell>
                        {canManageRoles && (
                          <div className="flex items-center space-x-2">
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() => handleViewRole(role)}
                              className="text-blue-600 hover:text-blue-700"
                            >
                              <Eye className="h-4 w-4" />
                            </Button>
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() => handleEditRole(role)}
                              className="text-yellow-600 hover:text-yellow-700"
                            >
                              <Edit className="h-4 w-4" />
                            </Button>
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() => handleDeleteRole(role.id)}
                              className="text-red-600 hover:text-red-700"
                            >
                              <Trash2 className="h-4 w-4" />
                            </Button>
                          </div>
                        )}
                      </TableCell>
                    )}
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>
    </div>
  );
};

export default RoleList;
