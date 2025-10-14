import React, { useEffect, useState } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import { ArrowLeft, Shield, AlertTriangle } from "lucide-react";
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
import { PermissionGuard } from "@/components/common/PermissionGuard";
import { ErrorCard } from "@/components/common/ErrorCard";
import { DetailHeaderCard } from "@/components/common";
import { useErrorHandler } from "@/hooks/useErrorHandler";
import { usePermissions } from "@/hooks/usePermissions";
import { BasicInformationCard } from "./BasicInformationCard";
import { PermissionDetailsCard } from "./PermissionDetailsCard";
import { PermissionStatusCard } from "./PermissionStatusCard";
import { AuditInformationCard } from "./AuditInformationCard";
import { httpClient } from "@/lib/httpClient";
import type { Permission } from "@/types";
import type { BaseError, ErrorWithActions } from "@/types/errors";

interface PermissionDetailProps {
  permissionId?: number;
}

export default function PermissionDetail({
  permissionId: propPermissionId,
}: PermissionDetailProps) {
  const { id: urlPermissionId } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { canViewPermissions, canUpdatePermissions } = usePermissions();
  const { handleError } = useErrorHandler();

  const permissionId = propPermissionId || urlPermissionId;

  const [permission, setPermission] = useState<Permission | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<ErrorWithActions | null>(null);
  const [updating, setUpdating] = useState(false);

  // Fetch permission data
  const fetchPermission = async () => {
    if (!permissionId) {
      setError({
        id: `validation-${Date.now()}`,
        type: "validation",
        severity: "medium",
        message: "Permission ID is required",
        timestamp: new Date(),
        actions: [
          {
            id: "navigate",
            label: "Back to Permissions",
            action: "navigate",
            variant: "outline",
          },
        ],
        primaryAction: "navigate",
      });
      setLoading(false);
      return;
    }

    try {
      setLoading(true);
      setError(null);
      const data = await httpClient.getPermissionById(Number(permissionId));
      console.log(data);
      setPermission(data);
    } catch (err: any) {
      const errorMessage =
        err.response?.data?.message ||
        err.message ||
        "Failed to load permission";

      setError({
        id: `server-${Date.now()}`,
        type: "server",
        severity: "high",
        message: errorMessage,
        timestamp: new Date(),
        details: err.response?.data?.details || err.stack,
        actions: [
          {
            id: "retry",
            label: "Try Again",
            action: "retry",
            variant: "default",
          },
          {
            id: "navigate",
            label: "Back to Permissions",
            action: "navigate",
            variant: "outline",
          },
        ],
        primaryAction: "retry",
      });
      handleError(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPermission();
  }, [permissionId]);

  // Check permissions
  if (!canViewPermissions) {
    return (
      <PermissionGuard permission="PERMISSION_MGMT:read">
        <div className="container mx-auto px-4 py-8">
          <Alert>
            <AlertTriangle className="h-4 w-4" />
            <AlertDescription>
              You don't have permission to view permission details.
            </AlertDescription>
          </Alert>
        </div>
      </PermissionGuard>
    );
  }

  // Handle basic information update
  const handleBasicInfoUpdate = async (updatedData: {
    name: string;
    description: string;
  }) => {
    if (!permission) return;

    try {
      setUpdating(true);
      const updatedPermission = await httpClient.updatePermission(
        permission.id,
        updatedData
      );
      setPermission((prev) =>
        prev ? { ...prev, ...updatedPermission } : null
      );
    } catch (err: any) {
      handleError(err);
      throw err;
    } finally {
      setUpdating(false);
    }
  };

  // Handle status update
  const handleStatusUpdate = async (action: "activate" | "deactivate") => {
    if (!permission) return;

    try {
      setUpdating(true);
      const isActive = action === "activate";
      const updatedPermission = await httpClient.updatePermissionStatus(
        permission.id,
        isActive
      );
      setPermission((prev) => (prev ? { ...prev, isActive } : null));
    } catch (err: any) {
      handleError(err);
      throw err;
    } finally {
      setUpdating(false);
    }
  };

  // Loading state
  if (loading) {
    return (
      <div className="container mx-auto p-6">
        <div className="space-y-6">
          <Skeleton className="h-8 w-64" />
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            <div className="lg:col-span-2 space-y-6">
              <Skeleton className="h-48 w-full" />
              <Skeleton className="h-32 w-full" />
            </div>
            <div className="space-y-6">
              <Skeleton className="h-32 w-full" />
              <Skeleton className="h-32 w-full" />
            </div>
          </div>
        </div>
      </div>
    );
  }

  // Error state
  if (error) {
    return (
      <div className="container mx-auto p-6">
        <ErrorCard
          error={error}
          onDismiss={() => setError(null)}
          onAction={(actionId) => {
            if (actionId === "retry") {
              fetchPermission();
            } else if (actionId === "navigate") {
              navigate("/permissions");
            }
          }}
        />
      </div>
    );
  }

  // Not found state
  if (!permission) {
    return (
      <div className="container mx-auto p-6">
        <Card>
          <CardContent className="pt-6">
            <div className="text-center">
              <Shield className="mx-auto h-12 w-12 text-gray-400 mb-4" />
              <h3 className="text-lg font-semibold text-gray-900 mb-2">
                Permission Not Found
              </h3>
              <p className="text-gray-600 mb-4">
                The permission you're looking for doesn't exist or has been
                removed.
              </p>
              <Button
                onClick={() => navigate("/permissions")}
                variant="outline"
              >
                <ArrowLeft className="mr-2 h-4 w-4" />
                Back to Permissions
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="container mx-auto p-6 space-y-6">
      {/* Header */}
      <DetailHeaderCard
        title={permission.name}
        description={`Permission ID: ${permission.id}`}
        breadcrumbs={[
          { label: "Permissions", href: "/permissions" },
          { label: permission.name }
        ]}
      />

      {/* Main Content Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column - Main Information */}
        <div className="lg:col-span-2 space-y-6">
          <BasicInformationCard
            permission={permission}
            onUpdate={handleBasicInfoUpdate}
          />

          <PermissionDetailsCard permission={permission} />
        </div>

        {/* Right Column - Status and Audit */}
        <div className="space-y-6">
          <PermissionStatusCard
            permission={permission}
            onStatusUpdate={handleStatusUpdate}
          />

          <AuditInformationCard permission={permission} />
        </div>
      </div>
    </div>
  );
}
