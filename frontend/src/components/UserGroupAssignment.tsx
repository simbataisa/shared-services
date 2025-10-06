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
import { Users, ChevronDown } from "lucide-react";

interface UserGroup {
  userGroupId: number;
  name: string;
  description: string;
}

interface UserGroupAssignmentProps {
  userId: number;
  currentUserGroups: UserGroup[];
  onUserGroupsUpdated: () => void;
  loading?: boolean;
}

const UserGroupAssignment: React.FC<UserGroupAssignmentProps> = ({
  userId,
  currentUserGroups,
  onUserGroupsUpdated,
  loading = false,
}) => {
  const [availableUserGroups, setAvailableUserGroups] = useState<UserGroup[]>([]);
  const [selectedUserGroupIds, setSelectedUserGroupIds] = useState<number[]>([]);
  const [isUserGroupsOpen, setIsUserGroupsOpen] = useState(true);
  const [isLoading, setIsLoading] = useState(false);

  // Initialize selected user groups from current user groups
  useEffect(() => {
    if (currentUserGroups) {
      setSelectedUserGroupIds(currentUserGroups.map((group) => group.userGroupId));
    }
  }, [currentUserGroups]);

  // Fetch available user groups
  useEffect(() => {
    const fetchUserGroups = async () => {
      try {
        const response = await fetch("/api/v1/user-groups", {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        });
        if (response.ok) {
          const data = await response.json();
          setAvailableUserGroups(data.data?.content || []);
        }
      } catch (error) {
        console.error("Error fetching user groups:", error);
      }
    };

    fetchUserGroups();
  }, []);

  const handleUserGroupAssignment = async () => {
    setIsLoading(true);
    try {
      const response = await fetch(`/api/v1/users/${userId}`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
        body: JSON.stringify({
          userGroupIds: selectedUserGroupIds,
        }),
      });

      if (response.ok) {
        onUserGroupsUpdated();
      } else {
        console.error("Failed to update user groups");
      }
    } catch (error) {
      console.error("Error updating user groups:", error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleUserGroupToggle = (userGroupId: number, checked: boolean) => {
    if (checked) {
      setSelectedUserGroupIds([...selectedUserGroupIds, userGroupId]);
    } else {
      setSelectedUserGroupIds(selectedUserGroupIds.filter((id) => id !== userGroupId));
    }
  };

  return (
    <PermissionGuard permission="user:assign_groups">
      <Collapsible
        open={isUserGroupsOpen}
        onOpenChange={setIsUserGroupsOpen}
      >
        <Card>
          <CollapsibleTrigger asChild>
            <CardHeader className="cursor-pointer hover:bg-muted/50 transition-colors">
              <div className="flex items-center justify-between">
                <div>
                  <CardTitle>User Groups</CardTitle>
                  <CardDescription>
                    Assign user to groups
                  </CardDescription>
                </div>
                <ChevronDown
                  className={`h-4 w-4 transition-transform ${
                    isUserGroupsOpen ? "rotate-180" : ""
                  }`}
                />
              </div>
            </CardHeader>
          </CollapsibleTrigger>
          <CollapsibleContent>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                {Array.isArray(availableUserGroups) &&
                availableUserGroups.length > 0 ? (
                  availableUserGroups.map((group) => (
                    <div
                      key={group.userGroupId}
                      className="flex items-center space-x-2"
                    >
                      <Checkbox
                        id={`group-${group.userGroupId}`}
                        checked={selectedUserGroupIds.includes(
                          group.userGroupId
                        )}
                        onCheckedChange={(checked: boolean) =>
                          handleUserGroupToggle(group.userGroupId, checked)
                        }
                        disabled={loading || isLoading}
                      />
                      <Label
                        htmlFor={`group-${group.userGroupId}`}
                        className="flex-1"
                      >
                        <div>
                          <div className="font-medium">
                            {group.name}
                          </div>
                          <div className="text-sm text-muted-foreground">
                            {group.description}
                          </div>
                        </div>
                      </Label>
                    </div>
                  ))
                ) : (
                  <div className="text-sm text-muted-foreground">
                    No user groups available
                  </div>
                )}
              </div>
              <PermissionGuard permission="user:update">
                <Button
                  onClick={handleUserGroupAssignment}
                  disabled={loading || isLoading}
                  className="w-full"
                >
                  <Users className="mr-2 h-4 w-4" />
                  Update User Groups
                </Button>
              </PermissionGuard>
            </CardContent>
          </CollapsibleContent>
        </Card>
      </Collapsible>
    </PermissionGuard>
  );
};

export default UserGroupAssignment;