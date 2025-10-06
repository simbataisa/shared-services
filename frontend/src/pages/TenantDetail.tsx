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
import { PermissionGuard } from "../components/PermissionGuard";
import api from "../lib/api";
import { type Tenant } from "../store/auth";

interface TenantDetails extends Tenant {
  userCount?: number;
  roleCount?: number;
  lastActivity?: string;
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
      const response = await api.get(`/tenants/${id}`);
      setTenant(response.data);
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
      await api.patch(`/tenants/${tenant.id}/status`, { status: newStatus });
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

  const getStatusColor = (status: string) => {
    switch (status) {
      case "ACTIVE":
        return "bg-green-100 text-green-800";
      case "INACTIVE":
        return "bg-red-100 text-red-800";
      case "SUSPENDED":
        return "bg-yellow-100 text-yellow-800";
      default:
        return "bg-gray-100 text-gray-800";
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
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (!tenant) {
    return (
      <div className="p-6">
        <div className="text-center">
          <Building2 className="mx-auto h-12 w-12 text-gray-400" />
          <h3 className="mt-2 text-sm font-medium text-gray-900">
            Tenant not found
          </h3>
          <p className="mt-1 text-sm text-gray-500">
            The tenant you're looking for doesn't exist or you don't have
            permission to view it.
          </p>
          <div className="mt-6">
            <button
              onClick={() => navigate("/tenants")}
              className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg"
            >
              Back to Tenants
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="p-6">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-4">
          <button
            onClick={() => navigate("/tenants")}
            className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
          >
            <ArrowLeft className="h-5 w-5" />
          </button>
          <div>
            <h1 className="text-2xl font-bold text-gray-900">{tenant.name}</h1>
            <p className="text-gray-600">{tenant.code}</p>
          </div>
        </div>

        <div className="flex items-center gap-3">
          <PermissionGuard permission="tenants:update">
            <Link
              to={`/tenants/${tenant.id}/edit`}
              className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg flex items-center gap-2 transition-colors"
            >
              <Edit className="h-4 w-4" />
              Edit Tenant
            </Link>
          </PermissionGuard>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Main Info */}
        <div className="lg:col-span-2 space-y-6">
          {/* Basic Information */}
          <div className="bg-white rounded-lg shadow-sm border p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">
              Basic Information
            </h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Tenant Code
                </label>
                <p className="text-sm text-gray-900 font-mono bg-gray-50 px-3 py-2 rounded">
                  {tenant.code}
                </p>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Tenant Name
                </label>
                <p className="text-sm text-gray-900">{tenant.name}</p>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Type
                </label>
                <p className="text-sm text-gray-900">
                  {getTypeLabel(tenant.type)}
                </p>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Status
                </label>
                <div className="flex items-center">
                  {getStatusIcon(tenant.status)}
                  <span
                    className={`ml-2 px-2 py-1 text-xs font-medium rounded-full ${getStatusColor(
                      tenant.status
                    )}`}
                  >
                    {tenant.status}
                  </span>
                </div>
              </div>
            </div>
          </div>

          {/* Statistics */}
          <div className="bg-white rounded-lg shadow-sm border p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">
              Statistics
            </h2>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              <div className="text-center">
                <div className="bg-blue-100 p-3 rounded-full w-12 h-12 flex items-center justify-center mx-auto mb-2">
                  <Users className="h-6 w-6 text-blue-600" />
                </div>
                <p className="text-2xl font-bold text-gray-900">
                  {tenant.userCount || 0}
                </p>
                <p className="text-sm text-gray-600">Users</p>
              </div>

              <div className="text-center">
                <div className="bg-purple-100 p-3 rounded-full w-12 h-12 flex items-center justify-center mx-auto mb-2">
                  <Shield className="h-6 w-6 text-purple-600" />
                </div>
                <p className="text-2xl font-bold text-gray-900">
                  {tenant.roleCount || 0}
                </p>
                <p className="text-sm text-gray-600">Roles</p>
              </div>

              <div className="text-center">
                <div className="bg-green-100 p-3 rounded-full w-12 h-12 flex items-center justify-center mx-auto mb-2">
                  <Activity className="h-6 w-6 text-green-600" />
                </div>
                <p className="text-sm font-bold text-gray-900">
                  {tenant.lastActivity
                    ? new Date(tenant.lastActivity).toLocaleDateString()
                    : "Never"}
                </p>
                <p className="text-sm text-gray-600">Last Activity</p>
              </div>
            </div>
          </div>

          {/* Audit Information */}
          {(tenant.createdAt || tenant.updatedAt) && (
            <div className="bg-white rounded-lg shadow-sm border p-6">
              <h2 className="text-lg font-semibold text-gray-900 mb-4">
                Audit Information
              </h2>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {tenant.createdAt && (
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Created At
                    </label>
                    <div className="flex items-center text-sm text-gray-900">
                      <Calendar className="h-4 w-4 mr-2" />
                      {new Date(tenant.createdAt).toLocaleString()}
                    </div>
                  </div>
                )}

                {tenant.updatedAt && (
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Last Updated
                    </label>
                    <div className="flex items-center text-sm text-gray-900">
                      <Calendar className="h-4 w-4 mr-2" />
                      {new Date(tenant.updatedAt).toLocaleString()}
                    </div>
                  </div>
                )}
              </div>
            </div>
          )}
        </div>

        {/* Sidebar */}
        <div className="space-y-6">
          {/* Status Management */}
          <PermissionGuard permission="tenants:update">
            <div className="bg-white rounded-lg shadow-sm border p-6">
              <h3 className="text-lg font-semibold text-gray-900 mb-4">
                Status Management
              </h3>
              <div className="space-y-3">
                <button
                  onClick={() => handleStatusChange("ACTIVE")}
                  disabled={updating || tenant.status === "active"}
                  className="w-full text-left p-3 rounded-lg border border-gray-200 hover:bg-green-50 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  <div className="flex items-center">
                    <CheckCircle className="h-4 w-4 text-green-600 mr-3" />
                    <span className="text-sm font-medium">Set Active</span>
                  </div>
                </button>

                <button
                  onClick={() => handleStatusChange("SUSPENDED")}
                  disabled={updating || tenant.status === "suspended"}
                  className="w-full text-left p-3 rounded-lg border border-gray-200 hover:bg-yellow-50 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  <div className="flex items-center">
                    <Clock className="h-4 w-4 text-yellow-600 mr-3" />
                    <span className="text-sm font-medium">Suspend</span>
                  </div>
                </button>

                <button
                  onClick={() => handleStatusChange("INACTIVE")}
                  disabled={updating || tenant.status === "inactive"}
                  className="w-full text-left p-3 rounded-lg border border-gray-200 hover:bg-red-50 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  <div className="flex items-center">
                    <XCircle className="h-4 w-4 text-red-600 mr-3" />
                    <span className="text-sm font-medium">Set Inactive</span>
                  </div>
                </button>
              </div>
            </div>
          </PermissionGuard>

          {/* Quick Actions */}
          <div className="bg-white rounded-lg shadow-sm border p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">
              Quick Actions
            </h3>
            <div className="space-y-3">
              <PermissionGuard permission="user:read">
                <Link
                  to={`/users?tenant=${tenant.id}`}
                  className="w-full text-left p-3 rounded-lg border border-gray-200 hover:bg-gray-50 transition-colors block"
                >
                  <div className="flex items-center">
                    <Users className="h-4 w-4 text-blue-600 mr-3" />
                    <span className="text-sm font-medium">View Users</span>
                  </div>
                </Link>
              </PermissionGuard>

              <PermissionGuard permission="role:read">
                <Link
                  to={`/roles?tenant=${tenant.id}`}
                  className="w-full text-left p-3 rounded-lg border border-gray-200 hover:bg-gray-50 transition-colors block"
                >
                  <div className="flex items-center">
                    <Shield className="h-4 w-4 text-purple-600 mr-3" />
                    <span className="text-sm font-medium">View Roles</span>
                  </div>
                </Link>
              </PermissionGuard>

              <PermissionGuard permission="audit:read">
                <Link
                  to={`/audit?tenant=${tenant.id}`}
                  className="w-full text-left p-3 rounded-lg border border-gray-200 hover:bg-gray-50 transition-colors block"
                >
                  <div className="flex items-center">
                    <Activity className="h-4 w-4 text-green-600 mr-3" />
                    <span className="text-sm font-medium">View Activity</span>
                  </div>
                </Link>
              </PermissionGuard>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
