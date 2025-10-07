import React, { useState, useEffect } from "react";
import { useNavigate, useParams, Link } from "react-router-dom";
import {
  Building2,
  ArrowLeft,
  Edit,
  Users,
  Shield,
  Activity,
  CheckCircle,
  XCircle,
  Clock,
  Calendar,
  User,
  Trash2,
} from "lucide-react";
import {
  Breadcrumb,
  BreadcrumbItem,
  BreadcrumbLink,
  BreadcrumbList,
  BreadcrumbPage,
  BreadcrumbSeparator,
} from "@/components/ui/breadcrumb";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { Label } from "@/components/ui/label";
import { StatusBadge } from "@/components/StatusBadge";
import { PermissionGuard } from "@/components/PermissionGuard";
import { usePermissions } from "@/hooks/usePermissions";
import { normalizeEntityStatus, getStatusVariant } from "@/lib/status-colors";
import api from "@/lib/api";
import { type Tenant } from "@/store/auth";

interface TenantDetails extends Tenant {
  userCount?: number;
  roleCount?: number;
  lastActivity?: string;
  createdBy?: string;
  updatedBy?: string;
}

export default function TenantDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { canViewTenants, canDeleteTenants, canViewAuditLogs } =
    usePermissions();

  const [tenant, setTenant] = useState<TenantDetails | null>(null);
  const [loading, setLoading] = useState(true);
  const [updating, setUpdating] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Redirect if user doesn't have permission to view tenants
  useEffect(() => {
    if (!canViewTenants) {
      navigate("/unauthorized");
      return;
    }
  }, [canViewTenants, navigate]);

  useEffect(() => {
    if (id && canViewTenants) {
      fetchTenantDetails();
    }
  }, [id, canViewTenants]);

  const fetchTenantDetails = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await api.get(`/v1/tenants/${id}`);
      setTenant(response.data);
    } catch (error) {
      console.error("Failed to fetch tenant details:", error);
      setError("Failed to load tenant details");
    } finally {
      setLoading(false);
    }
  };

  const handleStatusChange = async (newStatus: string) => {
    if (!tenant) return;

    try {
      setUpdating(true);
      await api.patch(`/v1/tenants/${tenant.id}/status`, {
        status: newStatus,
      });
      setTenant((prev) =>
        prev ? { ...prev, status: newStatus as any } : null
      );
    } catch (error) {
      console.error("Failed to update tenant status:", error);
    } finally {
      setUpdating(false);
    }
  };

  const handleDeleteTenant = async () => {
    if (!tenant || !canDeleteTenants) return;

    if (
      !window.confirm(
        `Are you sure you want to delete the tenant "${tenant.name}"? This action cannot be undone.`
      )
    ) {
      return;
    }

    try {
      setUpdating(true);
      await api.delete(`/v1/tenants/${tenant.id}`);
      navigate("/tenants");
    } catch (error) {
      console.error("Failed to delete tenant:", error);
      setError("Failed to delete tenant");
      setUpdating(false);
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("en-US", {
      year: "numeric",
      month: "long",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case "ACTIVE":
        return <CheckCircle className="h-5 w-5 text-green-600" />;
      case "INACTIVE":
        return <XCircle className="h-5 w-5 text-red-600" />;
      case "SUSPENDED":
        return <Clock className="h-5 w-5 text-yellow-600" />;
      default:
        return <XCircle className="h-5 w-5 text-gray-600" />;
    }
  };

  const getTypeLabel = (type: string) => {
    switch (type) {
      case "ENTERPRISE":
        return "Enterprise";
      case "STANDARD":
        return "Standard";
      case "BASIC":
        return "Basic";
      default:
        return type;
    }
  };

  if (loading) {
    return (
      <div className="p-6">
        <div className="flex items-center gap-4">
          <Skeleton className="h-10 w-10" />
          <div className="space-y-2">
            <Skeleton className="h-8 w-48" />
            <Skeleton className="h-4 w-32" />
          </div>
        </div>
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mt-6">
          <div className="lg:col-span-2 space-y-6">
            <Card>
              <CardHeader>
                <Skeleton className="h-6 w-32" />
              </CardHeader>
              <CardContent>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  {[...Array(4)].map((_, i) => (
                    <div key={i} className="space-y-2">
                      <Skeleton className="h-4 w-20" />
                      <Skeleton className="h-8 w-full" />
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    );
  }

  if (error || !tenant) {
    return (
      <div className="p-6">
        <div className="text-center py-12">
          <Building2 className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
          <h3 className="text-lg font-semibold mb-2">
            {error || "Tenant not found"}
          </h3>
          <p className="text-muted-foreground mb-4">
            {error ||
              "The tenant you're looking for doesn't exist or has been removed."}
          </p>
          <Button onClick={() => navigate("/tenants")}>
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back to Tenants
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="p-6">
      {/* Header */}
        <div className="mb-8">
          <Breadcrumb className="mb-4">
            <BreadcrumbList>
              <BreadcrumbItem>
                <BreadcrumbLink asChild>
                  <Link to="/tenants">Tenants</Link>
                </BreadcrumbLink>
              </BreadcrumbItem>
              <BreadcrumbSeparator />
              <BreadcrumbItem>
                <BreadcrumbPage>{tenant.name}</BreadcrumbPage>
              </BreadcrumbItem>
            </BreadcrumbList>
          </Breadcrumb>

        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold">{tenant.name}</h1>
            <p className="text-muted-foreground">
              Tenant Code: {tenant.code}
            </p>
          </div>
          <div className="flex items-center gap-3">
            {getStatusIcon(tenant.status)}
            <PermissionGuard permission="tenants:update">
              <Button asChild size="sm">
                <Link to={`/tenants/${tenant.id}/edit`}>
                  <Edit className="h-4 w-4 mr-2" />
                  Edit
                </Link>
              </Button>
            </PermissionGuard>
            <PermissionGuard permission="tenants:delete">
              <Button
                variant="destructive"
                size="sm"
                onClick={handleDeleteTenant}
              >
                <Trash2 className="h-4 w-4 mr-2" />
                Delete
              </Button>
            </PermissionGuard>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column - Main Information */}
        <div className="lg:col-span-2 space-y-6">
          {/* Basic Information */}
          <Card>
            <CardHeader>
              <CardTitle>Basic Information</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="space-y-2">
                  <Label>Tenant Code</Label>
                  <div className="font-mono text-sm bg-muted px-3 py-2 rounded-md">
                    {tenant.code}
                  </div>
                </div>

                <div className="space-y-2">
                  <Label>Tenant Name</Label>
                  <div className="text-sm">{tenant.name}</div>
                </div>

                <div className="space-y-2">
                  <Label>Type</Label>
                  <div className="text-sm">{getTypeLabel(tenant.type)}</div>
                </div>

                <div className="space-y-2">
                  <Label>Status</Label>
                  <div className="flex items-center gap-2">
                    {getStatusIcon(tenant.status)}
                    <StatusBadge
                      status={normalizeEntityStatus("tenant", tenant.status)}
                    />
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Statistics */}
          <Card>
            <CardHeader>
              <CardTitle>Statistics</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                <div className="text-center">
                  <div className="bg-blue-100 p-3 rounded-full w-12 h-12 flex items-center justify-center mx-auto mb-2">
                    <Users className="h-6 w-6 text-blue-600" />
                  </div>
                  <p className="text-2xl font-bold">{tenant.userCount || 0}</p>
                  <p className="text-sm text-muted-foreground">Users</p>
                </div>

                <div className="text-center">
                  <div className="bg-purple-100 p-3 rounded-full w-12 h-12 flex items-center justify-center mx-auto mb-2">
                    <Shield className="h-6 w-6 text-purple-600" />
                  </div>
                  <p className="text-2xl font-bold">{tenant.roleCount || 0}</p>
                  <p className="text-sm text-muted-foreground">Roles</p>
                </div>

                <div className="text-center">
                  <div className="bg-green-100 p-3 rounded-full w-12 h-12 flex items-center justify-center mx-auto mb-2">
                    <Activity className="h-6 w-6 text-green-600" />
                  </div>
                  <p className="text-2xl font-bold">Active</p>
                  <p className="text-sm text-muted-foreground">Status</p>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Audit Information */}
          <PermissionGuard permission="audit:read">
            <Card>
              <CardHeader>
                <CardTitle>Audit Information</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div className="space-y-2">
                    <Label>Created At</Label>
                    <div className="flex items-center gap-2 text-sm text-muted-foreground">
                      <Calendar className="h-4 w-4" />
                      {tenant.createdAt ? formatDate(tenant.createdAt) : "N/A"}
                    </div>
                  </div>

                  <div className="space-y-2">
                    <Label>Updated At</Label>
                    <div className="flex items-center gap-2 text-sm text-muted-foreground">
                      <Calendar className="h-4 w-4" />
                      {tenant.updatedAt ? formatDate(tenant.updatedAt) : "N/A"}
                    </div>
                  </div>

                  <div className="space-y-2">
                    <Label>Created By</Label>
                    <div className="flex items-center gap-2 text-sm text-muted-foreground">
                      <User className="h-4 w-4" />
                      {tenant.createdBy || "System"}
                    </div>
                  </div>

                  <div className="space-y-2">
                    <Label>Updated By</Label>
                    <div className="flex items-center gap-2 text-sm text-muted-foreground">
                      <User className="h-4 w-4" />
                      {tenant.updatedBy || "System"}
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>
          </PermissionGuard>
        </div>

        {/* Right Column - Actions */}
        <div className="space-y-6">
          {/* Status Management */}
          <Card>
            <CardHeader>
              <CardTitle>Status Management</CardTitle>
              <CardDescription>
                Change the tenant's operational status
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-3">
              <PermissionGuard permission="tenants:activate">
                <Button
                  variant={tenant.status === "ACTIVE" ? "default" : "outline"}
                  className="w-full justify-start"
                  onClick={() => handleStatusChange("ACTIVE")}
                  disabled={updating || tenant.status === "ACTIVE"}
                >
                  <CheckCircle className="h-4 w-4 mr-2" />
                  Active
                </Button>
              </PermissionGuard>

              <PermissionGuard permission="tenants:suspend">
                <Button
                  variant={
                    tenant.status === "SUSPENDED" ? "secondary" : "outline"
                  }
                  className="w-full justify-start"
                  onClick={() => handleStatusChange("SUSPENDED")}
                  disabled={updating || tenant.status === "SUSPENDED"}
                >
                  <Clock className="h-4 w-4 mr-2" />
                  Suspended
                </Button>
              </PermissionGuard>

              <PermissionGuard permission="tenants:deactivate">
                <Button
                  variant={
                    tenant.status === "INACTIVE" ? "destructive" : "outline"
                  }
                  className="w-full justify-start"
                  onClick={() => handleStatusChange("INACTIVE")}
                  disabled={updating || tenant.status === "INACTIVE"}
                >
                  <XCircle className="h-4 w-4 mr-2" />
                  Inactive
                </Button>
              </PermissionGuard>
            </CardContent>
          </Card>

          {/* Quick Actions */}
          <Card>
            <CardHeader>
              <CardTitle>Quick Actions</CardTitle>
              <CardDescription>
                Manage tenant resources and view activity
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-3">
              <PermissionGuard permission="users:read">
                <Button
                  variant="outline"
                  className="w-full justify-start"
                  asChild
                >
                  <Link to={`/users?tenant=${tenant.id}`}>
                    <Users className="h-4 w-4 mr-2" />
                    View Users ({tenant.userCount || 0})
                  </Link>
                </Button>
              </PermissionGuard>

              <PermissionGuard permission="roles:read">
                <Button
                  variant="outline"
                  className="w-full justify-start"
                  asChild
                >
                  <Link to={`/roles?tenant=${tenant.id}`}>
                    <Shield className="h-4 w-4 mr-2" />
                    View Roles ({tenant.roleCount || 0})
                  </Link>
                </Button>
              </PermissionGuard>

              <PermissionGuard permission="audit:read">
                <Button
                  variant="outline"
                  className="w-full justify-start"
                  asChild
                >
                  <Link to={`/activity?tenant=${tenant.id}`}>
                    <Activity className="h-4 w-4 mr-2" />
                    View Activity
                  </Link>
                </Button>
              </PermissionGuard>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
