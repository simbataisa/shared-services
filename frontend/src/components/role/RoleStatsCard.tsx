import React from "react";
import { StatisticsCard, type StatisticItem } from "@/components/common";
import type { RoleStats } from "@/types";
import { getStatusIcon } from "@/lib/status-utils";

interface RoleStatsCardProps {
  stats: RoleStats | null;
}

export const RoleStatsCard: React.FC<RoleStatsCardProps> = ({ stats }) => {
  if (!stats) {
    return null;
  }

  const statisticsData: StatisticItem[] = [
    {
      label: "Total Permissions",
      value: stats.totalPermissions,
    },
    {
      label: "Users with Role",
      value: stats.totalUsers,
    },
    {
      label: "User Groups",
      value: stats.totalUserGroups,
    },
    {
      label: "Status",
      value: stats.roleStatus,
      icon: getStatusIcon(stats.roleStatus),
    },
  ];

  if (stats.lastModified) {
    statisticsData.push({
      label: "Last Modified",
      value: new Date(stats.lastModified).toLocaleString("en-US", {
        year: "numeric",
        month: "long",
        day: "numeric",
        hour: "2-digit",
        minute: "2-digit",
      }),
    });
  }

  return (
    <StatisticsCard
      title="Statistics"
      layout="list"
      statistics={statisticsData}
    />
  );
};
