# Frontend Technical Documentation

## Overview

The AHSS Shared Services frontend is a comprehensive React application built with TypeScript, providing a complete Role-Based Access Control (RBAC) and Attribute-Based Access Control (ABAC) system. The application manages users, tenants, products, modules, roles, and permissions with a sophisticated permission system. It follows modern React patterns with functional components, hooks, and a clean architecture.

## Technology Stack

### Core Technologies
- **Framework**: React 19.1.1 with TypeScript
- **Build Tool**: Vite 7.1.7
- **Styling**: TailwindCSS 4.1.14
- **UI Components**: Shadcn/UI (integrated component library)
- **Routing**: React Router DOM 7.9.3
- **State Management**: Zustand 5.0.8
- **HTTP Client**: Axios 1.12.2
- **Form Handling**: React Hook Form 7.64.0
- **Validation**: Zod 4.1.11
- **Icons**: Lucide React (integrated with Shadcn/UI)

### Development Dependencies
- **TypeScript**: ~5.9.3
- **ESLint**: ^9.36.0 with React plugins
- **PostCSS**: ^8.5.6
- **Autoprefixer**: ^10.4.21

## Project Structure

```
frontend/
├── public/
│   └── vite.svg                 # Application icon
├── src/
│   ├── assets/
│   │   └── react.svg           # React logo asset
│   ├── components/
│   │   ├── ui/                 # Shadcn/UI component library
│   │   │   ├── alert.tsx       # Alert component for notifications
│   │   │   ├── badge.tsx       # Badge component for status indicators
│   │   │   ├── button.tsx      # Button component
│   │   │   ├── card.tsx        # Card component
│   │   │   ├── dialog.tsx      # Dialog component
│   │   │   ├── input.tsx       # Input component
│   │   │   ├── label.tsx       # Label component
│   │   │   ├── navigation-menu.tsx # Navigation menu component
│   │   │   ├── select.tsx      # Select component
│   │   │   ├── separator.tsx   # Separator component for visual division
│   │   │   ├── sheet.tsx       # Sheet component for slide-out panels
│   │   │   ├── sidebar.tsx     # Sidebar component for navigation layout
│   │   │   ├── skeleton.tsx    # Skeleton loading component
│   │   │   ├── table.tsx       # Table component
│   │   │   ├── textarea.tsx    # Textarea component
│   │   │   └── tooltip.tsx     # Tooltip component for contextual information
│   │   ├── Layout.tsx          # Main application layout with shadcn/ui Sidebar
│   │   ├── PermissionGuard.tsx # Permission-based component guard
│   │   └── ProtectedRoute.tsx  # Route-level permission protection
│   ├── hooks/
│   │   ├── use-mobile.tsx      # Hook for mobile device detection
│   │   └── usePermissions.ts   # Custom hooks for permission management
│   ├── lib/
│   │   ├── api.ts              # Axios configuration with JWT interceptors
│   │   └── utils.ts            # Utility functions (cn helper for Shadcn/UI)
│   ├── pages/
│   │   ├── Dashboard.tsx       # Main dashboard with statistics and quick actions
│   │   ├── Login.tsx           # Authentication page
│   │   ├── ProductCreate.tsx   # Product creation form
│   │   ├── ProductDetail.tsx   # Product details and module management
│   │   ├── ProductList.tsx     # Product listing with hierarchical modules
│   │   ├── TenantCreate.tsx    # Tenant creation form
│   │   ├── TenantDetail.tsx    # Tenant details and management
│   │   ├── TenantList.tsx      # Tenant listing and management
│   │   ├── Unauthorized.tsx    # Access denied page
│   │   └── UserGroups.tsx      # User groups management page
│   ├── store/
│   │   └── auth.ts             # Zustand store for authentication and permissions
│   ├── App.tsx                 # Main application component with routing
│   ├── App.css                 # Component-specific styles
│   ├── index.css               # Global styles and custom CSS
│   └── main.tsx                # Application entry point
├── components.json             # Shadcn/UI configuration
├── package.json                # Dependencies and scripts
├── vite.config.ts             # Vite configuration
├── tailwind.config.js         # TailwindCSS configuration
└── tsconfig.json              # TypeScript configuration
```

## Architecture

### Component Architecture
```
App (Router Provider)
├── Layout (Navigation & Header)
│   ├── Navigation Menu (Permission-based)
│   ├── User Profile & Tenant Info
│   └── Logout Functionality
└── Main Content (Route Container)
    ├── Dashboard (Statistics & Quick Actions)
    ├── Authentication Pages
    │   ├── Login
    │   └── Unauthorized
    ├── User Management
    │   └── UserGroups
    ├── Tenant Management
    │   ├── TenantList
    │   ├── TenantCreate
    │   └── TenantDetail
    └── Product Management
        ├── ProductList (with Module hierarchy)
        ├── ProductCreate
        └── ProductDetail (with Module management)
```

### Permission System Architecture
```
User Authentication → JWT Token → User Profile with Roles & Permissions
                                        ↓
Permission Guards → Component Level → PermissionGuard
                 → Route Level → ProtectedRoute
                 → Hook Level → usePermissions
                                        ↓
RBAC/ABAC Evaluation → Resource-Action-Condition Checks
```

### Recent Permission System Updates

**Tenant Permission Standardization (Latest Update)**
- **Issue Fixed**: Mismatch between JWT token permissions and frontend permission checks
- **Change**: Updated all tenant-related permissions from singular to plural form
  - `tenant:read` → `tenants:read`
  - `tenant:create` → `tenants:create`
  - `tenant:update` → `tenants:update`
  - `tenant:delete` → `tenants:delete`
- **Files Updated**: 
  - `usePermissions.ts` - Core permission hook definitions
  - `Dashboard.tsx` - Dashboard permission guards
  - `TenantDetail.tsx` - Tenant detail page permissions
  - `TenantList.tsx` - Tenant listing permissions
- **Impact**: Fixed "Tenant" menu item visibility issue for superadmin and admin users

### State Management Flow
```
User Action → Component → Permission Check → API Call → Backend
                ↓              ↓                ↓
            UI Update ← Zustand Store ← JWT Token ← Response
                ↓
        Local Storage Persistence
```

## UI Components (Shadcn/UI)

The application uses Shadcn/UI, a modern component library built on top of Radix UI primitives and styled with TailwindCSS. This provides a consistent, accessible, and customizable design system throughout the application.

