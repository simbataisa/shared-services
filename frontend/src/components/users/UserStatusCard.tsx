import React from "react";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { StatusBadge } from "../StatusBadge";
import { normalizeEntityStatus } from "@/lib/status-colors";
import { AlertTriangle, CheckCircle } from "lucide-react";
import type { User } from "./types";

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
        <div className="border border-gray-200 rounded-lg p-4">
          <div className="flex items-center justify-between">
            <div>
              <h4 className="font-medium text-gray-900">Current Status</h4>
              <p className="text-sm text-gray-600">
                User is currently {user.userStatus?.toLowerCase() || 'unknown'}
              </p>
            </div>
            <StatusBadge
              status={normalizeEntityStatus("user", user.userStatus)}
            />
          </div>
        </div>

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