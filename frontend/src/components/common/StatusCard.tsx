import React from "react";
import { getStatusColor, getStatusIcon } from "@/lib/status-utils";

interface StatusCardProps {
  label: string;
  status: string;
  className?: string;
}

/**
 * Reusable Status Card Component
 * 
 * Displays a status with label, icon, and appropriate styling.
 * Can be used across different components for consistent status display.
 */
const StatusCard: React.FC<StatusCardProps> = ({ 
  label, 
  status, 
  className = "" 
}) => {
  return (
    <div className={className}>
      <label className="block text-sm font-medium text-gray-700">
        {label}
      </label>
      <div
        className={`inline-flex items-center gap-2 px-3 py-1 rounded-full border text-sm font-medium ${getStatusColor(
          status
        )}`}
      >
        {getStatusIcon(status)}
        {status}
      </div>
    </div>
  );
};

export default StatusCard;