import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { PermissionGuard } from './PermissionGuard'
import api from '../lib/api'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from './ui/card'
import { Button } from './ui/button'
import { Input } from './ui/input'
import { Label } from './ui/label'
import { Textarea } from './ui/textarea'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from './ui/select'
import { Alert, AlertDescription } from './ui/alert'
import { Skeleton } from './ui/skeleton'
import { Separator } from './ui/separator'
import { ArrowLeft, Save, X } from 'lucide-react'

interface ModuleFormData {
  name: string
  description: string
  code: string
  productId: string
  isActive: boolean
}

interface Product {
  id: number
  name: string
}

const ModuleCreate: React.FC = () => {
  const [formData, setFormData] = useState<ModuleFormData>({
    name: '',
    description: '',
    code: '',
    productId: '',
    isActive: true
  })
  const [products, setProducts] = useState<Product[]>([])
  const [loading, setLoading] = useState(false)
  const [errors, setErrors] = useState<Record<string, string>>({})

  const navigate = useNavigate()

  useEffect(() => {
    fetchProducts()
  }, [])

  const fetchProducts = async () => {
    try {
      const response = await api.get('/products')
      setProducts(response.data || [])
    } catch (err) {
      console.error('Error fetching products:', err)
      setErrors({ submit: 'Failed to load products. Please try again.' })
    }
  }

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {}

    if (!formData.name.trim()) {
      newErrors.name = 'Module name is required'
    } else if (formData.name.length < 2) {
      newErrors.name = 'Module name must be at least 2 characters long'
    }

    if (!formData.description.trim()) {
      newErrors.description = 'Description is required'
    } else if (formData.description.length < 10) {
      newErrors.description = 'Description must be at least 10 characters long'
    }

    if (!formData.code.trim()) {
      newErrors.code = 'Module code is required'
    } else if (!/^[A-Z0-9_]+$/.test(formData.code)) {
      newErrors.code = 'Module code must contain only uppercase letters, numbers, and underscores'
    }

    if (!formData.productId) {
      newErrors.productId = 'Product selection is required'
    }

    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    
    if (!validateForm()) {
      return
    }

    setLoading(true)
    try {
      const moduleData = {
        name: formData.name,
        description: formData.description,
        code: formData.code,
        productId: parseInt(formData.productId),
        isActive: formData.isActive
      }
      
      await api.post('/v1/modules', moduleData)
      
      // Navigate back to modules list
      navigate('/modules')
    } catch (error) {
      console.error('Error creating module:', error)
      setErrors({ submit: 'Failed to create module. Please try again.' })
    } finally {
      setLoading(false)
    }
  }

  const handleInputChange = (field: keyof ModuleFormData, value: string | boolean) => {
    setFormData(prev => ({ ...prev, [field]: value }))
    // Clear error when user starts typing
    if (errors[field]) {
      setErrors(prev => ({ ...prev, [field]: '' }))
    }
  }

  const handleCancel = () => {
    navigate('/modules')
  }

  return (
    <PermissionGuard permission="module:create">
      <div className="min-h-screen bg-gray-50 py-8">
        <div className="max-w-2xl mx-auto px-4 sm:px-6 lg:px-8">
          <Card>
            <CardHeader>
              <div className="flex justify-between items-center">
                <div>
                  <CardTitle className="text-2xl">Create New Module</CardTitle>
                  <CardDescription>Add a new module to the system</CardDescription>
                </div>
                <Button variant="outline" onClick={() => navigate('/modules')}>
                  <ArrowLeft className="h-4 w-4 mr-2" />
                  Back to List
                </Button>
              </div>
            </CardHeader>

            <CardContent>
              {errors.submit && (
                <Alert variant="destructive" className="mb-6">
                  <AlertDescription>{errors.submit}</AlertDescription>
                </Alert>
              )}

              {loading ? (
                <div className="space-y-6">
                  {Array.from({ length: 5 }).map((_, i) => (
                    <div key={i}>
                      <Skeleton className="h-4 w-24 mb-2" />
                      <Skeleton className="h-10 w-full" />
                    </div>
                  ))}
                </div>
              ) : (
                <form onSubmit={handleSubmit} className="space-y-6">
                  <div>
                    <Label htmlFor="name">Module Name *</Label>
                    <Input
                      id="name"
                      value={formData.name}
                      onChange={(e) => handleInputChange('name', e.target.value)}
                      className={errors.name ? 'border-red-300' : ''}
                      placeholder="Enter module name"
                    />
                    {errors.name && (
                      <p className="mt-1 text-sm text-red-600">{errors.name}</p>
                    )}
                  </div>

                  <div>
                    <Label htmlFor="description">Description *</Label>
                    <Textarea
                      id="description"
                      rows={4}
                      value={formData.description}
                      onChange={(e) => handleInputChange('description', e.target.value)}
                      className={errors.description ? 'border-red-300' : ''}
                      placeholder="Enter module description"
                    />
                    {errors.description && (
                      <p className="mt-1 text-sm text-red-600">{errors.description}</p>
                    )}
                  </div>

                  <div>
                    <Label htmlFor="code">Module Code *</Label>
                    <Input
                      id="code"
                      value={formData.code}
                      onChange={(e) => handleInputChange('code', e.target.value.toUpperCase())}
                      className={errors.code ? 'border-red-300' : ''}
                      placeholder="Enter module code (e.g., USER_MGMT)"
                    />
                    {errors.code && (
                      <p className="mt-1 text-sm text-red-600">{errors.code}</p>
                    )}
                  </div>

                  <div>
                    <Label htmlFor="productId">Product *</Label>
                    <Select 
                      value={formData.productId} 
                      onValueChange={(value) => handleInputChange('productId', value)}
                    >
                      <SelectTrigger className={errors.productId ? 'border-red-300' : ''}>
                        <SelectValue placeholder="Select a product" />
                      </SelectTrigger>
                      <SelectContent>
                        {products.map(product => (
                          <SelectItem key={product.id} value={product.id.toString()}>
                            {product.name}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    {errors.productId && (
                      <p className="mt-1 text-sm text-red-600">{errors.productId}</p>
                    )}
                  </div>

                  <div className="flex items-center space-x-2">
                    <input
                      type="checkbox"
                      id="isActive"
                      checked={formData.isActive}
                      onChange={(e) => handleInputChange('isActive', e.target.checked)}
                      className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                    />
                    <Label htmlFor="isActive">Active</Label>
                  </div>

                  <Separator />

                  <div className="flex justify-end space-x-3">
                    <Button 
                      type="button" 
                      variant="outline" 
                      onClick={() => navigate('/modules')}
                    >
                      <X className="h-4 w-4 mr-2" />
                      Cancel
                    </Button>
                    <Button type="submit" disabled={loading}>
                       <Save className="h-4 w-4 mr-2" />
                       {loading ? 'Creating...' : 'Create Module'}
                     </Button>
                  </div>
                </form>
              )}
            </CardContent>
          </Card>
        </div>
      </div>
    </PermissionGuard>
  )
}

export default ModuleCreate