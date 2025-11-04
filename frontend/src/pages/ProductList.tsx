import React, { useState, useEffect, useMemo } from "react";
import { Link } from "react-router-dom";
import { Plus } from "lucide-react";
import { ProductTable } from "@/components/product/ProductTable";
import { PermissionGuard } from "@/components/common/PermissionGuard";
import { Button } from "@/components/ui/button";
import { Alert, AlertDescription } from "@/components/ui/alert";
import httpClient from "@/lib/httpClient";
import type { ProductWithModules } from "@/types";
import type { TableFilter } from "@/types/components";

export default function ProductList() {
  const [products, setProducts] = useState<ProductWithModules[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchProducts();
  }, []);

  const fetchProducts = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await httpClient.getProducts();
      // Ensure modules is always an array for ProductWithModules type
      const productsWithModules = response.map((product) => ({
        ...product,
        modules: product.modules || [],
      }));
      setProducts(productsWithModules);
    } catch (error) {
      console.error("Error fetching products:", error);
      setError("Failed to fetch products");
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteProduct = async (productId: string) => {
    if (!confirm("Are you sure you want to delete this product?")) {
      return;
    }

    try {
      await httpClient.deleteProduct(parseInt(productId));
      setProducts(
        products.filter((product) => product.id.toString() !== productId)
      );
    } catch (error) {
      console.error("Error deleting product:", error);
      setError("Failed to delete product");
    }
  };

  const filteredProducts = useMemo(() => {
    if (!searchTerm) return products;

    return products.filter(
      (product) =>
        product.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        product.code.toLowerCase().includes(searchTerm.toLowerCase()) ||
        product.description?.toLowerCase().includes(searchTerm.toLowerCase())
    );
  }, [products, searchTerm]);

  const filters: TableFilter[] = [
    {
      label: "Status",
      value: "all",
      onChange: () => {},
      options: [
        { label: "All", value: "all" },
        { label: "Active", value: "ACTIVE" },
        { label: "Inactive", value: "INACTIVE" },
        { label: "Draft", value: "DRAFT" },
      ],
    },
  ];

  const actions = (
    <PermissionGuard permission="PRODUCT_MGMT:create">
      <Button asChild>
        <Link to="/products/create">
          <Plus className="mr-2 h-4 w-4" />
          Create Product
        </Link>
      </Button>
    </PermissionGuard>
  );

  return (
    <div className="w-full py-6 px-4 sm:px-6 lg:px-8">
      <div className="mb-6">
        <h1 className="text-3xl font-bold">Products</h1>
        <p className="text-muted-foreground">
          Manage your products and their configurations
        </p>
      </div>

      {error && (
        <Alert variant="destructive" className="mb-4">
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      <ProductTable
        data={filteredProducts}
        loading={loading}
        searchTerm={searchTerm}
        onSearchChange={setSearchTerm}
        searchPlaceholder="Search products by name, code, or description..."
        filters={filters}
        actions={actions}
        onDeleteProduct={handleDeleteProduct}
      />
    </div>
  );
}
