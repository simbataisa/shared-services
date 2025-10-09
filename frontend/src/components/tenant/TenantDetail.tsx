import React, { useState, useEffect } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { ArrowLeft } from "lucide-react";
import type {
  TenantType,
  TenantStatus,
  TenantUpdateData,
  TenantDetail as TenantDetailType,
} from "@/types/tenant";
import { httpClient } from "@/lib/httpClient";
import {
  StatisticsCard,
  DetailHeaderCard,
  ErrorCard,
} from "@/components/common";
import type { ErrorWithActions } from "@/types/errors";
import TenantStatusCard from "./TenantStatusCard";
import TenantBasicInfoCard from "./TenantBasicInfoCard";
import TenantAuditInfoCard from "./TenantAuditInfoCard";
// Local overlay spinner removed; rely on global loader via axios interceptors

const TenantDetailComponent: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [tenant, setTenant] = useState<TenantDetailType | null>(null);
  const [updating, setUpdating] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchTenantDetails();
  }, [id]);

  const fetchTenantDetails = async () => {
    try {
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

  if (error || !tenant) {
    const notFoundError: ErrorWithActions = {
      id: "tenant-detail-error",
      message: error || "Tenant not found",
      type: error ? "network" : "client",
      severity: error ? "high" : "medium",
      timestamp: new Date(),
      code: error ? 404 : undefined,
      details:
        "The tenant you're looking for doesn't exist or couldn't be loaded.",
      actions: [
        {
          id: "back",
          label: "Back to Tenants",
          action: "navigate",
          variant: "default",
          handler: () => navigate("/tenants"),
        },
        {
          id: "retry",
          label: "Retry",
          action: "retry",
          variant: "outline",
          handler: () => fetchTenantDetails(),
        },
      ],
      primaryAction: "back",
    };

    return (
      <div className="p-6">
        <ErrorCard
          error={notFoundError}
          dismissible
          onDismiss={() => navigate("/tenants")}
          showTimestamp
        />
      </div>
    );
  }

  return (
    <div className="p-6">
      {/* Header */}
      <DetailHeaderCard
        title={tenant.name}
        description={`Tenant Details • ${tenant.code}`}
        breadcrumbs={[
          { label: "Tenants", href: "/tenants" },
          { label: tenant.code },
        ]}
      />

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
