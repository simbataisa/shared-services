import React, { useState, useEffect } from "react";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Checkbox } from "@/components/ui/checkbox";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import {
  Collapsible,
  CollapsibleContent,
  CollapsibleTrigger,
} from "@/components/ui/collapsible";
import { PermissionGuard } from "@/components/PermissionGuard";
import { Shield, ChevronDown } from "lucide-react";

interface Role {
  id: number;
  name: string;
  description: string;
}

interface RoleAssignmentProps {
  userId: number;
  currentRoles: Role[];
  onRolesUpdated: () => void;
  loading?: boolean;
}

const RoleAssignment: React.FC<RoleAssignmentProps> = ({
  userId,
  currentRoles,
  onRolesUpdated,
  loading = false,
}) => {
  const [availableRoles, setAvailableRoles] = useState<Role[]>([]);
  const [selectedRoleIds, setSelectedRoleIds] = useState<number[]>([]);
  const [isRolesOpen, setIsRolesOpen] = useState(true);
  const [isLoading, setIsLoading] = useState(false);

  // Initialize selected roles from current user roles
  useEffect(() => {
    if (currentRoles) {
      setSelectedRoleIds(currentRoles.map((role) => role.id));
    }
  }, [currentRoles]);

  // Fetch available roles
  useEffect(() => {
    const fetchRoles = async () => {
      try {
        const response = await fetch("/api/v1/roles", {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        });
        if (response.ok) {
          const data = await response.json();
          setAvailableRoles(data || []);
        }
      } catch (error) {
        console.error("Error fetching roles:", error);
      }
    };

    fetchRoles();
  }, []);

  const handleRoleAssignment = async () => {
    setIsLoading(true);
    try {
      const response = await fetch(`/api/v1/users/${userId}`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
        body: JSON.stringify({
          roleIds: selectedRoleIds,
        }),
      });

      if (response.ok) {
        onRolesUpdated();
      } else {
        console.error("Failed to update user roles");
      }
    } catch (error) {
      console.error("Error updating user roles:", error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleRoleToggle = (roleId: number, checked: boolean) => {
    if (checked) {
      setSelectedRoleIds([...selectedRoleIds, roleId]);
    } else {
      setSelectedRoleIds(selectedRoleIds.filter((id) => id !== roleId));
    }
  };

  return (
    <PermissionGuard permission="user:assign_roles">
      <Collapsible open={isRolesOpen} onOpenChange={setIsRolesOpen}>
        <Card>
          <CollapsibleTrigger asChild>
            <CardHeader className="cursor-pointer hover:bg-muted/50 transition-colors">
              <div className="flex items-center justify-between">
                <div>
                  <CardTitle>Roles</CardTitle>
                  <CardDescription>Assign or remove user roles</CardDescription>
                </div>
                <ChevronDown
                  className={`h-4 w-4 transition-transform ${
                    isRolesOpen ? "rotate-180" : ""
                  }`}
                />
              </div>
            </CardHeader>
          </CollapsibleTrigger>
          <CollapsibleContent>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                {Array.isArray(availableRoles) && availableRoles.length > 0 ? (
                  availableRoles.map((role) => (
                    <div key={role.id} className="flex items-center space-x-2">
                      <Checkbox
                        id={`role-${role.id}`}
                        checked={selectedRoleIds.includes(role.id)}
                        onCheckedChange={(checked: boolean) =>
                          handleRoleToggle(role.id, checked)
                        }
                        disabled={loading || isLoading}
                      />
                      <Label htmlFor={`role-${role.id}`} className="flex-1">
                        <div>
                          <div className="font-medium">{role.name}</div>
                          <div className="text-sm text-muted-foreground">
                            {role.description}
                          </div>
                        </div>
                      </Label>
                    </div>
                  ))
                ) : (
                  <div className="text-sm text-muted-foreground">
                    No roles available
                  </div>
                )}
              </div>
              <PermissionGuard permission="user:update">
                <Button
                  onClick={handleRoleAssignment}
                  disabled={loading || isLoading}
                  className="w-full"
                >
                  <Shield className="mr-2 h-4 w-4" />
                  Update Roles
                </Button>
              </PermissionGuard>
            </CardContent>
          </CollapsibleContent>
        </Card>
      </Collapsible>
    </PermissionGuard>
  );
};

export default RoleAssignment;
