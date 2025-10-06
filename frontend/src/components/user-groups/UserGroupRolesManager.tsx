import React from "react";
import { Button } from "@/components/ui/button";
import {
  Collapsible,
  CollapsibleContent,
  CollapsibleTrigger,
} from "@/components/ui/collapsible";
import { Shield, Settings, ChevronDown, Trash2 } from "lucide-react";

interface RoleAssignment {
  id: number;
  userGroupId: number;
  userGroupName: string;
  moduleId: number;
  moduleName: string;
  roleId: number;
  roleName: string;
  roleDescription: string;
  createdAt: string;
  updatedAt: string;
}

interface UserGroup {
  userGroupId: number;
  name: string;
  description: string;
  memberCount: number;
  roleAssignments?: RoleAssignment[];
}

interface UserGroupRolesManagerProps {
  group: UserGroup;
  onManageRoles: (group: UserGroup) => void;
  onRemoveRoleAssignment: (groupId: number, assignmentId: number) => void;
  isLoading?: boolean;
}

const UserGroupRolesManager: React.FC<UserGroupRolesManagerProps> = ({
  group,
  onManageRoles,
  onRemoveRoleAssignment,
  isLoading = false,
}) => {
  return (
    <div className="space-y-2">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <Shield className="h-4 w-4 text-muted-foreground" />
          <span className="text-sm font-medium">Role Assignments</span>
        </div>
        <Button
          variant="outline"
          size="sm"
          onClick={() => onManageRoles(group)}
          disabled={isLoading}
        >
          <Settings className="h-3 w-3 mr-1" />
          Manage
        </Button>
      </div>

      {group.roleAssignments && group.roleAssignments.length > 0 ? (
        <Collapsible>
          <CollapsibleTrigger asChild>
            <Button
              variant="ghost"
              size="sm"
              className="w-full justify-between p-2"
            >
              <span className="text-xs">
                {group.roleAssignments.length} role
                {group.roleAssignments.length !== 1 ? "s" : ""} assigned
              </span>
              <ChevronDown className="h-3 w-3" />
            </Button>
          </CollapsibleTrigger>
          <CollapsibleContent className="space-y-1 mt-2">
            {group.roleAssignments.map((assignment) => (
              <div
                key={assignment.id}
                className="flex items-center justify-between p-2 bg-muted/50 rounded text-xs"
              >
                <div>
                  <div className="font-medium">{assignment.roleName}</div>
                  <div className="text-muted-foreground">
                    {assignment.moduleName}
                  </div>
                </div>
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() =>
                    onRemoveRoleAssignment(group.userGroupId, assignment.id)
                  }
                  className="h-6 w-6 p-0"
                  disabled={isLoading}
                >
                  <Trash2 className="h-3 w-3" />
                </Button>
              </div>
            ))}
          </CollapsibleContent>
        </Collapsible>
      ) : (
        <div className="text-xs text-muted-foreground p-2 bg-muted/30 rounded">
          No roles assigned
        </div>
      )}
    </div>
  );
};

export default UserGroupRolesManager;
