import React, { useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Checkbox } from "@/components/ui/checkbox";
import { Badge } from "@/components/ui/badge";
import { Shield, ChevronDown, ChevronRight } from "lucide-react";

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

interface Role {
  id: number;
  name: string;
  description: string;
  moduleId: number;
  moduleName: string;
}

interface Module {
  id: number;
  name: string;
  description: string;
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
  modules: Module[];
  roles: Role[];
  onAssignRoles: (moduleId: number, roleIds: number[]) => void;
  onRemoveRoleAssignment: (groupId: number, assignmentId: number) => void;
  isLoading?: boolean;
}

const UserGroupRolesManager: React.FC<UserGroupRolesManagerProps> = ({
  group,
  modules,
  roles,
  onAssignRoles,
  onRemoveRoleAssignment,
  isLoading = false,
}) => {
  const [isExpanded, setIsExpanded] = useState(true);
  const [expandedModules, setExpandedModules] = useState<Set<number>>(new Set());

  // Toggle individual module expansion
  const toggleModuleExpansion = (moduleId: number) => {
    const newExpanded = new Set(expandedModules);
    if (newExpanded.has(moduleId)) {
      newExpanded.delete(moduleId);
    } else {
      newExpanded.add(moduleId);
    }
    setExpandedModules(newExpanded);
  };
  // Get assigned role IDs for quick lookup
  const getAssignedRoleIds = () => {
    if (!group.roleAssignments) return new Set<number>();
    return new Set(
      group.roleAssignments.map((assignment) => assignment.roleId)
    );
  };

  const assignedRoleIds = getAssignedRoleIds();

  // Get assignment ID for a specific role
  const getAssignmentIdForRole = (roleId: number) => {
    if (!group.roleAssignments) return null;
    const assignment = group.roleAssignments.find((a) => a.roleId === roleId);
    return assignment ? assignment.id : null;
  };

  // Group roles by module
  const getRolesByModule = () => {
    const rolesByModule: { [moduleId: number]: Role[] } = {};

    // Add safety checks for undefined arrays
    if (!modules || !roles) return rolesByModule;

    modules.forEach((module) => {
      rolesByModule[module.id] = roles.filter(
        (role) => role.moduleId === module.id
      );
    });

    return rolesByModule;
  };

  const rolesByModule = getRolesByModule();

  // Handle role checkbox toggle
  const handleRoleToggle = (role: Role, isAssigned: boolean) => {
    if (isAssigned) {
      // Remove role assignment
      const assignmentId = getAssignmentIdForRole(role.id);
      if (assignmentId) {
        onRemoveRoleAssignment(group.userGroupId, assignmentId);
      }
    } else {
      // Assign role
      onAssignRoles(role.moduleId, [role.id]);
    }
  };

  return (
    <Card>
      <CardHeader>
        <div 
          className="flex items-center justify-between cursor-pointer"
          onClick={() => setIsExpanded(!isExpanded)}
        >
          <CardTitle className="flex items-center gap-2">
            <Shield className="h-5 w-5" />
            Role Management
          </CardTitle>
          {isExpanded ? (
            <ChevronDown className="h-4 w-4" />
          ) : (
            <ChevronRight className="h-4 w-4" />
          )}
        </div>
      </CardHeader>
      {isExpanded && (
        <CardContent className="space-y-6">
      {!modules || modules.length === 0 ? (
        <div className="text-center py-8 text-muted-foreground">
          <Shield className="h-12 w-12 mx-auto mb-2 opacity-50" />
          <p>No modules available</p>
        </div>
      ) : (
        <div className="space-y-6">
          {modules.map((module) => {
            const moduleRoles = rolesByModule[module.id] || [];
            const assignedCount = moduleRoles.filter((role) =>
              assignedRoleIds.has(role.id)
            ).length;

            return (
              <div key={module.id} className="space-y-3">
                <div 
                  className="flex items-center justify-between cursor-pointer hover:bg-muted/30 p-2 rounded"
                  onClick={() => toggleModuleExpansion(module.id)}
                >
                  <div className="flex items-center gap-2">
                    <div className="flex items-center gap-2">
                      {expandedModules.has(module.id) ? (
                        <ChevronDown className="h-4 w-4" />
                      ) : (
                        <ChevronRight className="h-4 w-4" />
                      )}
                      <h3 className="text-lg font-semibold">{module.name}</h3>
                    </div>
                    <Badge variant="outline">
                      {assignedCount}/{moduleRoles.length} assigned
                    </Badge>
                  </div>
                </div>

                {module.description && expandedModules.has(module.id) && (
                  <p className="text-sm text-muted-foreground ml-6">
                    {module.description}
                  </p>
                )}

                {expandedModules.has(module.id) && (
                  <div className="border rounded-lg p-4 ml-6">
                  {moduleRoles.length > 0 ? (
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                      {moduleRoles.map((role) => {
                        const isAssigned = assignedRoleIds.has(role.id);

                        return (
                          <div
                            key={role.id}
                            className="flex items-start space-x-3 p-3 hover:bg-muted/50 rounded cursor-pointer"
                            onClick={() => handleRoleToggle(role, isAssigned)}
                          >
                            <Checkbox
                              checked={isAssigned}
                              onChange={() =>
                                handleRoleToggle(role, isAssigned)
                              }
                              disabled={isLoading}
                            />
                            <div className="flex-1 min-w-0">
                              <div className="text-sm font-medium">
                                {role.name}
                              </div>
                              <div className="text-xs text-muted-foreground">
                                {role.description}
                              </div>
                            </div>
                          </div>
                        );
                      })}
                    </div>
                  ) : (
                    <div className="text-center py-4 text-muted-foreground">
                      <p>No roles available for this module</p>
                    </div>
                  )}
                </div>
                )}
              </div>
            );
          })}
        </div>
      )}
        </CardContent>
      )}
    </Card>
  );
};

export default UserGroupRolesManager;
