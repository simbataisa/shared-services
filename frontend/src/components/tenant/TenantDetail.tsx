import React, { useState, useEffect } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import { Button } from "@/components/ui/button";
import {
  Breadcrumb,
  BreadcrumbItem,
  BreadcrumbLink,
  BreadcrumbList,
  BreadcrumbPage,
  BreadcrumbSeparator,
} from "@/components/ui/breadcrumb";
import { ArrowLeft, AlertCircle } from "lucide-react";
import type {
  TenantType,
  TenantStatus,
  TenantUpdateData,
  TenantDetail as TenantDetailType,
} from "@/types/tenant";
import { httpClient } from "@/lib/httpClient";
import { StatisticsCard } from "@/components/common";
import TenantStatusCard from "./TenantStatusCard";
import TenantBasicInfoCard from "./TenantBasicInfoCard";
import TenantAuditInfoCard from "./TenantAuditInfoCard";

const TenantDetailComponent: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [tenant, setTenant] = useState<TenantDetailType | null>(null);
  const [loading, setLoading] = useState(true);
  const [updating, setUpdating] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchTenantDetails();
  }, [id]);

  const fetchTenantDetails = async () => {
    try {
      setLoading(true);
      setError(null);
      const tenantData = await httpClient.getTenantById(Number(id));
      // Cast the basic Tenant to TenantDetailType with default values for missing properties
      const tenantDetail: TenantDetailType = {
        ...tenantData,
        code: tenantData.id.toString(), // Use ID as code if not available
        type: "BUSINESS_IN" as TenantType, // Default type
        status: tenantData.status as TenantStatus, // Cast status to TenantStatus
        createdBy: "System",
        updatedBy: "System",
      };
      setTenant(tenantDetail);
    } catch (error) {
      console.error("Failed to fetch tenant details:", error);
      setError("Failed to load tenant details");
    } finally {
      setLoading(false);
    }
  };

  const handleTenantUpdate = async (updateData: TenantUpdateData) => {
    if (!tenant) return;

    try {
      setUpdating(true);

      // Create the request data matching CreateTenantRequest structure
      const requestData = {
        name: updateData.name || tenant.name,
        description: undefined, // Add description if needed
      };

      await httpClient.updateTenant(tenant.id, requestData);

      // Update local state with all the fields
      setTenant((prev) =>
        prev
          ? {
              ...prev,
              ...updateData,
              updatedAt: new Date().toISOString(),
            }
          : null
      );
    } catch (error) {
      console.error("Error updating tenant:", error);
      throw error;
    } finally {
      setUpdating(false);
    }
  };

  const handleStatusChange = async (newStatus: TenantStatus) => {
    if (!tenant) return;

    try {
      setUpdating(true);
      await httpClient.updateTenantStatus(tenant.id, newStatus);
      setTenant((prev) =>
        prev
          ? {
              ...prev,
              status: newStatus,
              updatedAt: new Date().toISOString(),
            }
          : null
      );
    } catch (error) {
      console.error("Error updating tenant status:", error);
    } finally {
      setUpdating(false);
    }
  };

  const handleDeleteTenant = async () => {
    if (!tenant) return;

    try {
      await httpClient.deleteTenant(tenant.id);
      navigate("/tenants");
    } catch (error) {
      console.error("Error deleting tenant:", error);
    }
  };

  if (loading) {
    return (
      <div className="p-6">
        <div className="animate-pulse space-y-6">
          <div className="h-8 bg-muted rounded w-1/3"></div>
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <div className="h-64 bg-muted rounded"></div>
            <div className="h-64 bg-muted rounded"></div>
          </div>
        </div>
      </div>
    );
  }

  if (error || !tenant) {
    return (
      <div className="p-6">
        <div className="text-center py-12">
          <AlertCircle className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
          <h3 className="text-lg font-semibold mb-2">
            {error || "Tenant not found"}
          </h3>
          <p className="text-muted-foreground mb-4">
            The tenant you're looking for doesn't exist or couldn't be loaded.
          </p>
          <Button asChild>
            <Link to="/tenants">
              <ArrowLeft className="h-4 w-4 mr-2" />
              Back to Tenants
            </Link>
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="p-6">
      {/* Header */}
      <div className="mb-8">
        <Breadcrumb className="mb-4">
          <BreadcrumbList>
            <BreadcrumbItem>
              <BreadcrumbLink asChild>
                <Link to="/tenants">Tenants</Link>
              </BreadcrumbLink>
            </BreadcrumbItem>
            <BreadcrumbSeparator />
            <BreadcrumbItem>
              <BreadcrumbPage>{tenant.code}</BreadcrumbPage>
            </BreadcrumbItem>
          </BreadcrumbList>
        </Breadcrumb>

        <div className="flex items-center justify-between">
          <div className="flex items-center gap-4">
            <Button variant="ghost" size="icon" asChild>
              <Link to="/tenants">
                <ArrowLeft className="h-5 w-5" />
              </Link>
            </Button>
            <div>
              <h1 className="text-2xl font-bold">{tenant.name}</h1>
              <p className="text-muted-foreground">
                Tenant Details • {tenant.code}
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Left Column */}
        <div className="space-y-6">
          <TenantBasicInfoCard
            tenant={tenant}
            onUpdate={handleTenantUpdate}
            updating={updating}
          />
          <TenantAuditInfoCard tenant={tenant} />
        </div>

        {/* Right Column */}
        <div className="space-y-6">
          <TenantStatusCard
            tenant={tenant}
            onStatusUpdate={handleStatusChange}
            onDelete={handleDeleteTenant}
            updating={updating}
          />
          <StatisticsCard
            title="Statistics"
            description="Key metrics and information about this tenant"
            layout="grid"
            statistics={[
              {
                label: "Organization ID",
                value: tenant.organizationId || "N/A",
              },
              {
                label: "Active Status",
                value: tenant.status === "ACTIVE" ? "✓" : "✗",
              },
              {
                label: "Created Year",
                value: new Date(tenant.createdAt).getFullYear(),
              },
              {
                label: "Days Active",
                value: Math.floor(
                  (Date.now() - new Date(tenant.createdAt).getTime()) /
                    (1000 * 60 * 60 * 24)
                ),
              },
            ]}
          />
        </div>
      </div>
    </div>
  );
};

export const TenantDetail = TenantDetailComponent;
export default TenantDetailComponent;