### Available Components

#### Core UI Components
- **Alert** (`src/components/ui/alert.tsx`): Alert component for displaying notifications and important messages with different variants (default, destructive)
- **Badge** (`src/components/ui/badge.tsx`): Badge component for status indicators and labels with multiple variants (default, secondary, destructive, outline)
- **Button** (`src/components/ui/button.tsx`): Versatile button component with multiple variants (default, destructive, outline, secondary, ghost, link) and sizes
- **Card** (`src/components/ui/card.tsx`): Container component with header, content, and footer sections for organizing content
- **Input** (`src/components/ui/input.tsx`): Form input component with consistent styling and validation states
- **Label** (`src/components/ui/label.tsx`): Accessible form labels with proper association to form controls
- **Navigation Menu** (`src/components/ui/navigation-menu.tsx`): Navigation menu component with dropdown support and keyboard navigation
- **Separator** (`src/components/ui/separator.tsx`): Visual separator component for dividing content sections
- **Skeleton** (`src/components/ui/skeleton.tsx`): Loading placeholder component for better user experience during data fetching
- **Tooltip** (`src/components/ui/tooltip.tsx`): Contextual information component that appears on hover or focus

#### Advanced Components
- **Dialog** (`src/components/ui/dialog.tsx`): Modal dialog component for overlays and confirmations
- **Select** (`src/components/ui/select.tsx`): Dropdown selection component with search and multi-select capabilities
- **Sheet** (`src/components/ui/sheet.tsx`): Slide-out panel component for mobile-friendly navigation and content display
- **Sidebar** (`src/components/ui/sidebar.tsx`): Comprehensive sidebar navigation component with collapsible functionality, mobile responsiveness, and keyboard shortcuts
- **Table** (`src/components/ui/table.tsx`): Data table component with sorting, filtering, and pagination support
- **Textarea** (`src/components/ui/textarea.tsx`): Multi-line text input component for longer content

### Integration Benefits
- **Consistency**: Unified design language across all pages and components
- **Accessibility**: Built-in ARIA attributes and keyboard navigation support
- **Customization**: Easy theming through TailwindCSS variables and CSS custom properties
- **Performance**: Tree-shakable components with minimal bundle impact
- **Developer Experience**: TypeScript support with comprehensive prop interfaces

### Usage Examples

```typescript
// Card component usage in Dashboard
<Card>
  <CardHeader>
    <CardTitle>Total Users</CardTitle>
    <CardDescription>Active users in the system</CardDescription>
  </CardHeader>
  <CardContent>
    <div className="text-2xl font-bold">{userCount}</div>
  </CardContent>
</Card>

// Form components usage in Login
<div className="space-y-2">
  <Label htmlFor="email">Email</Label>
  <Input
    id="email"
    type="email"
    placeholder="Enter your email"
    value={email}
    onChange={(e) => setEmail(e.target.value)}
  />
</div>

// Loading state with Skeleton
{isLoading ? (
  <Skeleton className="h-4 w-[250px]" />
) : (
  <p>{content}</p>
)}

// Sidebar Layout Implementation
<SidebarProvider>
  <Sidebar>
    <SidebarHeader>
      <h2 className="text-lg font-semibold">AHSS Shared Services</h2>
    </SidebarHeader>
    <SidebarContent>
      <SidebarGroup>
        <SidebarGroupLabel>Navigation</SidebarGroupLabel>
        <SidebarGroupContent>
          <SidebarMenu>
            {navigationItems.map((item) => (
              <PermissionGuard key={item.path} permission={item.permission}>
                <SidebarMenuItem>
                  <SidebarMenuButton asChild>
                    <Link to={item.path}>
                      <item.icon />
                      <span>{item.label}</span>
                    </Link>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              </PermissionGuard>
            ))}
          </SidebarMenu>
        </SidebarGroupContent>
      </SidebarGroup>
    </SidebarContent>
    <SidebarFooter>
      <div className="flex items-center gap-2">
        <Badge variant="secondary">{user.roles[0]?.name}</Badge>
        <Button variant="ghost" onClick={logout}>
          Logout
        </Button>
      </div>
    </SidebarFooter>
  </Sidebar>
  <main className="flex-1">
    <SidebarTrigger />
    <Outlet />
  </main>
</SidebarProvider>

// Badge usage for status indicators
<Badge variant="default">Active</Badge>
<Badge variant="destructive">Inactive</Badge>
<Badge variant="outline">Pending</Badge>

// Alert usage for notifications
<Alert>
  <AlertCircle className="h-4 w-4" />
  <AlertTitle>Heads up!</AlertTitle>
  <AlertDescription>
    You can add components to your app using the cli.
  </AlertDescription>
</Alert>
```

## Application Entry Point

### main.tsx
```typescript
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import './index.css'
import App from './App'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </StrictMode>
)
```

**Features:**
- React 19 StrictMode for development checks
- BrowserRouter for client-side routing
- Root element mounting with createRoot API

## Main Application Component

### App.tsx
```typescript
function App() {
  const { isAuthenticated } = useAuth()

  return (
    <Router>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/unauthorized" element={<Unauthorized />} />
        
        <Route element={<Layout><Outlet /></Layout>}>
          <Route path="/" element={
            <ProtectedRoute requireAuth>
              <Navigate to="/dashboard" replace />
            </ProtectedRoute>
          } />
          
          <Route path="/dashboard" element={
            <ProtectedRoute requireAuth>
              <Dashboard />
            </ProtectedRoute>
          } />
          
          <Route path="/user-groups" element={
            <ProtectedRoute permission="user:read">
              <UserGroups />
            </ProtectedRoute>
          } />
          
          <Route path="/tenants" element={
            <ProtectedRoute permission="tenant:read">
              <TenantList />
            </ProtectedRoute>
          } />
          
          <Route path="/products" element={
            <ProtectedRoute permission="product:read">
              <ProductList />
            </ProtectedRoute>
          } />
          
          {/* Additional protected routes... */}
        </Route>
      </Routes>
    </Router>
  )
}
```

**Features:**
- Comprehensive routing with nested layouts
- Route-level permission protection
- Automatic redirection for authenticated users
- Centralized layout management
- Permission-based route access control

## Pages and Components

### Core Components

#### Layout Component (`components/Layout.tsx`)

**Purpose**: Main application layout with modern sidebar navigation using shadcn/ui components

