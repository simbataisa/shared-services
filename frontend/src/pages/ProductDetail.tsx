import React, { useState, useEffect } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import { PermissionGuard } from '../components/PermissionGuard'
import { usePermissions } from '../hooks/usePermissions'
import type { Product, Module } from '../store/auth'

interface ProductStats {
  totalModules: number
  activeModules: number
  inactiveModules: number
  lastUpdated: string
}

const ProductDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { canViewProducts, canUpdateProducts, canDeleteProducts, canViewModules, canCreateModules } = usePermissions()
  
  const [product, setProduct] = useState<Product | null>(null)
  const [modules, setModules] = useState<Module[]>([])
  const [stats, setStats] = useState<ProductStats | null>(null)
  const [loading, setLoading] = useState(true)
  const [updating, setUpdating] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!canViewProducts) {
      navigate('/unauthorized')
      return
    }

    fetchProductData()
  }, [id, canViewProducts, navigate])

  const fetchProductData = async () => {
    try {
      setLoading(true)
      
      // Simulate API calls
      await new Promise(resolve => setTimeout(resolve, 1000))
      
      // Mock product data
      const mockProduct: Product = {
        id: parseInt(id || '1'),
        name: 'Customer Management System',
        description: 'Comprehensive customer relationship management platform with advanced analytics and reporting capabilities.',
        code: 'CMS_PLATFORM',
        status: 'active',
        category: 'web',
        version: '2.1.0',
        createdAt: '2024-01-15T10:30:00Z',
        updatedAt: '2024-03-10T14:22:00Z',
        createdBy: 'admin@company.com',
        updatedBy: 'product.manager@company.com'
      }

      // Mock modules data
      const mockModules: Module[] = [
        {
          id: 1,
          name: 'User Authentication',
          description: 'Secure user login and registration system',
          code: 'AUTH_MODULE',
          status: 'active',
          productId: mockProduct.id,
          version: '1.5.0',
          createdAt: '2024-01-15T10:30:00Z',
          updatedAt: '2024-02-20T09:15:00Z'
        },
        {
          id: 2,
          name: 'Customer Dashboard',
          description: 'Interactive dashboard for customer data visualization',
          code: 'DASHBOARD_MODULE',
          status: 'active',
          productId: mockProduct.id,
          version: '2.0.1',
          createdAt: '2024-01-20T11:00:00Z',
          updatedAt: '2024-03-05T16:30:00Z'
        },
        {
          id: 3,
          name: 'Reporting Engine',
          description: 'Advanced reporting and analytics module',
          code: 'REPORTS_MODULE',
          status: 'inactive',
          productId: mockProduct.id,
          version: '1.2.0',
          createdAt: '2024-02-01T14:15:00Z',
          updatedAt: '2024-02-15T10:45:00Z'
        }
      ]

      // Mock stats
      const mockStats: ProductStats = {
        totalModules: mockModules.length,
        activeModules: mockModules.filter(m => m.status === 'active').length,
        inactiveModules: mockModules.filter(m => m.status === 'inactive').length,
        lastUpdated: mockProduct.updatedAt
      }

      setProduct(mockProduct)
      setModules(mockModules)
      setStats(mockStats)
    } catch (error) {
      console.error('Error fetching product data:', error)
      setError('Failed to load product data')
    } finally {
      setLoading(false)
    }
  }

  const handleStatusUpdate = async (newStatus: 'active' | 'inactive') => {
    if (!product || !canUpdateProducts) return

    try {
      setUpdating(true)
      
      // Simulate API call
      await new Promise(resolve => setTimeout(resolve, 500))
      
      setProduct(prev => prev ? { ...prev, status: newStatus, updatedAt: new Date().toISOString() } : null)
    } catch (error) {
      console.error('Error updating product status:', error)
      setError('Failed to update product status')
    } finally {
      setUpdating(false)
    }
  }

  const handleDeleteProduct = async () => {
    if (!product || !canDeleteProducts) return

    if (!window.confirm(`Are you sure you want to delete the product "${product.name}"? This action cannot be undone.`)) {
      return
    }

    try {
      setUpdating(true)
      
      // Simulate API call
      await new Promise(resolve => setTimeout(resolve, 1000))
      
      navigate('/products')
    } catch (error) {
      console.error('Error deleting product:', error)
      setError('Failed to delete product')
      setUpdating(false)
    }
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  const getStatusBadge = (status: string) => {
    const baseClasses = 'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium'
    
    switch (status) {
      case 'active':
        return `${baseClasses} bg-green-100 text-green-800`
      case 'inactive':
        return `${baseClasses} bg-red-100 text-red-800`
      default:
        return `${baseClasses} bg-gray-100 text-gray-800`
    }
  }

  if (!canViewProducts) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <h2 className="text-2xl font-bold text-gray-900 mb-4">Access Denied</h2>
          <p className="text-gray-600 mb-4">You don't have permission to view products.</p>
          <button
            onClick={() => navigate('/dashboard')}
            className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
          >
            Back to Dashboard
          </button>
        </div>
      </div>
    )
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
          <p className="text-gray-600">Loading product details...</p>
        </div>
      </div>
    )
  }

  if (error || !product) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <h2 className="text-2xl font-bold text-gray-900 mb-4">Product Not Found</h2>
          <p className="text-gray-600 mb-4">{error || 'The requested product could not be found.'}</p>
          <button
            onClick={() => navigate('/products')}
            className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
          >
            Back to Products
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Header */}
        <div className="mb-8">
          <nav className="flex items-center space-x-2 text-sm text-gray-500 mb-4">
            <Link to="/products" className="hover:text-gray-700">Products</Link>
            <span>/</span>
            <span className="text-gray-900">{product.name}</span>
          </nav>
          
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-3xl font-bold text-gray-900">{product.name}</h1>
              <p className="mt-2 text-gray-600">{product.description}</p>
            </div>
            
            <div className="flex items-center space-x-3">
              <span className={getStatusBadge(product.status)}>
                {product.status.charAt(0).toUpperCase() + product.status.slice(1)}
              </span>
              
              <PermissionGuard permission="product:update">
                <div className="flex space-x-2">
                  {product.status === 'active' ? (
                    <button
                      onClick={() => handleStatusUpdate('inactive')}
                      disabled={updating}
                      className="px-3 py-1 text-sm bg-red-600 text-white rounded-md hover:bg-red-700 disabled:opacity-50"
                    >
                      Deactivate
                    </button>
                  ) : (
                    <button
                      onClick={() => handleStatusUpdate('active')}
                      disabled={updating}
                      className="px-3 py-1 text-sm bg-green-600 text-white rounded-md hover:bg-green-700 disabled:opacity-50"
                    >
                      Activate
                    </button>
                  )}
                  
                  <Link
                    to={`/products/${product.id}/edit`}
                    className="px-3 py-1 text-sm bg-blue-600 text-white rounded-md hover:bg-blue-700"
                  >
                    Edit
                  </Link>
                </div>
              </PermissionGuard>
              
              <PermissionGuard permission="product:delete">
                <button
                  onClick={handleDeleteProduct}
                  disabled={updating}
                  className="px-3 py-1 text-sm bg-red-600 text-white rounded-md hover:bg-red-700 disabled:opacity-50"
                >
                  Delete
                </button>
              </PermissionGuard>
            </div>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Main Content */}
          <div className="lg:col-span-2 space-y-8">
            {/* Product Information */}
            <div className="bg-white shadow rounded-lg p-6">
              <h2 className="text-xl font-semibold text-gray-900 mb-4">Product Information</h2>
              
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label className="block text-sm font-medium text-gray-700">Product Code</label>
                  <p className="mt-1 text-sm text-gray-900 font-mono bg-gray-50 px-2 py-1 rounded">
                    {product.code}
                  </p>
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700">Category</label>
                  <p className="mt-1 text-sm text-gray-900 capitalize">{product.category}</p>
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700">Version</label>
                  <p className="mt-1 text-sm text-gray-900 font-mono">{product.version}</p>
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700">Status</label>
                  <span className={getStatusBadge(product.status)}>
                    {product.status.charAt(0).toUpperCase() + product.status.slice(1)}
                  </span>
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700">Created</label>
                  <p className="mt-1 text-sm text-gray-900">{formatDate(product.createdAt)}</p>
                  <p className="text-xs text-gray-500">by {product.createdBy}</p>
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700">Last Updated</label>
                  <p className="mt-1 text-sm text-gray-900">{formatDate(product.updatedAt)}</p>
                  <p className="text-xs text-gray-500">by {product.updatedBy}</p>
                </div>
              </div>
            </div>

            {/* Modules Section */}
            <div className="bg-white shadow rounded-lg p-6">
              <div className="flex items-center justify-between mb-4">
                <h2 className="text-xl font-semibold text-gray-900">Modules</h2>
                
                <PermissionGuard permission="module:create">
                  <Link
                    to={`/products/${product.id}/modules/create`}
                    className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 text-sm"
                  >
                    Add Module
                  </Link>
                </PermissionGuard>
              </div>
              
              {canViewModules ? (
                <div className="space-y-4">
                  {modules.length > 0 ? (
                    modules.map((module) => (
                      <div key={module.id} className="border border-gray-200 rounded-lg p-4">
                        <div className="flex items-center justify-between">
                          <div className="flex-1">
                            <div className="flex items-center space-x-3">
                              <h3 className="text-lg font-medium text-gray-900">{module.name}</h3>
                              <span className={getStatusBadge(module.status)}>
                                {module.status.charAt(0).toUpperCase() + module.status.slice(1)}
                              </span>
                              <span className="text-sm text-gray-500 font-mono">v{module.version}</span>
                            </div>
                            <p className="mt-1 text-sm text-gray-600">{module.description}</p>
                            <p className="mt-2 text-xs text-gray-500">
                              Code: <span className="font-mono">{module.code}</span> • 
                              Updated: {formatDate(module.updatedAt)}
                            </p>
                          </div>
                          
                          <div className="flex items-center space-x-2">
                            <Link
                              to={`/modules/${module.id}`}
                              className="px-3 py-1 text-sm bg-gray-100 text-gray-700 rounded-md hover:bg-gray-200"
                            >
                              View
                            </Link>
                          </div>
                        </div>
                      </div>
                    ))
                  ) : (
                    <div className="text-center py-8">
                      <p className="text-gray-500">No modules found for this product.</p>
                      {canCreateModules && (
                        <Link
                          to={`/products/${product.id}/modules/create`}
                          className="mt-2 inline-block px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 text-sm"
                        >
                          Create First Module
                        </Link>
                      )}
                    </div>
                  )}
                </div>
              ) : (
                <div className="text-center py-8">
                  <p className="text-gray-500">You don't have permission to view modules.</p>
                </div>
              )}
            </div>
          </div>

          {/* Sidebar */}
          <div className="space-y-6">
            {/* Statistics */}
            {stats && (
              <div className="bg-white shadow rounded-lg p-6">
                <h3 className="text-lg font-semibold text-gray-900 mb-4">Statistics</h3>
                
                <div className="space-y-4">
                  <div className="flex justify-between">
                    <span className="text-sm text-gray-600">Total Modules</span>
                    <span className="text-sm font-medium text-gray-900">{stats.totalModules}</span>
                  </div>
                  
                  <div className="flex justify-between">
                    <span className="text-sm text-gray-600">Active Modules</span>
                    <span className="text-sm font-medium text-green-600">{stats.activeModules}</span>
                  </div>
                  
                  <div className="flex justify-between">
                    <span className="text-sm text-gray-600">Inactive Modules</span>
                    <span className="text-sm font-medium text-red-600">{stats.inactiveModules}</span>
                  </div>
                  
                  <div className="pt-4 border-t border-gray-200">
                    <span className="text-sm text-gray-600">Last Updated</span>
                    <p className="text-sm font-medium text-gray-900">{formatDate(stats.lastUpdated)}</p>
                  </div>
                </div>
              </div>
            )}

            {/* Quick Actions */}
            <div className="bg-white shadow rounded-lg p-6">
              <h3 className="text-lg font-semibold text-gray-900 mb-4">Quick Actions</h3>
              
              <div className="space-y-3">
                <PermissionGuard permission="module:read">
                  <Link
                    to={`/products/${product.id}/modules`}
                    className="block w-full px-4 py-2 text-sm bg-gray-100 text-gray-700 rounded-md hover:bg-gray-200 text-center"
                  >
                    View All Modules
                  </Link>
                </PermissionGuard>
                
                <PermissionGuard permission="module:create">
                  <Link
                    to={`/products/${product.id}/modules/create`}
                    className="block w-full px-4 py-2 text-sm bg-blue-600 text-white rounded-md hover:bg-blue-700 text-center"
                  >
                    Add New Module
                  </Link>
                </PermissionGuard>
                
                <PermissionGuard permission="product:update">
                  <Link
                    to={`/products/${product.id}/edit`}
                    className="block w-full px-4 py-2 text-sm bg-green-600 text-white rounded-md hover:bg-green-700 text-center"
                  >
                    Edit Product
                  </Link>
                </PermissionGuard>
                
                <Link
                  to="/products"
                  className="block w-full px-4 py-2 text-sm border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50 text-center"
                >
                  Back to Products
                </Link>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default ProductDetail