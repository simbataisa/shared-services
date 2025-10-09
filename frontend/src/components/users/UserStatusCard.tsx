import React from "react";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { StatusBadge } from "@/components/common/StatusBadge";
import { normalizeEntityStatus } from "@/lib/status-utils";
import { AlertTriangle, CheckCircle } from "lucide-react";
import type { User } from "@/types";
import { StatusDisplayCard } from "../common";

interface UserStatusCardProps {
  user: User;
  onStatusChange?: (newStatus: "ACTIVE" | "INACTIVE") => void;
  loading?: boolean;
  canUpdate?: boolean;
  className?: string;
}

const UserStatusCard: React.FC<UserStatusCardProps> = ({
  user,
  onStatusChange,
  loading = false,
  canUpdate = false,
  className = "",
}) => {
  return (
    <Card className={className}>
      <CardHeader>
        <CardTitle className="flex items-center text-lg font-semibold text-gray-900">
          <AlertTriangle className="mr-2 h-5 w-5" />
          User Status Management
        </CardTitle>
        <CardDescription>
          Activate, deactivate, or suspend user account
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        <StatusDisplayCard
          title="User Status"
          description={`User is currently ${
            user.userStatus?.toLowerCase() || "unknown"
          }`}
          status={user.userStatus}
        />

        {canUpdate && onStatusChange && (
          <div className="space-y-2">
            {user.userStatus === "ACTIVE" ? (
              <Button
                onClick={() => onStatusChange("INACTIVE")}
                disabled={loading}
                variant="destructive"
                className="w-full"
              >
                <AlertTriangle className="mr-2 h-4 w-4" />
                Deactivate User
              </Button>
            ) : (
              <Button
                onClick={() => onStatusChange("ACTIVE")}
                disabled={loading}
                className="w-full bg-green-600 hover:bg-green-700"
              >
                <CheckCircle className="mr-2 h-4 w-4" />
                Activate User
              </Button>
            )}
          </div>
        )}
      </CardContent>
    </Card>
  );
};

export default UserStatusCard;
