import React, { useState, useEffect } from "react";
import { useNavigate, Link } from "react-router-dom";
import { Users, Plus } from "lucide-react";
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
import { PermissionGuard } from "@/components/common/PermissionGuard";
import { usePermissions } from "@/hooks/usePermissions";
import type { UserGroupFormData } from "@/types";
import httpClient from "@/lib/httpClient";

export default function UserGroupCreate() {
  const navigate = useNavigate();
  const { canManageUsers } = usePermissions();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [formData, setFormData] = useState<UserGroupFormData>({
    name: "",
    description: "",
  });
  const [errors, setErrors] = useState<Partial<UserGroupFormData>>({});

  // Redirect if user doesn't have permission to manage user groups
  useEffect(() => {
    if (!canManageUsers) {
      navigate("/unauthorized");
      return;
    }
  }, [canManageUsers, navigate]);

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

      const newGroup = await httpClient.createUserGroup({
        name: formData.name.trim(),
        description: formData.description.trim(),
      });

      // Navigate to the newly created group's detail page
      navigate(`/user-groups/${newGroup.userGroupId}`);
    } catch (error: any) {
      console.error("Failed to create user group:", error);
      if (error.response?.data?.message) {
        setError(error.response.data.message);
      } else {
        setError("Failed to create user group. Please try again.");
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

  return (
    <PermissionGuard permission="GROUP_MGMT:create">
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
                  <BreadcrumbPage>Create New Group</BreadcrumbPage>
                </BreadcrumbItem>
              </BreadcrumbList>
            </Breadcrumb>

            <div className="flex items-center justify-between">
              <div>
                <h1 className="text-2xl font-bold">Create New User Group</h1>
                <p className="text-muted-foreground">
                  Set up a new user group to organize and manage users
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
                  Provide the basic details for the new user group
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
                      onClick={() => navigate("/user-groups")}
                      disabled={loading}
                    >
                      Cancel
                    </Button>
                    <Button type="submit" disabled={loading}>
                      {loading ? (
                        <>
                          <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                          Creating...
                        </>
                      ) : (
                        <>
                          <Plus className="h-4 w-4 mr-2" />
                          Create Group
                        </>
                      )}
                    </Button>
                  </div>
                </form>
              </CardContent>
            </Card>
          </div>

          {/* Right Column - Info */}
          <div className="space-y-6">
            <Card>
              <CardHeader>
                <CardTitle>About User Groups</CardTitle>
                <CardDescription>
                  Learn about user groups and their benefits
                </CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  <div className="flex items-start">
                    <Users className="h-5 w-5 text-primary mt-0.5 mr-3" />
                    <div>
                      <h4 className="text-sm font-medium">Organization</h4>
                      <p className="text-sm text-muted-foreground mt-1">
                        Group users by department, role, or project to simplify
                        management and permissions.
                      </p>
                    </div>
                  </div>

                  <div className="space-y-2">
                    <h4 className="text-sm font-medium">Best Practices</h4>
                    <div className="text-sm text-muted-foreground space-y-1">
                      <p>
                        • Use descriptive names that reflect the group's purpose
                      </p>
                      <p>
                        • Keep groups focused on specific roles or departments
                      </p>
                      <p>
                        • Add clear descriptions to help others understand the
                        group
                      </p>
                      <p>• Review group membership regularly</p>
                    </div>
                  </div>

                  <div className="space-y-2">
                    <h4 className="text-sm font-medium">After Creation</h4>
                    <div className="text-sm text-muted-foreground space-y-1">
                      <p>• Add users to the group</p>
                      <p>• Assign appropriate roles and permissions</p>
                      <p>• Configure group-specific settings</p>
                      <p>• Monitor group activity and usage</p>
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Need Help?</CardTitle>
                <CardDescription>
                  Resources for managing user groups
                </CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  <div className="text-sm">
                    <p className="font-medium mb-1">Documentation</p>
                    <p className="text-muted-foreground">
                      Check our user management guide for detailed instructions
                      on creating and managing user groups.
                    </p>
                  </div>

                  <div className="text-sm">
                    <p className="font-medium mb-1">Support</p>
                    <p className="text-muted-foreground">
                      Contact your system administrator if you need additional
                      permissions or have questions about group setup.
                    </p>
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
