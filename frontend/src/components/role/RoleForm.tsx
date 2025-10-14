import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { ArrowLeft, Save, X } from "lucide-react";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { DetailHeaderCard } from "@/components/common";
import { RoleInfoCard } from "./RoleInfoCard";
import { RolePermissionCard } from "./RolePermissionCard";
import { usePermissions } from "@/hooks/usePermissions";
import httpClient from "@/lib/httpClient";
import { useFormValidation } from "@/utils/formValidator";
import { roleValidationSchema } from "@/utils/validationSchemas";
import { type RoleDetails, type Permission, type RoleFormData } from "@/types";

interface RoleFormProps {
  mode: "create" | "edit";
}

const RoleForm: React.FC<RoleFormProps> = ({ mode }) => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { canCreateRoles, canUpdateRoles } = usePermissions();

  const [role, setRole] = useState<RoleDetails | null>(null);
  const [formData, setFormData] = useState<{
    name: string;
    description: string;
    moduleId: number;
  }>({
    name: "",
    description: "",
    moduleId: 1,
  });
  const [loading, setLoading] = useState(mode === "edit");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [validationErrors, setValidationErrors] = useState<
    Record<string, string>
  >({});

  // Initialize form validation
  const { validateField, validateForm } =
    useFormValidation(roleValidationSchema);

  const isEditMode = mode === "edit";
  const isCreateMode = mode === "create";

  // Check permissions
  useEffect(() => {
    if (isCreateMode && !canCreateRoles) {
      navigate("/unauthorized");
      return;
    }
    if (isEditMode && !canUpdateRoles) {
      navigate("/unauthorized");
      return;
    }
  }, [isCreateMode, isEditMode, canCreateRoles, canUpdateRoles, navigate]);

  // Fetch role data for edit mode
  useEffect(() => {
    if (isEditMode && id && (canUpdateRoles || canCreateRoles)) {
      fetchRoleDetails();
    } else if (isCreateMode) {
      // Initialize empty role for create mode
      const initialRole = {
        id: 0,
        name: "",
        description: "",
        roleStatus: "ACTIVE",
        permissions: [],
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        moduleId: 1, // Default module ID
        moduleName: "System",
        userCount: 0,
        userGroupCount: 0,
      };
      setRole(initialRole);
      setFormData({
        name: initialRole.name,
        description: initialRole.description,
        moduleId: initialRole.moduleId || 1,
      });
      setLoading(false);
    }
  }, [id, isEditMode, isCreateMode, canUpdateRoles, canCreateRoles]);

  const fetchRoleDetails = async () => {
    try {
      setLoading(true);
      setError(null);

      const roleData: RoleDetails = await httpClient.getRoleDetails(Number(id));
      setRole(roleData);
    } catch (err: any) {
      console.error("Error fetching role details:", err);
      setError(err.response?.data?.message || "Failed to fetch role details");
    } finally {
      setLoading(false);
    }
  };

  const handleRoleUpdate = async (updatedData: {
    name: string;
    description: string;
  }) => {
    if (!role) return;

    try {
      setSaving(true);
      setError(null);
      setValidationErrors({});

      // Validate form data before submission
      const validationResult = validateForm(updatedData);
      if (!validationResult.isValid) {
        setValidationErrors(validationResult.errors);
        setSaving(false);
        return;
      }

      if (isCreateMode) {
        // Create new role
        const roleFormData: RoleFormData = {
          name: updatedData.name,
          description: updatedData.description,
          permissionIds: role.permissions?.map((p) => p.id) || [],
          moduleId: formData.moduleId,
        };

        const newRole = await httpClient.createRole(roleFormData);
        navigate(`/roles/${newRole.id}`);
      } else {
        // Update existing role
        await httpClient.updateRole(role.id, updatedData);

        // Update local state
        setRole({ ...role, ...updatedData });
      }
    } catch (err: any) {
      console.error("Error saving role:", err);
      setError(err.response?.data?.message || "Failed to save role");
      throw err; // Re-throw to let RoleInfoCard handle the error
    } finally {
      setSaving(false);
    }
  };

  const handlePermissionsUpdate = async (updatedPermissions: Permission[]) => {
    if (!role) return;

    try {
      // Update the role state with new permissions
      setRole({ ...role, permissions: updatedPermissions });
      setError(null);
    } catch (err: any) {
      console.error("Error updating permissions:", err);
      setError(err.response?.data?.message || "Failed to update permissions");
    }
  };

  const handleCancel = () => {
    if (isCreateMode) {
      navigate("/roles");
    } else {
      navigate(`/roles/${id}`);
    }
  };

  const handleSave = async () => {
    if (!role) return;

    try {
      setSaving(true);
      setError(null);
      setValidationErrors({});

      if (isCreateMode) {
        // Validate form data before creating role
        const roleData = {
          name: formData.name,
          description: formData.description,
        };
        const validationResult = validateForm(roleData);
        if (!validationResult.isValid) {
          setValidationErrors(validationResult.errors);
          setSaving(false);
          return;
        }

        const roleFormData: RoleFormData = {
          name: formData.name,
          description: formData.description,
          permissionIds: role.permissions?.map((p) => p.id) || [],
          moduleId: formData.moduleId,
        };

        const newRole = await httpClient.createRole(roleFormData);
        navigate(`/roles/${newRole.id}`);
      } else {
        // For edit mode, changes are saved automatically through individual cards
        navigate(`/roles/${id}`);
      }
    } catch (err: any) {
      console.error("Error saving role:", err);
      setError(err.response?.data?.message || "Failed to save role");
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 py-8">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="mb-8">
            <Skeleton className="h-6 w-48 mb-4" />
            <Skeleton className="h-8 w-64 mb-2" />
            <Skeleton className="h-4 w-32" />
          </div>
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            <div className="lg:col-span-2 space-y-8">
              <Skeleton className="h-64 w-full" />
              <Skeleton className="h-48 w-full" />
            </div>
            <div className="space-y-6">
              <Skeleton className="h-32 w-full" />
            </div>
          </div>
        </div>
      </div>
    );
  }

  if (error || !role) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <Card className="w-full max-w-md">
          <CardHeader>
            <CardTitle>
              {isCreateMode ? "Error Creating Role" : "Role Not Found"}
            </CardTitle>
            <CardDescription>
              {error || "The requested role could not be found."}
            </CardDescription>
          </CardHeader>
          <CardContent>
            <Button onClick={() => navigate("/roles")} className="w-full">
              <ArrowLeft className="mr-2 h-4 w-4" />
              Back to Roles
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  const pageTitle = isCreateMode ? "Create New Role" : `Edit ${role.name}`;
  const pageDescription = isCreateMode
    ? "Create a new role and assign permissions"
    : "Update role information and permissions";

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Header */}
        <DetailHeaderCard
          title={pageTitle}
          description={pageDescription}
          breadcrumbs={[
            { label: "Roles", href: "/roles" },
            { label: isCreateMode ? "New Role" : role.name },
          ]}
        />

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Main Content */}
          <div className="lg:col-span-2 space-y-8">
            {/* Role Information */}
            <RoleInfoCard
              role={role}
              onUpdate={handleRoleUpdate}
              onFormChange={setFormData}
              updating={saving}
              mode={mode}
              validationErrors={validationErrors}
              onFieldValidation={(fieldName: string, value: any) => {
                const error = validateField(fieldName, value);
                if (error) {
                  setValidationErrors((prev) => ({
                    ...prev,
                    [fieldName]: error,
                  }));
                } else {
                  setValidationErrors((prev) => {
                    const newErrors = { ...prev };
                    delete newErrors[fieldName];
                    return newErrors;
                  });
                }
              }}
            />

            {/* Permissions */}
            <RolePermissionCard
              roleId={role.id}
              permissions={role.permissions}
              title="Permissions"
              defaultExpanded={true}
              emptyMessage="No permissions assigned to this role."
              onPermissionsUpdate={handlePermissionsUpdate}
              updating={saving}
              mode={mode}
            />
          </div>

          {/* Sidebar */}
          <div className="space-y-6">
            {/* Action Buttons */}
            <Card>
              <CardHeader>
                <CardTitle>Actions</CardTitle>
                <CardDescription>
                  {isCreateMode
                    ? "Save the new role or cancel to go back"
                    : "Save changes or cancel to view the role"}
                </CardDescription>
              </CardHeader>
              <CardContent className="space-y-3">
                <Button
                  onClick={handleSave}
                  disabled={saving || !formData.name.trim()}
                  className="w-full"
                >
                  <Save className="mr-2 h-4 w-4" />
                  {saving
                    ? "Saving..."
                    : isCreateMode
                    ? "Create Role"
                    : "Save Changes"}
                </Button>
                <Button
                  variant="outline"
                  onClick={handleCancel}
                  disabled={saving}
                  className="w-full"
                >
                  <X className="mr-2 h-4 w-4" />
                  Cancel
                </Button>
              </CardContent>
            </Card>

            {/* Help Card */}
            <Card>
              <CardHeader>
                <CardTitle>Help</CardTitle>
              </CardHeader>
              <CardContent className="text-sm text-muted-foreground space-y-2">
                <p>
                  <strong>Role Name:</strong> Choose a descriptive name for the
                  role.
                </p>
                <p>
                  <strong>Description:</strong> Provide details about the role's
                  purpose.
                </p>
                <p>
                  <strong>Permissions:</strong> Select the permissions this role
                  should have.
                </p>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </div>
  );
};

export default RoleForm;
