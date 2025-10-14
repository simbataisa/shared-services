import React from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { PermissionGuard } from "@/components/common/PermissionGuard";
import StatusDisplayCard from "@/components/common/StatusDisplayCard";
import type { RoleDetails } from "@/types";
import type { RoleStatus } from "@/types/entities";
import { ENTITY_STATUS_MAPPINGS } from "@/types/entities";

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
          <PermissionGuard permission="ROLE_MGMT:update">
            <Button
              onClick={() =>
                handleStatusUpdate(
                  role.roleStatus === ENTITY_STATUS_MAPPINGS.role.ACTIVE 
                    ? ENTITY_STATUS_MAPPINGS.role.INACTIVE 
                    : ENTITY_STATUS_MAPPINGS.role.ACTIVE
                )
              }
              disabled={updating}
              variant="destructive"
              size="sm"
              className="w-full"
            >
              {updating
                ? "Updating..."
                : role.roleStatus === ENTITY_STATUS_MAPPINGS.role.ACTIVE
                ? "Deactivate Role"
                : "Activate Role"}
            </Button>
          </PermissionGuard>
        </div>
      </CardContent>
    </Card>
  );
};
