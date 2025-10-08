import React, { useState, useEffect } from "react";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Skeleton } from "@/components/ui/skeleton";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Textarea } from "@/components/ui/textarea";
import { usePermissions } from "@/hooks/usePermissions";
import { Plus, Edit, Trash2, Shield } from "lucide-react";
import { SearchAndFilter } from "@/components/common/SearchAndFilter";
import type { Permission, CreatePermissionForm } from "@/types";

const PermissionList: React.FC = () => {
  const [permissions, setPermissions] = useState<Permission[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchTerm, setSearchTerm] = useState("");
  const [resourceFilter, setResourceFilter] = useState<string>("all");
  const [actionFilter, setActionFilter] = useState<string>("all");
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false);
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false);
  const [selectedPermission, setSelectedPermission] =
    useState<Permission | null>(null);
  const [createForm, setCreateForm] = useState<CreatePermissionForm>({
    name: "",
    description: "",
    resource: "",
    action: "",
  });

  const { canViewPermissions, canAssignPermissions } = usePermissions();

  useEffect(() => {
    if (canViewPermissions) {
      fetchPermissions();
    }
  }, [canViewPermissions]);

  const fetchPermissions = async () => {
    try {
      setLoading(true);
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
      setError(
        err instanceof Error ? err.message : "Failed to fetch permissions"
      );
    } finally {
      setLoading(false);
    }
  };

  const handleCreatePermission = async () => {
    try {
      const response = await fetch("/api/permissions", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
        body: JSON.stringify(createForm),
      });

      if (!response.ok) {
        throw new Error("Failed to create permission");
      }

      setIsCreateDialogOpen(false);
      setCreateForm({
        name: "",
        description: "",
        resource: "",
        action: "",
      });
      fetchPermissions();
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "Failed to create permission"
      );
    }
  };

  const handleEditPermission = (permission: Permission) => {
    const { resource, action } = parsePermissionName(permission.name);
    setSelectedPermission(permission);
    setCreateForm({
      name: permission.name,
      description: permission.description,
      resource: resource,
      action: action,
    });
    setIsEditDialogOpen(true);
  };

  const handleUpdatePermission = async () => {
    if (!selectedPermission) return;

    try {
      const response = await fetch(
        `/api/permissions/${selectedPermission.id}`,
        {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
          body: JSON.stringify(createForm),
        }
      );

      if (!response.ok) {
        throw new Error("Failed to update permission");
      }

      setIsEditDialogOpen(false);
      setSelectedPermission(null);
      setCreateForm({
        name: "",
        description: "",
        resource: "",
        action: "",
      });
      fetchPermissions();
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "Failed to update permission"
      );
    }
  };

  const handleDeletePermission = async (permissionId: number) => {
    if (!confirm("Are you sure you want to delete this permission?")) {
      return;
    }

    try {
      const response = await fetch(`/api/permissions/${permissionId}`, {
        method: "DELETE",
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
      });

      if (!response.ok) {
        throw new Error("Failed to delete permission");
      }

      fetchPermissions();
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "Failed to delete permission"
      );
    }
  };

  // Helper function to parse resource and action from permission name
  const parsePermissionName = (name: string) => {
    if (!name || typeof name !== "string") {
      return { resource: "unknown", action: "unknown" };
    }

    const parts = name.split(":");
    if (parts.length >= 2) {
      return {
        resource: parts[0],
        action: parts[1],
      };
    }

    return { resource: name, action: "unknown" };
  };

  const filteredPermissions = permissions.filter((permission) => {
    const { resource, action } = parsePermissionName(permission.name);

    const matchesSearch =
      permission.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      permission.description.toLowerCase().includes(searchTerm.toLowerCase()) ||
      resource.toLowerCase().includes(searchTerm.toLowerCase()) ||
      action.toLowerCase().includes(searchTerm.toLowerCase());

    const matchesResource =
      resourceFilter === "all" || resource === resourceFilter;
    const matchesAction = actionFilter === "all" || action === actionFilter;

    return matchesSearch && matchesResource && matchesAction;
  });

  const uniqueResources = Array.from(
    new Set(permissions.map((p) => parsePermissionName(p.name).resource))
  ).sort();
  const uniqueActions = Array.from(
    new Set(permissions.map((p) => parsePermissionName(p.name).action))
  ).sort();

  const getActionBadgeVariant = (action: string) => {
    if (!action || typeof action !== "string") {
      return "outline";
    }

    switch (action.toLowerCase()) {
      case "create":
        return "default";
      case "read":
        return "secondary";
      case "update":
        return "outline";
      case "delete":
        return "destructive";
      case "admin":
        return "default";
      default:
        return "outline";
    }
  };

  if (!canViewPermissions) {
    return (
      <div className="container mx-auto py-10">
        <Alert>
          <Shield className="h-4 w-4" />
          <AlertDescription>
            You don't have permission to view permissions.
          </AlertDescription>
        </Alert>
      </div>
    );
  }

  return (
    <div className="container mx-auto py-10">
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">
            Permission Management
          </h1>
          <p className="text-muted-foreground">
            Manage system permissions and access controls
          </p>
        </div>
      </div>

      {/* Edit Dialog */}
      {canAssignPermissions && (
        <Dialog open={isEditDialogOpen} onOpenChange={setIsEditDialogOpen}>
          <DialogContent className="sm:max-w-[425px]">
            <DialogHeader>
              <DialogTitle>Edit Permission</DialogTitle>
              <DialogDescription>
                Update the permission details.
              </DialogDescription>
            </DialogHeader>
            <div className="grid gap-4 py-4">
              <div className="grid grid-cols-4 items-center gap-4">
                <Label htmlFor="edit-name" className="text-right">
                  Name
                </Label>
                <Input
                  id="edit-name"
                  value={createForm.name}
                  onChange={(e) =>
                    setCreateForm({ ...createForm, name: e.target.value })
                  }
                  className="col-span-3"
                />
              </div>
              <div className="grid grid-cols-4 items-center gap-4">
                <Label htmlFor="edit-description" className="text-right">
                  Description
                </Label>
                <Textarea
                  id="edit-description"
                  value={createForm.description}
                  onChange={(e) =>
                    setCreateForm({
                      ...createForm,
                      description: e.target.value,
                    })
                  }
                  className="col-span-3"
                />
              </div>
              <div className="grid grid-cols-4 items-center gap-4">
                <Label htmlFor="edit-resource" className="text-right">
                  Resource
                </Label>
                <Input
                  id="edit-resource"
                  value={createForm.resource}
                  onChange={(e) =>
                    setCreateForm({ ...createForm, resource: e.target.value })
                  }
                  className="col-span-3"
                />
              </div>
              <div className="grid grid-cols-4 items-center gap-4">
                <Label htmlFor="edit-action" className="text-right">
                  Action
                </Label>
                <Input
                  id="edit-action"
                  value={createForm.action}
                  onChange={(e) =>
                    setCreateForm({ ...createForm, action: e.target.value })
                  }
                  className="col-span-3"
                />
              </div>
            </div>
            <DialogFooter>
              <Button type="submit" onClick={handleUpdatePermission}>
                Update Permission
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
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
        searchPlaceholder="Search permissions by name or description..."
        filters={[
          {
            label: "Resource",
            value: resourceFilter,
            onChange: setResourceFilter,
            options: [
              { value: "all", label: "All Resources" },
              ...uniqueResources.map((resource) => ({
                value: resource,
                label: resource,
              })),
            ],
            placeholder: "Filter by resource",
            width: "w-[180px]",
          },
          {
            label: "Action",
            value: actionFilter,
            onChange: setActionFilter,
            options: [
              { value: "all", label: "All Actions" },
              ...uniqueActions.map((action) => ({
                value: action,
                label: action,
              })),
            ],
            placeholder: "Filter by action",
            width: "w-[180px]",
          },
        ]}
        actions={
          canAssignPermissions && (
            <Dialog
              open={isCreateDialogOpen}
              onOpenChange={setIsCreateDialogOpen}
            >
              <DialogTrigger asChild>
                <Button>
                  <Plus className="mr-2 h-4 w-4" />
                  Add Permission
                </Button>
              </DialogTrigger>
              <DialogContent className="sm:max-w-[425px]">
                <DialogHeader>
                  <DialogTitle>Create New Permission</DialogTitle>
                  <DialogDescription>
                    Create a new permission for system access control.
                  </DialogDescription>
                </DialogHeader>
                <div className="grid gap-4 py-4">
                  <div className="grid grid-cols-4 items-center gap-4">
                    <Label htmlFor="name" className="text-right">
                      Name
                    </Label>
                    <Input
                      id="name"
                      value={createForm.name}
                      onChange={(e) =>
                        setCreateForm({ ...createForm, name: e.target.value })
                      }
                      className="col-span-3"
                      placeholder="e.g., user:read"
                    />
                  </div>
                  <div className="grid grid-cols-4 items-center gap-4">
                    <Label htmlFor="description" className="text-right">
                      Description
                    </Label>
                    <Textarea
                      id="description"
                      value={createForm.description}
                      onChange={(e) =>
                        setCreateForm({
                          ...createForm,
                          description: e.target.value,
                        })
                      }
                      className="col-span-3"
                      placeholder="Describe what this permission allows"
                    />
                  </div>
                  <div className="grid grid-cols-4 items-center gap-4">
                    <Label htmlFor="resource" className="text-right">
                      Resource
                    </Label>
                    <Input
                      id="resource"
                      value={createForm.resource}
                      onChange={(e) =>
                        setCreateForm({
                          ...createForm,
                          resource: e.target.value,
                        })
                      }
                      className="col-span-3"
                      placeholder="e.g., user, role, permission"
                    />
                  </div>
                  <div className="grid grid-cols-4 items-center gap-4">
                    <Label htmlFor="action" className="text-right">
                      Action
                    </Label>
                    <Input
                      id="action"
                      value={createForm.action}
                      onChange={(e) =>
                        setCreateForm({ ...createForm, action: e.target.value })
                      }
                      className="col-span-3"
                      placeholder="e.g., create, read, update, delete"
                    />
                  </div>
                </div>
                <DialogFooter>
                  <Button type="submit" onClick={handleCreatePermission}>
                    Create Permission
                  </Button>
                </DialogFooter>
              </DialogContent>
            </Dialog>
          )
        }
      />

      <Card>
        <CardHeader>
          <CardTitle>Permissions</CardTitle>
          <CardDescription>
            A list of all permissions in the system
          </CardDescription>
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
                  <TableHead>Resource</TableHead>
                  <TableHead>Action</TableHead>
                  <TableHead>Created</TableHead>
                  <TableHead>Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {filteredPermissions.map((permission) => {
                  const { resource, action } = parsePermissionName(
                    permission.name
                  );
                  return (
                    <TableRow key={permission.id}>
                      <TableCell className="font-medium">
                        {permission.name}
                      </TableCell>
                      <TableCell>{permission.description}</TableCell>
                      <TableCell>
                        <Badge variant="outline">{resource}</Badge>
                      </TableCell>
                      <TableCell>
                        <Badge variant={getActionBadgeVariant(action)}>
                          {action}
                        </Badge>
                      </TableCell>
                      <TableCell>
                        {new Date(permission.createdAt).toLocaleDateString()}
                      </TableCell>
                      <TableCell>
                        {canAssignPermissions && (
                          <div className="flex items-center space-x-2">
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() => handleEditPermission(permission)}
                            >
                              <Edit className="h-4 w-4" />
                            </Button>
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() =>
                                handleDeletePermission(permission.id)
                              }
                              className="text-red-600 hover:text-red-700"
                            >
                              <Trash2 className="h-4 w-4" />
                            </Button>
                          </div>
                        )}
                      </TableCell>
                    </TableRow>
                  );
                })}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>
    </div>
  );
};

export default PermissionList;
