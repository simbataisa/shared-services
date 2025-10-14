import { useState } from "react";
import { Shield, Settings, Plus, X, Edit, Save } from "lucide-react";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Checkbox } from "@/components/ui/checkbox";
import { PermissionGuard } from "@/components/common/PermissionGuard";
import type { RoleAssignment, Module, Role } from "@/types";

interface RoleAssignmentsCardProps {
  roleAssignments: RoleAssignment[];
  availableModules: Module[];
  availableRoles: Role[];
  onAssignRoles: (roleIds: number[]) => Promise<void>;
  onRemoveRoleAssignment: (assignmentId: number) => Promise<void>;
  updating: boolean;
}

export function RoleAssignmentsCard({
  roleAssignments,
  availableModules,
  availableRoles,
  onAssignRoles,
  onRemoveRoleAssignment,
  updating,
}: RoleAssignmentsCardProps) {
  const [isEditing, setIsEditing] = useState(false);
  const [selectedModule, setSelectedModule] = useState<string>("");
  const [selectedRoles, setSelectedRoles] = useState<number[]>([]);

  const handleEditToggle = () => {
    if (isEditing) {
      // Cancel edit - reset selections
      setSelectedModule("");
      setSelectedRoles([]);
    }
    setIsEditing(!isEditing);
  };

  const handleAssignRoles = async () => {
    if (selectedRoles.length === 0) return;

    try {
      await onAssignRoles(selectedRoles);
      // Reset selections after successful assignment
      setSelectedModule("");
      setSelectedRoles([]);
      setIsEditing(false);
    } catch (error) {
      console.error("Failed to assign roles:", error);
    }
  };

  const handleRemoveRole = async (assignmentId: number) => {
    try {
      await onRemoveRoleAssignment(assignmentId);
    } catch (error) {
      console.error("Failed to remove role assignment:", error);
    }
  };

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <div>
            <CardTitle>Role Assignments</CardTitle>
            <CardDescription>Roles assigned to this user group</CardDescription>
          </div>
          <PermissionGuard permission="GROUP_MGMT:update">
            {isEditing ? (
              <div className="flex gap-2">
                <Button
                  size="sm"
                  onClick={handleAssignRoles}
                  disabled={updating || selectedRoles.length === 0}
                >
                  <Save className="h-4 w-4 mr-2" />
                  Save Changes
                </Button>
                <Button
                  variant="outline"
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
        {/* Add Role Section - Only visible in edit mode */}
        {isEditing && (
          <PermissionGuard permission="GROUP_MGMT:update">
            <div className="mb-6 p-4 border border-dashed border-gray-300 rounded-lg">
              <h4 className="text-sm font-medium mb-3">Add New Roles</h4>
              <div className="space-y-3">
                <div>
                  <Label htmlFor="module-select">Select Module</Label>
                  <Select
                    value={selectedModule}
                    onValueChange={setSelectedModule}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Choose a module" />
                    </SelectTrigger>
                    <SelectContent>
                      {availableModules.map((module) => (
                        <SelectItem
                          key={module.id}
                          value={module.id.toString()}
                        >
                          {module.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                {selectedModule && (
                  <div>
                    <Label>Available Roles</Label>
                    <div className="mt-2 space-y-2 max-h-32 overflow-y-auto">
                      {availableRoles
                        .filter(
                          (role) => role.moduleId?.toString() === selectedModule
                        )
                        .map((role) => (
                          <div
                            key={role.id}
                            className="flex items-center space-x-2"
                          >
                            <Checkbox
                              id={`role-${role.id}`}
                              checked={selectedRoles.includes(role.id)}
                              onCheckedChange={(checked) => {
                                if (checked) {
                                  setSelectedRoles([...selectedRoles, role.id]);
                                } else {
                                  setSelectedRoles(
                                    selectedRoles.filter((id) => id !== role.id)
                                  );
                                }
                              }}
                            />
                            <Label
                              htmlFor={`role-${role.id}`}
                              className="text-sm"
                            >
                              {role.name} - {role.description}
                            </Label>
                          </div>
                        ))}
                    </div>

                    {selectedRoles.length > 0 && (
                      <div className="mt-3 p-2 bg-blue-50 rounded-md">
                        <p className="text-sm text-blue-700">
                          {selectedRoles.length} role(s) selected for assignment
                        </p>
                      </div>
                    )}
                  </div>
                )}
              </div>
            </div>
          </PermissionGuard>
        )}

        {/* Current Role Assignments */}
        {roleAssignments && roleAssignments.length > 0 ? (
          <div className="space-y-4">
            {roleAssignments.map((assignment) => (
              <div
                key={assignment.id}
                className="border border-gray-200 rounded-lg p-4"
              >
                <div className="flex items-center justify-between">
                  <div className="flex-1">
                    <div className="flex items-center space-x-3">
                      <h3 className="text-lg font-medium text-gray-900">
                        {assignment.roleName}
                      </h3>
                      <Badge variant="outline">{assignment.moduleName}</Badge>
                    </div>
                    <p className="mt-1 text-sm text-gray-600">
                      {assignment.roleDescription}
                    </p>
                  </div>
                  <PermissionGuard permission="USER_MGMT:assign_roles">
                    {isEditing ? (
                      <Button
                        variant="destructive"
                        size="sm"
                        onClick={() => handleRemoveRole(assignment.id)}
                        disabled={updating}
                      >
                        <X className="h-4 w-4 mr-2" />
                        Remove
                      </Button>
                    ) : (
                      <Button variant="ghost" size="sm">
                        <Settings className="h-4 w-4" />
                      </Button>
                    )}
                  </PermissionGuard>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="text-center py-8">
            <Shield className="mx-auto h-12 w-12 text-gray-400" />
            <p className="mt-2 text-gray-500">
              No roles assigned to this user group.
            </p>
            <PermissionGuard permission="USER_MGMT:assign_roles">
              {!isEditing && (
                <Button
                  variant="outline"
                  className="mt-4"
                  onClick={handleEditToggle}
                >
                  <Shield className="h-4 w-4 mr-2" />
                  Assign Roles
                </Button>
              )}
            </PermissionGuard>
          </div>
        )}
      </CardContent>
    </Card>
  );
}
