import React, { useState, useEffect } from "react";
import { useNavigate, Link } from "react-router-dom";
import { Building2, Save } from "lucide-react";
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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { PermissionGuard } from "@/components/common/PermissionGuard";
import { usePermissions } from "@/hooks/usePermissions";
import api from "@/lib/api";

interface TenantFormData {
  tenantCode: string;
  name: string;
  type: "ENTERPRISE" | "STANDARD" | "BASIC";
  status: "ACTIVE" | "INACTIVE";
}

export default function TenantCreate() {
  const navigate = useNavigate();
  const { canCreateTenants } = usePermissions();
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState<TenantFormData>({
    tenantCode: "",
    name: "",
    type: "STANDARD",
    status: "ACTIVE",
  });
  const [errors, setErrors] = useState<Partial<TenantFormData>>({});

  // Redirect if user doesn't have permission to create tenants
  useEffect(() => {
    if (!canCreateTenants) {
      navigate("/unauthorized");
      return;
    }
  }, [canCreateTenants, navigate]);

  const validateForm = (): boolean => {
    const newErrors: Partial<TenantFormData> = {};

    if (!formData.tenantCode.trim()) {
      newErrors.tenantCode = "Tenant code is required";
    } else if (!/^[A-Z0-9_]+$/.test(formData.tenantCode)) {
      newErrors.tenantCode =
        "Tenant code must contain only uppercase letters, numbers, and underscores";
    }

    if (!formData.name.trim()) {
      newErrors.name = "Tenant name is required";
    } else if (formData.name.length < 2) {
      newErrors.name = "Tenant name must be at least 2 characters long";
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
      await api.post("/v1/tenants", formData);
      navigate("/tenants");
    } catch (error: any) {
      console.error("Failed to create tenant:", error);
      if (error.response?.data?.message) {
        setErrors({ tenantCode: error.response.data.message });
      }
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (field: keyof TenantFormData, value: string) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
    if (errors[field]) {
      setErrors((prev) => ({ ...prev, [field]: undefined }));
    }
  };

  // Don't render if user doesn't have permission
  if (!canCreateTenants) {
    return null;
  }

  return (
    <PermissionGuard permission="TENANT_MGMT:create">
      <div className="p-6">
        {/* Header */}
        <div className="flex items-center justify-between mb-6">
          <div className="mb-8">
            <Breadcrumb className="mb-4">
              <BreadcrumbList>
                <BreadcrumbItem>
                  <BreadcrumbLink asChild>
                    <Link to="/tenants">Tenants</Link>
                  </BreadcrumbLink>
                </BreadcrumbItem>
                <BreadcrumbSeparator />
                <BreadcrumbItem>
                  <BreadcrumbPage>Create</BreadcrumbPage>
                </BreadcrumbItem>
              </BreadcrumbList>
            </Breadcrumb>

            <div>
              <h1 className="text-2xl font-bold">Create New Tenant</h1>
              <p className="text-muted-foreground">
                Add a new tenant organization to the system
              </p>
            </div>
          </div>
        </div>

        {/* Main Content */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Left Column - Form */}
          <div className="lg:col-span-2">
            <Card>
              <CardHeader>
                <CardTitle>Tenant Information</CardTitle>
                <CardDescription>
                  Enter the details for the new tenant organization
                </CardDescription>
              </CardHeader>
              <CardContent>
                <form onSubmit={handleSubmit} className="space-y-6">
                  {/* Tenant Code */}
                  <div className="space-y-2">
                    <Label htmlFor="tenantCode">Tenant Code *</Label>
                    <Input
                      id="tenantCode"
                      value={formData.tenantCode}
                      onChange={(e) =>
                        handleInputChange(
                          "tenantCode",
                          e.target.value.toUpperCase()
                        )
                      }
                      placeholder="e.g., ACME_CORP"
                      maxLength={50}
                      className={errors.tenantCode ? "border-destructive" : ""}
                    />
                    {errors.tenantCode && (
                      <Alert variant="destructive">
                        <AlertDescription>{errors.tenantCode}</AlertDescription>
                      </Alert>
                    )}
                    <p className="text-sm text-muted-foreground">
                      Unique identifier for the tenant (uppercase letters,
                      numbers, and underscores only)
                    </p>
                  </div>

                  {/* Tenant Name */}
                  <div className="space-y-2">
                    <Label htmlFor="name">Tenant Name *</Label>
                    <Input
                      id="name"
                      value={formData.name}
                      onChange={(e) =>
                        handleInputChange("name", e.target.value)
                      }
                      placeholder="e.g., Acme Corporation"
                      maxLength={100}
                      className={errors.name ? "border-destructive" : ""}
                    />
                    {errors.name && (
                      <Alert variant="destructive">
                        <AlertDescription>{errors.name}</AlertDescription>
                      </Alert>
                    )}
                  </div>

                  {/* Tenant Type */}
                  <div className="space-y-2">
                    <Label htmlFor="type">Tenant Type *</Label>
                    <Select
                      value={formData.type}
                      onValueChange={(value) =>
                        handleInputChange(
                          "type",
                          value as TenantFormData["type"]
                        )
                      }
                    >
                      <SelectTrigger>
                        <SelectValue placeholder="Select tenant type" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="ENTERPRISE">Enterprise</SelectItem>
                        <SelectItem value="STANDARD">Standard</SelectItem>
                        <SelectItem value="BASIC">Basic</SelectItem>
                      </SelectContent>
                    </Select>
                    <p className="text-sm text-muted-foreground">
                      Select the type of tenant organization
                    </p>
                  </div>

                  {/* Status */}
                  <div className="space-y-2">
                    <Label htmlFor="status">Initial Status *</Label>
                    <Select
                      value={formData.status}
                      onValueChange={(value) =>
                        handleInputChange(
                          "status",
                          value as TenantFormData["status"]
                        )
                      }
                    >
                      <SelectTrigger>
                        <SelectValue placeholder="Select initial status" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="ACTIVE">Active</SelectItem>
                        <SelectItem value="INACTIVE">Inactive</SelectItem>
                      </SelectContent>
                    </Select>
                    <p className="text-sm text-muted-foreground">
                      Set the initial status for the tenant
                    </p>
                  </div>

                  {/* Form Actions */}
                  <div className="flex justify-end space-x-3 pt-6 border-t">
                    <Button
                      type="button"
                      variant="outline"
                      onClick={() => navigate("/tenants")}
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
                          <Save className="h-4 w-4 mr-2" />
                          Create Tenant
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
                <CardTitle>About Tenants</CardTitle>
                <CardDescription>
                  Understanding tenant organizations
                </CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  <div className="flex items-start">
                    <Building2 className="h-5 w-5 text-primary mt-0.5 mr-3" />
                    <div>
                      <h4 className="text-sm font-medium">Tenant Isolation</h4>
                      <p className="text-sm text-muted-foreground mt-1">
                        Each tenant has its own isolated data and user access
                        controls.
                      </p>
                    </div>
                  </div>

                  <div className="space-y-2">
                    <h4 className="text-sm font-medium">Tenant Types</h4>
                    <div className="text-sm text-muted-foreground space-y-1">
                      <p>
                        <strong>Enterprise:</strong> Full-featured tenant with
                        advanced capabilities
                      </p>
                      <p>
                        <strong>Standard:</strong> Standard tenant with core
                        features
                      </p>
                      <p>
                        <strong>Basic:</strong> Basic tenant with limited
                        features
                      </p>
                    </div>
                  </div>

                  <div className="space-y-2">
                    <h4 className="text-sm font-medium">Best Practices</h4>
                    <div className="text-sm text-muted-foreground space-y-1">
                      <p>• Use descriptive tenant codes</p>
                      <p>• Choose appropriate tenant type</p>
                      <p>• Start with ACTIVE status for immediate use</p>
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
