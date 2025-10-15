import React from "react";
import { Badge } from "@/components/ui/badge";
import {
  getStatusColor,
  getStatusConfig,
  getStatusIcon,
  type StatusType,
} from "@/lib/status-utils";
import { cn } from "@/lib/utils";

interface StatusBadgeProps {
  status: string;
  className?: string;
  showIcon?: boolean;
  customLabel?: string;
}

/**
 * Standardized Status Badge Component
 *
 * This component provides a consistent way to display status badges
 * across the application using the centralized status color system.
 */
export function StatusBadge({
  status,
  className,
  showIcon = true,
  customLabel,
}: StatusBadgeProps) {
  const config = getStatusConfig(status);
  return (
    <Badge
      variant={config.variant}
      className={cn(
        "inline-flex items-center gap-1.5",
        config.className,
        className
      )}
    >
      {showIcon && getStatusIcon(status)}
      {customLabel || config.label}
    </Badge>
  );
}

/**
 * Status Badge with Icon Component
 *
 * Enhanced version that includes status-appropriate icons
 */
interface StatusBadgeWithIconProps extends StatusBadgeProps {
  icon?: React.ReactNode;
}

export function StatusBadgeWithIcon({
  status,
  className,
  customLabel,
  icon,
}: StatusBadgeWithIconProps) {
  const config = getStatusConfig(status);

  return (
    <Badge
      variant={config.variant}
      className={cn(config.className, "flex items-center gap-1", className)}
    >
      {icon && <span className="w-3 h-3">{icon}</span>}
      {customLabel || config.label}
    </Badge>
  );
}

export default StatusBadge;
