import { useState, useEffect } from 'react'
import { useAuth } from '../store/auth'
import { usePermissions } from '../hooks/usePermissions'
import { PermissionGuard } from '../components/PermissionGuard'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Skeleton } from "@/components/ui/skeleton"
import { 
  Users, 
  Building2, 
  Shield, 
  Activity, 
  TrendingUp, 
  AlertTriangle,
  CheckCircle,
  Clock,
  BarChart3,
  PieChart,
  Calendar,
  Bell
} from 'lucide-react'
import api from '../lib/api'

interface DashboardStats {
  totalUsers: number
  activeTenants: number
  totalRoles: number
  recentActivities: number
  systemHealth: 'healthy' | 'warning' | 'critical'
  pendingApprovals: number
}

interface RecentActivity {
  id: string
  type: 'user_login' | 'role_assigned' | 'tenant_created' | 'permission_granted'
  description: string
  timestamp: string
  user: string
}

export default function Dashboard() {
  const { user, tenant } = useAuth()
  const { canViewAuditLogs } = usePermissions()
  const [stats, setStats] = useState<DashboardStats>({
    totalUsers: 0,
    activeTenants: 0,
    totalRoles: 0,
    recentActivities: 0,
    systemHealth: 'healthy',
    pendingApprovals: 0
  })
  const [recentActivities, setRecentActivities] = useState<RecentActivity[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetchDashboardData()
  }, [])

  const fetchDashboardData = async () => {
    try {
      setLoading(true)
      
      // Fetch dashboard statistics
      const [statsResponse, activitiesResponse] = await Promise.all([
        api.get('/dashboard/stats'),
        api.get('/dashboard/recent-activities')
      ])
      
      setStats(statsResponse.data)
      setRecentActivities(activitiesResponse.data)
    } catch (error) {
      console.error('Failed to fetch dashboard data:', error)
    } finally {
      setLoading(false)
    }
  }

  const getHealthColor = (health: string) => {
    switch (health) {
      case 'healthy': return 'text-green-600 bg-green-100'
      case 'warning': return 'text-yellow-600 bg-yellow-100'
      case 'critical': return 'text-red-600 bg-red-100'
      default: return 'text-gray-600 bg-gray-100'
    }
  }

  const getActivityIcon = (type: string) => {
    switch (type) {
      case 'user_login': return <Users className="h-4 w-4" />
      case 'role_assigned': return <Shield className="h-4 w-4" />
      case 'tenant_created': return <Building2 className="h-4 w-4" />
      case 'permission_granted': return <CheckCircle className="h-4 w-4" />
      default: return <Activity className="h-4 w-4" />
    }
  }

  if (loading) {
    return (
      <div className="p-6 space-y-6">
        <Skeleton className="h-32 w-full rounded-lg" />
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          {[...Array(4)].map((_, i) => (
            <Skeleton key={i} className="h-32 w-full rounded-lg" />
          ))}
        </div>
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <Skeleton className="h-96 w-full rounded-lg" />
          <Skeleton className="h-96 w-full rounded-lg" />
        </div>
      </div>
    )
  }

  return (
    <div className="p-6 space-y-6">
      {/* Welcome Section */}
      <Card className="bg-gradient-to-r from-blue-600 to-purple-600 border-0">
        <CardContent className="p-6 text-white">
          <h1 className="text-2xl font-bold mb-2">
            Welcome back, {user?.name || 'User'}!
          </h1>
          <p className="text-blue-100">
            {tenant ? `Managing ${tenant.name}` : 'System Administrator Dashboard'}
          </p>
        </CardContent>
      </Card>

      {/* Quick Stats */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <PermissionGuard permission="users:read">
          <Card>
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-muted-foreground">Total Users</p>
                  <p className="text-2xl font-bold">{stats.totalUsers}</p>
                </div>
                <div className="bg-blue-100 p-3 rounded-full">
                  <Users className="h-6 w-6 text-blue-600" />
                </div>
              </div>
            </CardContent>
          </Card>
        </PermissionGuard>

        <PermissionGuard permission="tenants:read">
          <Card>
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-muted-foreground">Active Tenants</p>
                  <p className="text-2xl font-bold">{stats.activeTenants}</p>
                </div>
                <div className="bg-green-100 p-3 rounded-full">
                  <Building2 className="h-6 w-6 text-green-600" />
                </div>
              </div>
            </CardContent>
          </Card>
        </PermissionGuard>

        <PermissionGuard permission="roles:read">
          <Card>
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-muted-foreground">Total Roles</p>
                  <p className="text-2xl font-bold">{stats.totalRoles}</p>
                </div>
                <div className="bg-purple-100 p-3 rounded-full">
                  <Shield className="h-6 w-6 text-purple-600" />
                </div>
              </div>
            </CardContent>
          </Card>
        </PermissionGuard>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">System Health</p>
                <p className={`text-sm font-medium px-2 py-1 rounded-full inline-block mt-1 ${getHealthColor(stats.systemHealth)}`}>
                  {stats.systemHealth.charAt(0).toUpperCase() + stats.systemHealth.slice(1)}
                </p>
              </div>
              <div className="bg-orange-100 p-3 rounded-full">
                <Activity className="h-6 w-6 text-orange-600" />
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Recent Activities */}
        <PermissionGuard permission="audit:read">
          <Card className="lg:col-span-2">
            <CardHeader>
              <div className="flex items-center justify-between">
                <CardTitle>Recent Activities</CardTitle>
                <button className="text-blue-600 hover:text-blue-800 text-sm font-medium">
                  View All
                </button>
              </div>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {recentActivities.map((activity) => (
                  <div key={activity.id} className="flex items-start space-x-3">
                    <div className="bg-muted p-2 rounded-full">
                      {getActivityIcon(activity.type)}
                    </div>
                    <div className="flex-1 min-w-0">
                      <p className="text-sm">{activity.description}</p>
                      <p className="text-xs text-muted-foreground">
                        by {activity.user} • {new Date(activity.timestamp).toLocaleString()}
                      </p>
                    </div>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        </PermissionGuard>

        {/* Quick Actions */}
        <Card>
          <CardHeader>
            <CardTitle>Quick Actions</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              <PermissionGuard permission="users:create">
                <button className="w-full text-left p-3 rounded-lg border border-border hover:bg-muted transition-colors">
                  <div className="flex items-center space-x-3">
                    <Users className="h-5 w-5 text-blue-600" />
                    <span className="text-sm font-medium">Add New User</span>
                  </div>
                </button>
              </PermissionGuard>

              <PermissionGuard permission="tenants:create">
                <button className="w-full text-left p-3 rounded-lg border border-border hover:bg-muted transition-colors">
                  <div className="flex items-center space-x-3">
                    <Building2 className="h-5 w-5 text-green-600" />
                    <span className="text-sm font-medium">Create Tenant</span>
                  </div>
                </button>
              </PermissionGuard>

              <PermissionGuard permission="roles:create">
                <button className="w-full text-left p-3 rounded-lg border border-border hover:bg-muted transition-colors">
                  <div className="flex items-center space-x-3">
                    <Shield className="h-5 w-5 text-purple-600" />
                    <span className="text-sm font-medium">Create Role</span>
                  </div>
                </button>
              </PermissionGuard>

              <PermissionGuard permission="audit:read">
                <button className="w-full text-left p-3 rounded-lg border border-border hover:bg-muted transition-colors">
                  <div className="flex items-center space-x-3">
                    <BarChart3 className="h-5 w-5 text-orange-600" />
                    <span className="text-sm font-medium">View Reports</span>
                  </div>
                </button>
              </PermissionGuard>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Pending Approvals */}
      {stats.pendingApprovals > 0 && (
        <PermissionGuard permission="approvals:read">
          <Card className="bg-yellow-50 border-yellow-200">
            <CardContent className="p-4">
              <div className="flex items-center">
                <AlertTriangle className="h-5 w-5 text-yellow-600 mr-3" />
                <div className="flex-1">
                  <h3 className="text-sm font-medium text-yellow-800">
                    Pending Approvals
                  </h3>
                  <p className="text-sm text-yellow-700">
                    You have {stats.pendingApprovals} items waiting for approval.
                  </p>
                </div>
                <button className="text-yellow-800 hover:text-yellow-900 text-sm font-medium">
                  Review
                </button>
              </div>
            </CardContent>
          </Card>
        </PermissionGuard>
      )}

      {/* Charts Section */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <PermissionGuard permission="analytics:read">
          <Card>
            <CardHeader>
              <CardTitle>User Growth</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="h-64 flex items-center justify-center text-muted-foreground">
                <div className="text-center">
                  <TrendingUp className="h-12 w-12 mx-auto mb-2" />
                  <p>Chart visualization would go here</p>
                </div>
              </div>
            </CardContent>
          </Card>
        </PermissionGuard>

        <PermissionGuard permission="analytics:read">
          <Card>
            <CardHeader>
              <CardTitle>Permission Distribution</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="h-64 flex items-center justify-center text-muted-foreground">
                <div className="text-center">
                  <PieChart className="h-12 w-12 mx-auto mb-2" />
                  <p>Chart visualization would go here</p>
                </div>
              </div>
            </CardContent>
          </Card>
        </PermissionGuard>
      </div>
    </div>
  )
}