**Features:**
- **Modern Sidebar Navigation**: Built with shadcn/ui Sidebar components for a professional, consistent design
- **Collapsible Functionality**: Sidebar can be collapsed/expanded with smooth animations and state persistence
- **Mobile Responsiveness**: Automatically adapts to mobile devices with sheet-based navigation overlay
- **Keyboard Shortcuts**: Built-in keyboard shortcut (Cmd+B / Ctrl+B) for toggling sidebar visibility
- **Permission-based Navigation**: Dynamic menu items based on user permissions and roles
- **User Profile Integration**: Displays user information, roles, and tenant context with Badge components
- **Logout Functionality**: Secure logout with proper token cleanup and state management

**Shadcn/UI Components Used:**
- **SidebarProvider**: Context provider for managing sidebar state and mobile detection
- **Sidebar**: Main sidebar container with collapsible behavior and responsive design
- **SidebarHeader**: Header section with application branding and user information
- **SidebarContent**: Main content area containing navigation groups and menu items
- **SidebarGroup**: Logical grouping of related navigation items
- **SidebarGroupLabel**: Section labels for navigation groups
- **SidebarGroupContent**: Container for navigation menu items
- **SidebarMenu**: Navigation menu container with proper accessibility
- **SidebarMenuItem**: Individual navigation items with active state support
- **SidebarMenuButton**: Interactive navigation buttons with icon and text support
- **SidebarFooter**: Footer section for user profile and logout functionality
- **SidebarTrigger**: Toggle button for sidebar visibility control
- **Badge**: Status indicators for user roles and permissions

**Key Navigation Items:**
- Dashboard (always accessible to authenticated users)
- User Groups (requires `user:read` permission)
- Products (requires `product:read` permission)
- Modules (requires `module:read` permission)
- Roles (requires `role:read` permission)
- Permissions (requires `permission:read` permission)
- Tenants (requires `tenant:read` permission)

**Implementation Highlights:**
- **State Management**: Uses SidebarProvider context for consistent state across components
- **Mobile Detection**: Integrates use-mobile hook for responsive behavior
- **Permission Integration**: Each navigation item is wrapped with PermissionGuard for access control
- **Active State**: Automatic highlighting of current page in navigation
- **Icon Support**: Lucide React icons for visual navigation enhancement
- **Accessibility**: Full keyboard navigation and screen reader support

#### PermissionGuard Component (`components/PermissionGuard.tsx`)

**Purpose**: Component-level permission control

**Features:**
- Granular permission checking at component level
- Support for single permission, role, or multiple roles
- Resource-action based access control
- Fallback content for unauthorized access
- Flexible permission evaluation (requireAll vs any)

```typescript
<PermissionGuard permission="product:update" fallback={<div>Access Denied</div>}>
  <EditButton />
</PermissionGuard>

<PermissionGuard roles={['admin', 'manager']} requireAll={false}>
  <AdminPanel />
</PermissionGuard>
```

#### ProtectedRoute Component (`components/ProtectedRoute.tsx`)

**Purpose**: Route-level permission protection

**Features:**
- Authentication requirement enforcement
- Permission-based route access
- Role-based route protection
- Resource-action authorization
- Automatic redirection to login or unauthorized pages

```typescript
<ProtectedRoute permission="tenant:read" redirectTo="/unauthorized">
  <TenantList />
</ProtectedRoute>
```

### Dashboard Page (`pages/Dashboard.tsx`)

**Purpose**: Main dashboard with system overview and quick actions

**Features:**
- Real-time statistics display (users, tenants, roles, system health)
- Permission-based widget visibility using `PermissionGuard` components
- Quick action buttons for common tasks
- System health monitoring with visual indicators
- Pending approvals notifications
- Responsive grid layout with Shadcn/UI Card components
- Enhanced loading states with Skeleton components
- Modern card-based design with consistent spacing and typography

**UI Components Used:**
- **Card, CardHeader, CardContent, CardTitle, CardDescription**: For all statistical sections and content organization
- **Skeleton**: For loading states instead of traditional spinners
- **Button**: For quick action buttons with consistent styling
- **Icons**: Lucide React icons for visual enhancement (Users, Building2, Shield, Activity, etc.)

**Key Statistics Sections:**
- **Welcome Section**: Personalized greeting with user and tenant information
- **Total Users** (requires `user:read`): Displays user count with growth indicators
- **Active Tenants** (requires `tenant:read`): Shows tenant statistics and status
- **Total Roles** (requires `role:read`): Role management overview
- **System Health** (always visible): Real-time system status monitoring
- **Recent Activities**: Activity feed with timestamps
- **Quick Actions**: Permission-based action buttons for common tasks
- **Pending Approvals** (requires `approval:read`): Workflow management
- **User Growth**: Analytics and trends visualization
- **Permission Distribution**: Security and access overview

**Design Improvements:**
- Consistent card-based layout using Shadcn/UI Card components
- Improved loading experience with skeleton placeholders
- Better visual hierarchy with proper typography scales
- Enhanced accessibility through semantic HTML and ARIA attributes
- Responsive design that works across all device sizes
- Modern color scheme using TailwindCSS design tokens

### Authentication Pages

#### Login Page (`pages/Login.tsx`)

**Purpose**: User authentication interface with modern form design

**Features:**
- Modern card-based login form using Shadcn/UI components
- Form handling with React state management
- JWT token management and secure storage
- Enhanced error handling and loading states with visual feedback
- Automatic redirection after successful login
- Integration with backend authentication API
- Responsive design that works across all devices
- Improved accessibility with proper form labels and ARIA attributes

**UI Components Used:**
- **Card, CardHeader, CardContent, CardTitle, CardDescription**: For the main login form container and structure
- **Input**: For email and password fields with consistent styling and validation states
- **Label**: For accessible form field labels with proper association
- **Button**: For login submission with loading states and disabled states
- **Icons**: Lucide React icons for visual feedback (AlertCircle for errors, Loader2 for loading)

**Form Features:**
- **Email Field**: Proper email input type with validation
- **Password Field**: Secure password input with visibility toggle capability
- **Error Display**: Clear error messaging with visual indicators
- **Loading States**: Button shows loading spinner during authentication
- **Form Validation**: Client-side validation with user-friendly error messages

**Authentication Flow:**
1. User enters credentials in Shadcn/UI form components
2. Form validation ensures proper input format
3. API call to `/auth/login` with loading state indication
4. JWT token extraction and secure storage
5. User profile and permissions loading
6. Automatic redirection to dashboard with success feedback

