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

interface Permission {
  id: number;
  name: string;
  description: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

interface Role {
  id: number;
  name: string;
  description: string;
  createdAt: string;
  updatedAt: string;
  permissions: Permission[];
}

interface RoleForm {
  name: string;
  description: string;
  permissionIds: number[];
}

interface RoleDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  role?: Role | null;
  permissions: Permission[];
  onSave: (form: RoleForm) => Promise<void>;
  loading?: boolean;
}

const RoleDialog: React.FC<RoleDialogProps> = ({
  open,
  onOpenChange,
  role,
  permissions,
  onSave,
  loading = false,
}) => {
  const [form, setForm] = useState<RoleForm>({
    name: "",
    description: "",
    permissionIds: [],
  });

  const isEdit = !!role;

  useEffect(() => {
    if (role) {
      setForm({
        name: role.name,
        description: role.description,
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

  const handleSave = async () => {
    await onSave(form);
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

  // Group permissions by resource (extracted from permission name)
  const groupedPermissions = permissions.reduce((acc, permission) => {
    const resource = permission.name.split(":")[0] || "General";
    if (!acc[resource]) {
      acc[resource] = [];
    }
    acc[resource].push(permission);
    return acc;
  }, {} as Record<string, Permission[]>);

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
          <div className="grid grid-cols-4 items-center gap-4">
            <Label htmlFor="name" className="text-right">
              Name
            </Label>
            <Input
              id="name"
              value={form.name}
              onChange={(e) =>
                setForm((prev) => ({ ...prev, name: e.target.value }))
              }
              className="col-span-3"
              placeholder="Enter role name"
            />
          </div>
          <div className="grid grid-cols-4 items-center gap-4">
            <Label htmlFor="description" className="text-right">
              Description
            </Label>
            <Textarea
              id="description"
              value={form.description}
              onChange={(e) =>
                setForm((prev) => ({ ...prev, description: e.target.value }))
              }
              className="col-span-3"
              placeholder="Enter role description"
            />
          </div>
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
          <Button variant="outline" onClick={() => onOpenChange(false)}>
            Cancel
          </Button>
          <Button onClick={handleSave} disabled={loading || !form.name.trim()}>
            {loading ? "Saving..." : isEdit ? "Update Role" : "Create Role"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};

export default RoleDialog;
