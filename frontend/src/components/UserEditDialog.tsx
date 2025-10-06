import React, { useState, useEffect } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import {
  Sheet,
  SheetContent,
  SheetDescription,
  SheetHeader,
  SheetTitle,
} from "@/components/ui/sheet";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { StatusBadge } from "@/components/StatusBadge";
import RoleAssignment from "@/components/UserRoleAssignment";
import UserGroupAssignment from "@/components/UserGroupAssignment";
import { usePermissions } from "@/hooks/usePermissions";
import { normalizeEntityStatus } from "@/lib/status-colors";
import {
  User,
  Key,
  AlertTriangle,
  CheckCircle,
  X,
} from "lucide-react";
import api from "@/lib/api";
import { useIsMobile } from "@/hooks/use-mobile";

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

interface PasswordChangeForm {
  newPassword: string;
  confirmPassword: string;
}

interface UserEditDialogProps {
  userId: number | null;
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onUserUpdated?: () => void;
}

const UserEditDialog: React.FC<UserEditDialogProps> = ({
  userId,
  open,
  onOpenChange,
  onUserUpdated,
}) => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(false);
  const [fetchLoading, setFetchLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState("profile");

  const [passwordForm, setPasswordForm] = useState<PasswordChangeForm>({
    newPassword: "",
    confirmPassword: "",
  });

  const { canUpdateUsers } = usePermissions();
  const isDesktop = useIsMobile() === false;

  useEffect(() => {
    const fetchUser = async () => {
      if (!userId || !open) return;
      
      try {
        setFetchLoading(true);
        setError(null);
        const response = await api.get(`/v1/users/${userId}`);
        setUser(response.data.data);
      } catch (err) {
        setError("Failed to fetch user data");
        console.error("Error fetching user:", err);
      } finally {
        setFetchLoading(false);
      }
    };

    fetchUser();
  }, [userId, open]);

  useEffect(() => {
    if (user) {
      setPasswordForm({ newPassword: "", confirmPassword: "" });
      setError(null);
      setSuccess(null);
    }
  }, [user]);

  const handleUserUpdated = async () => {
    if (!userId) return;
    
    try {
      const response = await api.get(`/v1/users/${userId}`);
      setUser(response.data.data);
      onUserUpdated?.();
    } catch (err) {
      console.error("Error refreshing user data:", err);
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
      handleUserUpdated();
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

  const handleClose = () => {
    setActiveTab("profile");
    setError(null);
    setSuccess(null);
    setPasswordForm({ newPassword: "", confirmPassword: "" });
    onOpenChange(false);
  };

  const DialogContentComponent = () => (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-lg font-semibold flex items-center gap-2">
            <User className="h-5 w-5" />
            {fetchLoading ? "Loading..." : user ? `Edit User: ${user.username}` : "User Not Found"}
          </h2>
          <p className="text-sm text-muted-foreground">
            Manage user status, password, roles, and permissions
          </p>
        </div>
        {!isDesktop && (
          <Button variant="ghost" size="sm" onClick={handleClose}>
            <X className="h-4 w-4" />
          </Button>
        )}
      </div>

      {/* Loading State */}
      {fetchLoading && (
        <div className="flex items-center justify-center py-8">
          <div className="text-center">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mx-auto mb-2"></div>
            <p className="text-sm text-muted-foreground">Loading user data...</p>
          </div>
        </div>
      )}

      {/* Error State */}
      {!fetchLoading && !user && (
        <div className="text-center py-8">
          <AlertTriangle className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
          <h3 className="text-lg font-medium mb-2">User Not Found</h3>
          <p className="text-sm text-muted-foreground">The requested user could not be found</p>
        </div>
      )}

      {/* Main Content */}
      {!fetchLoading && user && (
        <>
          {/* Alerts */}
          {error && (
            <Alert className="border-destructive">
              <AlertTriangle className="h-4 w-4" />
              <AlertDescription className="text-destructive">{error}</AlertDescription>
            </Alert>
          )}

          {success && (
            <Alert className="border-green-500 bg-green-50">
              <CheckCircle className="h-4 w-4 text-green-600" />
              <AlertDescription className="text-green-600">{success}</AlertDescription>
            </Alert>
          )}

          {/* Tabs */}
          <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full">
            <TabsList className={`grid w-full ${isDesktop ? 'grid-cols-4' : 'grid-cols-2'}`}>
              <TabsTrigger value="profile" className="text-xs sm:text-sm">Profile</TabsTrigger>
              <TabsTrigger value="status" className="text-xs sm:text-sm">Status</TabsTrigger>
              {isDesktop && <TabsTrigger value="password" className="text-xs sm:text-sm">Password</TabsTrigger>}
              {isDesktop && <TabsTrigger value="permissions" className="text-xs sm:text-sm">Permissions</TabsTrigger>}
              {!isDesktop && <TabsTrigger value="password" className="text-xs sm:text-sm">Password</TabsTrigger>}
              {!isDesktop && <TabsTrigger value="permissions" className="text-xs sm:text-sm">Permissions</TabsTrigger>}
            </TabsList>

            <div className={`mt-4 ${isDesktop ? 'max-h-[60vh]' : 'max-h-[50vh]'} overflow-y-auto`}>
              <TabsContent value="profile" className="space-y-4 mt-0">
                <Card>
                  <CardHeader className="pb-3">
                    <CardTitle className="text-base">User Information</CardTitle>
                    <CardDescription className="text-sm">Basic user details</CardDescription>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <div className={`grid ${isDesktop ? 'grid-cols-2' : 'grid-cols-1'} gap-4`}>
                      <div>
                        <Label className="text-sm">Username</Label>
                        <Input value={user.username} disabled className="text-sm" />
                      </div>
                      <div>
                        <Label className="text-sm">Email</Label>
                        <Input value={user.email} disabled className="text-sm" />
                      </div>
                    </div>
                    <div className={`grid ${isDesktop ? 'grid-cols-2' : 'grid-cols-1'} gap-4`}>
                      <div>
                        <Label className="text-sm">First Name</Label>
                        <Input value={user.firstName} disabled className="text-sm" />
                      </div>
                      <div>
                        <Label className="text-sm">Last Name</Label>
                        <Input value={user.lastName} disabled className="text-sm" />
                      </div>
                    </div>
                    <div>
                      <Label className="text-sm">Current Status</Label>
                      <div className="mt-2">
                        <StatusBadge
                          status={normalizeEntityStatus("user", user.userStatus)}
                        />
                      </div>
                    </div>
                  </CardContent>
                </Card>
              </TabsContent>

              <TabsContent value="status" className="space-y-4 mt-0">
                <Card>
                  <CardHeader className="pb-3">
                    <CardTitle className="text-base">User Status Management</CardTitle>
                    <CardDescription className="text-sm">
                      Activate, deactivate, or suspend user account
                    </CardDescription>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <div className="flex items-center justify-between p-3 border rounded-lg">
                      <div>
                        <h4 className="font-medium text-sm">Current Status</h4>
                        <p className="text-xs text-muted-foreground">
                          User is currently {user.userStatus?.toLowerCase() || 'unknown'}
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
                            className="w-full text-sm"
                            size={isDesktop ? "default" : "sm"}
                          >
                            <AlertTriangle className="mr-2 h-4 w-4" />
                            Suspend User
                          </Button>
                        ) : (
                          <Button
                            onClick={() => handleStatusChange("ACTIVE")}
                            disabled={loading}
                            className="w-full text-sm"
                            size={isDesktop ? "default" : "sm"}
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

              <TabsContent value="password" className="space-y-4 mt-0">
                <Card>
                  <CardHeader className="pb-3">
                    <CardTitle className="text-base">Change Password</CardTitle>
                    <CardDescription className="text-sm">
                      Set a new password for this user
                    </CardDescription>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <div>
                      <Label htmlFor="newPassword" className="text-sm">New Password</Label>
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
                        className="text-sm"
                      />
                    </div>
                    <div>
                      <Label htmlFor="confirmPassword" className="text-sm">Confirm Password</Label>
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
                        className="text-sm"
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
                        className="w-full text-sm"
                        size={isDesktop ? "default" : "sm"}
                      >
                        <Key className="mr-2 h-4 w-4" />
                        Change Password
                      </Button>
                    )}
                  </CardContent>
                </Card>
              </TabsContent>

              <TabsContent value="permissions" className="space-y-4 mt-0">
                <div className="grid gap-4">
                  <RoleAssignment
                    userId={user.id}
                    currentRoles={user.roles || []}
                    onRolesUpdated={handleUserUpdated}
                    loading={loading}
                  />

                  <UserGroupAssignment
                    userId={user.id}
                    currentUserGroups={user.userGroups || []}
                    onUserGroupsUpdated={handleUserUpdated}
                    loading={loading}
                  />
                </div>
              </TabsContent>
            </div>
          </Tabs>
        </>
      )}
    </div>
  );

  if (isDesktop) {
    return (
      <Dialog open={open} onOpenChange={handleClose}>
        <DialogContent className="max-w-4xl max-h-[90vh] overflow-hidden">
          <DialogHeader className="sr-only">
            <DialogTitle>Edit User</DialogTitle>
            <DialogDescription>
              Manage user status, password, roles, and permissions
            </DialogDescription>
          </DialogHeader>
          <DialogContentComponent />
        </DialogContent>
      </Dialog>
    );
  }

  return (
    <Sheet open={open} onOpenChange={handleClose}>
      <SheetContent className="max-h-[95vh] w-full sm:max-w-md">
        <SheetHeader className="sr-only">
          <SheetTitle>Edit User</SheetTitle>
          <SheetDescription>
            Manage user status, password, roles, and permissions
          </SheetDescription>
        </SheetHeader>
        <div className="px-4 pb-4">
          <DialogContentComponent />
        </div>
      </SheetContent>
    </Sheet>
  );
};

export default UserEditDialog;