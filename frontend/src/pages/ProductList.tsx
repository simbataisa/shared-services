import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../store/auth'
import { usePermissions } from '../hooks/usePermissions'
import { PermissionGuard } from '../components/PermissionGuard'
import api from '../lib/api'
import type { Product, Module } from '../store/auth'

interface ProductWithModules extends Product {
  modules: Module[]
}

export default function ProductList() {
  const { user, tenant } = useAuth()
  const { canManageProducts, canViewProducts } = usePermissions()
  const [products, setProducts] = useState<ProductWithModules[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [searchTerm, setSearchTerm] = useState('')
  const [statusFilter, setStatusFilter] = useState<string>('all')
  const [expandedProducts, setExpandedProducts] = useState<Set<string>>(new Set())

  useEffect(() => {
    fetchProducts()
  }, [])

  const fetchProducts = async () => {
    try {
      setLoading(true)
      const response = await api.get('/products')
      setProducts(response.data)
    } catch (err) {
      setError('Failed to fetch products')
      console.error('Error fetching products:', err)
    } finally {
      setLoading(false)
    }
  }

  const handleStatusChange = async (productId: string, newStatus: string) => {
    try {
      await api.patch(`/products/${productId}/status`, { status: newStatus })
      setProducts(products.map(product => 
        product.id === productId ? { ...product, status: newStatus } : product
      ))
    } catch (err) {
      setError('Failed to update product status')
      console.error('Error updating product status:', err)
    }
  }

  const handleDeleteProduct = async (productId: string) => {
    if (!confirm('Are you sure you want to delete this product?')) return

    try {
      await api.delete(`/products/${productId}`)
      setProducts(products.filter(product => product.id !== productId))
    } catch (err) {
      setError('Failed to delete product')
      console.error('Error deleting product:', err)
    }
  }

  const toggleProductExpansion = (productId: string) => {
    const newExpanded = new Set(expandedProducts)
    if (newExpanded.has(productId)) {
      newExpanded.delete(productId)
    } else {
      newExpanded.add(productId)
    }
    setExpandedProducts(newExpanded)
  }

  const filteredProducts = products.filter(product => {
    const matchesSearch = product.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         product.code.toLowerCase().includes(searchTerm.toLowerCase())
    const matchesStatus = statusFilter === 'all' || product.status === statusFilter
    return matchesSearch && matchesStatus
  })

  if (!canViewProducts) {
    return (
      <div className="container">
        <div className="error">
          You don't have permission to view products.
        </div>
      </div>
    )
  }

  if (loading) {
    return (
      <div className="container">
        <div className="loading">Loading products...</div>
      </div>
    )
  }

  return (
    <div className="container">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h1 style={{ margin: 0, fontSize: '2rem', fontWeight: 'bold' }}>Products</h1>
        <PermissionGuard permissions={['product:create']}>
          <Link to="/products/create" className="btn">
            Create Product
          </Link>
        </PermissionGuard>
      </div>

      {error && <div className="error">{error}</div>}

      {/* Filters */}
      <div className="card" style={{ marginBottom: '1.5rem' }}>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 200px', gap: '1rem', alignItems: 'end' }}>
          <div className="form-group" style={{ margin: 0 }}>
            <label className="form-label">Search Products</label>
            <input
              type="text"
              className="form-input"
              placeholder="Search by name or code..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
          <div className="form-group" style={{ margin: 0 }}>
            <label className="form-label">Status</label>
            <select
              className="form-input"
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
            >
              <option value="all">All Status</option>
              <option value="active">Active</option>
              <option value="inactive">Inactive</option>
              <option value="deprecated">Deprecated</option>
            </select>
          </div>
        </div>
      </div>

      {/* Products List */}
      <div className="card">
        {filteredProducts.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '2rem', color: '#6b7280' }}>
            No products found.
          </div>
        ) : (
          <div>
            {filteredProducts.map((product) => (
              <div key={product.id} style={{ borderBottom: '1px solid #e5e7eb', paddingBottom: '1rem', marginBottom: '1rem' }}>
                {/* Product Header */}
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                    <button
                      onClick={() => toggleProductExpansion(product.id)}
                      style={{ 
                        background: 'none', 
                        border: 'none', 
                        cursor: 'pointer',
                        fontSize: '1.2rem',
                        padding: '0.25rem'
                      }}
                    >
                      {expandedProducts.has(product.id) ? '▼' : '▶'}
                    </button>
                    <div>
                      <h3 style={{ margin: 0, fontSize: '1.25rem', fontWeight: '600' }}>
                        <Link 
                          to={`/products/${product.id}`}
                          style={{ textDecoration: 'none', color: '#2563eb' }}
                        >
                          {product.name}
                        </Link>
                      </h3>
                      <p style={{ margin: '0.25rem 0 0 0', color: '#6b7280', fontSize: '0.875rem' }}>
                        Code: {product.code} | Version: {product.version}
                      </p>
                    </div>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                    <span 
                      style={{ 
                        padding: '0.25rem 0.75rem',
                        borderRadius: '9999px',
                        fontSize: '0.75rem',
                        fontWeight: '500',
                        backgroundColor: product.status === 'active' ? '#dcfce7' : 
                                       product.status === 'inactive' ? '#fef3c7' : '#fee2e2',
                        color: product.status === 'active' ? '#166534' : 
                               product.status === 'inactive' ? '#92400e' : '#991b1b'
                      }}
                    >
                      {product.status}
                    </span>
                    <PermissionGuard permissions={['product:update']}>
                      <select
                        value={product.status}
                        onChange={(e) => handleStatusChange(product.id, e.target.value)}
                        style={{ padding: '0.25rem', fontSize: '0.875rem', border: '1px solid #d1d5db', borderRadius: '0.375rem' }}
                      >
                        <option value="active">Active</option>
                        <option value="inactive">Inactive</option>
                        <option value="deprecated">Deprecated</option>
                      </select>
                    </PermissionGuard>
                    <PermissionGuard permissions={['product:delete']}>
                      <button
                        onClick={() => handleDeleteProduct(product.id)}
                        className="btn btn-danger"
                        style={{ padding: '0.25rem 0.5rem', fontSize: '0.875rem' }}
                      >
                        Delete
                      </button>
                    </PermissionGuard>
                  </div>
                </div>

                {/* Product Description */}
                {product.description && (
                  <p style={{ margin: '0.5rem 0', color: '#4b5563', fontSize: '0.875rem', paddingLeft: '2.5rem' }}>
                    {product.description}
                  </p>
                )}

                {/* Modules (when expanded) */}
                {expandedProducts.has(product.id) && (
                  <div style={{ paddingLeft: '2.5rem', marginTop: '1rem' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                      <h4 style={{ margin: 0, fontSize: '1rem', fontWeight: '600', color: '#374151' }}>
                        Modules ({product.modules?.length || 0})
                      </h4>
                      <PermissionGuard permissions={['module:create']}>
                        <Link 
                          to={`/products/${product.id}/modules/create`} 
                          className="btn"
                          style={{ padding: '0.25rem 0.75rem', fontSize: '0.875rem' }}
                        >
                          Add Module
                        </Link>
                      </PermissionGuard>
                    </div>
                    
                    {product.modules && product.modules.length > 0 ? (
                      <div style={{ display: 'grid', gap: '0.75rem' }}>
                        {product.modules.map((module) => (
                          <div 
                            key={module.id} 
                            style={{ 
                              padding: '0.75rem',
                              backgroundColor: '#f9fafb',
                              borderRadius: '0.375rem',
                              border: '1px solid #e5e7eb'
                            }}
                          >
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                              <div>
                                <h5 style={{ margin: 0, fontSize: '0.875rem', fontWeight: '600' }}>
                                  <Link 
                                    to={`/products/${product.id}/modules/${module.id}`}
                                    style={{ textDecoration: 'none', color: '#2563eb' }}
                                  >
                                    {module.name}
                                  </Link>
                                </h5>
                                <p style={{ margin: '0.25rem 0 0 0', color: '#6b7280', fontSize: '0.75rem' }}>
                                  Code: {module.code} | Version: {module.version}
                                </p>
                                {module.description && (
                                  <p style={{ margin: '0.25rem 0 0 0', color: '#4b5563', fontSize: '0.75rem' }}>
                                    {module.description}
                                  </p>
                                )}
                              </div>
                              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                                <span 
                                  style={{ 
                                    padding: '0.125rem 0.5rem',
                                    borderRadius: '9999px',
                                    fontSize: '0.625rem',
                                    fontWeight: '500',
                                    backgroundColor: module.status === 'active' ? '#dcfce7' : 
                                                   module.status === 'inactive' ? '#fef3c7' : '#fee2e2',
                                    color: module.status === 'active' ? '#166534' : 
                                           module.status === 'inactive' ? '#92400e' : '#991b1b'
                                  }}
                                >
                                  {module.status}
                                </span>
                                <PermissionGuard permissions={['module:update']}>
                                  <Link 
                                    to={`/products/${product.id}/modules/${module.id}/edit`}
                                    className="btn"
                                    style={{ padding: '0.125rem 0.5rem', fontSize: '0.75rem' }}
                                  >
                                    Edit
                                  </Link>
                                </PermissionGuard>
                              </div>
                            </div>
                          </div>
                        ))}
                      </div>
                    ) : (
                      <div style={{ textAlign: 'center', padding: '1rem', color: '#6b7280', fontSize: '0.875rem' }}>
                        No modules found for this product.
                      </div>
                    )}
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}