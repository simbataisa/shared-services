import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { PermissionGuard } from "@/components/common/PermissionGuard";
import httpClient from "@/lib/httpClient";
import type { Module, Product } from "@/types/entities";
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
import { ArrowLeft, Save, X } from "lucide-react";

interface ModuleFormData {
  name: string;
  description: string;
  code: string;
  productId: string;
  isActive: boolean;
}

const ModuleEdit: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [module, setModule] = useState<Module | null>(null);
  const [formData, setFormData] = useState<ModuleFormData>({
    name: "",
    description: "",
    code: "",
    productId: "",
    isActive: true,
  });
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});

  const navigate = useNavigate();

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        const [moduleData, products] = await Promise.all([
          httpClient.getModuleById(Number(id)),
          httpClient.getProducts(),
        ]);

        setModule(moduleData);
        setFormData({
          name: moduleData.name,
          description: moduleData.description,
          code: moduleData.code,
          productId: moduleData.productId.toString(),
          isActive: moduleData.moduleStatus === 'ACTIVE',
        });
        setProducts(products);
      } catch (error) {
        console.error("Error fetching data:", error);
        setErrors({ fetch: "Failed to load module data" });
      } finally {
        setLoading(false);
      }
    };

    if (id) {
      fetchData();
    }
  }, [id]);

  const handleInputChange = (
    field: keyof ModuleFormData,
    value: string | boolean
  ) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
    if (errors[field]) {
      setErrors((prev) => ({ ...prev, [field]: "" }));
    }
  };

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!formData.name.trim()) {
      newErrors.name = "Module name is required";
    }

    if (!formData.description.trim()) {
      newErrors.description = "Description is required";
    }

    if (!formData.code.trim()) {
      newErrors.code = "Module code is required";
    }

    if (!formData.productId) {
      newErrors.productId = "Product selection is required";
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
      setSaving(true);
      setErrors({});

      await httpClient.updateModule(Number(id), {
        name: formData.name.trim(),
        description: formData.description.trim(),
        productId: parseInt(formData.productId),
        isActive: formData.isActive,
      });

      navigate(`/modules/${id}`);
    } catch (error: any) {
      console.error("Error updating module:", error);
      if (error.response?.data?.message) {
        setErrors({ submit: error.response.data.message });
      } else {
        setErrors({ submit: "Failed to update module. Please try again." });
      }
    } finally {
      setSaving(false);
    }
  };

  const handleCancel = () => {
    navigate(`/modules/${id}`);
  };

  return (
    <PermissionGuard permission="MODULE_MGMT:update">
      {loading ? (
        <div className="min-h-screen bg-gray-50 py-8">
          <div className="max-w-2xl mx-auto px-4 sm:px-6 lg:px-8">
            <Card>
              <CardHeader>
                <Skeleton className="h-8 w-48" />
                <Skeleton className="h-4 w-32" />
              </CardHeader>
              <CardContent className="space-y-6">
                {Array.from({ length: 5 }).map((_, i) => (
                  <div key={i}>
                    <Skeleton className="h-4 w-24 mb-2" />
                    <Skeleton className="h-10 w-full" />
                  </div>
                ))}
              </CardContent>
            </Card>
          </div>
        </div>
      ) : !module ? (
        <div className="min-h-screen flex items-center justify-center">
          <Card className="w-full max-w-md">
            <CardHeader className="text-center">
              <CardTitle className="text-2xl">Module Not Found</CardTitle>
              <CardDescription>
                The requested module could not be found.
              </CardDescription>
            </CardHeader>
            <CardContent className="text-center">
              <Button onClick={() => navigate("/modules")}>
                <ArrowLeft className="h-4 w-4 mr-2" />
                Back to Modules
              </Button>
            </CardContent>
          </Card>
        </div>
      ) : (
        <div className="min-h-screen bg-gray-50 py-8">
          <div className="max-w-2xl mx-auto px-4 sm:px-6 lg:px-8">
            <Card>
              <CardHeader>
                <div className="flex justify-between items-center">
                  <div>
                    <CardTitle className="text-2xl">Edit Module</CardTitle>
                    <CardDescription>
                      Update module details for "{module.name}"
                    </CardDescription>
                  </div>
                  <Button
                    variant="outline"
                    onClick={() => navigate(`/modules/${id}`)}
                  >
                    <ArrowLeft className="h-4 w-4 mr-2" />
                    Back to Details
                  </Button>
                </div>
              </CardHeader>

              <CardContent>
                {errors.submit && (
                  <Alert variant="destructive" className="mb-6">
                    <AlertDescription>{errors.submit}</AlertDescription>
                  </Alert>
                )}

                {errors.fetch && (
                  <Alert variant="destructive" className="mb-6">
                    <AlertDescription>{errors.fetch}</AlertDescription>
                  </Alert>
                )}

                <form onSubmit={handleSubmit} className="space-y-6">
                  <div>
                    <Label htmlFor="name">Module Name *</Label>
                    <Input
                      id="name"
                      value={formData.name}
                      onChange={(e) =>
                        handleInputChange("name", e.target.value)
                      }
                      className={errors.name ? "border-red-300" : ""}
                      placeholder="Enter module name"
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
                      placeholder="Enter module description"
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
                      placeholder="Enter module code (e.g., USER_MGMT)"
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
                    <Button
                      type="button"
                      variant="outline"
                      onClick={handleCancel}
                    >
                      <X className="h-4 w-4 mr-2" />
                      Cancel
                    </Button>
                    <Button type="submit" disabled={saving}>
                      <Save className="h-4 w-4 mr-2" />
                      {saving ? "Saving..." : "Save Changes"}
                    </Button>
                  </div>
                </form>
              </CardContent>
            </Card>
          </div>
        </div>
      )}
    </PermissionGuard>
  );
};

export default ModuleEdit;
