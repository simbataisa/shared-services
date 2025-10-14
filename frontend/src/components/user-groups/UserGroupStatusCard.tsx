import React from "react";
import { CheckCircle, XCircle } from "lucide-react";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import StatusDisplayCard from "@/components/common/StatusDisplayCard";
import { PermissionGuard } from "@/components/common/PermissionGuard";
import { StatusBadge } from "@/components/common/StatusBadge";

interface UserGroupStatusCardProps {
  memberCount: number;
  onStatusUpdate?: (action: "activate" | "deactivate") => Promise<void>;
  updating?: boolean;
  className?: string;
}

const UserGroupStatusCard: React.FC<UserGroupStatusCardProps> = ({
  memberCount,
  onStatusUpdate,
  updating = false,
  className = "",
}) => {
  const getGroupStatus = (memberCount: number) => {
    return memberCount > 0 ? "ACTIVE" : "INACTIVE";
  };

  const getStatusDescription = (memberCount: number) => {
    return `User group is currently ${memberCount > 0 ? "active" : "inactive"} with ${memberCount} member${memberCount !== 1 ? 's' : ''}`;
  };

  const handleStatusAction = async (action: "activate" | "deactivate") => {
    if (onStatusUpdate) {
      await onStatusUpdate(action);
    }
  };

  return (
    <Card className={className}>
      <CardHeader>
        <CardTitle>Status Management</CardTitle>
        <CardDescription>
          Manage the user group's operational status
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        {/* Status Display using StatusDisplayCard */}
        <StatusDisplayCard
          title="Current Status"
          description={getStatusDescription(memberCount)}
          status={getGroupStatus(memberCount)}
        />

        {/* Status Actions */}
        <PermissionGuard permission="GROUP_MGMT:update">
          <div className="space-y-2">
            {memberCount > 0 ? (
              <Button
                variant="destructive"
                className="w-full"
                disabled={updating}
                onClick={() => handleStatusAction("deactivate")}
              >
                <XCircle className="h-4 w-4 mr-2" />
                Deactivate Group
              </Button>
            ) : (
              <Button
                variant="outline"
                className="w-full"
                disabled={updating}
                onClick={() => handleStatusAction("activate")}
              >
                <CheckCircle className="h-4 w-4 mr-2" />
                Activate Group
              </Button>
            )}
          </div>
        </PermissionGuard>
      </CardContent>
    </Card>
  );
};

export default UserGroupStatusCard;