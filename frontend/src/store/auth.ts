import { create } from 'zustand'

export interface Permission {
  id: string
  name: string
  resource: string
  action: string
  conditions?: Record<string, any>
}

export interface Role {
  id: string
  name: string
  description?: string
  permissions: Permission[]
  tenantId?: string
}

export interface Tenant {
  id: string
  name: string
  code: string
  type: 'enterprise' | 'standard' | 'basic'
  status: 'active' | 'inactive' | 'suspended'
  createdAt: string
  updatedAt: string
}

export interface UserProfile {
  id: string
  email: string
  name: string
  roles: Role[]
  tenantId?: string
  permissions: Permission[]
  lastLoginAt?: string
  createdAt: string
}

export interface Product {
  id: number
  name: string
  description: string
  code: string
  status: 'active' | 'inactive'
  category: string
  version: string
  createdAt: string
  updatedAt: string
  createdBy: string
  updatedBy: string
}

export interface Module {
  id: number
  name: string
  description: string
  code: string
  status: 'active' | 'inactive'
  productId: number
  version: string
  createdAt: string
  updatedAt: string
}

type AuthState = {
  token: string | null
  user: UserProfile | null
  tenant: Tenant | null
  permissions: string[]
  isAuthenticated: boolean
  setToken: (token: string | null) => void
  setUser: (user: UserProfile | null) => void
  setTenant: (tenant: Tenant | null) => void
  hasPermission: (permission: string) => boolean
  hasRole: (roleName: string) => boolean
  hasAnyRole: (roleNames: string[]) => boolean
  canAccessResource: (resource: string, action: string) => boolean
  logout: () => void
}

export const useAuth = create<AuthState>((set, get) => ({
  token: localStorage.getItem('token'),
  user: null,
  tenant: null,
  permissions: [],
  isAuthenticated: false,
  
  setToken: (token) => {
    if (token) {
      localStorage.setItem('token', token)
      set({ token, isAuthenticated: true })
    } else {
      localStorage.removeItem('token')
      set({ token, isAuthenticated: false, user: null, tenant: null, permissions: [] })
    }
  },
  
  setUser: (user) => {
    const permissions = user?.permissions.map(p => p.name) || []
    set({ user, permissions })
  },
  
  setTenant: (tenant) => {
    set({ tenant })
  },
  
  hasPermission: (permission: string) => {
    const { permissions } = get()
    return permissions.includes(permission)
  },
  
  hasRole: (roleName: string) => {
    const { user } = get()
    return user?.roles.some(role => role.name === roleName) || false
  },
  
  hasAnyRole: (roleNames: string[]) => {
    const { user } = get()
    return user?.roles.some(role => roleNames.includes(role.name)) || false
  },
  
  canAccessResource: (resource: string, action: string) => {
    const { user } = get()
    return user?.permissions.some(p => 
      p.resource === resource && p.action === action
    ) || false
  },
  
  logout: () => {
    localStorage.removeItem('token')
    set({ 
      token: null, 
      user: null, 
      tenant: null, 
      permissions: [], 
      isAuthenticated: false 
    })
  },
}))