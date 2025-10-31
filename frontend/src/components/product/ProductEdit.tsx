import React, { useState, useEffect } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import { ArrowLeft, Save, AlertCircle } from "lucide-react";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
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
import { PermissionGuard } from "@/components/common/PermissionGuard";
import { DetailHeaderCard } from "@/components/common";
import { ENTITY_STATUS_MAPPINGS, type Product } from "@/types/entities";
import httpClient from "@/lib/httpClient";

const ProductEdit: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [product, setProduct] = useState<Product | null>(null);
  const [formData, setFormData] = useState({
    name: "",
    description: "",
    code: "",
    productStatus: ENTITY_STATUS_MAPPINGS.product.ACTIVE,
    version: "",
  });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    fetchProduct();
  }, [id, navigate]);

  const validateForm = () => {
    const errors: Record<string, string> = {};

    if (!formData.name.trim()) {
      errors.name = "Product name is required";
    } else if (formData.name.trim().length < 2) {
      errors.name = "Product name must be at least 2 characters";
    }

    if (!formData.code.trim()) {
      errors.code = "Product code is required";
    } else if (!/^[A-Z0-9_-]+$/i.test(formData.code.trim())) {
      errors.code =
        "Product code can only contain letters, numbers, hyphens, and underscores";
    }

    if (formData.version && !/^\d+\.\d+\.\d+$/.test(formData.version.trim())) {
      errors.version = "Version must be in format X.Y.Z (e.g., 1.0.0)";
    }

    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const fetchProduct = async () => {
    try {
      setLoading(true);
      const productData = await httpClient.getProductById(Number(id));

      const transformedProduct: Product = {
        id: productData.id,
        name: productData.name,
        description: productData.description,
        code: productData.code,
        productStatus: productData.productStatus,
        version: productData.version || "1.0.0",
        createdAt: productData.createdAt,
        updatedAt: productData.updatedAt,
        createdBy: productData.createdBy || "system",
        updatedBy: productData.updatedBy || "system",
      };

      setProduct(transformedProduct);
      setFormData({
        name: transformedProduct.name,
        description: transformedProduct.description,
        code: transformedProduct.code,
        productStatus: transformedProduct.productStatus,
        version: transformedProduct.version,
      });
    } catch (error) {
      console.error("Error fetching product:", error);
      setError("Failed to load product data");
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (
    e: React.ChangeEvent<
      HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement
    >
  ) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));

    // Clear field error when user starts typing
    if (fieldErrors[name]) {
      setFieldErrors((prev) => ({
        ...prev,
        [name]: "",
      }));
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateForm()) {
      setError("Please fix the errors below");
      return;
    }

    try {
      setSaving(true);
      setError(null);
      setFieldErrors({});

      const updateData = {
        name: formData.name.trim(),
        description: formData.description.trim(),
        productCode: formData.code.trim(),
        productStatus: formData.productStatus,
        version: formData.version.trim(),
      };

      await httpClient.updateProduct(Number(id), updateData);
      navigate(`/products/${id}`);
    } catch (error: any) {
      console.error("Error updating product:", error);
      if (error.response?.status === 409) {
        setError("A product with this code already exists");
      } else if (error.response?.status === 400) {
        setError("Invalid product data. Please check your inputs.");
      } else {
        setError("Failed to update product. Please try again.");
      }
    } finally {
      setSaving(false);
    }
  };

  const handleCancel = () => {
    navigate(`/products/${id}`);
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 py-8">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="space-y-6">
            <Skeleton className="h-8 w-64" />
            <div className="bg-white shadow rounded-lg p-6">
              <div className="space-y-6">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <Skeleton className="h-20" />
                  <Skeleton className="h-20" />
                  <Skeleton className="h-20" />
                  <Skeleton className="h-20" />
                </div>
                <Skeleton className="h-32" />
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  if (error && !product) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <Card className="w-full max-w-md">
          <CardHeader>
            <CardTitle>Error</CardTitle>
            <CardDescription className="text-destructive">
              {error}
            </CardDescription>
          </CardHeader>
          <CardContent>
            <Button onClick={() => navigate("/products")} className="w-full">
              <ArrowLeft className="mr-2 h-4 w-4" />
              Back to Products
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <PermissionGuard
      permission="PRODUCT_MGMT:update"
      fallback={
        <div className="min-h-screen flex items-center justify-center">
          <Card className="w-full max-w-md">
            <CardHeader>
              <CardTitle>Access Denied</CardTitle>
              <CardDescription>
                You don't have permission to edit products.
              </CardDescription>
            </CardHeader>
            <CardContent>
              <Button onClick={() => navigate("/dashboard")} className="w-full">
                <ArrowLeft className="mr-2 h-4 w-4" />
                Back to Dashboard
              </Button>
            </CardContent>
          </Card>
        </div>
      }
    >
      <div className="min-h-screen bg-gray-50 py-8">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <DetailHeaderCard
            title="Edit Product"
            description="Update product information and settings"
            breadcrumbs={[
              { label: "Products", href: "/products" },
              { label: product?.name || "Product", href: `/products/${id}` },
              { label: "Edit" },
            ]}
          />

          {error && (
            <Alert variant="destructive" className="mb-6">
              <AlertCircle className="h-4 w-4" />
              <AlertDescription>{error}</AlertDescription>
            </Alert>
          )}

          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            {/* Main Content */}
            <div className="lg:col-span-2 space-y-8">
              {/* Product Information */}
              <div className="bg-white shadow rounded-lg p-6">
                <h2 className="text-xl font-semibold text-gray-900 mb-4">
                  Product Information
                </h2>

                <form onSubmit={handleSubmit} className="space-y-6">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div>
                      <label className="block text-sm font-medium text-gray-700">
                        Product Name *
                      </label>
                      <Input
                        name="name"
                        value={formData.name}
                        onChange={handleInputChange}
                        placeholder="Enter product name"
                        required
                        className={`mt-1 ${
                          fieldErrors.name ? "border-red-500" : ""
                        }`}
                      />
                      {fieldErrors.name && (
                        <p className="mt-1 text-sm text-red-500 flex items-center gap-1">
                          <AlertCircle className="h-3 w-3" />
                          {fieldErrors.name}
                        </p>
                      )}
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700">
                        Product Code *
                      </label>
                      <Input
                        name="code"
                        value={formData.code}
                        onChange={handleInputChange}
                        placeholder="Enter product code"
                        required
                        className={`mt-1 font-mono bg-gray-50 ${
                          fieldErrors.code ? "border-red-500" : ""
                        }`}
                      />
                      {fieldErrors.code && (
                        <p className="mt-1 text-sm text-red-500 flex items-center gap-1">
                          <AlertCircle className="h-3 w-3" />
                          {fieldErrors.code}
                        </p>
                      )}
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700">
                        Version
                      </label>
                      <Input
                        type="text"
                        name="version"
                        value={formData.version}
                        onChange={handleInputChange}
                        placeholder="Enter version (e.g., 1.0.0)"
                        className={`mt-1 font-mono ${
                          fieldErrors.version ? "border-red-500" : ""
                        }`}
                      />
                      {fieldErrors.version && (
                        <p className="mt-1 text-sm text-red-500 flex items-center gap-1">
                          <AlertCircle className="h-3 w-3" />
                          {fieldErrors.version}
                        </p>
                      )}
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700">
                        Status
                      </label>
                      <Select
                        value={formData.productStatus}
                        onValueChange={(value) =>
                          setFormData((prev) => ({
                            ...prev,
                            productStatus: value as
                              | typeof ENTITY_STATUS_MAPPINGS.product.ACTIVE
                              | typeof ENTITY_STATUS_MAPPINGS.product.INACTIVE,
                          }))
                        }
                      >
                        <SelectTrigger className="mt-1">
                          <SelectValue placeholder="Select status" />
                        </SelectTrigger>
                        <SelectContent>
                          <SelectItem value="active">Active</SelectItem>
                          <SelectItem value="inactive">Inactive</SelectItem>
                        </SelectContent>
                      </Select>
                    </div>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700">
                      Description
                    </label>
                    <Textarea
                      name="description"
                      value={formData.description}
                      onChange={handleInputChange}
                      rows={4}
                      placeholder="Enter product description"
                      className="mt-1"
                    />
                  </div>

                  <div className="flex justify-end space-x-4 pt-6 border-t border-gray-200">
                    <Button
                      type="button"
                      variant="outline"
                      onClick={handleCancel}
                      disabled={saving}
                    >
                      Cancel
                    </Button>
                    <Button type="submit" disabled={saving}>
                      <Save className="w-4 h-4 mr-2" />
                      {saving ? "Saving..." : "Save Changes"}
                    </Button>
                  </div>
                </form>
              </div>
            </div>

            {/* Sidebar */}
            <div className="space-y-6">
              {/* Metadata */}
              {product && (
                <div className="bg-white shadow rounded-lg p-6">
                  <h3 className="text-lg font-semibold text-gray-900 mb-4">
                    Metadata
                  </h3>

                  <div className="space-y-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-700">
                        Created At
                      </label>
                      <p className="mt-1 text-sm text-gray-900">
                        {new Date(product.createdAt).toLocaleDateString(
                          "en-US",
                          {
                            year: "numeric",
                            month: "long",
                            day: "numeric",
                            hour: "2-digit",
                            minute: "2-digit",
                          }
                        )}
                      </p>
                      <p className="text-xs text-gray-500">
                        by {product.createdBy || "N/A"}
                      </p>
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700">
                        Last Updated
                      </label>
                      <p className="mt-1 text-sm text-gray-900">
                        {new Date(product.updatedAt).toLocaleDateString(
                          "en-US",
                          {
                            year: "numeric",
                            month: "long",
                            day: "numeric",
                            hour: "2-digit",
                            minute: "2-digit",
                          }
                        )}
                      </p>
                      <p className="text-xs text-gray-500">
                        by {product.updatedBy || "N/A"}
                      </p>
                    </div>
                  </div>
                </div>
              )}

              {/* Quick Actions */}
              <div className="bg-white shadow rounded-lg p-6">
                <h3 className="text-lg font-semibold text-gray-900 mb-4">
                  Quick Actions
                </h3>

                <div className="space-y-3">
                  <Link
                    to={`/products/${id}`}
                    className="block w-full px-4 py-2 text-sm bg-gray-100 text-gray-700 rounded-md hover:bg-gray-200 text-center"
                  >
                    View Product Details
                  </Link>

                  <Link
                    to="/products"
                    className="block w-full px-4 py-2 text-sm border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50 text-center"
                  >
                    Back to Products
                  </Link>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </PermissionGuard>
  );
};

export default ProductEdit;
