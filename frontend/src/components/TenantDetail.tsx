import { useState, useEffect } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
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
} from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Skeleton } from "@/components/ui/skeleton";
import { Label } from "@/components/ui/label";
import { StatusBadge } from "./StatusBadge";
import { PermissionGuard } from "../components/PermissionGuard";
import { normalizeEntityStatus } from "../lib/status-colors";
import api from "../lib/api";
import { type Tenant } from "../store/auth";

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
  const [tenant, setTenant] = useState<TenantDetails | null>(null);
  const [loading, setLoading] = useState(true);
  const [updating, setUpdating] = useState(false);

  useEffect(() => {
    if (id) {
      fetchTenantDetails();
    }
  }, [id]);

  const fetchTenantDetails = async () => {
    try {
      setLoading(true);
      const response = await api.get(`/v1/tenants/${id}`);
      // Fix: Access the actual tenant data from the ApiResponse wrapper
      setTenant(response.data.data || response.data);
    } catch (error) {
      console.error("Failed to fetch tenant details:", error);
      navigate("/tenants");
    } finally {
      setLoading(false);
    }
  };

  const handleStatusChange = async (newStatus: string) => {
    if (!tenant) return;

    try {
      setUpdating(true);
      await api.patch(`/v1/tenants/${tenant.id}/status`, { status: newStatus });
      setTenant((prev) =>
        prev ? { ...prev, status: newStatus as any } : null
      );
    } catch (error) {
      console.error("Failed to update tenant status:", error);
    } finally {
      setUpdating(false);
    }
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

  const getStatusVariant = (status: string) => {
    switch (status) {
      case "ACTIVE":
        return "default";
      case "INACTIVE":
        return "destructive";
      case "SUSPENDED":
        return "secondary";
      default:
        return "outline";
    }
  };

  const getTypeLabel = (type: string) => {
    switch (type) {
      case "BUSINESS_IN":
        return "Business Internal";
      case "BUSINESS_OUT":
        return "Business External";
      case "INDIVIDUAL":
        return "Individual";
      default:
        return type;
    }
  };

  if (loading) {
    return (
      <div className="p-6 space-y-6">
        <div className="flex items-center gap-4">
          <Skeleton className="h-10 w-10 rounded-lg" />
          <div className="space-y-2">
            <Skeleton className="h-8 w-48" />
            <Skeleton className="h-4 w-32" />
          </div>
        </div>
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <div className="lg:col-span-2 space-y-6">
            <Card>
              <CardHeader>
                <Skeleton className="h-6 w-40" />
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  {[...Array(4)].map((_, i) => (
                    <div key={i} className="space-y-2">
                      <Skeleton className="h-4 w-24" />
                      <Skeleton className="h-6 w-full" />
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          </div>
          <div className="space-y-6">
            <Card>
              <CardHeader>
                <Skeleton className="h-6 w-32" />
              </CardHeader>
              <CardContent className="space-y-3">
                {[...Array(3)].map((_, i) => (
                  <Skeleton key={i} className="h-10 w-full" />
                ))}
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    );
  }

  if (!tenant) {
    return (
      <div className="p-6">
        <Card className="max-w-md mx-auto">
          <CardContent className="pt-6 text-center">
            <Building2 className="mx-auto h-12 w-12 text-muted-foreground mb-4" />
            <CardTitle className="mb-2">Tenant not found</CardTitle>
            <CardDescription className="mb-6">
              The tenant you're looking for doesn't exist or you don't have
              permission to view it.
            </CardDescription>
            <Button onClick={() => navigate("/tenants")}>
              Back to Tenants
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="p-6">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-4">
          <Button
            variant="ghost"
            size="icon"
            onClick={() => navigate("/tenants")}
          >
            <ArrowLeft className="h-5 w-5" />
          </Button>
          <div>
            <h1 className="text-2xl font-bold">{tenant.name}</h1>
            <p className="text-muted-foreground">{tenant.code}</p>
          </div>
        </div>

        <div className="flex items-center gap-3">
          <PermissionGuard permission="tenants:update">
            <Button asChild>
              <Link to={`/tenants/${tenant.id}/edit`}>
                <Edit className="h-4 w-4 mr-2" />
                Edit Tenant
              </Link>
            </Button>
          </PermissionGuard>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Main Info */}
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
                      status={normalizeEntityStatus('tenant', tenant.status)}
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
                  <p className="text-2xl font-bold">
                    {tenant.userCount || 0}
                  </p>
                  <p className="text-sm text-muted-foreground">Users</p>
                </div>

                <div className="text-center">
                  <div className="bg-purple-100 p-3 rounded-full w-12 h-12 flex items-center justify-center mx-auto mb-2">
                    <Shield className="h-6 w-6 text-purple-600" />
                  </div>
                  <p className="text-2xl font-bold">
                    {tenant.roleCount || 0}
                  </p>
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
                    {tenant.createdAt
                      ? new Date(tenant.createdAt).toLocaleDateString()
                      : "N/A"}
                  </div>
                </div>

                <div className="space-y-2">
                  <Label>Updated At</Label>
                  <div className="flex items-center gap-2 text-sm text-muted-foreground">
                    <Calendar className="h-4 w-4" />
                    {tenant.updatedAt
                      ? new Date(tenant.updatedAt).toLocaleDateString()
                      : "N/A"}
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
        </div>

        {/* Sidebar */}
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
                  variant={tenant.status === "SUSPENDED" ? "secondary" : "outline"}
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
                  variant={tenant.status === "INACTIVE" ? "destructive" : "outline"}
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
