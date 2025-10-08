import React from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { PermissionGuard } from "@/components/common/PermissionGuard";
import StatusDisplayCard from "@/components/common/StatusDisplayCard";
import type { RoleDetails, RoleStatus } from "@/types";

interface RoleStatusCardProps {
  role: RoleDetails;
  onStatusUpdate: (roleId: number, newStatus: RoleStatus) => void;
  updating?: boolean;
}

export const RoleStatusCard: React.FC<RoleStatusCardProps> = ({
  role,
  onStatusUpdate,
  updating = false,
}) => {
  const handleStatusUpdate = async (newStatus: string) => {
    await onStatusUpdate(role.id, newStatus);
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle>Status Management</CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        <StatusDisplayCard
          title="Current Status"
          description="Manage the role's current status"
          status={role.roleStatus}
        />

        <div className="flex gap-2">
          <PermissionGuard permission="ROLE_UPDATE">
            {role.roleStatus === "INACTIVE" && (
              <Button
                onClick={() => handleStatusUpdate("ACTIVE")}
                disabled={updating}
                variant="default"
                size="sm"
              >
                {updating ? "Updating..." : "Activate Role"}
              </Button>
            )}
            {role.roleStatus === "ACTIVE" && (
              <Button
                onClick={() => handleStatusUpdate("INACTIVE")}
                disabled={updating}
                variant="destructive"
                size="sm"
              >
                {updating ? "Updating..." : "Deactivate Role"}
              </Button>
            )}
          </PermissionGuard>
        </div>
      </CardContent>
    </Card>
  );
};
