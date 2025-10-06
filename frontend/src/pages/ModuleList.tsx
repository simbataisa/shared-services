import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { PermissionGuard } from "../components/PermissionGuard";
import { StatusBadge } from "../components/StatusBadge";
import { usePermissions } from "../hooks/usePermissions";
import { normalizeEntityStatus } from "../lib/status-colors";
import { SearchAndFilter } from "../components/SearchAndFilter";
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
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Plus, Edit, Trash2, Eye } from "lucide-react";
import api from "../lib/api";

interface Module {
  id: number;
  name: string;
  description: string;
  code?: string;
  isActive: boolean;
  productId: number;
  productName: string;
  createdAt: string;
  updatedAt: string;
}

const ModuleList: React.FC = () => {
  const [modules, setModules] = useState<Module[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState<
    "all" | "active" | "inactive"
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
      module.description.toLowerCase().includes(searchTerm.toLowerCase()) ||
      (module.code &&
        module.code.toLowerCase().includes(searchTerm.toLowerCase()));

    const matchesStatus =
      statusFilter === "all" ||
      (statusFilter === "active" && module.isActive) ||
      (statusFilter === "inactive" && !module.isActive);

    const matchesProduct =
      productFilter === "all" || module.productId.toString() === productFilter;

    return matchesSearch && matchesStatus && matchesProduct;
  });

  return (
    <PermissionGuard permission="module:read">
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
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Module</TableHead>
                    <TableHead>Product</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead>Last Updated</TableHead>
                    <TableHead className="text-right">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {filteredModules.map((module) => (
                    <TableRow key={module.id}>
                      <TableCell>
                        <div>
                          <div className="font-medium">{module.name}</div>
                          <div className="text-sm text-muted-foreground">
                            {module.description}
                          </div>
                          {module.code && (
                            <div className="text-xs text-muted-foreground mt-1">
                              Code: {module.code}
                            </div>
                          )}
                        </div>
                      </TableCell>
                      <TableCell>
                        <div className="text-sm">{module.productName}</div>
                      </TableCell>
                      <TableCell>
                        <StatusBadge
                          status={normalizeEntityStatus(
                            "module",
                            module.isActive ? "ACTIVE" : "INACTIVE"
                          )}
                        />
                      </TableCell>
                      <TableCell className="text-sm text-muted-foreground">
                        {new Date(module.updatedAt).toLocaleDateString()}
                      </TableCell>
                      <TableCell className="text-right">
                        <div className="flex justify-end space-x-2">
                          <Button
                            variant="ghost"
                            size="sm"
                            asChild
                            className="text-blue-600 hover:text-blue-700"
                          >
                            <Link to={`/modules/${module.id}`}>
                              <Eye className="h-4 w-4" />
                            </Link>
                          </Button>
                          {canUpdateModules && (
                            <>
                              <Button
                                variant="ghost"
                                size="sm"
                                asChild
                                className="text-yellow-600 hover:text-yellow-700"
                              >
                                <Link to={`/modules/${module.id}/edit`}>
                                  <Edit className="h-4 w-4" />
                                </Link>
                              </Button>
                              <Button
                                variant="ghost"
                                size="sm"
                                onClick={() =>
                                  handleStatusChange(
                                    module.id,
                                    !module.isActive
                                  )
                                }
                              >
                                {module.isActive ? "Deactivate" : "Activate"}
                              </Button>
                            </>
                          )}
                          {canDeleteModules && (
                            <Button
                              variant="ghost"
                              size="sm"
                              onClick={() => handleDelete(module.id)}
                              className="text-red-600 hover:text-red-700"
                            >
                              <Trash2 className="h-4 w-4" />
                            </Button>
                          )}
                        </div>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
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
