import React, { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { ArrowLeft, Save, Package, AlertCircle } from 'lucide-react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Skeleton } from "@/components/ui/skeleton"
import { PermissionGuard } from '../components/PermissionGuard'
import { usePermissions } from '../hooks/usePermissions'
import type { Product } from '../store/auth'
import api from '../lib/api'

const ProductEdit: React.FC = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { canUpdateProducts } = usePermissions()
  
  const [product, setProduct] = useState<Product | null>(null)
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    code: '',
    status: 'active' as 'active' | 'inactive',
    category: '',
    version: ''
  })
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})

  useEffect(() => {
    if (!canUpdateProducts) {
      navigate('/unauthorized')
      return
    }

    fetchProduct()
  }, [id, canUpdateProducts, navigate])

  const validateForm = () => {
    const errors: Record<string, string> = {}
    
    if (!formData.name.trim()) {
      errors.name = 'Product name is required'
    } else if (formData.name.trim().length < 2) {
      errors.name = 'Product name must be at least 2 characters'
    }
    
    if (!formData.code.trim()) {
      errors.code = 'Product code is required'
    } else if (!/^[A-Z0-9_-]+$/i.test(formData.code.trim())) {
      errors.code = 'Product code can only contain letters, numbers, hyphens, and underscores'
    }
    
    if (formData.version && !/^\d+\.\d+\.\d+$/.test(formData.version.trim())) {
      errors.version = 'Version must be in format X.Y.Z (e.g., 1.0.0)'
    }
    
    setFieldErrors(errors)
    return Object.keys(errors).length === 0
  }

  const fetchProduct = async () => {
    try {
      setLoading(true)
      const response = await api.get(`/products/${id}`)
      const productData = response.data

      const transformedProduct: Product = {
        id: productData.id,
        name: productData.name,
        description: productData.description,
        code: productData.productCode,
        status: productData.productStatus?.toLowerCase() || 'active',
        category: productData.category || 'general',
        version: productData.version || '1.0.0',
        createdAt: productData.createdAt,
        updatedAt: productData.updatedAt,
        createdBy: productData.createdBy || 'system',
        updatedBy: productData.updatedBy || 'system'
      }

      setProduct(transformedProduct)
      setFormData({
        name: transformedProduct.name,
        description: transformedProduct.description,
        code: transformedProduct.code,
        status: transformedProduct.status,
        category: transformedProduct.category,
        version: transformedProduct.version
      })
    } catch (error) {
      console.error('Error fetching product:', error)
      setError('Failed to load product data')
    } finally {
      setLoading(false)
    }
  }

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value } = e.target
    setFormData(prev => ({
      ...prev,
      [name]: value
    }))
    
    // Clear field error when user starts typing
    if (fieldErrors[name]) {
      setFieldErrors(prev => ({
        ...prev,
        [name]: ''
      }))
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    
    if (!validateForm()) {
      setError('Please fix the errors below')
      return
    }

    try {
      setSaving(true)
      setError(null)
      setFieldErrors({})

      const updateData = {
        name: formData.name.trim(),
        description: formData.description.trim(),
        productCode: formData.code.trim(),
        productStatus: formData.status.toUpperCase(),
        category: formData.category.trim(),
        version: formData.version.trim()
      }

      await api.put(`/products/${id}`, updateData)
      navigate(`/products/${id}`)
    } catch (error: any) {
      console.error('Error updating product:', error)
      if (error.response?.status === 409) {
        setError('A product with this code already exists')
      } else if (error.response?.status === 400) {
        setError('Invalid product data. Please check your inputs.')
      } else {
        setError('Failed to update product. Please try again.')
      }
    } finally {
      setSaving(false)
    }
  }

  const handleCancel = () => {
    navigate(`/products/${id}`)
  }

  if (!canUpdateProducts) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <Card className="w-full max-w-md">
          <CardHeader>
            <CardTitle>Access Denied</CardTitle>
            <CardDescription>You don't have permission to edit products.</CardDescription>
          </CardHeader>
          <CardContent>
            <Button onClick={() => navigate('/dashboard')} className="w-full">
              <ArrowLeft className="mr-2 h-4 w-4" />
              Back to Dashboard
            </Button>
          </CardContent>
        </Card>
      </div>
    )
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 py-8">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="space-y-6">
            <Skeleton className="h-8 w-64" />
            <Card>
              <CardContent className="p-6">
                <div className="space-y-6">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <Skeleton className="h-20" />
                    <Skeleton className="h-20" />
                    <Skeleton className="h-20" />
                    <Skeleton className="h-20" />
                  </div>
                  <Skeleton className="h-32" />
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    )
  }

  if (error && !product) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <Card className="w-full max-w-md">
          <CardHeader>
            <CardTitle>Error</CardTitle>
            <CardDescription className="text-destructive">{error}</CardDescription>
          </CardHeader>
          <CardContent>
            <Button onClick={() => navigate('/products')} className="w-full">
              <ArrowLeft className="mr-2 h-4 w-4" />
              Back to Products
            </Button>
          </CardContent>
        </Card>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="mb-8 flex items-center gap-4">
          <Button variant="ghost" size="sm" onClick={() => navigate(`/products/${id}`)}>
            <ArrowLeft className="h-4 w-4" />
          </Button>
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Edit Product</h1>
            <p className="mt-2 text-gray-600">Update product information and settings</p>
          </div>
        </div>

        {error && (
          <Alert variant="destructive" className="mb-6">
            <AlertCircle className="h-4 w-4" />
            <AlertDescription>{error}</AlertDescription>
          </Alert>
        )}

        <div className="space-y-6">
          {/* Product Information Card */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Package className="h-5 w-5" />
                Product Information
              </CardTitle>
              <CardDescription>
                Update the details for this product
              </CardDescription>
            </CardHeader>
            <CardContent>
              <form onSubmit={handleSubmit} className="space-y-6">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                   <div className="space-y-2">
                     <Label htmlFor="name">Product Name *</Label>
                     <Input
                       id="name"
                       name="name"
                       value={formData.name}
                       onChange={handleInputChange}
                       placeholder="Enter product name"
                       required
                       className={fieldErrors.name ? "border-red-500" : ""}
                     />
                     {fieldErrors.name && (
                       <p className="text-sm text-red-500 flex items-center gap-1">
                         <AlertCircle className="h-3 w-3" />
                         {fieldErrors.name}
                       </p>
                     )}
                   </div>

                   <div className="space-y-2">
                     <Label htmlFor="code">Product Code *</Label>
                     <Input
                       id="code"
                       name="code"
                       value={formData.code}
                       onChange={handleInputChange}
                       placeholder="Enter product code"
                       required
                       className={fieldErrors.code ? "border-red-500" : ""}
                     />
                     {fieldErrors.code && (
                       <p className="text-sm text-red-500 flex items-center gap-1">
                         <AlertCircle className="h-3 w-3" />
                         {fieldErrors.code}
                       </p>
                     )}
                   </div>

                   <div className="space-y-2">
                     <Label htmlFor="category">Category</Label>
                     <Input
                       id="category"
                       name="category"
                       value={formData.category}
                       onChange={handleInputChange}
                       placeholder="Enter category"
                     />
                   </div>

                   <div className="space-y-2">
                     <Label htmlFor="version">Version</Label>
                     <Input
                       type="text"
                       id="version"
                       name="version"
                       value={formData.version}
                       onChange={handleInputChange}
                       placeholder="Enter version (e.g., 1.0.0)"
                       className={fieldErrors.version ? "border-red-500" : ""}
                     />
                     {fieldErrors.version && (
                       <p className="text-sm text-red-500 flex items-center gap-1">
                         <AlertCircle className="h-3 w-3" />
                         {fieldErrors.version}
                       </p>
                     )}
                   </div>

                   <div className="space-y-2">
                     <Label htmlFor="status">Status</Label>
                     <Select value={formData.status} onValueChange={(value) => setFormData(prev => ({ ...prev, status: value as 'active' | 'inactive' }))}>
                       <SelectTrigger>
                         <SelectValue placeholder="Select status" />
                       </SelectTrigger>
                       <SelectContent>
                         <SelectItem value="active">Active</SelectItem>
                         <SelectItem value="inactive">Inactive</SelectItem>
                       </SelectContent>
                     </Select>
                   </div>
                 </div>

                <div className="space-y-2">
                  <Label htmlFor="description">Description</Label>
                  <Textarea
                    id="description"
                    name="description"
                    value={formData.description}
                    onChange={handleInputChange}
                    rows={4}
                    placeholder="Enter product description"
                  />
                </div>

                <div className="flex justify-end space-x-4 pt-6 border-t">
                  <Button
                    type="button"
                    variant="outline"
                    onClick={handleCancel}
                    disabled={saving}
                  >
                    Cancel
                  </Button>
                  <Button
                    type="submit"
                    disabled={saving}
                  >
                    <Save className="w-4 h-4 mr-2" />
                    {saving ? 'Saving...' : 'Save Changes'}
                  </Button>
                </div>
              </form>
            </CardContent>
          </Card>

          {/* Metadata Card */}
          {product && (
            <Card>
              <CardHeader>
                <CardTitle>Metadata</CardTitle>
                <CardDescription>
                  Read-only information about this product
                </CardDescription>
              </CardHeader>
              <CardContent>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div className="space-y-2">
                    <Label>Created At</Label>
                    <Input
                      value={new Date(product.createdAt).toLocaleString()}
                      readOnly
                      className="bg-muted"
                    />
                  </div>

                  <div className="space-y-2">
                    <Label>Updated At</Label>
                    <Input
                      value={new Date(product.updatedAt).toLocaleString()}
                      readOnly
                      className="bg-muted"
                    />
                  </div>

                  <div className="space-y-2">
                    <Label>Created By</Label>
                    <Input
                      value={product.createdBy || 'N/A'}
                      readOnly
                      className="bg-muted"
                    />
                  </div>

                  <div className="space-y-2">
                    <Label>Updated By</Label>
                    <Input
                      value={product.updatedBy || 'N/A'}
                      readOnly
                      className="bg-muted"
                    />
                  </div>
                </div>
              </CardContent>
            </Card>
          )}
        </div>
    </div>
  </div>
  )
}

export default ProductEdit