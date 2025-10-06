import React, { useState, useEffect } from "react";
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
import { Plus, Edit, Trash2, Shield } from "lucide-react";
import RoleDialog from "@/components/role/RoleDialog";
import SearchAndFilter from "@/components/SearchAndFilter";

interface Role {
  id: number;
  name: string;
  description: string;
  createdAt: string;
  updatedAt: string;
  permissions: Permission[];
}

interface Permission {
  id: number;
  name: string;
  description: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

interface CreateRoleForm {
  name: string;
  description: string;
  permissionIds: number[];
}

const RoleList: React.FC = () => {
  const [roles, setRoles] = useState<Role[]>([]);
  const [permissions, setPermissions] = useState<Permission[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchTerm, setSearchTerm] = useState("");
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false);
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false);
  const [selectedRole, setSelectedRole] = useState<Role | null>(null);
  const [saving, setSaving] = useState(false);

  const { canViewRoles, canManageRoles } = usePermissions();

  useEffect(() => {
    if (canViewRoles) {
      fetchRoles();
      fetchPermissions();
    }
  }, [canViewRoles]);

  const fetchRoles = async () => {
    try {
      setLoading(true);
      const response = await fetch("/api/v1/roles", {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
      });

      if (!response.ok) {
        throw new Error("Failed to fetch roles");
      }

      const data = await response.json();
      setRoles(data || []);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to fetch roles");
    } finally {
      setLoading(false);
    }
  };

  const fetchPermissions = async () => {
    try {
      const response = await fetch("/api/permissions", {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
      });

      if (!response.ok) {
        throw new Error("Failed to fetch permissions");
      }

      const data = await response.json();
      setPermissions(data || []);
    } catch (err) {
      console.error("Failed to fetch permissions:", err);
    }
  };

  const handleCreateRole = async (form: CreateRoleForm) => {
    try {
      setSaving(true);
      const response = await fetch("/api/v1/roles", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
        body: JSON.stringify(form),
      });

      if (!response.ok) {
        throw new Error("Failed to create role");
      }

      setIsCreateDialogOpen(false);
      fetchRoles();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to create role");
    } finally {
      setSaving(false);
    }
  };

  const handleEditRole = (role: Role) => {
    setSelectedRole(role);
    setIsEditDialogOpen(true);
  };

  const handleUpdateRole = async (form: CreateRoleForm) => {
    if (!selectedRole) return;

    try {
      setSaving(true);
      const response = await fetch(`/api/v1/roles/${selectedRole.id}`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
        body: JSON.stringify(form),
      });

      if (!response.ok) {
        throw new Error("Failed to update role");
      }

      setIsEditDialogOpen(false);
      setSelectedRole(null);
      fetchRoles();
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
      const response = await fetch(`/api/v1/roles/${roleId}`, {
        method: "DELETE",
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
      });

      if (!response.ok) {
        throw new Error("Failed to delete role");
      }

      fetchRoles();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to delete role");
    }
  };

  const filteredRoles = roles.filter((role) => {
    const matchesSearch =
      role.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      role.description.toLowerCase().includes(searchTerm.toLowerCase());

    return matchesSearch;
  });

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
        searchTerm={searchTerm}
        onSearchChange={setSearchTerm}
        searchPlaceholder="Search roles by name or description..."
        actions={
          canManageRoles && (
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
                  <TableHead>Permissions</TableHead>
                  <TableHead>Created</TableHead>
                  <TableHead>Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {filteredRoles.map((role) => (
                  <TableRow key={role.id}>
                    <TableCell className="font-medium">{role.name}</TableCell>
                    <TableCell>{role.description}</TableCell>
                    <TableCell>
                      <div className="flex flex-wrap gap-1">
                        {role.permissions?.slice(0, 3).map((permission) => (
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
                      {new Date(role.createdAt).toLocaleDateString()}
                    </TableCell>
                    <TableCell>
                      {canManageRoles && (
                        <div className="flex items-center space-x-2">
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => handleEditRole(role)}
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