**Design Improvements:**
- Clean, centered card layout with proper spacing
- Consistent form styling using Shadcn/UI Input and Label components
- Better visual hierarchy with proper typography
- Enhanced loading experience with animated button states
- Improved error handling with clear visual feedback
- Modern color scheme and consistent with application theme

#### Unauthorized Page (`pages/Unauthorized.tsx`)

**Purpose**: Access denied interface

**Features:**
- Clear access denied messaging
- Navigation options (back/dashboard)
- User-friendly error explanation
- Consistent styling with application theme

### Tenant Management Pages

#### TenantList Page (`pages/TenantList.tsx`)

**Purpose**: Tenant listing and management interface

**Features:**
- Paginated tenant listing
- Search and filtering capabilities
- Status-based filtering (Active, Inactive, Suspended)
- Bulk operations support
- Permission-based action buttons
- Real-time tenant statistics

**Key Functions:**
- Create new tenant (requires `tenant:create`)
- Edit tenant details (requires `tenant:update`)
- Delete tenant (requires `tenant:delete`)
- View tenant details (requires `tenant:read`)

#### TenantCreate Page (`pages/TenantCreate.tsx`)

**Purpose**: New tenant creation interface

**Features:**
- Comprehensive tenant creation form
- Tenant type selection (Business In/Out, Individual)
- Form validation and error handling
- Status management
- Integration with tenant creation API

#### TenantDetail Page (`pages/TenantDetail.tsx`)

**Purpose**: Individual tenant management and details

**Features:**
- Complete tenant information display
- Tenant statistics and metrics
- User management within tenant
- Role assignment capabilities
- Activity history and audit logs
- Status management and updates

### Product Management Pages

#### ProductList Page (`pages/ProductList.tsx`)

**Purpose**: Product listing with hierarchical module display

**Features:**
- Hierarchical product-module structure
- Advanced search and filtering
- Category-based organization
- Status management (Active/Inactive)
- Module count and statistics per product
- Permission-based CRUD operations

**Key Features:**
- Product creation (requires `product:create`)
- Product editing (requires `product:update`)
- Product deletion (requires `product:delete`)
- Module viewing within products (requires `module:read`)
- Responsive grid layout with product cards

#### ProductCreate Page (`pages/ProductCreate.tsx`)

**Purpose**: New product creation interface

**Features:**
- Comprehensive product creation form
- Category selection and management
- Version control and status setting
- Form validation with error handling
- Integration with product creation API
- Permission-based access control

**Form Fields:**
- Product name and description
- Product code (unique identifier)
- Category selection
- Version management
- Status (Active/Inactive)

#### ProductDetail Page (`pages/ProductDetail.tsx`)

**Purpose**: Individual product management with module hierarchy

**Features:**
- Complete product information display
- Module management within product
- Product statistics and metrics
- Module creation and management
- Status updates and version control
- Activity tracking and audit logs

**Module Management:**
- View all modules within product
- Create new modules (requires `module:create`)
- Edit module details (requires `module:update`)
- Delete modules (requires `module:delete`)
- Module status management

### User Management Pages

#### UserGroups Page (`pages/UserGroups.tsx`)

**Purpose**: User group management interface

**Features:**
- User group listing and management
- Group creation and editing
- Member management within groups
- Permission assignment to groups
- Role-based access control
- Real-time group statistics

## State Management

### Authentication Store (`store/auth.ts`)

**Technology**: Zustand (lightweight state management)

**Core Interfaces:**
```typescript
interface Permission {
  id: string
  name: string
  resource: string
  action: string
  conditions?: Record<string, any>
}

interface Role {
  id: string
  name: string
  description?: string
  permissions: Permission[]
  tenantId?: string
}

interface UserProfile {
  id: string
  email: string
  name: string
  roles: Role[]
  tenantId?: string
  permissions: Permission[]
  lastLoginAt?: string
  createdAt: string
}

interface Tenant {
  id: string
  name: string
  code: string
  type: 'enterprise' | 'standard' | 'basic'
  status: 'active' | 'inactive' | 'suspended'
  createdAt: string
  updatedAt: string
}
```

**Features:**
- JWT token persistence in localStorage
- Comprehensive user profile management
- Multi-tenant support with tenant context
- Role-based and permission-based access control
- Automatic token restoration on app load
- Permission evaluation methods

**Key Methods:**
```typescript
export const useAuth = create<AuthState>((set, get) => ({
  // Authentication state
  token: localStorage.getItem('token'),
  user: null,
  tenant: null,
  permissions: [],
  isAuthenticated: false,
  
  // Permission checking methods
  hasPermission: (permission: string) => boolean,
  hasRole: (roleName: string) => boolean,
  hasAnyRole: (roleNames: string[]) => boolean,
  canAccessResource: (resource: string, action: string) => boolean,
  
  // State management
  setToken: (token: string | null) => void,
  setUser: (user: UserProfile | null) => void,
  setTenant: (tenant: Tenant | null) => void,
  logout: () => void,
}))
```

**Benefits:**
- Minimal boilerplate compared to Redux
- TypeScript support out of the box
- Automatic persistence with localStorage
- Comprehensive permission system
- Multi-tenant architecture support

### Permission Management Hook (`hooks/usePermissions.ts`)

**Purpose**: Centralized permission checking and management

**Features:**
- Granular permission checks for all resources
- Role-based access control helpers
- Multi-tenant permission evaluation
- CRUD operation permission helpers
- Navigation permission management

**Key Permission Categories:**
```typescript
// User Management Permissions
const canViewUsers = hasPermission('user:read')
const canCreateUsers = hasPermission('user:create')
const canUpdateUsers = hasPermission('user:update')
const canDeleteUsers = hasPermission('user:delete')

// Tenant Management Permissions (Updated to plural form)
const canViewTenants = hasPermission('tenants:read')
const canCreateTenants = hasPermission('tenants:create')
const canUpdateTenants = hasPermission('tenants:update')
const canDeleteTenants = hasPermission('tenants:delete')

// Product and Module Permissions
const canViewProducts = hasPermission('product:read')
const canCreateProducts = hasPermission('product:create')
const canUpdateProducts = hasPermission('product:update')
const canDeleteProducts = hasPermission('product:delete')

// Role and Permission Management
const canViewRoles = hasPermission('role:read')
const canCreateRoles = hasPermission('role:create')
const canAssignPermissions = hasPermission('permission:assign')

// Admin and Multi-tenant Checks
const isAdmin = hasRole('admin')
const isSuperAdmin = hasRole('super_admin')
const isSystemAdmin = hasAnyRole(['admin', 'super_admin', 'system_admin'])
const canAccessMultipleTenants = hasPermission('multi_tenant:access')
```

