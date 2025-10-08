import React from "react";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { StatusBadge } from "@/components/common/StatusBadge";
import { normalizeEntityStatus } from "@/lib/status-colors";
import type { RoleStats } from "@/types";

interface RoleStatsCardProps {
  stats: RoleStats | null;
}

export const RoleStatsCard: React.FC<RoleStatsCardProps> = ({ stats }) => {
  if (!stats) {
    return null;
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-lg font-semibold text-gray-900">
          Statistics
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="flex justify-between">
          <span className="text-sm text-gray-600">
            Total Permissions
          </span>
          <span className="text-sm font-medium text-gray-900">
            {stats.totalPermissions}
          </span>
        </div>

        <div className="flex justify-between">
          <span className="text-sm text-gray-600">
            Users with Role
          </span>
          <span className="text-sm font-medium text-gray-900">
            {stats.totalUsers}
          </span>
        </div>

        <div className="flex justify-between">
          <span className="text-sm text-gray-600">User Groups</span>
          <span className="text-sm font-medium text-gray-900">
            {stats.totalUserGroups}
          </span>
        </div>

        <div className="flex justify-between">
          <span className="text-sm text-gray-600">Status</span>
          <StatusBadge
            status={normalizeEntityStatus("role", stats.status)}
          />
        </div>

        {stats.lastModified && (
          <div className="pt-4 border-t border-gray-200">
            <span className="text-sm text-gray-600">
              Last Modified
            </span>
            <p className="text-sm font-medium text-gray-900">
              {new Date(stats.lastModified).toLocaleDateString(
                "en-US",
                {
                  year: "numeric",
                  month: "long",
                  day: "numeric",
                  hour: "2-digit",
                  minute: "2-digit",
                }
              )}
            </p>
          </div>
        )}
      </CardContent>
    </Card>
  );
};