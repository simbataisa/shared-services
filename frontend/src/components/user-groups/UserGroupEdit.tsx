import React, { useState, useEffect } from "react";
import { useNavigate, useParams, Link } from "react-router-dom";
import { Users, ArrowLeft, Save } from "lucide-react";
import {
  Breadcrumb,
  BreadcrumbItem,
  BreadcrumbLink,
  BreadcrumbList,
  BreadcrumbPage,
  BreadcrumbSeparator,
} from "@/components/ui/breadcrumb";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Skeleton } from "@/components/ui/skeleton";
import { PermissionGuard } from "@/components/PermissionGuard";
import { usePermissions } from "@/hooks/usePermissions";
import UserGroupRolesManager from "./UserGroupRolesManager";
import api from "@/lib/api";

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

interface UserGroupFormData {
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

export default function UserGroupEdit() {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const { canManageUsers } = usePermissions();
  const [loading, setLoading] = useState(false);
  const [initialLoading, setInitialLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [formData, setFormData] = useState<UserGroupFormData>({
    name: "",
    description: "",
  });
  const [errors, setErrors] = useState<Partial<UserGroupFormData>>({});
  const [userGroup, setUserGroup] = useState<UserGroup | null>(null);

  // Role management state
  const [modules, setModules] = useState<Module[]>([]);
  const [roles, setRoles] = useState<Role[]>([]);
  const [roleLoading, setRoleLoading] = useState(false);

  // Role assignment functions
  const fetchModulesAndRoles = async () => {
    try {
      setRoleLoading(true);
      console.log("Fetching modules and roles...");
      const [modulesResponse, rolesResponse] = await Promise.all([
        api.get("/v1/modules"),
        api.get("/v1/roles"),
      ]);
      console.log("Modules response:", modulesResponse.data);
      console.log("Roles response:", rolesResponse.data);
      
      // The API returns the data directly, not wrapped in a data property
      const modulesData = Array.isArray(modulesResponse.data) ? modulesResponse.data : modulesResponse.data.data || [];
      const rolesData = Array.isArray(rolesResponse.data) ? rolesResponse.data : rolesResponse.data.data || [];
      
      setModules(modulesData);
      setRoles(rolesData);
      console.log("Set modules:", modulesData);
      console.log("Set roles:", rolesData);
    } catch (e) {
      console.error("Error fetching modules and roles:", e);
      setError("Failed to fetch modules and roles");
    } finally {
      setRoleLoading(false);
    }
  };

  const assignRoles = async (moduleId: number, roleIds: number[]) => {
    if (!userGroup || roleIds.length === 0) return;

    try {
      setRoleLoading(true);
      await api.post(`/v1/user-groups/${userGroup.userGroupId}/roles`, {
        moduleId: moduleId,
        roleIds: roleIds,
      });
      // Refresh user group data to show updated roles
      const response = await api.get(`/v1/user-groups/${id}`);
      const group = response.data.data;
      setUserGroup(group);
    } catch (e) {
      setError("Failed to assign roles");
    } finally {
      setRoleLoading(false);
    }
  };

  const removeRoleAssignment = async (
    groupId: number,
    assignmentId: number
  ) => {
    try {
      setLoading(true);
      await api.delete(`/v1/user-groups/${groupId}/roles`, {
        data: { roleAssignmentIds: [assignmentId] },
      });
      // Refresh user group data to show updated roles
      const response = await api.get(`/v1/user-groups/${id}`);
      const group = response.data.data;
      setUserGroup(group);
    } catch (e) {
      setError("Failed to remove role assignment");
    } finally {
      setLoading(false);
    }
  };

  // Redirect if user doesn't have permission to manage user groups
  useEffect(() => {
    if (!canManageUsers) {
      navigate("/unauthorized");
      return;
    }
  }, [canManageUsers, navigate]);

  useEffect(() => {
    const fetchUserGroup = async () => {
      if (!id || !canManageUsers) {
        navigate("/user-groups");
        return;
      }

      try {
        setInitialLoading(true);
        setError(null);
        const response = await api.get(`/v1/user-groups/${id}`);
        const group = response.data.data; // Access the actual data from ApiResponse wrapper

        setUserGroup(group);
        setFormData({
          name: group.name,
          description: group.description || "",
        });
      } catch (error) {
        console.error("Failed to fetch user group:", error);
        setError("Failed to load user group details");
      } finally {
        setInitialLoading(false);
      }
    };

    fetchUserGroup();
    fetchModulesAndRoles(); // Add this line to fetch modules and roles
  }, [id, canManageUsers, navigate]);

  const validateForm = (): boolean => {
    const newErrors: Partial<UserGroupFormData> = {};

    if (!formData.name.trim()) {
      newErrors.name = "Group name is required";
    } else if (formData.name.trim().length < 2) {
      newErrors.name = "Group name must be at least 2 characters";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    try {
      setLoading(true);
      setError(null);

      await api.put(`/v1/user-groups/${id}`, {
        name: formData.name.trim(),
        description: formData.description.trim(),
      });

      navigate(`/user-groups/${id}`);
    } catch (error: any) {
      console.error("Failed to update user group:", error);
      if (error.response?.data?.message) {
        setError(error.response.data.message);
      } else {
        setError("Failed to update user group. Please try again.");
      }
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (field: keyof UserGroupFormData, value: string) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
    if (errors[field]) {
      setErrors((prev) => ({ ...prev, [field]: undefined }));
    }
  };

  // Don't render if user doesn't have permission
  if (!canManageUsers) {
    return null;
  }

  if (initialLoading) {
    return (
      <div className="p-6">
        <div className="flex items-center gap-4 mb-6">
          <Button variant="ghost" size="icon" disabled>
            <ArrowLeft className="h-5 w-5" />
          </Button>
          <div>
            <Skeleton className="h-8 w-48 mb-2" />
            <Skeleton className="h-4 w-64" />
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <div className="lg:col-span-2">
            <Card>
              <CardHeader>
                <Skeleton className="h-6 w-32 mb-2" />
                <Skeleton className="h-4 w-64" />
              </CardHeader>
              <CardContent className="space-y-6">
                {[...Array(3)].map((_, i) => (
                  <div key={i} className="space-y-2">
                    <Skeleton className="h-4 w-24" />
                    <Skeleton className="h-10 w-full" />
                  </div>
                ))}
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    );
  }

  if (error && !userGroup) {
    return (
      <div className="p-6">
        <div className="text-center py-12">
          <Users className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
          <h3 className="text-lg font-semibold mb-2">
            Failed to load user group
          </h3>
          <p className="text-muted-foreground mb-4">{error}</p>
          <Button onClick={() => navigate("/user-groups")}>
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back to User Groups
          </Button>
        </div>
      </div>
    );
  }

  return (
    <PermissionGuard permission="user-groups:update">
      <div className="p-6">
        {/* Header */}
        <div className="flex items-center justify-between mb-6">
          <div className="mb-8">
            <Breadcrumb className="mb-4">
              <BreadcrumbList>
                <BreadcrumbItem>
                  <BreadcrumbLink asChild>
                    <Link to="/user-groups">User Groups</Link>
                  </BreadcrumbLink>
                </BreadcrumbItem>
                <BreadcrumbSeparator />
                <BreadcrumbItem>
                  <BreadcrumbLink asChild>
                    <Link to={`/user-groups/${id}`}>
                      {formData.name || "User Group"}
                    </Link>
                  </BreadcrumbLink>
                </BreadcrumbItem>
                <BreadcrumbSeparator />
                <BreadcrumbItem>
                  <BreadcrumbPage>Edit</BreadcrumbPage>
                </BreadcrumbItem>
              </BreadcrumbList>
            </Breadcrumb>

            <div className="flex items-center justify-between">
              <div>
                <h1 className="text-2xl font-bold">Edit User Group</h1>
                <p className="text-muted-foreground">
                  Update user group information and settings
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* Main Content */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Left Column - Form */}
          <div className="lg:col-span-2">
            <Card>
              <CardHeader>
                <CardTitle>Group Information</CardTitle>
                <CardDescription>
                  Update the details for this user group
                </CardDescription>
              </CardHeader>
              <CardContent>
                <form onSubmit={handleSubmit} className="space-y-6">
                  {error && (
                    <Alert variant="destructive">
                      <AlertDescription>{error}</AlertDescription>
                    </Alert>
                  )}

                  {/* Group Name */}
                  <div className="space-y-2">
                    <Label htmlFor="name">Group Name *</Label>
                    <Input
                      id="name"
                      type="text"
                      value={formData.name}
                      onChange={(e) =>
                        handleInputChange("name", e.target.value)
                      }
                      placeholder="Enter group name"
                      disabled={loading}
                      className={errors.name ? "border-red-500" : ""}
                    />
                    {errors.name && (
                      <Alert variant="destructive">
                        <AlertDescription>{errors.name}</AlertDescription>
                      </Alert>
                    )}
                  </div>

                  {/* Description */}
                  <div className="space-y-2">
                    <Label htmlFor="description">Description</Label>
                    <Textarea
                      id="description"
                      value={formData.description}
                      onChange={(e) =>
                        handleInputChange("description", e.target.value)
                      }
                      placeholder="Enter group description (optional)"
                      disabled={loading}
                      rows={4}
                    />
                    <p className="text-sm text-muted-foreground">
                      Provide a brief description of the group's purpose
                    </p>
                  </div>

                  {/* Form Actions */}
                  <div className="flex justify-end space-x-3 pt-6 border-t">
                    <Button
                      type="button"
                      variant="outline"
                      onClick={() => navigate(`/user-groups/${id}`)}
                      disabled={loading}
                    >
                      Cancel
                    </Button>
                    <Button type="submit" disabled={loading}>
                      {loading ? (
                        <>
                          <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                          Updating...
                        </>
                      ) : (
                        <>
                          <Save className="h-4 w-4 mr-2" />
                          Update Group
                        </>
                      )}
                    </Button>
                  </div>
                </form>
              </CardContent>
            </Card>

            {/* Role Management Section */}
            {userGroup && (
              <div className="mt-6">
                <UserGroupRolesManager
                  group={userGroup}
                  modules={modules}
                  roles={roles}
                  onAssignRoles={assignRoles}
                  onRemoveRoleAssignment={removeRoleAssignment}
                  isLoading={loading || roleLoading}
                />
              </div>
            )}
          </div>

          {/* Right Column - Info */}
          <div className="space-y-6">
            <Card>
              <CardHeader>
                <CardTitle>About Group Editing</CardTitle>
                <CardDescription>
                  Important information about group updates
                </CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  <div className="flex items-start">
                    <Users className="h-5 w-5 text-primary mt-0.5 mr-3" />
                    <div>
                      <h4 className="text-sm font-medium">Impact of Changes</h4>
                      <p className="text-sm text-muted-foreground mt-1">
                        Changes to group information will be visible to all
                        members and administrators.
                      </p>
                    </div>
                  </div>

                  <div className="space-y-2">
                    <h4 className="text-sm font-medium">
                      Group Name Guidelines
                    </h4>
                    <div className="text-sm text-muted-foreground space-y-1">
                      <p>
                        • Use descriptive names that reflect the group's purpose
                      </p>
                      <p>• Keep names concise but meaningful</p>
                      <p>• Avoid special characters or numbers when possible</p>
                    </div>
                  </div>

                  <div className="space-y-2">
                    <h4 className="text-sm font-medium">Current Group Stats</h4>
                    <div className="text-sm text-muted-foreground space-y-1">
                      <p>
                        <strong>Members:</strong> {userGroup?.memberCount || 0}
                      </p>
                      <p>
                        <strong>Created:</strong>{" "}
                        {userGroup ? "Active" : "Unknown"}
                      </p>
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </PermissionGuard>
  );
}