**Specialized Hooks:**
```typescript
// CRUD permissions for any resource
export const useCrudPermissions = (resource: string) => ({
  canView: hasPermission(`${resource}:read`),
  canCreate: hasPermission(`${resource}:create`),
  canUpdate: hasPermission(`${resource}:update`),
  canDelete: hasPermission(`${resource}:delete`),
})

// Navigation permissions for menu items
export const useNavigationPermissions = () => ({
  canAccessDashboard: true, // Everyone can access dashboard
  canAccessUsers: canViewUsers,
  canAccessTenants: canViewTenants, // Updated to use plural permission
  canAccessProducts: canViewProducts,
  canAccessModules: canViewModules,
  canAccessRoles: canViewRoles,
  canAccessAuditLogs: canViewAuditLogs,
  canAccessSystemSettings: isSystemAdmin,
})
```

## API Integration

### HTTP Client Configuration (`lib/api.ts`)

**Technology**: Axios with interceptors

**Features:**
- Automatic JWT token injection
- Request/response interceptors
- Error handling and logging
- Base URL configuration
- Response data transformation

```typescript
import axios from 'axios'
import { useAuth } from '../store/auth'

const api = axios.create({
  baseURL: process.env.REACT_APP_API_URL || 'http://localhost:8080/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Request interceptor for JWT token
api.interceptors.request.use(
  (config) => {
    const token = useAuth.getState().token
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// Response interceptor for error handling
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      useAuth.getState().logout()
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)
```

### API Service Layer

**Authentication Services:**
```typescript
// Login and authentication
export const authService = {
  login: (credentials: LoginCredentials) => 
    api.post('/auth/login', credentials),
  
  refreshToken: () => 
    api.post('/auth/refresh'),
  
  logout: () => 
    api.post('/auth/logout'),
  
  getCurrentUser: () => 
    api.get('/auth/me'),
}
```

**Resource Management Services:**
```typescript
// User management
export const userService = {
  getUsers: (params?: QueryParams) => 
    api.get('/users', { params }),
  
  createUser: (userData: CreateUserRequest) => 
    api.post('/users', userData),
  
  updateUser: (id: string, userData: UpdateUserRequest) => 
    api.put(`/users/${id}`, userData),
  
  deleteUser: (id: string) => 
    api.delete(`/users/${id}`),
  
  assignRole: (userId: string, roleId: string) => 
    api.post(`/users/${userId}/roles/${roleId}`),
}

// Tenant management
export const tenantService = {
  getTenants: (params?: QueryParams) => 
    api.get('/tenants', { params }),
  
  createTenant: (tenantData: CreateTenantRequest) => 
    api.post('/tenants', tenantData),
  
  updateTenant: (id: string, tenantData: UpdateTenantRequest) => 
    api.put(`/tenants/${id}`, tenantData),
  
  deleteTenant: (id: string) => 
    api.delete(`/tenants/${id}`),
}

// Product and module management
export const productService = {
  getProducts: (params?: QueryParams) => 
    api.get('/products', { params }),
  
  createProduct: (productData: CreateProductRequest) => 
    api.post('/products', productData),
  
  updateProduct: (id: string, productData: UpdateProductRequest) => 
    api.put(`/products/${id}`, productData),
  
  deleteProduct: (id: string) => 
    api.delete(`/products/${id}`),
  
  getProductModules: (productId: string) => 
    api.get(`/products/${productId}/modules`),
}

// Role and permission management
export const roleService = {
  getRoles: (params?: QueryParams) => 
    api.get('/roles', { params }),
  
  createRole: (roleData: CreateRoleRequest) => 
    api.post('/roles', roleData),
  
  updateRole: (id: string, roleData: UpdateRoleRequest) => 
    api.put(`/roles/${id}`, roleData),
  
  deleteRole: (id: string) => 
    api.delete(`/roles/${id}`),
  
  getPermissions: () => 
    api.get('/permissions'),
  
  assignPermissions: (roleId: string, permissionIds: string[]) => 
    api.post(`/roles/${roleId}/permissions`, { permissionIds }),
}
```

**Error Handling Strategy:**
- Automatic 401 handling with logout and redirect
- Centralized error logging and reporting
- User-friendly error messages
- Network error detection and retry logic
- Permission-based error responses

**Environment Variables:**
- `VITE_API_BASE_URL`: Backend API base URL
- Fallback to localhost for development

## Styling and UI

### TailwindCSS Configuration

**Technology**: TailwindCSS with custom configuration

**Features:**
- Utility-first CSS framework
- Custom color palette and design tokens
- Responsive design utilities
- Dark mode support (ready for implementation)
- Component-specific styling patterns

**Configuration (`tailwind.config.js`):**
```javascript
module.exports = {
  content: ['./src/**/*.{js,jsx,ts,tsx}'],
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#eff6ff',
          500: '#3b82f6',
          600: '#2563eb',
          700: '#1d4ed8',
        },
        gray: {
          50: '#f9fafb',
          100: '#f3f4f6',
          200: '#e5e7eb',
          500: '#6b7280',
          700: '#374151',
          900: '#111827',
        },
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
      },
    },
  },
  plugins: [
    require('@tailwindcss/forms'),
    require('@tailwindcss/typography'),
  ],
}
```

### Shadcn/UI Components

**Technology**: Shadcn/UI component library with TailwindCSS

**Implemented Components:**
- **Button**: Multiple variants (default, destructive, outline, ghost)
- **Input**: Form inputs with validation states
- **Card**: Content containers with headers and footers
- **Badge**: Status indicators and labels
- **Dialog**: Modal dialogs for confirmations and forms
- **Table**: Data tables with sorting and pagination
- **Form**: Form components with validation
- **Select**: Dropdown selection components
- **Checkbox**: Checkbox inputs with labels
- **Alert**: Notification and alert messages

