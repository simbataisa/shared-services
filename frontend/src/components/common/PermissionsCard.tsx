import React, { useState } from "react";
import { Activity, ChevronDown, ChevronUp } from "lucide-react";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import type { Permission } from "@/types";

export interface PermissionsCardProps {
  permissions?: Permission[];
  title?: string;
  emptyMessage?: string;
  className?: string;
  showIcon?: boolean;
  badgeVariant?: "default" | "secondary" | "destructive" | "outline";
  gridCols?: {
    base?: number;
    md?: number;
    lg?: number;
  };
  defaultExpanded?: boolean;
}

export const PermissionsCard: React.FC<PermissionsCardProps> = ({
  permissions = [],
  title = "Permissions",
  emptyMessage = "No permissions assigned.",
  className = "",
  showIcon = true,
  badgeVariant = "outline",
  gridCols = { base: 1, md: 2, lg: 3 },
  defaultExpanded = true,
}) => {
  const [isExpanded, setIsExpanded] = useState(defaultExpanded);
  
  const gridClasses = `grid grid-cols-${gridCols.base} ${
    gridCols.md ? `md:grid-cols-${gridCols.md}` : ""
  } ${gridCols.lg ? `lg:grid-cols-${gridCols.lg}` : ""} gap-2`;

  const toggleExpanded = () => {
    setIsExpanded(!isExpanded);
  };

  return (
    <Card className={className}>
      <CardHeader 
        className="cursor-pointer hover:bg-gray-50 transition-colors"
        onClick={toggleExpanded}
      >
        <CardTitle className="flex items-center justify-between">
          <div className="flex items-center">
            {showIcon && <Activity className="mr-2 h-5 w-5" />}
            {title} ({permissions.length})
          </div>
          {isExpanded ? (
            <ChevronUp className="h-4 w-4 text-gray-500" />
          ) : (
            <ChevronDown className="h-4 w-4 text-gray-500" />
          )}
        </CardTitle>
      </CardHeader>
      {isExpanded && (
        <CardContent>
          {permissions.length > 0 ? (
            <div className={gridClasses}>
              {permissions.map((permission) => (
                <Badge
                  key={permission.id}
                  variant={badgeVariant}
                  className="justify-start"
                  title={permission.description}
                >
                  {permission.name}
                </Badge>
              ))}
            </div>
          ) : (
            <p className="text-sm text-gray-500">{emptyMessage}</p>
          )}
        </CardContent>
      )}
    </Card>
  );
};

export default PermissionsCard;