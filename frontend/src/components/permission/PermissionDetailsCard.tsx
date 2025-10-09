import React from "react";
import { User, Shield } from "lucide-react";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import type { Permission } from "@/types";

interface PermissionDetailsCardProps {
  permission: Permission;
}

export const PermissionDetailsCard: React.FC<PermissionDetailsCardProps> = ({
  permission,
}) => {
  const parsePermissionName = (name: string) => {
    if (!name || typeof name !== "string") {
      return { resource: "unknown", action: "unknown" };
    }

    const parts = name.split(":");
    if (parts.length >= 2) {
      return {
        resource: parts[0],
        action: parts[1],
      };
    }

    return { resource: name, action: "unknown" };
  };

  const { resource, action } = parsePermissionName(permission.name);

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center">
          <User className="mr-2 h-5 w-5" />
          Permission Details
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        <div>
          <Label className="text-sm font-medium text-gray-500">Resource</Label>
          <div className="mt-1">
            <Badge variant="outline" className="font-mono">
              {resource}
            </Badge>
          </div>
        </div>
        
        <div>
          <Label className="text-sm font-medium text-gray-500">Action</Label>
          <div className="mt-1">
            <Badge variant="secondary" className="font-mono">
              {action}
            </Badge>
          </div>
        </div>
        
        <div>
          <Label className="text-sm font-medium text-gray-500">
            Permission ID
          </Label>
          <div className="font-mono text-sm bg-gray-50 p-2 rounded mt-1">
            {permission.id}
          </div>
        </div>
      </CardContent>
    </Card>
  );
};