**Component Usage Patterns:**
```typescript
// Button variants
<Button variant="default">Primary Action</Button>
<Button variant="destructive">Delete</Button>
<Button variant="outline">Secondary</Button>
<Button variant="ghost">Subtle Action</Button>

// Form components with validation
<Form>
  <FormField
    control={form.control}
    name="email"
    render={({ field }) => (
      <FormItem>
        <FormLabel>Email</FormLabel>
        <FormControl>
          <Input placeholder="Enter email" {...field} />
        </FormControl>
        <FormMessage />
      </FormItem>
    )}
  />
</Form>

// Data display with cards
<Card>
  <CardHeader>
    <CardTitle>Product Information</CardTitle>
    <CardDescription>Manage product details</CardDescription>
  </CardHeader>
  <CardContent>
    {/* Content */}
  </CardContent>
  <CardFooter>
    <Button>Save Changes</Button>
  </CardFooter>
</Card>
```

### Design System

**Color Scheme:**
- **Primary**: Blue tones for main actions and navigation
- **Gray Scale**: Neutral colors for text and backgrounds
- **Status Colors**: Green (success), Red (error), Yellow (warning), Blue (info)
- **Semantic Colors**: Consistent color usage across components

**Typography:**
- **Font Family**: Inter for clean, modern appearance
- **Font Sizes**: Responsive scale from text-xs to text-4xl
- **Font Weights**: Regular (400), Medium (500), Semibold (600), Bold (700)

**Spacing and Layout:**
- **Grid System**: CSS Grid and Flexbox for layouts
- **Spacing Scale**: Consistent padding and margin using Tailwind's spacing scale
- **Responsive Breakpoints**: Mobile-first responsive design
- **Container Sizes**: Max-width containers for content areas

**Component Styling Patterns:**
```css
/* Permission-based styling */
.permission-guard-hidden {
  @apply hidden;
}

.permission-guard-disabled {
  @apply opacity-50 pointer-events-none;
}

/* Status indicators */
.status-active {
  @apply bg-green-100 text-green-800;
}

.status-inactive {
  @apply bg-red-100 text-red-800;
}

.status-pending {
  @apply bg-yellow-100 text-yellow-800;
}

/* Interactive elements */
.interactive-hover {
  @apply hover:bg-gray-50 transition-colors duration-200;
}

.focus-ring {
  @apply focus:outline-none focus:ring-2 focus:ring-primary-500 focus:ring-offset-2;
}
```

**Accessibility Features:**
- Focus management with visible focus rings
- ARIA labels and descriptions
- Semantic HTML structure
- Color contrast compliance
- Keyboard navigation support
- Screen reader compatibility

## Routing Configuration

### React Router Setup

**Technology**: React Router DOM with protected routes

**Features:**
- Nested routing with layout
- Protected routes with permission checks
- Automatic redirection for unauthorized access
- Dynamic route generation based on permissions
- Error handling for invalid routes

**Route Structure:**
```typescript
<Routes>
  {/* Public Routes */}
  <Route path="/login" element={<Login />} />
  <Route path="/unauthorized" element={<Unauthorized />} />
  
  {/* Protected Routes with Layout */}
  <Route element={<Layout><Outlet /></Layout>}>
    <Route path="/" element={
      <ProtectedRoute requireAuth>
        <Navigate to="/dashboard" replace />
      </ProtectedRoute>
    } />
    
    <Route path="/dashboard" element={
      <ProtectedRoute requireAuth permission="dashboard:view">
        <Dashboard />
      </ProtectedRoute>
    } />
    
    <Route path="/user-groups" element={
      <ProtectedRoute permission="user:read">
        <UserGroups />
      </ProtectedRoute>
    } />
    
    <Route path="/tenants" element={
      <ProtectedRoute permission="tenant:read">
        <TenantList />
      </ProtectedRoute>
    } />
    <Route path="/tenants/create" element={
      <ProtectedRoute permission="tenant:create">
        <TenantCreate />
      </ProtectedRoute>
    } />
    <Route path="/tenants/:id" element={
      <ProtectedRoute permission="tenant:read">
        <TenantDetail />
      </ProtectedRoute>
    } />
    
    <Route path="/products" element={
      <ProtectedRoute permission="product:read">
        <ProductList />
      </ProtectedRoute>
    } />
    <Route path="/products/create" element={
      <ProtectedRoute permission="product:create">
        <ProductCreate />
      </ProtectedRoute>
    } />
    <Route path="/products/:id" element={
      <ProtectedRoute permission="product:read">
        <ProductDetail />
      </ProtectedRoute>
    } />
    
    {/* Additional protected routes for modules, roles, etc. */}
  </Route>
  
  {/* Catch-all route */}
  <Route path="*" element={<Navigate to="/" replace />} />
</Routes>
```

**Navigation Features:**
- Permission-based menu items in Layout
- Active link highlighting with NavLink
- Programmatic navigation using useNavigate
- Location-aware components with useLocation
- Dynamic menu generation based on user permissions

**Navigation Pattern:**
```typescript
const { canAccessUserGroups } = useNavigationPermissions()

return (
  {canAccessUserGroups && (
    <NavLink
      to="/user-groups"
      className={({ isActive }) =>
        isActive ? 'text-primary-600 font-semibold' : 'text-gray-700 hover:text-primary-500'
      }
    >
      User Groups
    </NavLink>
  )}
)
```

**Protected Route Features:**
- Authentication checking
- Permission validation
- Role-based access
- Custom redirect paths
- Fallback rendering for unauthorized access

## Security Implementation

### Authentication Security

**JWT Token Management:**
- Secure token storage in localStorage with automatic cleanup
- Token expiration handling with automatic logout
- Refresh token rotation (ready for implementation)
- XSS protection through proper token handling

**Authentication Flow:**
```typescript
// Secure login process
const login = async (credentials: LoginCredentials) => {
  try {
    const response = await authService.login(credentials)
    const { token, user, permissions } = response.data
    
    // Store token securely
    setToken(token)
    setUser(user)
    
    // Validate permissions
    if (user.permissions) {
      setPermissions(user.permissions)
    }
    
    return { success: true }
  } catch (error) {
    // Secure error handling without exposing sensitive data
    return { success: false, error: 'Invalid credentials' }
  }
}
```

### Authorization Security (RBAC/ABAC)

**Permission-Based Access Control:**
- Granular permission checking at component level
- Resource-based access control with conditions
- Multi-tenant permission isolation
- Role hierarchy and inheritance support

