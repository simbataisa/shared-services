import React from "react";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from "@/components/ui/alert-dialog";
import {
  CheckCircle,
  XCircle,
  AlertCircle,
  Trash2,
  Shield,
} from "lucide-react";
import { PermissionGuard } from "@/components/common/PermissionGuard";
import StatusDisplayCard from "@/components/common/StatusDisplayCard";
import type {
  TenantStatus,
  TenantDetail as TenantDetailType,
} from "@/types/tenant";
import { getStatusIcon } from "@/lib/status-utils";

interface TenantStatusCardProps {
  tenant: TenantDetailType;
  onStatusUpdate: (status: TenantStatus) => Promise<void>;
  onDelete: () => Promise<void>;
  updating?: boolean;
  className?: string;
}

const TenantStatusCard: React.FC<TenantStatusCardProps> = ({
  tenant,
  onStatusUpdate,
  onDelete,
  updating = false,
  className = "",
}) => {
  return (
    <Card className={className}>
      <CardHeader>
        <CardTitle className="flex items-center text-lg font-semibold text-gray-900">
          <Shield className="mr-2 h-5 w-5" />
          Tenant Status Management
        </CardTitle>
        <CardDescription>
          Manage tenant status and perform administrative actions
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-6">
        {/* Current Status Display */}
        <StatusDisplayCard
          title="Current Status"
          description={`Tenant is currently ${tenant.status.toLowerCase()}`}
          status={tenant.status}
        />

        {/* Status Change Actions */}
        <PermissionGuard permission="TENANT_MGMT:update">
          <div className="space-y-3">
            <Label className="text-sm font-medium text-gray-900">
              Change Status
            </Label>
            <div className="grid grid-cols-1 gap-2">
              {(["ACTIVE", "INACTIVE", "SUSPENDED"] as TenantStatus[]).map(
                (status) => (
                  <Button
                    key={status}
                    variant={tenant.status === status ? "default" : "outline"}
                    size="sm"
                    onClick={() => onStatusUpdate(status)}
                    disabled={updating || tenant.status === status}
                    className="w-full justify-start"
                  >
                    {getStatusIcon(status)}
                    <span className="ml-2">{status}</span>
                  </Button>
                )
              )}
            </div>
          </div>
        </PermissionGuard>

        {/* Danger Zone */}
        <PermissionGuard permission="TENANT_MGMT:delete">
          <div className="pt-4 border-t border-red-200">
            <div className="space-y-3">
              <Label className="text-red-600 font-medium">Danger Zone</Label>
              <div className="border border-red-200 rounded-lg p-4 bg-red-50">
                <div className="flex items-center justify-between">
                  <div>
                    <h4 className="font-medium text-red-900">Delete Tenant</h4>
                    <p className="text-sm text-red-700">
                      Permanently remove this tenant and all associated data
                    </p>
                  </div>
                  <AlertDialog>
                    <AlertDialogTrigger asChild>
                      <Button
                        variant="destructive"
                        size="sm"
                        disabled={updating}
                        className="ml-4"
                      >
                        <Trash2 className="h-4 w-4 mr-2" />
                        Delete
                      </Button>
                    </AlertDialogTrigger>
                    <AlertDialogContent>
                      <AlertDialogHeader>
                        <AlertDialogTitle>Delete Tenant</AlertDialogTitle>
                        <AlertDialogDescription>
                          Are you sure you want to delete "{tenant.name}"? This
                          action cannot be undone and will permanently remove
                          all associated data.
                        </AlertDialogDescription>
                      </AlertDialogHeader>
                      <AlertDialogFooter>
                        <AlertDialogCancel>Cancel</AlertDialogCancel>
                        <AlertDialogAction
                          onClick={onDelete}
                          className="bg-red-600 hover:bg-red-700"
                        >
                          Delete Tenant
                        </AlertDialogAction>
                      </AlertDialogFooter>
                    </AlertDialogContent>
                  </AlertDialog>
                </div>
              </div>
            </div>
          </div>
        </PermissionGuard>
      </CardContent>
    </Card>
  );
};

export default TenantStatusCard;
