import React from "react";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Calendar, Clock, User } from "lucide-react";
import type { TenantDetail as TenantDetailType } from "@/types/tenant";

interface TenantAuditInfoCardProps {
  tenant: TenantDetailType;
}

const TenantAuditInfoCard: React.FC<TenantAuditInfoCardProps> = ({
  tenant,
}) => {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Audit Information</CardTitle>
        <CardDescription>
          Track creation and modification history
        </CardDescription>
      </CardHeader>
      <CardContent>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div className="space-y-4">
            <div className="flex items-center gap-2">
              <Calendar className="h-4 w-4 text-muted-foreground" />
              <span className="text-sm font-medium">Created</span>
            </div>
            <div className="ml-6 space-y-1">
              <div className="text-sm">
                {new Date(tenant.createdAt).toLocaleDateString("en-US", {
                  year: "numeric",
                  month: "long",
                  day: "numeric",
                  hour: "2-digit",
                  minute: "2-digit",
                })}
              </div>
              {tenant.createdBy && (
                <div className="flex items-center gap-1 text-xs text-muted-foreground">
                  <User className="h-3 w-3" />
                  {tenant.createdBy}
                </div>
              )}
            </div>
          </div>

          <div className="space-y-4">
            <div className="flex items-center gap-2">
              <Clock className="h-4 w-4 text-muted-foreground" />
              <span className="text-sm font-medium">Last Updated</span>
            </div>
            <div className="ml-6 space-y-1">
              <div className="text-sm">
                {new Date(tenant.updatedAt).toLocaleDateString("en-US", {
                  year: "numeric",
                  month: "long",
                  day: "numeric",
                  hour: "2-digit",
                  minute: "2-digit",
                })}
              </div>
              {tenant.updatedBy && (
                <div className="flex items-center gap-1 text-xs text-muted-foreground">
                  <User className="h-3 w-3" />
                  {tenant.updatedBy}
                </div>
              )}
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  );
};

export default TenantAuditInfoCard;