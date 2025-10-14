import React from "react";
import { Activity } from "lucide-react";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { StatusBadge } from "@/components/common/StatusBadge";
import { PermissionGuard } from "@/components/common/PermissionGuard";
import { getStatusIcon, getStatusLabel } from "@/lib/status-utils";
import type { Permission } from "@/types";
import { StatusDisplayCard } from "../common";

interface PermissionStatusCardProps {
  permission: Permission;
  onStatusUpdate?: (action: "activate" | "deactivate") => Promise<void>;
  updating?: boolean;
}

export const PermissionStatusCard: React.FC<PermissionStatusCardProps> = ({
  permission,
  onStatusUpdate,
  updating = false,
}) => {
  const status = permission.isActive ? "ACTIVE" : "INACTIVE";

  const handleStatusToggle = async () => {
    if (!onStatusUpdate) return;

    const action = permission.isActive ? "deactivate" : "activate";
    await onStatusUpdate(action);
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center">
          <Activity className="mr-2 h-5 w-5" />
          Status Management
        </CardTitle>
        <CardDescription>Manage permission activation status</CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        <StatusDisplayCard
          title="Permission Status"
          description={
            permission.isActive
              ? "This permission is currently active and can be assigned to roles."
              : "This permission is inactive and cannot be assigned to roles."
          }
          status={status}
        />

        <PermissionGuard permission="PERMISSION_MGMT:update">
          <Button
            variant={permission.isActive ? "destructive" : "default"}
            size="sm"
            onClick={handleStatusToggle}
            disabled={updating}
            className="w-full"
          >
            {updating
              ? "Updating..."
              : permission.isActive
              ? "Deactivate Permission"
              : "Activate Permission"}
          </Button>
        </PermissionGuard>
      </CardContent>
    </Card>
  );
};