**Security Guards:**
```typescript
// PermissionGuard component for UI protection
<PermissionGuard permission="user:delete">
  <Button variant="destructive">Delete User</Button>
</PermissionGuard>

// Route-level protection
<ProtectedRoute permission="admin:access">
  <AdminPanel />
</ProtectedRoute>

// Programmatic permission checking
const canDeleteUser = hasPermission('user:delete')
const canAccessTenant = canAccessResource('tenant', 'read')
```

**Multi-Tenant Security:**
- Tenant context isolation
- Cross-tenant data access prevention
- Tenant-specific permission evaluation
- Secure tenant switching with re-authentication

### Input Validation and Sanitization

**Form Validation:**
```typescript
// Zod schema validation
const userSchema = z.object({
  email: z.string().email('Invalid email format'),
  name: z.string().min(2, 'Name must be at least 2 characters'),
  password: z.string().min(8, 'Password must be at least 8 characters')
    .regex(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/, 'Password must contain uppercase, lowercase, and number'),
})

// Client-side validation with sanitization
const validateAndSanitizeInput = (data: unknown) => {
  try {
    return userSchema.parse(data)
  } catch (error) {
    throw new ValidationError('Invalid input data')
  }
}
```

**XSS Prevention:**
- React's built-in XSS protection through JSX
- Sanitization of user-generated content
- CSP (Content Security Policy) headers
- Secure handling of dynamic content

### API Security

**Request Security:**
```typescript
// Automatic token injection with validation
api.interceptors.request.use((config) => {
  const token = useAuth.getState().token
  
  // Validate token before sending
  if (token && !isTokenExpired(token)) {
    config.headers.Authorization = `Bearer ${token}`
  } else {
    // Handle expired token
    useAuth.getState().logout()
    throw new Error('Authentication required')
  }
  
  return config
})
```

**Response Security:**
```typescript
// Secure response handling
api.interceptors.response.use(
  (response) => {
    // Validate response structure
    if (response.data && typeof response.data === 'object') {
      return response
    }
    throw new Error('Invalid response format')
  },
  (error) => {
    // Secure error handling
    if (error.response?.status === 401) {
      useAuth.getState().logout()
      window.location.href = '/login'
    }
    
    // Don't expose sensitive error details
    const safeError = {
      message: error.response?.data?.message || 'An error occurred',
      status: error.response?.status,
    }
    
    return Promise.reject(safeError)
  }
)
```

### Environment Security

**Environment Variables:**
```typescript
// Secure environment configuration
const config = {
  apiUrl: process.env.REACT_APP_API_URL || 'http://localhost:8080/api',
  environment: process.env.NODE_ENV,
  // Never expose sensitive keys in frontend
  publicKey: process.env.REACT_APP_PUBLIC_KEY, // Only public keys
}

// Runtime environment validation
if (!config.apiUrl) {
  throw new Error('API URL is required')
}
```

**Build Security:**
- Environment-specific builds
- Sensitive data exclusion from bundles
- Source map protection in production
- Dependency vulnerability scanning

### Security Best Practices

**Code Security:**
- TypeScript for type safety and runtime error prevention
- ESLint security rules and static analysis
- Dependency vulnerability monitoring
- Regular security updates

**Runtime Security:**
- Error boundary implementation for graceful error handling
- Secure state management with proper cleanup
- Memory leak prevention
- Performance monitoring for security anomalies

**Deployment Security:**
- HTTPS enforcement
- Security headers configuration
- CSP implementation
- Regular security audits and penetration testing

## Development Workflow

### Environment Setup

**Prerequisites:**
- Node.js 18+ (LTS version recommended)
- npm 8+ or yarn 1+
- Git for version control
- Visual Studio Code (recommended IDE with extensions)

**Installation Steps:**
```bash
# Clone repository
git clone <repository-url>
cd frontend

# Install dependencies
npm install

# Start development server
npm run dev
```

### Build Scripts

**Available Commands:**
```json
{
  "scripts": {
    "dev": "vite --port 5173",             // Development server with HMR
    "build": "tsc -b && vite build",       // Production build with optimization
    "lint": "eslint . --ext .ts,.tsx",     // Code linting with TypeScript support
    "preview": "vite preview",             // Local preview of production build
    "typecheck": "tsc --noEmit",           // Type checking without compilation
    "format": "prettier --write src/**/*.{ts,tsx,json,css}" // Code formatting
  }
}
```

### Development Server

**Features:**
- **Port**: 5173 (configurable via VITE_PORT)
- **Hot Module Replacement**: Real-time updates without full reload
- **TypeScript Compilation**: On-the-fly compilation with error overlay
- **ESLint Integration**: Real-time linting feedback
- **Proxy Configuration**: API proxy to backend server
- **Environment Variables**: Loaded from .env files

**Running the Server:**
```bash
npm run dev
# Access at http://localhost:5173
```

### Build Process

**Steps:**
1. TypeScript type checking and compilation
2. Vite bundling with Rollup
3. Code splitting and lazy loading optimization
4. Asset minification (JS, CSS, images)
5. Tree shaking for unused code removal
6. Output generation to `dist/` directory

**Build Command:**
```bash
npm run build
# Outputs optimized static assets in dist/
```

**Preview Build:**
```bash
npm run preview
# Serves dist/ locally for testing
```

### Code Quality Workflow

**Linting:**
```bash
npm run lint
# Or with auto-fix: npm run lint -- --fix
```

**Type Checking:**
```bash
npm run typecheck
# Runs TypeScript compiler without emitting files
```

**Formatting:**
```bash
npm run format
# Uses Prettier to format all source files
```

### Git Workflow

**Branching Strategy:**
- `main`: Production-ready code
- `develop`: Integration branch for features
- `feature/*`: New features and enhancements
- `bugfix/*`: Bug fixes
- `hotfix/*`: Critical production fixes

**Commit Guidelines:**
- Use conventional commits (feat:, fix:, chore:, etc.)
- Include descriptive messages
- Reference issue numbers
- Keep commits atomic

### Continuous Integration (Recommended)

**CI Pipeline:**
- Install dependencies
- Run type checking
- Execute linting
- Build production bundle
- Run unit tests
- Check code coverage

**Tools:**
- GitHub Actions or Jenkins
- ESLint and TypeScript integration
- Vitest for testing

## TypeScript Configuration

### tsconfig.json Overview

The project uses a comprehensive TypeScript configuration to ensure type safety and developer productivity.

