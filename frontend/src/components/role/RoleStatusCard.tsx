import React from "react";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { StatusBadge } from "@/components/common/StatusBadge";
import { PermissionGuard } from "@/components/common/PermissionGuard";
import { normalizeEntityStatus } from "@/lib/status-colors";
import type { RoleDetails } from "@/types";

interface RoleStatusCardProps {
  role: RoleDetails;
  onStatusUpdate: (status: "ACTIVE" | "INACTIVE") => Promise<void>;
  updating: boolean;
}

export const RoleStatusCard: React.FC<RoleStatusCardProps> = ({
  role,
  onStatusUpdate,
  updating,
}) => {
  const handleStatusUpdate = async (status: "ACTIVE" | "INACTIVE") => {
    try {
      await onStatusUpdate(status);
    } catch (error) {
      console.error("Failed to update role status:", error);
    }
  };

  return (
    <PermissionGuard permission="role:update">
      <Card>
        <CardHeader>
          <CardTitle className="text-lg font-semibold text-gray-900">
            Status Management
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex items-center justify-between">
            <span className="text-sm text-gray-600">
              Current Status
            </span>
            <StatusBadge
              status={normalizeEntityStatus("role", role.roleStatus)}
            />
          </div>
          <div className="space-y-2">
            {role.roleStatus === "ACTIVE" ? (
              <Button
                onClick={() => handleStatusUpdate("INACTIVE")}
                disabled={updating}
                variant="destructive"
                size="sm"
                className="w-full"
              >
                Deactivate Role
              </Button>
            ) : (
              <Button
                onClick={() => handleStatusUpdate("ACTIVE")}
                disabled={updating}
                variant="outline"
                size="sm"
                className="w-full"
              >
                Activate Role
              </Button>
            )}
          </div>
        </CardContent>
      </Card>
    </PermissionGuard>
  );
};