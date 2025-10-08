import React, { useState } from "react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Label } from "@/components/ui/label";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Users, Shield, Plus, X, Edit, Save, XCircle } from "lucide-react";
import type { User, Role, UserGroup } from "@/types";

interface UserRoleGroupCardProps {
  user: User;
  availableRoles?: Role[];
  availableGroups?: UserGroup[];
  onRoleAdd?: (roleId: string) => void;
  onRoleRemove?: (roleId: string) => void;
  onGroupAdd?: (groupId: string) => void;
  onGroupRemove?: (groupId: string) => void;
  loading?: boolean;
  canUpdate?: boolean;
  className?: string;
}

const UserRoleGroupCard: React.FC<UserRoleGroupCardProps> = ({
  user,
  availableRoles = [],
  availableGroups = [],
  onRoleAdd,
  onRoleRemove,
  onGroupAdd,
  onGroupRemove,
  loading = false,
  canUpdate = false,
  className = "",
}) => {
  const [isEditing, setIsEditing] = useState(false);

  const userRoleIds = user.roles?.map(role => role.id) || [];
  const userGroupIds = user.userGroups?.map(group => group.userGroupId) || [];

  const availableRolesToAdd = availableRoles.filter(
    role => !userRoleIds.includes(role.id)
  );
  const availableGroupsToAdd = availableGroups.filter(
    group => !userGroupIds.includes(group.userGroupId)
  );

  const handleEdit = () => {
    setIsEditing(true);
  };

  const handleSave = () => {
    setIsEditing(false);
  };

  const handleCancel = () => {
    setIsEditing(false);
  };

  return (
    <Card className={className}>
      <CardHeader>
        <div className="flex items-center justify-between">
          <div>
            <CardTitle className="flex items-center text-lg font-semibold text-gray-900">
              <Shield className="mr-2 h-5 w-5" />
              Roles & Groups
            </CardTitle>
            <CardDescription>
              Manage user permissions through roles and group memberships
            </CardDescription>
          </div>
          {canUpdate && (
            <div className="flex items-center gap-2">
              {!isEditing ? (
                <Button
                  variant="outline"
                  size="sm"
                  onClick={handleEdit}
                  disabled={loading}
                  className="flex items-center gap-1"
                >
                  <Edit className="h-4 w-4" />
                  Edit
                </Button>
              ) : (
                <div className="flex items-center gap-2">
                  <Button
                    variant="default"
                    size="sm"
                    onClick={handleSave}
                    disabled={loading}
                    className="flex items-center gap-1"
                  >
                    <Save className="h-4 w-4" />
                    Save
                  </Button>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={handleCancel}
                    disabled={loading}
                    className="flex items-center gap-1"
                  >
                    <XCircle className="h-4 w-4" />
                    Cancel
                  </Button>
                </div>
              )}
            </div>
          )}
        </div>
      </CardHeader>
      <CardContent className="space-y-6">
        {/* Current Roles */}
        <div>
          <Label className="block text-sm font-medium text-gray-700 mb-2">
            Current Roles
          </Label>
          <div className="flex flex-wrap gap-2">
            {user.roles && user.roles.length > 0 ? (
              user.roles.map((role) => (
                <Badge
                  key={role.id}
                  variant="secondary"
                  className="flex items-center gap-1"
                >
                  {role.name}
                  {canUpdate && onRoleRemove && isEditing && (
                    <Button
                      size="sm"
                      variant="ghost"
                      className="h-4 w-4 p-0 hover:bg-red-100"
                      onClick={() => onRoleRemove(role.id.toString())}
                      disabled={loading}
                    >
                      <X className="h-3 w-3" />
                    </Button>
                  )}
                </Badge>
              ))
            ) : (
              <p className="text-sm text-gray-500">No roles assigned</p>
            )}
          </div>

          {/* Add Role Buttons */}
          {canUpdate && onRoleAdd && availableRolesToAdd.length > 0 && isEditing && (
            <div className="mt-2">
              <Label className="block text-xs font-medium text-gray-600 mb-1">
                Add Role:
              </Label>
              <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-1">
                {availableRolesToAdd.map((role) => (
                  <Button
                    key={role.id}
                    size="sm"
                    variant="outline"
                    className="h-6 text-xs"
                    onClick={() => onRoleAdd(role.id.toString())}
                    disabled={loading}
                  >
                    <Plus className="h-3 w-3 mr-1" />
                    {role.name}
                  </Button>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* Current Groups */}
        <div>
          <Label className="block text-sm font-medium text-gray-700 mb-2">
            Group Memberships
          </Label>
          <div className="flex flex-wrap gap-2">
            {user.userGroups && user.userGroups.length > 0 ? (
              user.userGroups.map((group) => (
                <Badge
                  key={group.userGroupId}
                  variant="outline"
                  className="flex items-center gap-1"
                >
                  <Users className="h-3 w-3" />
                  {group.name}
                  {canUpdate && onGroupRemove && isEditing && (
                    <Button
                      size="sm"
                      variant="ghost"
                      className="h-4 w-4 p-0 hover:bg-red-100"
                      onClick={() => onGroupRemove(group.userGroupId.toString())}
                      disabled={loading}
                    >
                      <X className="h-3 w-3" />
                    </Button>
                  )}
                </Badge>
              ))
            ) : (
              <p className="text-sm text-gray-500">No group memberships</p>
            )}
          </div>

          {/* Add Group Buttons */}
          {canUpdate && onGroupAdd && availableGroupsToAdd.length > 0 && isEditing && (
            <div className="mt-2">
              <Label className="block text-xs font-medium text-gray-600 mb-1">
                Add to Group:
              </Label>
              <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-1">
                {availableGroupsToAdd.map((group) => (
                  <Button
                    key={group.userGroupId}
                    size="sm"
                    variant="outline"
                    className="h-6 text-xs"
                    onClick={() => onGroupAdd(group.userGroupId.toString())}
                    disabled={loading}
                  >
                    <Plus className="h-3 w-3 mr-1" />
                    {group.name}
                  </Button>
                ))}
              </div>
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  );
};

export default UserRoleGroupCard;