**Key Compiler Options:**
```json
{
  "compilerOptions": {
    "target": "ES2020",
    "module": "ESNext",
    "moduleResolution": "bundler",
    "strict": true,
    "esModuleInterop": true,
    "skipLibCheck": true,
    "forceConsistentCasingInFileNames": true,
    "noImplicitAny": true,
    "strictNullChecks": true,
    "strictFunctionTypes": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "baseUrl": ".",
    "paths": {
      "@/*": ["src/*"]
    },
    "types": ["vite/client"]
  },
  "include": ["src/**/*.ts", "src/**/*.tsx", "vite.config.ts"],
  "exclude": ["node_modules", "dist"]
}
```

### Type Safety Features

- **Strict Mode**: Enabled for comprehensive type checking
- **Path Aliases**: @/ for src/ directory imports
- **Interface-based Props**: All React components use typed props
- **Generic Components**: Reusable components with type parameters
- **API Typing**: Strongly typed API responses and requests
- **Utility Types**: Extensive use of Partial, Pick, Omit
- **Type Guards**: Custom guards for runtime type checking
- **Zustand Store Typing**: Strongly typed state and actions

### Key Type Definitions

#### Core Interfaces

**UserProfile:**
```typescript
interface UserProfile {
  id: string;
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  roles: Role[];
  permissions: Permission[];
  tenants: Tenant[];
}
```

**Permission:**
```typescript
interface Permission {
  id: string;
  name: string;
  description?: string;
  resource: string;    // e.g., 'user', 'tenant', 'product'
  action: string;      // e.g., 'create', 'read', 'update', 'delete'
  attributes?: Record<string, any>;  // For ABAC
}
```

**Role:**
```typescript
interface Role {
  id: string;
  name: string;
  description?: string;
  permissions: Permission[];
}
```

**Tenant:**
```typescript
interface Tenant {
  id: string;
  name: string;
  description?: string;
  users: UserProfile[];
  products: Product[];
}
```

#### API Response Types

**Generic API Response:**
```typescript
interface ApiResponse<T> {
  data: T;
  status: number;
  message?: string;
  error?: string;
}
```

**Paginated Response:**
```typescript
interface PaginatedResponse<T> {
  items: T[];
  total: number;
  page: number;
  pageSize: number;
}
```

### Best Practices

- Use explicit types for all variables and functions
- Prefer interfaces over types for object shapes
- Implement type guards for complex conditional logic
- Use mapped types for dynamic object structures
- Regularly run type checking as part of CI/CD
- Avoid 'any' type usage

## Performance Considerations

### Optimization Strategies

- **Code Splitting**: Dynamic imports with React.lazy and Suspense for route-based splitting
- **Memoization**: React.memo for components, useMemo for expensive calculations
- **Virtualization**: react-window for long lists in management pages
- **Image Optimization**: Responsive images with srcset, lazy loading
- **Bundle Optimization**: Vite's built-in tree-shaking and minification
- **Caching**: Browser caching for static assets, service workers for PWA

### React Performance Best Practices

- **Render Optimization**:
  - Avoid unnecessary re-renders using shouldComponentUpdate or PureComponent
  - Use React Profiler to identify bottlenecks
- **Hook Optimization**:
  - useCallback for stable function references
  - useMemo for computed values
- **State Management**:
  - Minimal state updates in Zustand
  - Batch updates where possible
- **API Optimization**:
  - Debouncing search inputs
  - Pagination for list endpoints
  - Caching responses with React Query (if integrated)

### Loading States and User Experience

- **Skeleton Screens**: Implemented in list and detail pages
- **Lazy Loading**: Components and routes
- **Progressive Enhancement**: Core functionality first, then enhancements
- **Error Boundaries**: Graceful error handling with fallback UI

### Monitoring and Tools

- **Performance Monitoring**: Lighthouse audits, Web Vitals
- **Profiling Tools**: React DevTools Profiler, Chrome Performance tab
- **Benchmarking**: Regular performance tests in CI/CD

## Security Considerations

### Authentication Security
- JWT token storage in localStorage
- Automatic token cleanup on logout
- Token expiration handling (client-side)

### API Security
- CORS configuration with backend
- Request/response validation
- Error message sanitization

### Input Validation
- Form validation with React Hook Form + Zod
- XSS prevention through React's built-in escaping
- Type safety with TypeScript

## Testing Strategy (Recommended)

### Unit Testing
- **Framework**: Vitest (Vite-native testing)
- **Library**: React Testing Library
- **Coverage**: Component logic and utilities

### Integration Testing
- API integration tests
- User flow testing
- Form submission testing

### E2E Testing
- **Framework**: Playwright or Cypress
- **Scenarios**: Login flow, CRUD operations
- **Cross-browser testing**

## Deployment

### Build Output
```bash
npm run build
# Generates optimized static files in dist/
```

### Deployment Targets
- **Vercel**: Recommended (zero-config)
- **Netlify**: Static site hosting
- **AWS S3 + CloudFront**: Enterprise deployment
- **Docker**: Containerized deployment

### Environment Configuration
```bash
# .env.production
VITE_API_BASE_URL=https://api.yourapp.com/api/v1
```

## Browser Support

### Target Browsers
- Chrome 90+
- Firefox 88+
- Safari 14+
- Edge 90+

### Polyfills
- Modern browsers only (ES2020+)
- No IE11 support
- Native ES modules support

## Development Guidelines

### Code Style
- **ESLint**: Enforced code standards
- **Prettier**: Code formatting (recommended)
- **TypeScript**: Strict type checking

### Component Patterns
- Functional components with hooks
- Custom hooks for reusable logic
- Props interface definitions
- Error boundary implementation

### State Management Guidelines
- Zustand for global state
- React state for component-local state
- Avoid prop drilling with context when needed

### API Integration Patterns
- Centralized API client configuration
- Consistent error handling
- Loading state management
- Response data transformation

## Future Enhancements

### Planned Features
1. **Enhanced Authentication**: Multi-factor authentication
2. **Real-time Updates**: WebSocket integration
3. **Advanced UI**: Component library integration
4. **Internationalization**: Multi-language support
5. **Progressive Web App**: Offline capabilities
6. **Advanced State Management**: React Query for server state
7. **Testing Suite**: Comprehensive test coverage
8. **Performance Monitoring**: Analytics integration

### Technical Debt
- Add comprehensive error boundaries
- Implement proper loading skeletons
- Add form validation with Zod schemas
- Implement proper TypeScript strict mode
- Add accessibility (a11y) improvements
- Implement proper SEO optimization