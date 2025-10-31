import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { PermissionGuard } from "@/components/common/PermissionGuard";
import { StatusBadge } from "@/components/common/StatusBadge";
import { usePermissions } from "@/hooks/usePermissions";
import { normalizeEntityStatus } from "@/lib/status-utils";
import httpClient from "@/lib/httpClient";
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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Skeleton } from "@/components/ui/skeleton";
import { Separator } from "@/components/ui/separator";
import { ArrowLeft, Edit, Trash2, Save, X } from "lucide-react";
import type { Module, Product } from "@/types/entities";

interface ModuleFormData {
  name: string;
  description: string;
  code: string;
  productId: string;
  isActive: boolean;
}

const ModuleDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { canUpdateModules, canDeleteModules } = usePermissions();

  const [module, setModule] = useState<Module | null>(null);
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [editing, setEditing] = useState(false);
  const [saving, setSaving] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});

  const [formData, setFormData] = useState({
    name: "",
    description: "",
    code: "",
    productId: "",
    isActive: true,
  });

  useEffect(() => {
    if (id) {
      fetchModuleData();
      fetchProducts();
    }
  }, [id]);

  const fetchModuleData = async () => {
    try {
      setLoading(true);
      const moduleData = await httpClient.getModuleById(Number(id));

      setModule(moduleData);
      setFormData({
        name: moduleData.name,
        description: moduleData.description,
        code: moduleData.code,
        productId: moduleData.productId.toString(),
        isActive: moduleData.moduleStatus === 'ACTIVE',
      });
    } catch (error) {
      console.error("Error fetching module:", error);
      setErrors({ fetch: "Failed to load module data" });
    } finally {
      setLoading(false);
    }
  };

  const fetchProducts = async () => {
    try {
      const products = await httpClient.getProducts();
      setProducts(products || []);
    } catch (error) {
      console.error("Error fetching products:", error);
    }
  };

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!formData.name.trim()) {
      newErrors.name = "Module name is required";
    } else if (formData.name.length < 2) {
      newErrors.name = "Module name must be at least 2 characters long";
    }

    if (!formData.description.trim()) {
      newErrors.description = "Description is required";
    } else if (formData.description.length < 10) {
      newErrors.description = "Description must be at least 10 characters long";
    }

    if (!formData.code.trim()) {
      newErrors.code = "Module code is required";
    } else if (!/^[A-Z0-9_]+$/.test(formData.code)) {
      newErrors.code =
        "Module code must contain only uppercase letters, numbers, and underscores";
    }

    if (!formData.productId) {
      newErrors.productId = "Product selection is required";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSave = async () => {
    if (!validateForm()) {
      return;
    }

    setSaving(true);
    try {
      const updateData = {
        name: formData.name,
        description: formData.description,
        productId: parseInt(formData.productId),
        isActive: formData.isActive,
      };

      await httpClient.updateModule(Number(id), updateData);

      // Refresh module data
      await fetchModuleData();
      setEditing(false);
      setErrors({});
    } catch (error) {
      console.error("Error updating module:", error);
      setErrors({ save: "Failed to update module. Please try again." });
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    if (
      !window.confirm(
        "Are you sure you want to delete this module? This action cannot be undone."
      )
    ) {
      return;
    }

    setDeleting(true);
    try {
      await httpClient.deleteModule(Number(id));
      navigate("/modules");
    } catch (error) {
      console.error("Error deleting module:", error);
      setErrors({ delete: "Failed to delete module. Please try again." });
    } finally {
      setDeleting(false);
    }
  };

  const handleInputChange = (field: string, value: string | boolean) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
    // Clear error when user starts typing
    if (errors[field]) {
      setErrors((prev) => ({ ...prev, [field]: "" }));
    }
  };

  const handleCancel = () => {
    if (module) {
      setFormData({
        name: module.name,
        description: module.description,
        code: module.code,
        productId: module.productId.toString(),
        isActive: module.moduleStatus === 'ACTIVE',
      });
    }
    setEditing(false);
    setErrors({});
  };

  return (
    <PermissionGuard permission="MODULE_MGMT:read">
      <div className="min-h-screen bg-gray-50 py-8">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
          <Card>
            <CardHeader>
              <div className="flex justify-between items-center">
                <div>
                  <CardTitle className="text-2xl">
                    {module?.name || "Loading..."}
                  </CardTitle>
                  <CardDescription>Module Details</CardDescription>
                </div>
                <div className="flex space-x-3">
                  <Button
                    variant="outline"
                    onClick={() => navigate("/modules")}
                  >
                    <ArrowLeft className="h-4 w-4 mr-2" />
                    Back to List
                  </Button>
                  {canUpdateModules && !editing && (
                    <Button onClick={() => setEditing(true)}>
                      <Edit className="h-4 w-4 mr-2" />
                      Edit Module
                    </Button>
                  )}
                  {canDeleteModules && !editing && (
                    <Button
                      variant="destructive"
                      onClick={handleDelete}
                      disabled={deleting}
                    >
                      <Trash2 className="h-4 w-4 mr-2" />
                      {deleting ? "Deleting..." : "Delete"}
                    </Button>
                  )}
                </div>
              </div>
            </CardHeader>

            <CardContent>
              {(errors.save || errors.delete || errors.fetch) && (
                <Alert variant="destructive" className="mb-4">
                  <AlertDescription>
                    {errors.save || errors.delete || errors.fetch}
                  </AlertDescription>
                </Alert>
              )}

              {loading ? (
                <div className="space-y-6">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    {Array.from({ length: 6 }).map((_, i) => (
                      <div key={i}>
                        <Skeleton className="h-4 w-24 mb-2" />
                        <Skeleton className="h-6 w-full" />
                      </div>
                    ))}
                  </div>
                </div>
              ) : !module ? (
                <div className="text-center py-12">
                  <CardTitle className="text-2xl mb-2">
                    Module Not Found
                  </CardTitle>
                  <CardDescription className="mb-4">
                    The requested module could not be found.
                  </CardDescription>
                  <Button onClick={() => navigate("/modules")}>
                    <ArrowLeft className="h-4 w-4 mr-2" />
                    Back to Modules
                  </Button>
                </div>
              ) : editing ? (
                <div className="space-y-6">
                  <div>
                    <Label htmlFor="name">Module Name *</Label>
                    <Input
                      id="name"
                      value={formData.name}
                      onChange={(e) =>
                        handleInputChange("name", e.target.value)
                      }
                      className={errors.name ? "border-red-300" : ""}
                    />
                    {errors.name && (
                      <p className="mt-1 text-sm text-red-600">{errors.name}</p>
                    )}
                  </div>

                  <div>
                    <Label htmlFor="description">Description *</Label>
                    <Textarea
                      id="description"
                      rows={4}
                      value={formData.description}
                      onChange={(e) =>
                        handleInputChange("description", e.target.value)
                      }
                      className={errors.description ? "border-red-300" : ""}
                    />
                    {errors.description && (
                      <p className="mt-1 text-sm text-red-600">
                        {errors.description}
                      </p>
                    )}
                  </div>

                  <div>
                    <Label htmlFor="code">Module Code *</Label>
                    <Input
                      id="code"
                      value={formData.code}
                      onChange={(e) =>
                        handleInputChange("code", e.target.value.toUpperCase())
                      }
                      className={errors.code ? "border-red-300" : ""}
                    />
                    {errors.code && (
                      <p className="mt-1 text-sm text-red-600">{errors.code}</p>
                    )}
                  </div>

                  <div>
                    <Label htmlFor="productId">Product *</Label>
                    <Select
                      value={formData.productId}
                      onValueChange={(value) =>
                        handleInputChange("productId", value)
                      }
                    >
                      <SelectTrigger
                        className={errors.productId ? "border-red-300" : ""}
                      >
                        <SelectValue placeholder="Select a product" />
                      </SelectTrigger>
                      <SelectContent>
                        {products.map((product) => (
                          <SelectItem
                            key={product.id}
                            value={product.id.toString()}
                          >
                            {product.name}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    {errors.productId && (
                      <p className="mt-1 text-sm text-red-600">
                        {errors.productId}
                      </p>
                    )}
                  </div>

                  <div className="flex items-center space-x-2">
                    <input
                      type="checkbox"
                      id="isActive"
                      checked={formData.isActive}
                      onChange={(e) =>
                        handleInputChange("isActive", e.target.checked)
                      }
                      className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                    />
                    <Label htmlFor="isActive">Active</Label>
                  </div>

                  <Separator />

                  <div className="flex justify-end space-x-3">
                    <Button variant="outline" onClick={handleCancel}>
                      <X className="h-4 w-4 mr-2" />
                      Cancel
                    </Button>
                    <Button onClick={handleSave} disabled={saving}>
                      <Save className="h-4 w-4 mr-2" />
                      {saving ? "Saving..." : "Save Changes"}
                    </Button>
                  </div>
                </div>
              ) : (
                <div className="space-y-6">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div>
                      <h3 className="text-sm font-medium text-gray-500">
                        Module Name
                      </h3>
                      <p className="mt-1 text-sm text-gray-900">
                        {module.name}
                      </p>
                    </div>
                    <div>
                      <h3 className="text-sm font-medium text-gray-500">
                        Module Code
                      </h3>
                      <p className="mt-1 text-sm text-gray-900 font-mono">
                        {module.code}
                      </p>
                    </div>
                    <div>
                      <h3 className="text-sm font-medium text-gray-500">
                        Product
                      </h3>
                      <p className="mt-1 text-sm text-gray-900">
                        {module.productName}
                      </p>
                    </div>
                    <div>
                      <h3 className="text-sm font-medium text-gray-500">
                        Status
                      </h3>
                      <StatusBadge
                        status={normalizeEntityStatus(
                          "module",
                          module.moduleStatus
                        )}
                        className="mt-1"
                      />
                    </div>
                    <div>
                      <h3 className="text-sm font-medium text-gray-500">
                        Created At
                      </h3>
                      <p className="mt-1 text-sm text-gray-900">
                        {new Date(module.createdAt).toLocaleDateString()}
                      </p>
                    </div>
                    <div>
                      <h3 className="text-sm font-medium text-gray-500">
                        Last Updated
                      </h3>
                      <p className="mt-1 text-sm text-gray-900">
                        {new Date(module.updatedAt).toLocaleDateString()}
                      </p>
                    </div>
                  </div>

                  <Separator />

                  <div>
                    <h3 className="text-sm font-medium text-gray-500">
                      Description
                    </h3>
                    <p className="mt-1 text-sm text-gray-900">
                      {module.description}
                    </p>
                  </div>
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      </div>
    </PermissionGuard>
  );
};

export default ModuleDetail;
