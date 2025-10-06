import { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { Building2, ArrowLeft, Save } from "lucide-react";
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
import { Skeleton } from "@/components/ui/skeleton";
import api from "../../lib/api";

interface TenantFormData {
  tenantCode: string;
  name: string;
  type: "BUSINESS_IN" | "BUSINESS_OUT" | "INDIVIDUAL";
  status: "ACTIVE" | "INACTIVE" | "SUSPENDED";
}

export default function TenantEdit() {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const [loading, setLoading] = useState(false);
  const [initialLoading, setInitialLoading] = useState(true);
  const [formData, setFormData] = useState<TenantFormData>({
    tenantCode: "",
    name: "",
    type: "BUSINESS_IN",
    status: "ACTIVE",
  });
  const [errors, setErrors] = useState<Partial<TenantFormData>>({});

  useEffect(() => {
    const fetchTenant = async () => {
      if (!id) {
        navigate("/tenants");
        return;
      }

      try {
        setInitialLoading(true);
        const response = await api.get(`/v1/tenants/${id}`);
        const tenant = response.data.data || response.data;

        setFormData({
          tenantCode: tenant.code,
          name: tenant.name,
          type: tenant.type,
          status: tenant.status,
        });
      } catch (error) {
        console.error("Failed to fetch tenant:", error);
        navigate("/tenants");
      } finally {
        setInitialLoading(false);
      }
    };

    fetchTenant();
  }, [id, navigate]);

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
      await api.put(`/v1/tenants/${id}`, {
        code: formData.tenantCode,
        name: formData.name,
        type: formData.type,
        status: formData.status,
      });
      navigate("/tenants");
    } catch (error: any) {
      console.error("Failed to update tenant:", error);
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

        <div className="max-w-2xl">
          <Card>
            <CardHeader>
              <Skeleton className="h-6 w-32 mb-2" />
              <Skeleton className="h-4 w-64" />
            </CardHeader>
            <CardContent className="space-y-6">
              <div className="space-y-2">
                <Skeleton className="h-4 w-24" />
                <Skeleton className="h-10 w-full" />
              </div>
              <div className="space-y-2">
                <Skeleton className="h-4 w-24" />
                <Skeleton className="h-10 w-full" />
              </div>
              <div className="space-y-2">
                <Skeleton className="h-4 w-24" />
                <Skeleton className="h-10 w-full" />
              </div>
              <div className="space-y-2">
                <Skeleton className="h-4 w-24" />
                <Skeleton className="h-10 w-full" />
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    );
  }

  return (
    <div className="p-6">
      {/* Header */}
      <div className="flex items-center gap-4 mb-6">
        <Button
          variant="ghost"
          size="icon"
          onClick={() => navigate("/tenants")}
        >
          <ArrowLeft className="h-5 w-5" />
        </Button>
        <div>
          <h1 className="text-2xl font-bold">Edit Tenant</h1>
          <p className="text-muted-foreground">
            Update tenant organization details
          </p>
        </div>
      </div>

      {/* Form */}
      <div className="max-w-2xl">
        <Card>
          <CardHeader>
            <CardTitle>Tenant Information</CardTitle>
            <CardDescription>
              Update the details for this tenant organization
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
                  Unique identifier for the tenant (uppercase letters, numbers,
                  and underscores only)
                </p>
              </div>

              {/* Tenant Name */}
              <div className="space-y-2">
                <Label htmlFor="name">Tenant Name *</Label>
                <Input
                  id="name"
                  value={formData.name}
                  onChange={(e) => handleInputChange("name", e.target.value)}
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
                    handleInputChange("type", value as TenantFormData["type"])
                  }
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Select tenant type" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="BUSINESS_IN">
                      Business Internal
                    </SelectItem>
                    <SelectItem value="BUSINESS_OUT">
                      Business External
                    </SelectItem>
                    <SelectItem value="INDIVIDUAL">Individual</SelectItem>
                  </SelectContent>
                </Select>
                <p className="text-sm text-muted-foreground">
                  Select the type of tenant organization
                </p>
              </div>

              {/* Status */}
              <div className="space-y-2">
                <Label htmlFor="status">Status *</Label>
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
                    <SelectValue placeholder="Select status" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="ACTIVE">Active</SelectItem>
                    <SelectItem value="INACTIVE">Inactive</SelectItem>
                    <SelectItem value="SUSPENDED">Suspended</SelectItem>
                  </SelectContent>
                </Select>
                <p className="text-sm text-muted-foreground">
                  Set the status for the tenant
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
                      Updating...
                    </>
                  ) : (
                    <>
                      <Save className="h-4 w-4 mr-2" />
                      Update Tenant
                    </>
                  )}
                </Button>
              </div>
            </form>
          </CardContent>
        </Card>

        {/* Info Card */}
        <Card className="mt-6">
          <CardContent className="pt-6">
            <div className="flex items-start">
              <Building2 className="h-5 w-5 text-primary mt-0.5 mr-3" />
              <div>
                <h3 className="text-sm font-medium">About Tenant Editing</h3>
                <p className="text-sm text-muted-foreground mt-1">
                  Changes to tenant information will affect all users and data
                  associated with this tenant. Please ensure all details are
                  correct before saving.
                </p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
