import React, { useState, useEffect, useMemo } from "react";
import { Link } from "react-router-dom";
import { PermissionGuard } from "@/components/common/PermissionGuard";
import { usePermissions } from "@/hooks/usePermissions";
import { Button } from "@/components/ui/button";
import { Plus } from "lucide-react";
import httpClient from "@/lib/httpClient";
import {
  ENTITY_STATUS_MAPPINGS,
  type ErrorWithActions,
  type Module,
} from "@/types";
import { type TableFilter } from "@/types/components";
import ModuleTable from "@/components/module/ModuleTable";
import LoadingSpinner from "@/components/common/LoadingSpinner";
import { ErrorCard } from "@/components/common";

const ModuleList: React.FC = () => {
  const [modules, setModules] = useState<Module[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<ErrorWithActions | null>(null);
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState<
    | "all"
    | typeof ENTITY_STATUS_MAPPINGS.module.ACTIVE
    | typeof ENTITY_STATUS_MAPPINGS.module.INACTIVE
  >("all");
  const [productFilter, setProductFilter] = useState<string>("all");
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
      const modules = await httpClient.getModules();
      setModules(modules || []);
    } catch (err) {
      setError({
        id: `server-${Date.now()}`,
        type: "server",
        severity: "high",
        message: err instanceof Error ? err.message : "Unknown error",
        timestamp: new Date(),
        details: err instanceof Error ? err.message : "Unknown error",
        actions: [],
      });
      console.error("Error fetching modules:", err);
    } finally {
      setLoading(false);
    }
  };

  const fetchProducts = async () => {
    try {
      const products = await httpClient.getProducts();
      setProducts(products || []);
    } catch (err) {
      setError({
        id: `server-${Date.now()}`,
        type: "server",
        severity: "high",
        message: err instanceof Error ? err.message : "Unknown error",
        timestamp: new Date(),
        details: err instanceof Error ? err.message : "Unknown error",
        actions: [],
      });
      console.error("Error fetching products:", err);
    }
  };

  const handleDelete = async (moduleId: number) => {
    if (!canDeleteModules) return;

    if (window.confirm("Are you sure you want to delete this module?")) {
      try {
        await httpClient.deleteModule(moduleId);
        setModules((prev) => prev.filter((module) => module.id !== moduleId));
      } catch (err) {
        setError({
          id: `server-${Date.now()}`,
          type: "server",
          severity: "high",
          message: err instanceof Error ? err.message : "Unknown error",
          timestamp: new Date(),
          details: err instanceof Error ? err.message : "Unknown error",
          actions: [],
        });
        console.error("Error deleting module:", err);
      }
    }
  };

  // Filter modules using useMemo for performance
  const filteredModules = useMemo(() => {
    return modules.filter((module) => {
      const matchesSearch =
        module.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        (module.description &&
          module.description
            .toLowerCase()
            .includes(searchTerm.toLowerCase())) ||
        (module.code &&
          module.code.toLowerCase().includes(searchTerm.toLowerCase()));

      const matchesStatus =
        statusFilter === "all" ||
        (statusFilter === ENTITY_STATUS_MAPPINGS.module.ACTIVE &&
          module.moduleStatus === ENTITY_STATUS_MAPPINGS.module.ACTIVE) ||
        (statusFilter === ENTITY_STATUS_MAPPINGS.module.INACTIVE &&
          module.moduleStatus === ENTITY_STATUS_MAPPINGS.module.INACTIVE);

      const matchesProduct =
        productFilter === "all" ||
        module.productId?.toString() === productFilter;

      return matchesSearch && matchesStatus && matchesProduct;
    });
  }, [modules, searchTerm, statusFilter, productFilter]);

  // Define filters for the table
  const filters: TableFilter[] = [
    {
      label: "Status",
      value: statusFilter,
      onChange: (value: string) =>
        setStatusFilter(value as "all" | "active" | "inactive"),
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
  ];

  // Define actions for the table
  const actions = canCreateModules ? (
    <Button asChild>
      <Link to="/modules/create">
        <Plus className="mr-2 h-4 w-4" />
        Create Module
      </Link>
    </Button>
  ) : null;

  if (loading) {
    return <LoadingSpinner />;
  }

  if (error) {
    return <ErrorCard error={error} />;
  }

  return (
    <PermissionGuard permission="MODULE_MGMT:read">
      <div className="w-full py-6 px-4 sm:px-6 lg:px-8">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-8 space-y-4 sm:space-y-0">
          <div className="space-y-1">
            <h1 className="text-2xl sm:text-3xl font-bold tracking-tight">
              Module Management
            </h1>
            <p className="text-sm sm:text-base text-muted-foreground">
              Manage system modules and their configurations
            </p>
          </div>
        </div>

        <ModuleTable
          data={filteredModules}
          loading={loading}
          searchTerm={searchTerm}
          onSearchChange={setSearchTerm}
          filters={filters}
          actions={actions}
          onDelete={handleDelete}
        />
      </div>
    </PermissionGuard>
  );
};

export default ModuleList;
