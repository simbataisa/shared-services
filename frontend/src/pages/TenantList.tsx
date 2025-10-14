import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import {
  Building2,
  Edit,
  Trash2,
  Eye,
  MoreHorizontal,
  Plus,
} from "lucide-react";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { StatusBadge } from "@/components/common/StatusBadge";
import { PermissionGuard } from "@/components/common/PermissionGuard";
import { getStatusIcon } from "@/lib/status-utils";
import { normalizeEntityStatus } from "@/lib/status-utils";
import api from "@/lib/api";
import { type Tenant } from "@/types/entities";
import SearchAndFilter from "@/components/common/SearchAndFilter";
import TenantTable from "@/components/tenant/TenantTable";

export default function TenantList() {
  const [tenants, setTenants] = useState<Tenant[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState<string>("all");
  const [typeFilter, setTypeFilter] = useState<string>("all");

  useEffect(() => {
    fetchTenants();
  }, []);

  const fetchTenants = async () => {
    try {
      setLoading(true);
      const response = await api.get("/v1/tenants");
      // Extract the actual tenant array from the ApiResponse wrapper
      setTenants(response.data.data || []);
    } catch (error) {
      console.error("Failed to fetch tenants:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleStatusChange = async (tenantId: number, newStatus: string) => {
    try {
      await api.patch(`/v1/tenants/${tenantId}/status`, { status: newStatus });
      await fetchTenants(); // Refresh the list
    } catch (error) {
      console.error("Failed to update tenant status:", error);
    }
  };

  const handleDelete = async (tenantId: number) => {
    if (window.confirm("Are you sure you want to delete this tenant?")) {
      try {
        await api.delete(`/v1/tenants/${tenantId}`);
        await fetchTenants(); // Refresh the list
      } catch (error) {
        console.error("Failed to delete tenant:", error);
      }
    }
  };

  const filteredTenants = tenants.filter((tenant) => {
    const matchesSearch =
      tenant.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      tenant.code.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesStatus =
      statusFilter === "all" || tenant.status === statusFilter;
    const matchesType = typeFilter === "all" || tenant.type === typeFilter;

    return matchesSearch && matchesStatus && matchesType;
  });

  if (loading) {
    return (
      <div className="p-6 space-y-6">
        <div className="flex justify-between items-center">
          <div className="space-y-2">
            <Skeleton className="h-8 w-48" />
            <Skeleton className="h-4 w-64" />
          </div>
          <Skeleton className="h-10 w-32" />
        </div>

        <Card>
          <CardContent className="p-4">
            <div className="flex flex-col sm:flex-row gap-4">
              <Skeleton className="h-10 flex-1" />
              <div className="flex gap-2">
                <Skeleton className="h-10 w-32" />
                <Skeleton className="h-10 w-32" />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-4">
            <div className="space-y-4">
              {[...Array(5)].map((_, i) => (
                <div
                  key={i}
                  className="flex items-center gap-3 p-4 border rounded-lg"
                >
                  <Skeleton className="h-10 w-10 rounded-full" />
                  <div className="flex-1 space-y-1">
                    <Skeleton className="h-4 w-32" />
                    <Skeleton className="h-3 w-24" />
                  </div>
                  <Skeleton className="h-4 w-24" />
                  <Skeleton className="h-6 w-20" />
                  <Skeleton className="h-8 w-24" />
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="p-6 space-y-6">
      {/* Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold">Tenants</h1>
          <p className="text-muted-foreground">
            Manage tenant organizations and their access
          </p>
        </div>
      </div>

      {/* Search and Filters */}
      <SearchAndFilter
        searchTerm={searchTerm}
        onSearchChange={setSearchTerm}
        searchPlaceholder="Search tenants by name or code..."
        filters={[
          {
            label: "Status",
            value: statusFilter,
            onChange: setStatusFilter,
            options: [
              { value: "all", label: "All Status" },
              { value: "ACTIVE", label: "Active" },
              { value: "INACTIVE", label: "Inactive" },
              { value: "SUSPENDED", label: "Suspended" },
            ],
            width: "w-40",
          },
          {
            label: "Type",
            value: typeFilter,
            onChange: setTypeFilter,
            options: [
              { value: "all", label: "All Types" },
              { value: "BUSINESS_IN", label: "Business Internal" },
              { value: "BUSINESS_OUT", label: "Business External" },
              { value: "INDIVIDUAL", label: "Individual" },
            ],
            width: "w-48",
          },
        ]}
        actions={
          <PermissionGuard permission="TENANT_MGMT:create">
            <Button asChild>
              <Link to="/tenants/create">
                <Plus className="h-4 w-4 mr-2" />
                Create Tenant
              </Link>
            </Button>
          </PermissionGuard>
        }
      />

      {/* Tenants Table */}
      <Card>
        <CardContent className="p-0">
          <TenantTable tenants={filteredTenants} />

          {filteredTenants.length === 0 && (
            <div className="text-center py-12">
              <Building2 className="mx-auto h-12 w-12 text-muted-foreground" />
              <h3 className="mt-2 text-sm font-medium">No tenants found</h3>
              <p className="mt-1 text-sm text-muted-foreground">
                {searchTerm || statusFilter !== "all" || typeFilter !== "all"
                  ? "Try adjusting your search or filter criteria."
                  : "Get started by creating a new tenant."}
              </p>
              {!searchTerm &&
                statusFilter === "all" &&
                typeFilter === "all" && (
                  <PermissionGuard permission="TENANT_MGMT:create">
                    <Button className="mt-4" asChild>
                      <Link to="/tenants/create">
                        <Plus className="h-4 w-4 mr-2" />
                        Create Your First Tenant
                      </Link>
                    </Button>
                  </PermissionGuard>
                )}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
