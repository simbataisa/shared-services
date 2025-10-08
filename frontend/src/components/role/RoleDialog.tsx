import React, { useState, useEffect } from "react";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import type { Permission, RoleFormData, RoleDialogProps } from "@/types";

const RoleDialog: React.FC<RoleDialogProps> = ({
  open,
  onOpenChange,
  role,
  permissions,
  onSave,
  loading = false,
}) => {
  const [form, setForm] = useState<RoleFormData>({
    name: "",
    description: "",
    permissionIds: [],
  });

  const isEdit = !!role;

  // Initialize form data when role or dialog state changes
  useEffect(() => {
    if (role) {
      setForm({
        name: role.name,
        description: role.description || "",
        permissionIds: role.permissions?.map((p) => p.id) || [],
      });
    } else {
      setForm({
        name: "",
        description: "",
        permissionIds: [],
      });
    }
  }, [role, open]);

  // Event handlers
  const handleSave = async () => {
    await onSave(form);
  };

  const handleNameChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm((prev) => ({ ...prev, name: e.target.value }));
  };

  const handleDescriptionChange = (
    e: React.ChangeEvent<HTMLTextAreaElement>
  ) => {
    setForm((prev) => ({ ...prev, description: e.target.value }));
  };

  const handlePermissionChange = (permissionId: number, checked: boolean) => {
    if (checked) {
      setForm((prev) => ({
        ...prev,
        permissionIds: [...prev.permissionIds, permissionId],
      }));
    } else {
      setForm((prev) => ({
        ...prev,
        permissionIds: prev.permissionIds.filter((id) => id !== permissionId),
      }));
    }
  };

  const handleCancel = () => {
    onOpenChange(false);
  };

  // Group permissions by resource (extracted from permission name)
  const groupedPermissions = permissions.reduce((acc, permission) => {
    const resource = permission.name.split(":")[0] || "General";
    if (!acc[resource]) {
      acc[resource] = [];
    }
    acc[resource].push(permission);
    return acc;
  }, {} as Record<string, Permission[]>);

  const isFormValid = form.name.trim().length > 0;

  // Render method
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[600px]">
        <DialogHeader>
          <DialogTitle>{isEdit ? "Edit Role" : "Create New Role"}</DialogTitle>
          <DialogDescription>
            {isEdit
              ? "Update the role and its permissions."
              : "Create a new role and assign permissions to it."}
          </DialogDescription>
        </DialogHeader>

        <div className="grid gap-4 py-4">
          {/* Role Name Field */}
          <div className="grid grid-cols-4 items-center gap-4">
            <Label htmlFor="name" className="text-right">
              Name
            </Label>
            <Input
              id="name"
              value={form.name}
              onChange={handleNameChange}
              className="col-span-3"
              placeholder="Enter role name"
              required
            />
          </div>

          {/* Role Description Field */}
          <div className="grid grid-cols-4 items-center gap-4">
            <Label htmlFor="description" className="text-right">
              Description
            </Label>
            <Textarea
              id="description"
              value={form.description}
              onChange={handleDescriptionChange}
              className="col-span-3"
              placeholder="Enter role description"
            />
          </div>

          {/* Permissions Section */}
          <div className="grid grid-cols-4 items-start gap-4">
            <Label className="text-right mt-2">Permissions</Label>
            <div className="col-span-3 space-y-4 max-h-64 overflow-y-auto">
              {Object.entries(groupedPermissions).map(
                ([resource, resourcePermissions]) => (
                  <div key={resource} className="space-y-2">
                    <h4 className="font-medium text-sm capitalize">
                      {resource}
                    </h4>
                    <div className="grid grid-cols-2 gap-2">
                      {resourcePermissions.map((permission) => (
                        <div
                          key={permission.id}
                          className="flex items-center space-x-2"
                        >
                          <input
                            type="checkbox"
                            id={`permission-${permission.id}`}
                            checked={form.permissionIds.includes(permission.id)}
                            onChange={(e) =>
                              handlePermissionChange(
                                permission.id,
                                e.target.checked
                              )
                            }
                            className="rounded border-gray-300"
                          />
                          <Label
                            htmlFor={`permission-${permission.id}`}
                            className="text-sm"
                          >
                            {permission.name}
                          </Label>
                        </div>
                      ))}
                    </div>
                  </div>
                )
              )}
            </div>
          </div>
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={handleCancel}>
            Cancel
          </Button>
          <Button onClick={handleSave} disabled={loading || !isFormValid}>
            {loading ? "Saving..." : isEdit ? "Update Role" : "Create Role"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};

export default RoleDialog;
