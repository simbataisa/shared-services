import React, { useState, useEffect } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import { Edit, Trash2, Save, X } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { PermissionGuard } from "@/components/common/PermissionGuard";
import { DetailHeaderCard, StatusDisplayCard } from "@/components/common";
import { usePermissions } from "@/hooks/usePermissions";
import { getStatusColor, getStatusIcon } from "@/lib/status-utils";
import {
  type Product,
  type Module,
  type ProductStatus,
  ENTITY_STATUS_MAPPINGS,
} from "@/types/entities";
import httpClient from "@/lib/httpClient";
import LoadingSpinner from "../common/LoadingSpinner";
import ProductDetailsCard from "./ProductDetailsCard";
import ProductStatusCard from "./ProductStatusCard";
import ProductList from "@/pages/ProductList";

interface ProductStats {
  totalModules: number;
  activeModules: number;
  inactiveModules: number;
  lastUpdated: string;
}

const ProductDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const {
    canViewProducts,
    canUpdateProducts,
    canDeleteProducts,
    canViewModules,
    canCreateModules,
  } = usePermissions();

  const [product, setProduct] = useState<Product | null>(null);
  const [isEditing, setIsEditing] = useState(false);
  const [editForm, setEditForm] = useState({
    name: "",
    description: "",
    code: "",
    status: "ACTIVE" as ProductStatus,
    category: "",
    version: "",
  });
  const [modules, setModules] = useState<Module[]>([]);
  const [stats, setStats] = useState<ProductStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [updating, setUpdating] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Add useEffect to monitor product state changes
  useEffect(() => {
    console.log("Product state changed:", product);
  }, [product]);

  useEffect(() => {
    if (!canViewProducts) {
      navigate("/products");
      return;
    }

    if (id) {
      fetchProductData();
    }
  }, [id, canViewProducts, navigate]);

  const fetchProductData = async () => {
    try {
      setLoading(true);
      setError(null);

      const [productResponse, modulesResponse] = await Promise.all([
        httpClient.getProductById(Number(id)),
        httpClient.getModulesByProductId(Number(id)),
      ]);

      const productData = productResponse;
      const modulesData = modulesResponse;
      console.log("modulesData", modulesData);
      console.log("productData", productData);
      setProduct(productData);
      console.log("product set to:", productData); // Log the data being set, not the state

      const normalizedModules = modulesData.map((module: any) => ({
        ...module,
        // Convert isActive boolean to status string for compatibility
        status: module.isActive ? "active" : "inactive",
      }));
      setModules(normalizedModules);

      // Calculate stats
      const activeModules = normalizedModules.filter(
        (m: any) => m.status === "active"
      ).length;
      const inactiveModules = normalizedModules.length - activeModules;

      setStats({
        totalModules: normalizedModules.length,
        activeModules,
        inactiveModules,
        lastUpdated: productData?.updatedAt || new Date().toISOString(),
      });
    } catch (err: any) {
      console.error("Error fetching product data:", err);
      setError(
        err.response?.data?.message ||
          "Failed to load product data. Please try again."
      );
    } finally {
      setLoading(false);
    }
  };

  const handleStartEdit = () => {
    if (product) {
      setEditForm({
        name: product.name,
        description: product.description || "",
        code: product.code,
        status: product.productStatus,
        category: "", // Remove category as it's no longer in Product interface
        version: product.version,
      });
      setIsEditing(true);
    }
  };

  const handleCancelEdit = () => {
    setIsEditing(false);
    setEditForm({
      name: "",
      description: "",
      code: "",
      status: "ACTIVE",
      category: "",
      version: "",
    });
  };

  const handleSaveEdit = async () => {
    if (!product) return;

    try {
      setUpdating(true);

      const updateData = {
        name: editForm.name,
        description: editForm.description,
        productCode: editForm.code,
        productStatus: editForm.status,
        category: editForm.category,
        version: editForm.version,
      };

      // Optimistic local update
      setProduct((prev) =>
        prev
          ? {
              ...prev,
              name: updateData.name || prev.name,
              description: updateData.description || prev.description,
              code: updateData.productCode,
              productStatus: updateData.productStatus,
              version: updateData.version || prev.version,
              updatedAt: new Date().toISOString(),
            }
          : prev
      );

      // API call
      await httpClient.updateProduct(product.id, updateData);

      setIsEditing(false);
      await fetchProductData(); // Refresh to get server state
    } catch (err: any) {
      console.error("Error updating product:", err);
      // Revert optimistic update on error
      await fetchProductData();
    } finally {
      setUpdating(false);
    }
  };

  const handleFormChange = (
    e: React.ChangeEvent<
      HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement
    >
  ) => {
    const { name, value } = e.target;
    setEditForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleStatusUpdate = async (newStatus: ProductStatus) => {
    if (!product) return;

    try {
      setUpdating(true);
      const updateData: any = {
        productStatus: newStatus,
      };

      await httpClient.updateProduct(product.id, updateData);

      setProduct((prev) =>
        prev ? { ...prev, productStatus: newStatus } : prev
      );
    } catch (err: any) {
      console.error("Error updating product status:", err);
    } finally {
      setUpdating(false);
    }
  };

  const handleDeleteProduct = async () => {
    if (!product) return;

    const confirmed = window.confirm(
      `Are you sure you want to delete "${product.name}"? This action cannot be undone.`
    );

    if (!confirmed) return;

    try {
      setUpdating(true);
      await httpClient.deleteProduct(product.id);
      navigate("/products");
    } catch (err: any) {
      console.error("Error deleting product:", err);
      alert(
        err.response?.data?.message ||
          "Failed to delete product. Please try again."
      );
    } finally {
      setUpdating(false);
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  if (!canViewProducts) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <h1 className="text-2xl font-bold text-gray-900 mb-4">
            Access Denied
          </h1>
          <p className="text-gray-600 mb-6">
            You don't have permission to view products.
          </p>
          <Button onClick={() => navigate("/")}>Go to Dashboard</Button>
        </div>
      </div>
    );
  }

  if (loading) {
    return <LoadingSpinner />;
  }

  if (error || !product) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <h1 className="text-2xl font-bold text-gray-900 mb-4">
            Product Not Found
          </h1>
          <p className="text-gray-600 mb-6">
            {error || "The requested product could not be found."}
          </p>
          <div className="space-x-4">
            <Button onClick={() => navigate("/products")}>
              Back to Products
            </Button>
            <Button variant="outline" onClick={() => window.location.reload()}>
              Retry
            </Button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Header */}
        <DetailHeaderCard
          title={product.name}
          description={product.description}
          breadcrumbs={[
            { label: "Products", href: "/products" },
            { label: product.name },
          ]}
          actions={
            <div className="flex items-center space-x-3">
              <PermissionGuard permission="PRODUCT_MGMT:delete">
                <Button
                  onClick={handleDeleteProduct}
                  disabled={updating}
                  variant="destructive"
                  size="sm"
                >
                  <Trash2 className="mr-2 h-4 w-4" />
                  Delete
                </Button>
              </PermissionGuard>
            </div>
          }
        />

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Main Content */}
          <div className="lg:col-span-2 space-y-8">
            {/* Product Information */}
            <div className="bg-white shadow rounded-lg p-6">
              <div className="flex items-center justify-between mb-4">
                <h2 className="text-xl font-semibold text-gray-900">
                  Product Information
                </h2>
                <PermissionGuard permission="PRODUCT_MGMT:update">
                  {isEditing ? (
                    <div className="flex gap-2">
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={handleSaveEdit}
                        disabled={updating}
                      >
                        <Save className="h-4 w-4 mr-2" />
                        Save
                      </Button>
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={handleCancelEdit}
                        disabled={updating}
                      >
                        <X className="h-4 w-4 mr-2" />
                        Cancel
                      </Button>
                    </div>
                  ) : (
                    <Button size="sm" onClick={handleStartEdit}>
                      <Edit className="mr-2 h-4 w-4" />
                      Edit
                    </Button>
                  )}
                </PermissionGuard>
              </div>

              {isEditing ? (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div className="space-y-2">
                    <label className="block text-sm font-medium text-gray-700">
                      Product Name
                    </label>
                    <Input
                      name="name"
                      value={editForm.name}
                      onChange={handleFormChange}
                      placeholder="Enter product name"
                      disabled={updating}
                    />
                  </div>

                  <div className="space-y-2">
                    <label className="block text-sm font-medium text-gray-700">
                      Product Code
                    </label>
                    <Input
                      name="code"
                      value={editForm.code}
                      onChange={handleFormChange}
                      placeholder="Enter product code"
                      disabled={updating}
                    />
                  </div>

                  <div className="space-y-2 md:col-span-2">
                    <label className="block text-sm font-medium text-gray-700">
                      Description
                    </label>
                    <Textarea
                      name="description"
                      value={editForm.description}
                      onChange={handleFormChange}
                      placeholder="Enter description"
                      rows={3}
                      disabled={updating}
                    />
                  </div>

                  <div className="space-y-2">
                    <label className="block text-sm font-medium text-gray-700">
                      Category
                    </label>
                    <Input
                      name="category"
                      value={editForm.category}
                      onChange={handleFormChange}
                      placeholder="Enter category"
                      disabled={updating}
                    />
                  </div>

                  <div className="space-y-2">
                    <label className="block text-sm font-medium text-gray-700">
                      Version
                    </label>
                    <Input
                      name="version"
                      value={editForm.version}
                      onChange={handleFormChange}
                      placeholder="e.g., 1.0.0"
                      disabled={updating}
                    />
                  </div>

                  <div className="space-y-2">
                    <label className="block text-sm font-medium text-gray-700">
                      Status
                    </label>
                    <div className="flex gap-2">
                      <Button
                        type="button"
                        variant={
                          editForm.status === "DRAFT" ? "default" : "outline"
                        }
                        size="sm"
                        onClick={() =>
                          setEditForm((prev) => ({ ...prev, status: "DRAFT" }))
                        }
                        disabled={updating}
                      >
                        Draft
                      </Button>
                      <Button
                        type="button"
                        variant={
                          editForm.status === "ACTIVE" ? "default" : "outline"
                        }
                        size="sm"
                        onClick={() =>
                          setEditForm((prev) => ({ ...prev, status: "ACTIVE" }))
                        }
                        disabled={updating}
                      >
                        Active
                      </Button>
                      <Button
                        type="button"
                        variant={
                          editForm.status === "INACTIVE"
                            ? "destructive"
                            : "outline"
                        }
                        size="sm"
                        onClick={() =>
                          setEditForm((prev) => ({
                            ...prev,
                            status: "INACTIVE",
                          }))
                        }
                        disabled={updating}
                      >
                        Inactive
                      </Button>
                    </div>
                  </div>
                </div>
              ) : (
                <ProductDetailsCard product={product} />
              )}
            </div>

            {/* Modules Section */}
            <div className="bg-white shadow rounded-lg p-6">
              <div className="flex items-center justify-between mb-4">
                <h2 className="text-xl font-semibold text-gray-900">Modules</h2>

                <PermissionGuard permission="MODULE_MGMT:create">
                  <Link
                    to={`/products/${product.id}/modules/create`}
                    className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 text-sm"
                  >
                    Add Module
                  </Link>
                </PermissionGuard>
              </div>

              {canViewModules ? (
                <div className="space-y-4">
                  {modules.length > 0 ? (
                    modules.map((module) => (
                      <div
                        key={module.id}
                        className="border border-gray-200 rounded-lg p-4"
                      >
                        <div className="flex items-center justify-between">
                          <div className="flex-1">
                            <div className="flex items-center space-x-3">
                              <h3 className="text-lg font-medium text-gray-900">
                                {module.name}
                              </h3>
                              {getStatusIcon(
                                module.isActive ? "active" : "inactive"
                              )}
                              <span className="text-sm text-gray-500 font-mono">
                                v{product.version}
                              </span>
                            </div>
                            <p className="mt-1 text-sm text-gray-600">
                              {module.description}
                            </p>
                            <p className="mt-2 text-xs text-gray-500">
                              Code:{" "}
                              <span className="font-mono">{product.code}</span>{" "}
                              • Updated: {formatDate(module.updatedAt)}
                            </p>
                          </div>

                          <div className="flex items-center space-x-2">
                            <Link
                              to={`/modules/${module.id}`}
                              className="px-3 py-1 text-sm bg-gray-100 text-gray-700 rounded-md hover:bg-gray-200"
                            >
                              View
                            </Link>
                          </div>
                        </div>
                      </div>
                    ))
                  ) : (
                    <div className="text-center py-8">
                      <p className="text-gray-500">
                        No modules found for this product.
                      </p>
                      {canCreateModules && (
                        <Link
                          to={`/products/${product.id}/modules/create`}
                          className="mt-2 inline-block px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 text-sm"
                        >
                          Create First Module
                        </Link>
                      )}
                    </div>
                  )}
                </div>
              ) : (
                <div className="text-center py-8">
                  <p className="text-gray-500">
                    You don't have permission to view modules.
                  </p>
                </div>
              )}
            </div>
          </div>

          {/* Sidebar */}
          <div className="space-y-6">
            {/* Status Card */}
            <ProductStatusCard
              product={product}
              onStatusUpdate={handleStatusUpdate}
              onDelete={handleDeleteProduct}
              updating={updating}
            />
            {/* Statistics */}
            {stats && (
              <div className="bg-white shadow rounded-lg p-6">
                <h3 className="text-lg font-medium text-gray-900 mb-4">
                  Statistics
                </h3>

                <div className="space-y-4">
                  <div className="flex justify-between">
                    <span className="text-sm text-gray-600">Total Modules</span>
                    <span className="text-sm font-medium text-gray-900">
                      {stats.totalModules}
                    </span>
                  </div>

                  <div className="flex justify-between">
                    <span className="text-sm text-gray-600">
                      Active Modules
                    </span>
                    <span className="text-sm font-medium text-green-600">
                      {stats.activeModules}
                    </span>
                  </div>

                  <div className="flex justify-between">
                    <span className="text-sm text-gray-600">
                      Inactive Modules
                    </span>
                    <span className="text-sm font-medium text-red-600">
                      {stats.inactiveModules}
                    </span>
                  </div>

                  <div className="pt-4 border-t border-gray-200">
                    <span className="text-sm text-gray-600">Last Updated</span>
                    <p className="text-sm font-medium text-gray-900">
                      {formatDate(stats.lastUpdated)}
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
                <PermissionGuard permission="MODULE_MGMT:read">
                  <Link
                    to={`/products/${product.id}/modules`}
                    className="block w-full px-4 py-2 text-sm bg-gray-100 text-gray-700 rounded-md hover:bg-gray-200 text-center"
                  >
                    View All Modules
                  </Link>
                </PermissionGuard>

                <PermissionGuard permission="MODULE_MGMT:create">
                  <Link
                    to={`/products/${product.id}/modules/create`}
                    className="block w-full px-4 py-2 text-sm bg-blue-600 text-white rounded-md hover:bg-blue-700 text-center"
                  >
                    Add New Module
                  </Link>
                </PermissionGuard>

                <PermissionGuard permission="PRODUCT_MGMT:update">
                  <Link
                    to={`/products/${product.id}/edit`}
                    className="block w-full px-4 py-2 text-sm bg-green-600 text-white rounded-md hover:bg-green-700 text-center"
                  >
                    Edit Product
                  </Link>
                </PermissionGuard>

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
  );
};

export default ProductDetail;
