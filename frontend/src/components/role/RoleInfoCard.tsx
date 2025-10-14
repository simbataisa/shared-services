import React, { useState } from "react";
import { Shield, Edit, Save, X } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Label } from "@/components/ui/label";
import { getStatusColor, getStatusIcon } from "@/lib/status-utils";
import { PermissionGuard } from "@/components/common/PermissionGuard";
import type { RoleDetails } from "@/types";

interface RoleInfoCardProps {
  role: RoleDetails;
  onUpdate?: (data: { name: string; description: string }) => Promise<void>;
  onFormChange?: (data: { name: string; description: string; moduleId: number }) => void;
  updating?: boolean;
  mode?: "create" | "edit";
  validationErrors?: Record<string, string>;
  onFieldValidation?: (fieldName: string, value: any) => void;
}

export const RoleInfoCard: React.FC<RoleInfoCardProps> = ({ 
  role, 
  onUpdate,
  onFormChange,
  updating = false,
  mode = "edit",
  validationErrors = {},
  onFieldValidation
}) => {
  const [isEditing, setIsEditing] = useState(mode === "create");
  const [editForm, setEditForm] = useState({
    name: role.name,
    description: role.description || '',
  });
  const [errors, setErrors] = useState<Record<string, string>>({});

  const validateForm = () => {
    const newErrors: Record<string, string> = {};
    
    if (!editForm.name.trim()) {
      newErrors.name = 'Role name is required';
    }
    
    if (editForm.name.length > 100) {
      newErrors.name = 'Role name must be less than 100 characters';
    }
    
    if (editForm.description.length > 500) {
      newErrors.description = 'Description must be less than 500 characters';
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleEditToggle = () => {
    if (isEditing) {
      // Cancel editing - reset form
      setEditForm({
        name: role.name,
        description: role.description || '',
      });
      setErrors({});
    }
    setIsEditing(!isEditing);
  };

  const handleSaveChanges = async () => {
    if (!validateForm()) return;
    
    try {
      if (onUpdate) {
        await onUpdate({
          name: editForm.name.trim(),
          description: editForm.description.trim(),
        });
      }
      setIsEditing(false);
      setErrors({});
    } catch (error) {
      console.error('Failed to update role:', error);
    }
  };

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle className="flex items-center">
            <Shield className="mr-2 h-5 w-5" />
            Role Information
          </CardTitle>
          <PermissionGuard permission="ROLE_MGMT:update">
            {mode === "create" ? null : (
              isEditing ? (
                <div className="flex gap-2">
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={handleSaveChanges}
                    disabled={!editForm.name.trim() || updating}
                  >
                    <Save className="h-4 w-4 mr-2" />
                    Save
                  </Button>
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={handleEditToggle}
                    disabled={updating}
                  >
                    <X className="h-4 w-4 mr-2" />
                    Cancel
                  </Button>
                </div>
              ) : (
                <Button
                  variant="outline"
                  size="sm"
                  onClick={handleEditToggle}
                >
                  <Edit className="h-4 w-4 mr-2" />
                  Edit
                </Button>
              )
            )}
          </PermissionGuard>
        </div>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="space-y-2">
            <Label>Role Name</Label>
            {isEditing ? (
              <div>
                <Input
                  value={editForm.name}
                  onChange={(e) => {
                    const newForm = { ...editForm, name: e.target.value };
                    setEditForm(newForm);
                    onFormChange?.({ ...newForm, moduleId: role.moduleId || 1 });
                    onFieldValidation?.('name', e.target.value);
                  }}
                  placeholder="Enter role name"
                  disabled={updating}
                  className={validationErrors.name || errors.name ? "border-red-500" : ""}
                />
                {(validationErrors.name || errors.name) && (
                  <p className="text-sm text-red-500 mt-1">{validationErrors.name || errors.name}</p>
                )}
              </div>
            ) : (
              <div className="text-sm">{role.name}</div>
            )}
          </div>
          {mode !== "create" && (
            <div className="space-y-2">
              <Label className="block text-sm font-medium text-gray-700">
                Status
              </Label>
              <div
                className={`inline-flex items-center gap-2 px-3 py-1 rounded-full border text-sm font-medium ${getStatusColor(
                  role.roleStatus
                )}`}
              >
                {getStatusIcon(role.roleStatus)}
                {role.roleStatus}
              </div>
            </div>
          )}
          <div className="md:col-span-2 space-y-2">
            <Label>Description</Label>
            {isEditing ? (
              <div>
                <Textarea
                  value={editForm.description}
                  onChange={(e) => {
                    const newForm = { ...editForm, description: e.target.value };
                    setEditForm(newForm);
                    onFormChange?.({ ...newForm, moduleId: role.moduleId || 1 });
                    onFieldValidation?.('description', e.target.value);
                  }}
                  placeholder="Enter description"
                  rows={3}
                  disabled={updating}
                  className={validationErrors.description || errors.description ? "border-red-500" : ""}
                />
                {(validationErrors.description || errors.description) && (
                  <p className="text-sm text-red-500 mt-1">{validationErrors.description || errors.description}</p>
                )}
              </div>
            ) : (
              <div className="text-sm text-muted-foreground">
                {role.description || "No description provided"}
              </div>
            )}
          </div>
          {mode !== "create" && (
            <>
              <div>
                <Label className="text-sm font-medium text-gray-500">
                  Created At
                </Label>
                <p className="mt-1 text-sm text-gray-900">
                  {new Date(role.createdAt).toLocaleDateString("en-US", {
                    year: "numeric",
                    month: "long",
                    day: "numeric",
                  })}
                </p>
              </div>
              <div>
                <Label className="text-sm font-medium text-gray-500">
                  Last Updated
                </Label>
                <p className="mt-1 text-sm text-gray-900">
                  {new Date(role.updatedAt).toLocaleDateString("en-US", {
                    year: "numeric",
                    month: "long",
                    day: "numeric",
                  })}
                </p>
              </div>
            </>
          )}
        </div>
      </CardContent>
    </Card>
  );
};
