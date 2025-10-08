import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { usePermissions } from "@/hooks/usePermissions";
import { PermissionGuard } from "@/components/common/PermissionGuard";
import api from "@/lib/api";
import type { Product, Module, ProductWithModules } from "@/types";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Skeleton } from "@/components/ui/skeleton";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Plus, Edit, Trash2, Package, AlertCircle, Eye } from "lucide-react";
import SearchAndFilter from "@/components/common/SearchAndFilter";
import { StatusBadge } from "@/components/common/StatusBadge";
import { normalizeEntityStatus } from "@/lib/status-colors";

export default function ProductList() {
  const { canViewProducts } = usePermissions();
  const [products, setProducts] = useState<ProductWithModules[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState<string>("all");
  const [expandedProducts, setExpandedProducts] = useState<Set<string>>(
    new Set()
  );

  useEffect(() => {
    fetchProducts();
  }, []);

  const fetchProducts = async () => {
    try {
      setLoading(true);
      const response = await api.get("/products");
      // Map backend response to frontend format
      const mappedProducts = response.data.map((product: any) => ({
        ...product,
        status: product.isActive ? "active" : "inactive",
        modules: product.modules || [],
      }));
      setProducts(mappedProducts);
    } catch (err) {
      setError("Failed to fetch products");
      console.error("Error fetching products:", err);
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteProduct = async (productId: string) => {
    if (!confirm("Are you sure you want to delete this product?")) return;

    try {
      await api.delete(`/products/${productId}`);
      setProducts(
        products.filter((product) => product.id.toString() !== productId)
      );
    } catch (err) {
      setError("Failed to delete product");
      console.error("Error deleting product:", err);
    }
  };

  const filteredProducts = products.filter((product) => {
    const matchesSearch =
      product.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      product.code.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesStatus =
      statusFilter === "all" || product.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  if (!canViewProducts) {
    return (
      <div className="container mx-auto py-10">
        <Alert>
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>
            You don't have permission to view products.
          </AlertDescription>
        </Alert>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="container mx-auto py-10">
        <Card>
          <CardHeader>
            <Skeleton className="h-8 w-48" />
          </CardHeader>
          <CardContent className="space-y-4">
            <Skeleton className="h-4 w-full" />
            <Skeleton className="h-4 w-3/4" />
            <Skeleton className="h-4 w-1/2" />
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="container mx-auto py-10 space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Products</h1>
          <p className="text-muted-foreground">
            Manage your products and their modules
          </p>
        </div>
      </div>

      {error && (
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      {/* Search and Filters */}
      <SearchAndFilter
        searchTerm={searchTerm}
        onSearchChange={setSearchTerm}
        searchPlaceholder="Search products by name or code..."
        filters={[
          {
            label: "Status",
            value: statusFilter,
            onChange: setStatusFilter,
            options: [
              { value: "all", label: "All Status" },
              { value: "active", label: "Active" },
              { value: "inactive", label: "Inactive" },
              { value: "deprecated", label: "Deprecated" },
            ],
          },
        ]}
        actions={
          <PermissionGuard permission="product:create">
            <Button asChild>
              <Link to="/products/create">
                <Plus className="mr-2 h-4 w-4" />
                Create Product
              </Link>
            </Button>
          </PermissionGuard>
        }
      />

      {/* Products Table */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Package className="h-5 w-5" />
            Products ({filteredProducts.length})
          </CardTitle>
        </CardHeader>
        <CardContent>
          {filteredProducts.length === 0 ? (
            <div className="text-center py-8 text-muted-foreground">
              <Package className="h-12 w-12 mx-auto mb-4 opacity-50" />
              <p>No products found.</p>
            </div>
          ) : (
            <div className="space-y-4">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Name</TableHead>
                    <TableHead>Code</TableHead>
                    <TableHead>Version</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead>Description</TableHead>
                    <TableHead>Modules</TableHead>
                    <TableHead className="text-right">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {filteredProducts.map((product) => (
                    <React.Fragment key={product.id}>
                      <TableRow className="hover:bg-muted/50">
                        <TableCell className="font-medium">
                          {product.name}
                        </TableCell>
                        <TableCell className="font-mono text-sm">
                          {product.code}
                        </TableCell>
                        <TableCell>{product.version}</TableCell>
                        <TableCell>
                          <StatusBadge
                            status={normalizeEntityStatus(
                              "product",
                              product.status.toUpperCase()
                            )}
                          />
                        </TableCell>
                        <TableCell className="max-w-xs truncate">
                          {product.description || "-"}
                        </TableCell>
                        <TableCell>
                          <Badge variant="outline">
                            {product.modules?.length || 0}
                          </Badge>
                        </TableCell>
                        <TableCell className="text-right">
                          <Button
                            variant="ghost"
                            size="icon"
                            asChild
                            className="text-blue-600 hover:text-blue-700"
                          >
                            <Link
                              to={`/products/${product.id}`}
                              title="View Details"
                            >
                              <Eye className="h-4 w-4" />
                            </Link>
                          </Button>
                          <PermissionGuard permission="product:update">
                            <Button
                              variant="ghost"
                              size="icon"
                              asChild
                              className="text-yellow-600 hover:text-yellow-700"
                            >
                              <Link
                                to={`/products/${product.id}/edit`}
                                className="flex items-center text-yellow-600 hover:text-yellow-700"
                              >
                                <Edit className="mr-2 h-4 w-4" />
                              </Link>
                            </Button>
                          </PermissionGuard>
                          <PermissionGuard permission="product:delete">
                            <Button
                              variant="ghost"
                              size="icon"
                              asChild
                              className="text-red-600 hover:text-red-700"
                              onClick={() =>
                                handleDeleteProduct(product.id.toString())
                              }
                            >
                              <Link
                                to={`#`}
                                className="flex items-center text-red-600 hover:text-red-700"
                              >
                                <Trash2 className="mr-2 h-4 w-4" />
                              </Link>
                            </Button>
                          </PermissionGuard>
                        </TableCell>
                      </TableRow>
                    </React.Fragment>
                  ))}
                </TableBody>
              </Table>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
