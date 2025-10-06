import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { 
  Building2, 
  Search, 
  Filter, 
  Edit, 
  Trash2, 
  Eye,
  MoreHorizontal,
  CheckCircle,
  XCircle,
  Clock,
  Plus
} from 'lucide-react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "@/components/ui/dropdown-menu"
import { Skeleton } from "@/components/ui/skeleton"
import { StatusBadge } from '../components/StatusBadge'
import { PermissionGuard } from '../components/PermissionGuard'
import { normalizeEntityStatus } from '../lib/status-colors'
import api from '../lib/api'
import { type Tenant } from '../store/auth'
import SearchAndFilter from '../components/SearchAndFilter'

export default function TenantList() {
  const [tenants, setTenants] = useState<Tenant[]>([])
  const [loading, setLoading] = useState(true)
  const [searchTerm, setSearchTerm] = useState('')
  const [statusFilter, setStatusFilter] = useState<string>('all')
  const [typeFilter, setTypeFilter] = useState<string>('all')

  useEffect(() => {
    fetchTenants()
  }, [])

  const fetchTenants = async () => {
    try {
      setLoading(true)
      const response = await api.get('/v1/tenants')
      // Extract the actual tenant array from the ApiResponse wrapper
      setTenants(response.data.data || [])
    } catch (error) {
      console.error('Failed to fetch tenants:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleStatusChange = async (tenantId: number, newStatus: string) => {
    try {
      await api.patch(`/v1/tenants/${tenantId}/status`, { status: newStatus })
      await fetchTenants() // Refresh the list
    } catch (error) {
      console.error('Failed to update tenant status:', error)
    }
  }

  const handleDelete = async (tenantId: number) => {
    if (window.confirm('Are you sure you want to delete this tenant?')) {
      try {
        await api.delete(`/v1/tenants/${tenantId}`)
        await fetchTenants() // Refresh the list
      } catch (error) {
        console.error('Failed to delete tenant:', error)
      }
    }
  }

  const filteredTenants = tenants.filter(tenant => {
    const matchesSearch = tenant.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         tenant.code.toLowerCase().includes(searchTerm.toLowerCase())
    const matchesStatus = statusFilter === 'all' || tenant.status === statusFilter
    const matchesType = typeFilter === 'all' || tenant.type === typeFilter
    
    return matchesSearch && matchesStatus && matchesType
  })

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'ACTIVE': return <CheckCircle className="h-4 w-4 text-green-600" />
      case 'INACTIVE': return <XCircle className="h-4 w-4 text-red-600" />
      case 'SUSPENDED': return <Clock className="h-4 w-4 text-yellow-600" />
      default: return <XCircle className="h-4 w-4 text-gray-600" />
    }
  }

  const getStatusVariant = (status: string) => {
    switch (status) {
      case 'ACTIVE': return 'default'
      case 'INACTIVE': return 'destructive'
      case 'SUSPENDED': return 'secondary'
      default: return 'outline'
    }
  }

  const getTypeLabel = (type: string) => {
    switch (type) {
      case 'BUSINESS_IN': return 'Business Internal'
      case 'BUSINESS_OUT': return 'Business External'
      case 'INDIVIDUAL': return 'Individual'
      default: return type
    }
  }

  if (loading) {
    return (
      <div className="p-6 space-y-6">
        <div className="flex justify-between items-center">
          <div className="space-y-2">
            <Skeleton className="h-8 w-48" />
            <Skeleton className="h-4 w-64" />
          </div>
          <Skeleton className="h-10 w-32" />
        </div>
        
        <Card>
          <CardContent className="p-4">
            <div className="flex flex-col sm:flex-row gap-4">
              <Skeleton className="h-10 flex-1" />
              <div className="flex gap-2">
                <Skeleton className="h-10 w-32" />
                <Skeleton className="h-10 w-32" />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-0">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Tenant</TableHead>
                  <TableHead>Type</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {[...Array(5)].map((_, i) => (
                  <TableRow key={i}>
                    <TableCell>
                      <div className="flex items-center gap-3">
                        <Skeleton className="h-10 w-10 rounded-full" />
                        <div className="space-y-1">
                          <Skeleton className="h-4 w-32" />
                          <Skeleton className="h-3 w-24" />
                        </div>
                      </div>
                    </TableCell>
                    <TableCell><Skeleton className="h-4 w-24" /></TableCell>
                    <TableCell><Skeleton className="h-6 w-20" /></TableCell>
                    <TableCell><Skeleton className="h-8 w-24" /></TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      </div>
    )
  }

  return (
    <div className="p-6 space-y-6">
      {/* Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold">Tenants</h1>
          <p className="text-muted-foreground">Manage tenant organizations and their access</p>
        </div>
      </div>

      {/* Search and Filters */}
      <SearchAndFilter
        searchTerm={searchTerm}
        onSearchChange={setSearchTerm}
        searchPlaceholder="Search tenants by name or code..."
        filters={[
          {
            label: "Status",
            value: statusFilter,
            onChange: setStatusFilter,
            options: [
              { value: "all", label: "All Status" },
              { value: "ACTIVE", label: "Active" },
              { value: "INACTIVE", label: "Inactive" },
              { value: "SUSPENDED", label: "Suspended" },
            ],
            width: "w-40",
          },
          {
            label: "Type",
            value: typeFilter,
            onChange: setTypeFilter,
            options: [
              { value: "all", label: "All Types" },
              { value: "BUSINESS_IN", label: "Business Internal" },
              { value: "BUSINESS_OUT", label: "Business External" },
              { value: "INDIVIDUAL", label: "Individual" },
            ],
            width: "w-48",
          },
        ]}
        actions={
          <PermissionGuard permission="tenants:create">
            <Button asChild>
              <Link to="/tenants/create">
                <Plus className="h-4 w-4 mr-2" />
                Create Tenant
              </Link>
            </Button>
          </PermissionGuard>
        }
      />

      {/* Tenants Table */}
      <Card>
        <CardContent className="p-0">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Tenant</TableHead>
                <TableHead>Type</TableHead>
                <TableHead>Status</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filteredTenants.map((tenant) => (
                <TableRow key={tenant.id}>
                  <TableCell>
                    <div className="flex items-center gap-3">
                      <div className="bg-primary/10 p-2 rounded-full">
                        <Building2 className="h-4 w-4 text-primary" />
                      </div>
                      <div>
                        <div className="font-medium">{tenant.name}</div>
                        <div className="text-sm text-muted-foreground font-mono">
                          {tenant.code}
                        </div>
                      </div>
                    </div>
                  </TableCell>
                  <TableCell>
                    <span className="text-sm">
                      {getTypeLabel(tenant.type)}
                    </span>
                  </TableCell>
                  <TableCell>
                    <div className="flex items-center gap-2">
                      {getStatusIcon(tenant.status)}
                      <StatusBadge 
                        status={normalizeEntityStatus('tenant', tenant.status)}
                      />
                    </div>
                  </TableCell>
                  <TableCell className="text-right">
                    <div className="flex items-center justify-end gap-2">
                      <PermissionGuard permission="tenants:read">
                        <Button variant="ghost" size="icon" asChild>
                          <Link to={`/tenants/${tenant.id}`} title="View Details">
                            <Eye className="h-4 w-4" />
                          </Link>
                        </Button>
                      </PermissionGuard>
                      
                      <PermissionGuard permission="tenants:update">
                        <Button variant="ghost" size="icon" asChild>
                          <Link to={`/tenants/${tenant.id}/edit`} title="Edit">
                            <Edit className="h-4 w-4" />
                          </Link>
                        </Button>
                      </PermissionGuard>
                      
                      <PermissionGuard permission="tenants:update">
                        <DropdownMenu>
                          <DropdownMenuTrigger asChild>
                            <Button variant="ghost" size="icon">
                              <MoreHorizontal className="h-4 w-4" />
                            </Button>
                          </DropdownMenuTrigger>
                          <DropdownMenuContent align="end">
                            <DropdownMenuItem
                              onClick={() => handleStatusChange(Number(tenant.id), 'ACTIVE')}
                            >
                              <CheckCircle className="h-4 w-4 mr-2" />
                              Set Active
                            </DropdownMenuItem>
                            <DropdownMenuItem
                              onClick={() => handleStatusChange(Number(tenant.id), 'INACTIVE')}
                            >
                              <XCircle className="h-4 w-4 mr-2" />
                              Set Inactive
                            </DropdownMenuItem>
                            <DropdownMenuItem
                              onClick={() => handleStatusChange(Number(tenant.id), 'SUSPENDED')}
                            >
                              <Clock className="h-4 w-4 mr-2" />
                              Suspend
                            </DropdownMenuItem>
                          </DropdownMenuContent>
                        </DropdownMenu>
                      </PermissionGuard>
                      
                      <PermissionGuard permission="tenants:delete">
                        <Button 
                          variant="ghost" 
                          size="icon"
                          onClick={() => handleDelete(Number(tenant.id))}
                          title="Delete"
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </PermissionGuard>
                    </div>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
          
          {filteredTenants.length === 0 && (
            <div className="text-center py-12">
              <Building2 className="mx-auto h-12 w-12 text-muted-foreground" />
              <h3 className="mt-2 text-sm font-medium">No tenants found</h3>
              <p className="mt-1 text-sm text-muted-foreground">
                {searchTerm || statusFilter !== 'all' || typeFilter !== 'all'
                  ? 'Try adjusting your search or filter criteria.'
                  : 'Get started by creating a new tenant.'}
              </p>
              {!searchTerm && statusFilter === 'all' && typeFilter === 'all' && (
                <PermissionGuard permission="tenants:create">
                  <Button className="mt-4" asChild>
                    <Link to="/tenants/create">
                      <Plus className="h-4 w-4 mr-2" />
                      Create Your First Tenant
                    </Link>
                  </Button>
                </PermissionGuard>
              )}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  )
}