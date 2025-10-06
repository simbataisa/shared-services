import React, { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { PermissionGuard } from '../components/PermissionGuard'
import { usePermissions } from '../hooks/usePermissions'
import api from '../lib/api'

interface Module {
  id: number
  name: string
  description: string
  code?: string
  isActive: boolean
  productId: number
  productName: string
  createdAt: string
  updatedAt: string
}

const ModuleList: React.FC = () => {
  const [modules, setModules] = useState<Module[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [searchTerm, setSearchTerm] = useState('')
  const [statusFilter, setStatusFilter] = useState<'all' | 'active' | 'inactive'>('all')
  const [productFilter, setProductFilter] = useState<string>('all')
  const [products, setProducts] = useState<{id: number, name: string}[]>([])

  const navigate = useNavigate()
  const { canViewModules, canCreateModules, canUpdateModules, canDeleteModules } = usePermissions()

  useEffect(() => {
    if (!canViewModules) {
      navigate('/unauthorized')
      return
    }

    fetchModules()
    fetchProducts()
  }, [canViewModules, navigate])

  const fetchModules = async () => {
    try {
      setLoading(true)
      const response = await api.get('/v1/modules')
      setModules(response.data || [])
    } catch (err) {
      setError('Failed to fetch modules')
      console.error('Error fetching modules:', err)
    } finally {
      setLoading(false)
    }
  }

  const fetchProducts = async () => {
    try {
      const response = await api.get('/products')
      setProducts(response.data || [])
    } catch (err) {
      console.error('Error fetching products:', err)
    }
  }

  const handleStatusChange = async (moduleId: number, newStatus: boolean) => {
    if (!canUpdateModules) return

    try {
      await api.put(`/v1/modules/${moduleId}`, { isActive: newStatus })
      setModules(prev => 
        prev.map(module => 
          module.id === moduleId ? { ...module, isActive: newStatus } : module
        )
      )
    } catch (err) {
      setError('Failed to update module status')
      console.error('Error updating module status:', err)
    }
  }

  const handleDelete = async (moduleId: number) => {
    if (!canDeleteModules) return

    if (window.confirm('Are you sure you want to delete this module?')) {
      try {
        await api.delete(`/v1/modules/${moduleId}`)
        setModules(prev => prev.filter(module => module.id !== moduleId))
      } catch (err) {
        setError('Failed to delete module')
        console.error('Error deleting module:', err)
      }
    }
  }

  const filteredModules = modules.filter(module => {
    const matchesSearch = module.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         module.description.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         (module.code && module.code.toLowerCase().includes(searchTerm.toLowerCase()))
    
    const matchesStatus = statusFilter === 'all' || 
                         (statusFilter === 'active' && module.isActive) ||
                         (statusFilter === 'inactive' && !module.isActive)
    
    const matchesProduct = productFilter === 'all' || module.productId.toString() === productFilter

    return matchesSearch && matchesStatus && matchesProduct
  })

  if (!canViewModules) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <h2 className="text-2xl font-bold text-gray-900 mb-4">Access Denied</h2>
          <p className="text-gray-600 mb-4">You don't have permission to view modules.</p>
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

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="bg-white shadow rounded-lg">
          <div className="px-6 py-4 border-b border-gray-200">
            <div className="flex justify-between items-center">
              <div>
                <h1 className="text-2xl font-bold text-gray-900">Modules</h1>
                <p className="mt-1 text-sm text-gray-600">
                  Manage system modules and their configurations.
                </p>
              </div>
              {canCreateModules && (
                <Link
                  to="/modules/create"
                  className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                >
                  Create Module
                </Link>
              )}
            </div>
          </div>

          {/* Filters */}
          <div className="px-6 py-4 border-b border-gray-200 bg-gray-50">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div>
                <label htmlFor="search" className="block text-sm font-medium text-gray-700 mb-1">
                  Search
                </label>
                <input
                  type="text"
                  id="search"
                  placeholder="Search modules..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                />
              </div>
              <div>
                <label htmlFor="status" className="block text-sm font-medium text-gray-700 mb-1">
                  Status
                </label>
                <select
                  id="status"
                  value={statusFilter}
                  onChange={(e) => setStatusFilter(e.target.value as 'all' | 'active' | 'inactive')}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                >
                  <option value="all">All Status</option>
                  <option value="active">Active</option>
                  <option value="inactive">Inactive</option>
                </select>
              </div>
              <div>
                <label htmlFor="product" className="block text-sm font-medium text-gray-700 mb-1">
                  Product
                </label>
                <select
                  id="product"
                  value={productFilter}
                  onChange={(e) => setProductFilter(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                >
                  <option value="all">All Products</option>
                  {products.map(product => (
                    <option key={product.id} value={product.id.toString()}>
                      {product.name}
                    </option>
                  ))}
                </select>
              </div>
            </div>
          </div>

          {/* Content */}
          <div className="px-6 py-4">
            {error && (
              <div className="mb-4 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
                {error}
              </div>
            )}

            {loading ? (
              <div className="flex justify-center items-center py-12">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
                <span className="ml-2 text-gray-600">Loading modules...</span>
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Module
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Product
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Status
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Last Updated
                      </th>
                      <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Actions
                      </th>
                    </tr>
                  </thead>
                  <tbody className="bg-white divide-y divide-gray-200">
                    {filteredModules.map((module) => (
                      <tr key={module.id} className="hover:bg-gray-50">
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div>
                            <div className="text-sm font-medium text-gray-900">
                              {module.name}
                            </div>
                            <div className="text-sm text-gray-500">
                              {module.description}
                            </div>
                            {module.code && (
                              <div className="text-xs text-gray-400 mt-1">
                                Code: {module.code}
                              </div>
                            )}
                          </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="text-sm text-gray-900">
                            {module.productName}
                          </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                            module.isActive
                              ? 'bg-green-100 text-green-800'
                              : 'bg-red-100 text-red-800'
                          }`}>
                            {module.isActive ? 'Active' : 'Inactive'}
                          </span>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {new Date(module.updatedAt).toLocaleDateString()}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                          <div className="flex justify-end space-x-2">
                            <Link
                              to={`/modules/${module.id}`}
                              className="text-blue-600 hover:text-blue-900"
                            >
                              View
                            </Link>
                            {canUpdateModules && (
                              <>
                                <Link
                                  to={`/modules/${module.id}/edit`}
                                  className="text-indigo-600 hover:text-indigo-900"
                                >
                                  Edit
                                </Link>
                                <button
                                  onClick={() => handleStatusChange(module.id, !module.isActive)}
                                  className={`${
                                    module.isActive
                                      ? 'text-red-600 hover:text-red-900'
                                      : 'text-green-600 hover:text-green-900'
                                  }`}
                                >
                                  {module.isActive ? 'Deactivate' : 'Activate'}
                                </button>
                              </>
                            )}
                            {canDeleteModules && (
                              <button
                                onClick={() => handleDelete(module.id)}
                                className="text-red-600 hover:text-red-900"
                              >
                                Delete
                              </button>
                            )}
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>

                {filteredModules.length === 0 && (
                  <div className="text-center py-12">
                    <div className="text-gray-500">
                      {modules.length === 0 ? 'No modules found.' : 'No modules match your filters.'}
                    </div>
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}

export default ModuleList