import React, { useState } from "react";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Badge } from "@/components/ui/badge";
import {
  Building2,
  Edit,
  Save,
  X,
} from "lucide-react";
import { PermissionGuard } from "@/components/common/PermissionGuard";
import type { TenantType, TenantUpdateData, TenantDetail as TenantDetailType } from "@/types/tenant";

interface TenantBasicInfoCardProps {
  tenant: TenantDetailType;
  onUpdate: (data: TenantUpdateData) => Promise<void>;
  updating: boolean;
}

const TenantBasicInfoCard: React.FC<TenantBasicInfoCardProps> = ({
  tenant,
  onUpdate,
  updating,
}) => {
  const [isEditing, setIsEditing] = useState(false);
  const [editForm, setEditForm] = useState({
    code: tenant.code,
    name: tenant.name,
    type: tenant.type,
  });
  const [errors, setErrors] = useState<Record<string, string>>({});

  const validateForm = () => {
    const newErrors: Record<string, string> = {};

    if (!editForm.code.trim()) {
      newErrors.code = "Tenant code is required";
    }

    if (!editForm.name.trim()) {
      newErrors.name = "Tenant name is required";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSave = async () => {
    if (!validateForm()) return;

    try {
      await onUpdate({
        code: editForm.code,
        name: editForm.name,
        type: editForm.type,
      });
      setIsEditing(false);
    } catch (error) {
      console.error("Error updating tenant:", error);
    }
  };

  const handleCancel = () => {
    setEditForm({
      code: tenant.code,
      name: tenant.name,
      type: tenant.type,
    });
    setErrors({});
    setIsEditing(false);
  };

  const getTypeLabel = (type: TenantType) => {
    switch (type) {
      case "BUSINESS_IN":
        return "Business In";
      case "BUSINESS_OUT":
        return "Business Out";
      case "INDIVIDUAL":
        return "Individual";
      default:
        return type;
    }
  };

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <div>
            <CardTitle>Basic Information</CardTitle>
            <CardDescription>
              Core tenant details and configuration
            </CardDescription>
          </div>
          <PermissionGuard permission="TENANT_MGMT:update">
            {!isEditing ? (
              <Button
                variant="outline"
                size="sm"
                onClick={() => setIsEditing(true)}
                disabled={updating}
              >
                <Edit className="h-4 w-4 mr-2" />
                Edit
              </Button>
            ) : (
              <div className="flex gap-2">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={handleCancel}
                  disabled={updating}
                >
                  <X className="h-4 w-4 mr-2" />
                  Cancel
                </Button>
                <Button
                  size="sm"
                  onClick={handleSave}
                  disabled={updating}
                >
                  <Save className="h-4 w-4 mr-2" />
                  Save
                </Button>
              </div>
            )}
          </PermissionGuard>
        </div>
      </CardHeader>
      <CardContent>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div className="space-y-2">
            <Label htmlFor="code">Tenant Code</Label>
            {isEditing ? (
              <div>
                <Input
                  id="code"
                  value={editForm.code}
                  onChange={(e) =>
                    setEditForm((prev) => ({ ...prev, code: e.target.value }))
                  }
                  className={errors.code ? "border-red-500" : ""}
                />
                {errors.code && (
                  <p className="text-sm text-red-500 mt-1">{errors.code}</p>
                )}
              </div>
            ) : (
              <div className="flex items-center gap-2">
                <span className="font-mono text-sm bg-muted px-2 py-1 rounded">
                  {tenant.code}
                </span>
              </div>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="name">Tenant Name</Label>
            {isEditing ? (
              <div>
                <Input
                  id="name"
                  value={editForm.name}
                  onChange={(e) =>
                    setEditForm((prev) => ({ ...prev, name: e.target.value }))
                  }
                  className={errors.name ? "border-red-500" : ""}
                />
                {errors.name && (
                  <p className="text-sm text-red-500 mt-1">{errors.name}</p>
                )}
              </div>
            ) : (
              <div className="flex items-center gap-2">
                <Building2 className="h-4 w-4 text-muted-foreground" />
                <span>{tenant.name}</span>
              </div>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="type">Tenant Type</Label>
            {isEditing ? (
              <Select
                value={editForm.type}
                onValueChange={(value: TenantType) =>
                  setEditForm((prev) => ({ ...prev, type: value }))
                }
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="BUSINESS_IN">Business In</SelectItem>
                  <SelectItem value="BUSINESS_OUT">Business Out</SelectItem>
                  <SelectItem value="INDIVIDUAL">Individual</SelectItem>
                </SelectContent>
              </Select>
            ) : (
              <div className="flex items-center gap-2">
                <Badge variant="secondary">{getTypeLabel(tenant.type)}</Badge>
              </div>
            )}
          </div>

          {tenant.organizationId && (
            <div className="space-y-2">
              <Label>Organization ID</Label>
              <div className="flex items-center gap-2">
                <span className="font-mono text-sm">{tenant.organizationId}</span>
              </div>
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  );
};

export default TenantBasicInfoCard;