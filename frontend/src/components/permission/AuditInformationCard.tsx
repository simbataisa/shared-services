import React from "react";
import { Clock, User } from "lucide-react";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Label } from "@/components/ui/label";
import type { Permission } from "@/types";

interface AuditInformationCardProps {
  permission: Permission;
}

export const AuditInformationCard: React.FC<AuditInformationCardProps> = ({
  permission,
}) => {
  const formatDateTime = (dateString: string | null | undefined) => {
    if (!dateString) return "N/A";
    
    try {
      const date = new Date(dateString);
      return date.toLocaleString("en-US", {
        year: "numeric",
        month: "short",
        day: "numeric",
        hour: "2-digit",
        minute: "2-digit",
        second: "2-digit",
      });
    } catch (error) {
      return "Invalid Date";
    }
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center">
          <Clock className="mr-2 h-5 w-5" />
          Audit Information
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <Label className="text-sm font-medium text-gray-500">
              Created At
            </Label>
            <div className="text-sm mt-1">
              {formatDateTime(permission.createdAt)}
            </div>
          </div>
          
          <div>
            <Label className="text-sm font-medium text-gray-500">
              Updated At
            </Label>
            <div className="text-sm mt-1">
              {formatDateTime(permission.updatedAt)}
            </div>
          </div>
        </div>

        <div className="border-t pt-4">
          <div className="text-sm text-muted-foreground">
            <div className="flex items-center">
              <User className="mr-1 h-3 w-3" />
              Audit trail information is tracked by the system
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  );
};