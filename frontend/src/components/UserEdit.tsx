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
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
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
import { StatusBadge } from "./StatusBadge";
import { usePermissions } from "@/hooks/usePermissions";
import { normalizeEntityStatus } from "@/lib/status-colors";
import {
  User,
  Shield,
  Key,
  Users,
  AlertTriangle,
  CheckCircle,
  ChevronDown,
} from "lucide-react";

interface User {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  userStatus: "ACTIVE" | "INACTIVE";
  createdAt: string;
  updatedAt: string;
  roles: Role[];
  userGroups: UserGroup[];
}

interface Role {
  id: number;
  name: string;
  description: string;
}

interface UserGroup {
  userGroupId: number;
  name: string;
  description: string;
}

interface UserEditProps {
  user: User | null;
  isOpen: boolean;
  onClose: () => void;
  onUserUpdated: () => void;
}

interface PasswordChangeForm {
  newPassword: string;
  confirmPassword: string;
}

const UserEdit: React.FC<UserEditProps> = ({
  user,
  isOpen,
  onClose,
  onUserUpdated,
}) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState("profile");

  // Available roles and user groups
  const [availableRoles, setAvailableRoles] = useState<Role[]>([]);
  const [availableUserGroups, setAvailableUserGroups] = useState<UserGroup[]>(
    []
  );

  // Collapsible states
  const [isRolesOpen, setIsRolesOpen] = useState(true);
  const [isUserGroupsOpen, setIsUserGroupsOpen] = useState(true);

  // Form states
  const [selectedRoleIds, setSelectedRoleIds] = useState<number[]>([]);
  const [selectedUserGroupIds, setSelectedUserGroupIds] = useState<number[]>(
    []
  );
  const [passwordForm, setPasswordForm] = useState<PasswordChangeForm>({
    newPassword: "",
    confirmPassword: "",
  });

  const { canUpdateUsers, canAssignPermissions } = usePermissions();

  useEffect(() => {
    if (user && isOpen) {
      setSelectedRoleIds(user.roles?.map((role) => role.id) || []);
      setSelectedUserGroupIds(
        user.userGroups?.map((group) => group.userGroupId) || []
      );
      setPasswordForm({ newPassword: "", confirmPassword: "" });
      setError(null);
      setSuccess(null);
      fetchAvailableRolesAndGroups();
    }
  }, [user, isOpen]);

  const fetchAvailableRolesAndGroups = async () => {
    try {
      // Fetch roles
      const rolesResponse = await fetch("/api/v1/roles", {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
      });
      if (rolesResponse.ok) {
        const rolesData = await rolesResponse.json();
        setAvailableRoles(rolesData || []);
      }

      // Fetch user groups
      const groupsResponse = await fetch("/api/v1/user-groups", {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
      });
      if (groupsResponse.ok) {
        const groupsData = await groupsResponse.json();
        setAvailableUserGroups(groupsData.data?.content || []);
      }
    } catch (err) {
      console.error("Failed to fetch roles and groups:", err);
    }
  };

  const handleStatusChange = async (newStatus: "ACTIVE" | "INACTIVE") => {
    if (!user) return;

    try {
      setLoading(true);
      setError(null);

      const endpoint = newStatus === "ACTIVE" ? "activate" : "deactivate";
      const response = await fetch(`/api/v1/users/${user.id}/${endpoint}`, {
        method: "PUT",
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
      });

      if (!response.ok) {
        throw new Error(`Failed to ${newStatus.toLowerCase()} user`);
      }

      setSuccess(`User ${newStatus.toLowerCase()}d successfully`);
      onUserUpdated();
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : `Failed to ${newStatus.toLowerCase()} user`
      );
    } finally {
      setLoading(false);
    }
  };

  const handlePasswordChange = async () => {
    if (!user) return;

    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      setError("Passwords do not match");
      return;
    }

    if (passwordForm.newPassword.length < 6) {
      setError("Password must be at least 6 characters long");
      return;
    }

    try {
      setLoading(true);
      setError(null);

      const response = await fetch(`/api/v1/users/${user.id}/change-password`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
        body: JSON.stringify({
          newPassword: passwordForm.newPassword,
        }),
      });

      if (!response.ok) {
        throw new Error("Failed to change password");
      }

      setSuccess("Password changed successfully");
      setPasswordForm({ newPassword: "", confirmPassword: "" });
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "Failed to change password"
      );
    } finally {
      setLoading(false);
    }
  };

  const handleRoleAssignment = async () => {
    if (!user) return;

    try {
      setLoading(true);
      setError(null);

      // Get current role IDs
      const currentRoleIds = user.roles?.map((role) => role.id) || [];

      // Find roles to add and remove
      const rolesToAdd = selectedRoleIds.filter(
        (id) => !currentRoleIds.includes(id)
      );
      const rolesToRemove = currentRoleIds.filter(
        (id) => !selectedRoleIds.includes(id)
      );

      // Add new roles
      for (const roleId of rolesToAdd) {
        const response = await fetch(
          `/api/v1/users/${user.id}/roles/${roleId}`,
          {
            method: "POST",
            headers: {
              Authorization: `Bearer ${localStorage.getItem("token")}`,
            },
          }
        );
        if (!response.ok) {
          throw new Error(`Failed to assign role ${roleId}`);
        }
      }

      // Remove roles
      for (const roleId of rolesToRemove) {
        const response = await fetch(
          `/api/v1/users/${user.id}/roles/${roleId}`,
          {
            method: "DELETE",
            headers: {
              Authorization: `Bearer ${localStorage.getItem("token")}`,
            },
          }
        );
        if (!response.ok) {
          throw new Error(`Failed to remove role ${roleId}`);
        }
      }

      setSuccess("Roles updated successfully");
      onUserUpdated();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to update roles");
    } finally {
      setLoading(false);
    }
  };

  const handleUserGroupAssignment = async () => {
    if (!user) return;

    try {
      setLoading(true);
      setError(null);

      const response = await fetch(`/api/v1/users/${user.id}/user-groups`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
        body: JSON.stringify({
          userGroupIds: selectedUserGroupIds,
        }),
      });

      if (!response.ok) {
        throw new Error("Failed to update user groups");
      }

      setSuccess("User groups updated successfully");
      onUserUpdated();
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "Failed to update user groups"
      );
    } finally {
      setLoading(false);
    }
  };

  if (!user) return null;

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="sm:max-w-[600px] max-h-[80vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <User className="h-5 w-5" />
            Edit User: {user.username}
          </DialogTitle>
          <DialogDescription>
            Manage user status, password, roles, and permissions
          </DialogDescription>
        </DialogHeader>

        {error && (
          <Alert className="mb-4">
            <AlertTriangle className="h-4 w-4" />
            <AlertDescription>{error}</AlertDescription>
          </Alert>
        )}

        {success && (
          <Alert className="mb-4">
            <CheckCircle className="h-4 w-4" />
            <AlertDescription className="text-green-600">
              {success}
            </AlertDescription>
          </Alert>
        )}

        <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full">
          <TabsList className="grid w-full grid-cols-4">
            <TabsTrigger value="profile">Profile</TabsTrigger>
            <TabsTrigger value="status">Status</TabsTrigger>
            <TabsTrigger value="password">Password</TabsTrigger>
            <TabsTrigger value="permissions">Permissions</TabsTrigger>
          </TabsList>

          <TabsContent value="profile" className="space-y-4">
            <Card>
              <CardHeader>
                <CardTitle>User Information</CardTitle>
                <CardDescription>Basic user details</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <Label>Username</Label>
                    <Input value={user.username} disabled />
                  </div>
                  <div>
                    <Label>Email</Label>
                    <Input value={user.email} disabled />
                  </div>
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <Label>First Name</Label>
                    <Input value={user.firstName} disabled />
                  </div>
                  <div>
                    <Label>Last Name</Label>
                    <Input value={user.lastName} disabled />
                  </div>
                </div>
                <div>
                  <Label>Current Status</Label>
                  <div className="mt-2">
                    <StatusBadge
                      status={normalizeEntityStatus("user", user.userStatus)}
                    />
                  </div>
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="status" className="space-y-4">
            <Card>
              <CardHeader>
                <CardTitle>User Status Management</CardTitle>
                <CardDescription>
                  Activate, deactivate, or suspend user account
                </CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex items-center justify-between p-4 border rounded-lg">
                  <div>
                    <h4 className="font-medium">Current Status</h4>
                    <p className="text-sm text-muted-foreground">
                      User is currently {user.userStatus.toLowerCase()}
                    </p>
                  </div>
                  <StatusBadge
                    status={normalizeEntityStatus("user", user.userStatus)}
                  />
                </div>

                {canUpdateUsers && (
                  <div className="space-y-2">
                    {user.userStatus === "ACTIVE" ? (
                      <Button
                        onClick={() => handleStatusChange("INACTIVE")}
                        disabled={loading}
                        variant="destructive"
                        className="w-full"
                      >
                        <AlertTriangle className="mr-2 h-4 w-4" />
                        Suspend User
                      </Button>
                    ) : (
                      <Button
                        onClick={() => handleStatusChange("ACTIVE")}
                        disabled={loading}
                        className="w-full"
                      >
                        <CheckCircle className="mr-2 h-4 w-4" />
                        Reactivate User
                      </Button>
                    )}
                  </div>
                )}
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="password" className="space-y-4">
            <Card>
              <CardHeader>
                <CardTitle>Change Password</CardTitle>
                <CardDescription>
                  Set a new password for this user
                </CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div>
                  <Label htmlFor="newPassword">New Password</Label>
                  <Input
                    id="newPassword"
                    type="password"
                    value={passwordForm.newPassword}
                    onChange={(e) =>
                      setPasswordForm({
                        ...passwordForm,
                        newPassword: e.target.value,
                      })
                    }
                    placeholder="Enter new password"
                  />
                </div>
                <div>
                  <Label htmlFor="confirmPassword">Confirm Password</Label>
                  <Input
                    id="confirmPassword"
                    type="password"
                    value={passwordForm.confirmPassword}
                    onChange={(e) =>
                      setPasswordForm({
                        ...passwordForm,
                        confirmPassword: e.target.value,
                      })
                    }
                    placeholder="Confirm new password"
                  />
                </div>
                {canUpdateUsers && (
                  <Button
                    onClick={handlePasswordChange}
                    disabled={
                      loading ||
                      !passwordForm.newPassword ||
                      !passwordForm.confirmPassword
                    }
                    className="w-full"
                  >
                    <Key className="mr-2 h-4 w-4" />
                    Change Password
                  </Button>
                )}
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="permissions" className="space-y-4">
            <div className="grid gap-4">
              <Collapsible open={isRolesOpen} onOpenChange={setIsRolesOpen}>
                <Card>
                  <CollapsibleTrigger asChild>
                    <CardHeader className="cursor-pointer hover:bg-muted/50 transition-colors">
                      <div className="flex items-center justify-between">
                        <div>
                          <CardTitle>Roles</CardTitle>
                          <CardDescription>
                            Assign or remove user roles
                          </CardDescription>
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
                        {Array.isArray(availableRoles) &&
                        availableRoles.length > 0 ? (
                          availableRoles.map((role) => (
                            <div
                              key={role.id}
                              className="flex items-center space-x-2"
                            >
                              <Checkbox
                                id={`role-${role.id}`}
                                checked={selectedRoleIds.includes(role.id)}
                                onCheckedChange={(checked: boolean) => {
                                  if (checked) {
                                    setSelectedRoleIds([
                                      ...selectedRoleIds,
                                      role.id,
                                    ]);
                                  } else {
                                    setSelectedRoleIds(
                                      selectedRoleIds.filter(
                                        (id) => id !== role.id
                                      )
                                    );
                                  }
                                }}
                                disabled={!canAssignPermissions}
                              />
                              <Label
                                htmlFor={`role-${role.id}`}
                                className="flex-1"
                              >
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
                      {canAssignPermissions && (
                        <Button
                          onClick={handleRoleAssignment}
                          disabled={loading}
                          className="w-full"
                        >
                          <Shield className="mr-2 h-4 w-4" />
                          Update Roles
                        </Button>
                      )}
                    </CardContent>
                  </CollapsibleContent>
                </Card>
              </Collapsible>

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
                                onCheckedChange={(checked: boolean) => {
                                  if (checked) {
                                    setSelectedUserGroupIds([
                                      ...selectedUserGroupIds,
                                      group.userGroupId,
                                    ]);
                                  } else {
                                    setSelectedUserGroupIds(
                                      selectedUserGroupIds.filter(
                                        (id) => id !== group.userGroupId
                                      )
                                    );
                                  }
                                }}
                                disabled={!canAssignPermissions}
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
                      {canAssignPermissions && (
                        <Button
                          onClick={handleUserGroupAssignment}
                          disabled={loading}
                          className="w-full"
                        >
                          <Users className="mr-2 h-4 w-4" />
                          Update User Groups
                        </Button>
                      )}
                    </CardContent>
                  </CollapsibleContent>
                </Card>
              </Collapsible>
            </div>
          </TabsContent>
        </Tabs>

        <DialogFooter>
          <Button variant="outline" onClick={onClose}>
            Close
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};

export default UserEdit;
