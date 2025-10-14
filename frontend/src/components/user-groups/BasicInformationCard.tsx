import React, { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Label } from '@/components/ui/label';
import { Badge } from '@/components/ui/badge';
import { Users, Edit, Save, X } from 'lucide-react';
import { PermissionGuard } from '@/components/common/PermissionGuard';
import type { UserGroup, BasicInformationCardProps } from '@/types';

export const BasicInformationCard: React.FC<BasicInformationCardProps> = ({
  userGroup,
  onUpdate,
  updating = false,
}) => {
  const [isEditing, setIsEditing] = useState(false);
  const [editForm, setEditForm] = useState({
    name: userGroup.name,
    description: userGroup.description || '',
  });

  const handleEditToggle = () => {
    if (isEditing) {
      // Cancel editing - reset form
      setEditForm({
        name: userGroup.name,
        description: userGroup.description || '',
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
      console.error('Failed to update user group:', error);
    }
  };

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle>Basic Information</CardTitle>
          <PermissionGuard permission="GROUP_MGMT:update">
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
              <Button
                variant="outline"
                size="sm"
                onClick={handleEditToggle}
              >
                <Edit className="h-4 w-4 mr-2" />
                Edit
              </Button>
            )}
          </PermissionGuard>
        </div>
      </CardHeader>
      <CardContent>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div className="space-y-2">
            <Label>Group Name</Label>
            {isEditing ? (
              <Input
                value={editForm.name}
                onChange={(e) =>
                  setEditForm({ ...editForm, name: e.target.value })
                }
                placeholder="Enter group name"
                disabled={updating}
              />
            ) : (
              <div className="text-sm">{userGroup.name}</div>
            )}
          </div>

          <div className="space-y-2">
            <Label>Member Count</Label>
            <div className="flex items-center gap-2">
              <Users className="h-4 w-4 text-muted-foreground" />
              <Badge variant="secondary">
                {userGroup.memberCount}{" "}
                {userGroup.memberCount === 1 ? "member" : "members"}
              </Badge>
            </div>
          </div>

          <div className="space-y-2 md:col-span-2">
            <Label>Description</Label>
            {isEditing ? (
              <Textarea
                value={editForm.description}
                onChange={(e) =>
                  setEditForm({ ...editForm, description: e.target.value })
                }
                placeholder="Enter group description"
                rows={3}
                disabled={updating}
              />
            ) : (
              <div className="text-sm text-muted-foreground">
                {userGroup.description || "No description provided"}
              </div>
            )}
          </div>
        </div>
      </CardContent>
    </Card>
  );
};