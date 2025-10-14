import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { PermissionGuard } from "@/components/common/PermissionGuard";
import { usePermissions } from "@/hooks/usePermissions";
import { SearchAndFilter } from "@/components/common/SearchAndFilter";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Skeleton } from "@/components/ui/skeleton";
import { Plus } from "lucide-react";
import api from "@/lib/api";
import { ENTITY_STATUS_MAPPINGS, type Module } from "@/types";
import ModuleTable from "@/components/module/ModuleTable";

const ModuleList: React.FC = () => {
  const [modules, setModules] = useState<Module[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState<
    | "all"
    | typeof ENTITY_STATUS_MAPPINGS.module.ACTIVE
    | typeof ENTITY_STATUS_MAPPINGS.module.INACTIVE
  >("all");
  const [productFilter, setProductFilter] = useState<string>("all");

  const handleStatusFilterChange = (value: string) => {
    setStatusFilter(value as "all" | "active" | "inactive");
  };
  const [products, setProducts] = useState<{ id: number; name: string }[]>([]);

  const { canCreateModules, canUpdateModules, canDeleteModules } =
    usePermissions();

  useEffect(() => {
    fetchModules();
    fetchProducts();
  }, []);

  const fetchModules = async () => {
    try {
      setLoading(true);
      const response = await api.get("/v1/modules");
      setModules(response.data || []);
    } catch (err) {
      setError("Failed to fetch modules");
      console.error("Error fetching modules:", err);
    } finally {
      setLoading(false);
    }
  };

  const fetchProducts = async () => {
    try {
      const response = await api.get("/products");
      setProducts(response.data || []);
    } catch (err) {
      console.error("Error fetching products:", err);
    }
  };

  const handleStatusChange = async (moduleId: number, newStatus: boolean) => {
    if (!canUpdateModules) return;

    try {
      await api.put(`/v1/modules/${moduleId}`, { isActive: newStatus });
      setModules((prev) =>
        prev.map((module) =>
          module.id === moduleId ? { ...module, isActive: newStatus } : module
        )
      );
    } catch (err) {
      setError("Failed to update module status");
      console.error("Error updating module status:", err);
    }
  };

  const handleDelete = async (moduleId: number) => {
    if (!canDeleteModules) return;

    if (window.confirm("Are you sure you want to delete this module?")) {
      try {
        await api.delete(`/v1/modules/${moduleId}`);
        setModules((prev) => prev.filter((module) => module.id !== moduleId));
      } catch (err) {
        setError("Failed to delete module");
        console.error("Error deleting module:", err);
      }
    }
  };

  const filteredModules = modules.filter((module) => {
    const matchesSearch =
      module.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      (module.description &&
        module.description.toLowerCase().includes(searchTerm.toLowerCase())) ||
      (module.code &&
        module.code.toLowerCase().includes(searchTerm.toLowerCase()));

    const matchesStatus =
      statusFilter === "all" ||
      (statusFilter === ENTITY_STATUS_MAPPINGS.module.ACTIVE &&
        module.moduleStatus === ENTITY_STATUS_MAPPINGS.module.ACTIVE) ||
      (statusFilter === ENTITY_STATUS_MAPPINGS.module.INACTIVE &&
        module.moduleStatus === ENTITY_STATUS_MAPPINGS.module.INACTIVE);

    const matchesProduct =
      productFilter === "all" || module.productId?.toString() === productFilter;

    return matchesSearch && matchesStatus && matchesProduct;
  });

  return (
    <PermissionGuard permission="MODULE_MGMT:read">
      <div className="container mx-auto py-10">
        <div className="flex items-center justify-between mb-8">
          <div>
            <h1 className="text-3xl font-bold tracking-tight">
              Module Management
            </h1>
            <p className="text-muted-foreground">
              Manage system modules and their configurations
            </p>
          </div>
        </div>

        {error && (
          <Alert className="mb-6">
            <AlertDescription>{error}</AlertDescription>
          </Alert>
        )}

        {/* Search and Filters */}
        <SearchAndFilter
          searchTerm={searchTerm}
          onSearchChange={setSearchTerm}
          searchPlaceholder="Search modules by name, description, or code..."
          filters={[
            {
              label: "Status",
              value: statusFilter,
              onChange: handleStatusFilterChange,
              options: [
                { value: "all", label: "All Status" },
                { value: "active", label: "Active" },
                { value: "inactive", label: "Inactive" },
              ],
              placeholder: "Filter by status",
              width: "w-[180px]",
            },
            {
              label: "Product",
              value: productFilter,
              onChange: setProductFilter,
              options: [
                { value: "all", label: "All Products" },
                ...products.map((product) => ({
                  value: product.id.toString(),
                  label: product.name,
                })),
              ],
              placeholder: "Filter by product",
              width: "w-[200px]",
            },
          ]}
          actions={
            canCreateModules && (
              <Button asChild>
                <Link to="/modules/create">
                  <Plus className="mr-2 h-4 w-4" />
                  Create Module
                </Link>
              </Button>
            )
          }
        />

        <Card>
          <CardHeader>
            <CardTitle>Modules</CardTitle>
            <CardDescription>
              A list of all modules in the system
            </CardDescription>
          </CardHeader>
          <CardContent>
            {loading ? (
              <div className="space-y-2">
                {[...Array(5)].map((_, i) => (
                  <Skeleton key={i} className="h-12 w-full" />
                ))}
              </div>
            ) : (
              <ModuleTable modules={filteredModules} onDelete={handleDelete} />
            )}

            {!loading && filteredModules.length === 0 && (
              <div className="text-center py-12">
                <div className="text-muted-foreground">
                  {modules.length === 0
                    ? "No modules found."
                    : "No modules match your filters."}
                </div>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </PermissionGuard>
  );
};

export default ModuleList;
