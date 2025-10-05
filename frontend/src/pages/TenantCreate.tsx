import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Building2, ArrowLeft, Save } from 'lucide-react'
import api from '../lib/api'

interface TenantFormData {
  tenantCode: string
  name: string
  type: 'BUSINESS_IN' | 'BUSINESS_OUT' | 'INDIVIDUAL'
  status: 'ACTIVE' | 'INACTIVE'
}

export default function TenantCreate() {
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)
  const [formData, setFormData] = useState<TenantFormData>({
    tenantCode: '',
    name: '',
    type: 'BUSINESS_IN',
    status: 'ACTIVE'
  })
  const [errors, setErrors] = useState<Partial<TenantFormData>>({})

  const validateForm = (): boolean => {
    const newErrors: Partial<TenantFormData> = {}

    if (!formData.tenantCode.trim()) {
      newErrors.tenantCode = 'Tenant code is required'
    } else if (!/^[A-Z0-9_]+$/.test(formData.tenantCode)) {
      newErrors.tenantCode = 'Tenant code must contain only uppercase letters, numbers, and underscores'
    }

    if (!formData.name.trim()) {
      newErrors.name = 'Tenant name is required'
    } else if (formData.name.length < 2) {
      newErrors.name = 'Tenant name must be at least 2 characters long'
    }

    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    
    if (!validateForm()) {
      return
    }

    try {
      setLoading(true)
      await api.post('/tenants', formData)
      navigate('/tenants')
    } catch (error: any) {
      console.error('Failed to create tenant:', error)
      if (error.response?.data?.message) {
        setErrors({ tenantCode: error.response.data.message })
      }
    } finally {
      setLoading(false)
    }
  }

  const handleInputChange = (field: keyof TenantFormData, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }))
    if (errors[field]) {
      setErrors(prev => ({ ...prev, [field]: undefined }))
    }
  }

  return (
    <div className="p-6">
      {/* Header */}
      <div className="flex items-center gap-4 mb-6">
        <button
          onClick={() => navigate('/tenants')}
          className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
        >
          <ArrowLeft className="h-5 w-5" />
        </button>
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Create New Tenant</h1>
          <p className="text-gray-600">Add a new tenant organization to the system</p>
        </div>
      </div>

      {/* Form */}
      <div className="max-w-2xl">
        <div className="bg-white rounded-lg shadow-sm border p-6">
          <form onSubmit={handleSubmit} className="space-y-6">
            {/* Tenant Code */}
            <div>
              <label htmlFor="tenantCode" className="block text-sm font-medium text-gray-700 mb-2">
                Tenant Code *
              </label>
              <input
                type="text"
                id="tenantCode"
                value={formData.tenantCode}
                onChange={(e) => handleInputChange('tenantCode', e.target.value.toUpperCase())}
                className={`w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                  errors.tenantCode ? 'border-red-300' : 'border-gray-300'
                }`}
                placeholder="e.g., ACME_CORP"
                maxLength={50}
              />
              {errors.tenantCode && (
                <p className="mt-1 text-sm text-red-600">{errors.tenantCode}</p>
              )}
              <p className="mt-1 text-sm text-gray-500">
                Unique identifier for the tenant (uppercase letters, numbers, and underscores only)
              </p>
            </div>

            {/* Tenant Name */}
            <div>
              <label htmlFor="name" className="block text-sm font-medium text-gray-700 mb-2">
                Tenant Name *
              </label>
              <input
                type="text"
                id="name"
                value={formData.name}
                onChange={(e) => handleInputChange('name', e.target.value)}
                className={`w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                  errors.name ? 'border-red-300' : 'border-gray-300'
                }`}
                placeholder="e.g., Acme Corporation"
                maxLength={100}
              />
              {errors.name && (
                <p className="mt-1 text-sm text-red-600">{errors.name}</p>
              )}
            </div>

            {/* Tenant Type */}
            <div>
              <label htmlFor="type" className="block text-sm font-medium text-gray-700 mb-2">
                Tenant Type *
              </label>
              <select
                id="type"
                value={formData.type}
                onChange={(e) => handleInputChange('type', e.target.value as TenantFormData['type'])}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                <option value="BUSINESS_IN">Business Internal</option>
                <option value="BUSINESS_OUT">Business External</option>
                <option value="INDIVIDUAL">Individual</option>
              </select>
              <p className="mt-1 text-sm text-gray-500">
                Select the type of tenant organization
              </p>
            </div>

            {/* Status */}
            <div>
              <label htmlFor="status" className="block text-sm font-medium text-gray-700 mb-2">
                Initial Status *
              </label>
              <select
                id="status"
                value={formData.status}
                onChange={(e) => handleInputChange('status', e.target.value as TenantFormData['status'])}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                <option value="ACTIVE">Active</option>
                <option value="INACTIVE">Inactive</option>
              </select>
              <p className="mt-1 text-sm text-gray-500">
                Set the initial status for the tenant
              </p>
            </div>

            {/* Form Actions */}
            <div className="flex justify-end space-x-3 pt-6 border-t">
              <button
                type="button"
                onClick={() => navigate('/tenants')}
                className="px-4 py-2 text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors"
                disabled={loading}
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={loading}
                className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg flex items-center gap-2 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {loading ? (
                  <>
                    <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                    Creating...
                  </>
                ) : (
                  <>
                    <Save className="h-4 w-4" />
                    Create Tenant
                  </>
                )}
              </button>
            </div>
          </form>
        </div>

        {/* Info Card */}
        <div className="mt-6 bg-blue-50 border border-blue-200 rounded-lg p-4">
          <div className="flex items-start">
            <Building2 className="h-5 w-5 text-blue-600 mt-0.5 mr-3" />
            <div>
              <h3 className="text-sm font-medium text-blue-900">About Tenants</h3>
              <p className="text-sm text-blue-700 mt-1">
                Tenants represent separate organizations or business units within the system. 
                Each tenant has its own isolated data and user access controls.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}