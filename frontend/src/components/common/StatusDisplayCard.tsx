import React from "react";
import { getStatusIcon, getStatusColor } from "@/lib/status-utils";
import type { RoleStatus } from "@/types";

interface StatusDisplayCardProps {
  title: string;
  description: string;
  status: RoleStatus;
  className?: string;
}

const StatusDisplayCard: React.FC<StatusDisplayCardProps> = ({
  title,
  description,
  status,
  className = "",
}) => {
  return (
    <div className={`border border-gray-200 rounded-lg p-4 ${className}`}>
      <div className="flex items-center justify-between">
        <div>
          <h4 className="font-medium text-gray-900">{title}</h4>
          <p className="text-sm text-gray-600">{description}</p>
        </div>
        <div
          className={`flex items-center gap-2 px-3 py-1 rounded-full border text-sm font-medium ${getStatusColor(
            status
          )}`}
        >
          {getStatusIcon(status)}
          {status}
        </div>
      </div>
    </div>
  );
};

export default StatusDisplayCard;
