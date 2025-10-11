import React from "react";
import { getStatusIcon, getStatusColor } from "@/lib/status-utils";
import type { RoleStatus, ProductStatus } from "@/types";

interface StatusDisplayCardProps {
  title: string;
  description: string;
  status: RoleStatus | ProductStatus;
  className?: string;
  actions?: React.ReactNode;
}

const StatusDisplayCard: React.FC<StatusDisplayCardProps> = ({
  title,
  description,
  status,
  className = "",
  actions,
}) => {
  return (
    <div className={`border border-gray-200 rounded-lg p-4 ${className}`}>
      <div className="flex items-center justify-between">
        <div>
          <h4 className="font-medium text-gray-900">{title}</h4>
          <p className="text-sm text-gray-600">{description}</p>
        </div>
        <div className="flex items-center gap-3">
          <div
            className={`flex items-center gap-2 px-3 py-1 rounded-full border text-sm font-medium ${getStatusColor(
              status
            )}`}
          >
            {getStatusIcon(status)}
            {status}
          </div>
          {actions && <div className="flex items-center gap-2">{actions}</div>}
        </div>
      </div>
    </div>
  );
};

export default StatusDisplayCard;
