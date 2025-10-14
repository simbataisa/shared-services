import React, { useState } from "react";
import { Edit, Save, X } from "lucide-react";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { PermissionGuard } from "@/components/common/PermissionGuard";
import type { Permission } from "@/types";

interface BasicInformationCardProps {
  permission: Permission;
  onUpdate?: (data: { name: string; description: string }) => Promise<void>;
  updating?: boolean;
}

export const BasicInformationCard: React.FC<BasicInformationCardProps> = ({
  permission,
  onUpdate,
  updating = false,
}) => {
  const [isEditing, setIsEditing] = useState(false);
  const [editForm, setEditForm] = useState({
    name: permission.name,
    description: permission.description || "",
  });

  const handleEditToggle = () => {
    if (isEditing) {
      // Cancel editing - reset form
      setEditForm({
        name: permission.name,
        description: permission.description || "",
      });
    }
    setIsEditing(!isEditing);
  };

  const handleSaveChanges = async () => {
    if (!editForm.name.trim()) return;

    try {
      if (onUpdate) {
        await onUpdate({
          name: editForm.name.trim(),
          description: editForm.description.trim(),
        });
      }
      setIsEditing(false);
    } catch (error) {
      console.error("Failed to update permission:", error);
    }
  };

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle>Basic Information</CardTitle>
          <PermissionGuard permission="PERMISSION_MGMT:update">
            {isEditing ? (
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
              <Button variant="outline" size="sm" onClick={handleEditToggle}>
                <Edit className="h-4 w-4 mr-2" />
                Edit
              </Button>
            )}
          </PermissionGuard>
        </div>
      </CardHeader>
      <CardContent>
        <div className="grid grid-cols-1 gap-6">
          <div className="space-y-2">
            <Label>Permission Name</Label>
            {isEditing ? (
              <Input
                value={editForm.name}
                onChange={(e) =>
                  setEditForm({ ...editForm, name: e.target.value })
                }
                placeholder="e.g., user:read"
                disabled={updating}
              />
            ) : (
              <div className="font-mono text-sm bg-gray-50 p-2 rounded">
                {permission.name}
              </div>
            )}
          </div>

          <div className="space-y-2">
            <Label>Description</Label>
            {isEditing ? (
              <Textarea
                value={editForm.description}
                onChange={(e) =>
                  setEditForm({ ...editForm, description: e.target.value })
                }
                placeholder="Describe what this permission allows..."
                rows={3}
                disabled={updating}
              />
            ) : (
              <div className="text-sm text-muted-foreground">
                {permission.description || "No description provided"}
              </div>
            )}
          </div>
        </div>
      </CardContent>
    </Card>
  );
};