import React, { useState, useEffect } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import { ArrowLeft, Edit, Trash2 } from "lucide-react";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { PermissionGuard } from "@/components/common/PermissionGuard";
import { StatusBadge } from "@/components/common/StatusBadge";
import { DetailHeaderCard } from "@/components/common";
import { usePermissions } from "@/hooks/usePermissions";
import { normalizeEntityStatus } from "@/lib/status-utils";
import type { Product, Module } from "@/store/auth";
import api from "@/lib/api";

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
  const [modules, setModules] = useState<Module[]>([]);
  const [stats, setStats] = useState<ProductStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [updating, setUpdating] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!canViewProducts) {
      navigate("/unauthorized");
      return;
    }

    fetchProductData();
  }, [id, canViewProducts, navigate]);

  const fetchProductData = async () => {
    try {
      setLoading(true);

      // Fetch product data from API
      const [productResponse, modulesResponse] = await Promise.all([
        api.get(`/products/${id}`),
        api.get(`/v1/modules/product/${id}`),
      ]);

      const productData = productResponse.data;
      const modulesData = modulesResponse.data || [];

      // Calculate stats
      const stats: ProductStats = {
        totalModules: modulesData.length,
        activeModules: modulesData.filter((m: any) => m.isActive).length,
        inactiveModules: modulesData.filter((m: any) => !m.isActive).length,
        lastUpdated: productData.updatedAt,
      };

      // Transform module data to match frontend interface
      const transformedModules: Module[] = modulesData.map((module: any) => ({
        id: module.id,
        name: module.name,
        description: module.description,
        code: module.code || `MODULE_${module.id}`,
        status: module.isActive ? "active" : "inactive",
        productId: module.productId,
        version: "1.0.0", // Default version if not provided
        createdAt: module.createdAt,
        updatedAt: module.updatedAt,
      }));

      // Transform product data to match frontend interface
      const transformedProduct: Product = {
        id: productData.id,
        name: productData.name,
        description: productData.description,
        code: productData.productCode,
        status: productData.productStatus?.toLowerCase() || "active",
        category: productData.category || "general",
        version: productData.version || "1.0.0",
        createdAt: productData.createdAt,
        updatedAt: productData.updatedAt,
        createdBy: productData.createdBy || "system",
        updatedBy: productData.updatedBy || "system",
      };

      setProduct(transformedProduct);
      setModules(transformedModules);
      setStats(stats);
    } catch (error) {
      console.error("Error fetching product data:", error);
      setError("Failed to load product data");
    } finally {
      setLoading(false);
    }
  };

  const handleStatusUpdate = async (newStatus: "active" | "inactive") => {
    if (!product || !canUpdateProducts) return;

    try {
      setUpdating(true);

      // Simulate API call
      await new Promise((resolve) => setTimeout(resolve, 500));

      setProduct((prev) =>
        prev
          ? { ...prev, status: newStatus, updatedAt: new Date().toISOString() }
          : null
      );
    } catch (error) {
      console.error("Error updating product status:", error);
      setError("Failed to update product status");
    } finally {
      setUpdating(false);
    }
  };

  const handleDeleteProduct = async () => {
    if (!product || !canDeleteProducts) return;

    if (
      !window.confirm(
        `Are you sure you want to delete the product "${product.name}"? This action cannot be undone.`
      )
    ) {
      return;
    }

    try {
      setUpdating(true);

      // Simulate API call
      await new Promise((resolve) => setTimeout(resolve, 1000));

      navigate("/products");
    } catch (error) {
      console.error("Error deleting product:", error);
      setError("Failed to delete product");
      setUpdating(false);
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("en-US", {
      year: "numeric",
      month: "long",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  const getStatusBadge = (status: string) => {
    const normalizedStatus = normalizeEntityStatus(
      "product",
      status.toUpperCase()
    );
    return <StatusBadge status={normalizedStatus} />;
  };

  if (!canViewProducts) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <Card className="w-full max-w-md">
          <CardHeader>
            <CardTitle>Access Denied</CardTitle>
            <CardDescription>
              You don't have permission to view products.
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
    );
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 py-8">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="space-y-6">
            <Skeleton className="h-8 w-64" />
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              <Skeleton className="h-32" />
              <Skeleton className="h-32" />
              <Skeleton className="h-32" />
            </div>
            <Skeleton className="h-64" />
          </div>
        </div>
      </div>
    );
  }

  if (error || !product) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <Card className="w-full max-w-md">
          <CardHeader>
            <CardTitle>Product Not Found</CardTitle>
            <CardDescription>
              {error || "The requested product could not be found."}
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
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Header */}
        <DetailHeaderCard
          title={product.name}
          description={product.description}
          breadcrumbs={[
            { label: "Products", href: "/products" },
            { label: product.name }
          ]}
          actions={
            <div className="flex items-center space-x-3">
              {getStatusBadge(product.status)}

              <PermissionGuard permission="product:update">
                <div className="flex space-x-2">
                  {product.status === "active" ? (
                    <Button
                      onClick={() => handleStatusUpdate("inactive")}
                      disabled={updating}
                      variant="destructive"
                      size="sm"
                    >
                      Deactivate
                    </Button>
                  ) : (
                    <Button
                      onClick={() => handleStatusUpdate("active")}
                      disabled={updating}
                      variant="default"
                      size="sm"
                      className="bg-green-600 hover:bg-green-700"
                    >
                      Activate
                    </Button>
                  )}

                  <Button asChild size="sm">
                    <Link to={`/products/${product.id}/edit`}>
                      <Edit className="mr-2 h-4 w-4" />
                      Edit
                    </Link>
                  </Button>
                </div>
              </PermissionGuard>

              <PermissionGuard permission="product:delete">
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
              <h2 className="text-xl font-semibold text-gray-900 mb-4">
                Product Information
              </h2>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label className="block text-sm font-medium text-gray-700">
                    Product Code
                  </label>
                  <p className="mt-1 text-sm text-gray-900 font-mono bg-gray-50 px-2 py-1 rounded">
                    {product.code}
                  </p>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700">
                    Category
                  </label>
                  <p className="mt-1 text-sm text-gray-900 capitalize">
                    {product.category}
                  </p>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700">
                    Version
                  </label>
                  <p className="mt-1 text-sm text-gray-900 font-mono">
                    {product.version}
                  </p>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700">
                    Status
                  </label>
                  {getStatusBadge(product.status)}
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700">
                    Created
                  </label>
                  <p className="mt-1 text-sm text-gray-900">
                    {formatDate(product.createdAt)}
                  </p>
                  <p className="text-xs text-gray-500">
                    by {product.createdBy}
                  </p>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700">
                    Last Updated
                  </label>
                  <p className="mt-1 text-sm text-gray-900">
                    {formatDate(product.updatedAt)}
                  </p>
                  <p className="text-xs text-gray-500">
                    by {product.updatedBy}
                  </p>
                </div>
              </div>
            </div>

            {/* Modules Section */}
            <div className="bg-white shadow rounded-lg p-6">
              <div className="flex items-center justify-between mb-4">
                <h2 className="text-xl font-semibold text-gray-900">Modules</h2>

                <PermissionGuard permission="module:create">
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
                              {getStatusBadge(module.status)}
                              <span className="text-sm text-gray-500 font-mono">
                                v{module.version}
                              </span>
                            </div>
                            <p className="mt-1 text-sm text-gray-600">
                              {module.description}
                            </p>
                            <p className="mt-2 text-xs text-gray-500">
                              Code:{" "}
                              <span className="font-mono">{module.code}</span> •
                              Updated: {formatDate(module.updatedAt)}
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
            {/* Statistics */}
            {stats && (
              <div className="bg-white shadow rounded-lg p-6">
                <h3 className="text-lg font-semibold text-gray-900 mb-4">
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
                <PermissionGuard permission="module:read">
                  <Link
                    to={`/products/${product.id}/modules`}
                    className="block w-full px-4 py-2 text-sm bg-gray-100 text-gray-700 rounded-md hover:bg-gray-200 text-center"
                  >
                    View All Modules
                  </Link>
                </PermissionGuard>

                <PermissionGuard permission="module:create">
                  <Link
                    to={`/products/${product.id}/modules/create`}
                    className="block w-full px-4 py-2 text-sm bg-blue-600 text-white rounded-md hover:bg-blue-700 text-center"
                  >
                    Add New Module
                  </Link>
                </PermissionGuard>

                <PermissionGuard permission="product:update">
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
