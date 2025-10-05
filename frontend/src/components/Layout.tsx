import { type ReactNode } from 'react'
import { NavLink, useNavigate, Link } from 'react-router-dom'
import { LogOut, User, Settings, Shield, Users, Package, Layers, Key, Building, BarChart3 } from 'lucide-react'
import { useAuth } from '../store/auth'
import { useNavigationPermissions } from '../hooks/usePermissions'
import { PermissionGuard } from './PermissionGuard'
import { Button } from "./ui/button"
import { Badge } from "./ui/badge"
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarProvider,
  SidebarTrigger,
} from "./ui/sidebar"

interface LayoutProps {
  children: ReactNode
}

interface NavigationItem {
  to: string
  label: string
  icon: React.ComponentType<{ className?: string }>
  canAccess: boolean
}

export default function Layout({ children }: LayoutProps) {
  const { user, tenant, logout } = useAuth()
  const navigate = useNavigate()
  const navPermissions = useNavigationPermissions()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const navigationItems: NavigationItem[] = [
    {
      to: '/dashboard',
      label: 'Dashboard',
      icon: BarChart3,
      canAccess: navPermissions.canAccessDashboard
    },
    {
      to: '/user-groups',
      label: 'User Groups',
      icon: Users,
      canAccess: navPermissions.canAccessUsers
    },
    {
      to: '/products',
      label: 'Products',
      icon: Package,
      canAccess: navPermissions.canAccessProducts
    },
    {
      to: '/modules',
      label: 'Modules',
      icon: Layers,
      canAccess: navPermissions.canAccessModules
    },
    {
      to: '/roles',
      label: 'Roles',
      icon: Shield,
      canAccess: navPermissions.canAccessRoles
    },
    {
      to: '/permissions',
      label: 'Permissions',
      icon: Key,
      canAccess: navPermissions.canAccessRoles
    },
    {
      to: '/tenants',
      label: 'Tenants',
      icon: Building,
      canAccess: navPermissions.canAccessTenants
    }
  ]

  return (
    <SidebarProvider>
      <div className="flex min-h-screen w-full">
        <Sidebar variant="inset">
          <SidebarHeader>
            <div className="flex items-center gap-2 px-4 py-2">
              <div className="flex aspect-square size-8 items-center justify-center rounded-lg bg-sidebar-primary text-sidebar-primary-foreground">
                <Building className="size-4" />
              </div>
              <div className="grid flex-1 text-left text-sm leading-tight">
                <span className="truncate font-semibold">Shared Services</span>
                <span className="truncate text-xs">{tenant?.name || 'Platform'}</span>
              </div>
            </div>
          </SidebarHeader>
          
          <SidebarContent>
            <SidebarGroup>
              <SidebarGroupLabel>Navigation</SidebarGroupLabel>
              <SidebarGroupContent>
                <SidebarMenu>
                  {navigationItems.map((item) => (
                    <PermissionGuard key={item.to} fallback={null}>
                      {item.canAccess && (
                        <SidebarMenuItem>
                          <SidebarMenuButton asChild>
                            <NavLink
                              to={item.to}
                              className={({ isActive }) =>
                                `flex items-center gap-2 ${
                                  isActive ? 'bg-sidebar-accent text-sidebar-accent-foreground' : ''
                                }`
                              }
                            >
                              <item.icon className="size-4" />
                              <span>{item.label}</span>
                            </NavLink>
                          </SidebarMenuButton>
                        </SidebarMenuItem>
                      )}
                    </PermissionGuard>
                  ))}
                </SidebarMenu>
              </SidebarGroupContent>
            </SidebarGroup>
          </SidebarContent>
          
          <SidebarFooter>
            {user && (
              <div className="p-4">
                <div className="rounded-lg border bg-sidebar-accent/50 p-3">
                  <div className="flex items-center gap-2 mb-2">
                    <User className="size-4" />
                    <span className="text-sm font-medium">{user.username}</span>
                  </div>
                  <div className="mb-3">
                    <p className="text-xs text-muted-foreground mb-1">Your Roles:</p>
                    <div className="flex flex-wrap gap-1">
                      {user.roles.map((role) => (
                        <Badge key={role.id} variant="secondary" className="text-xs">
                          {role.name}
                        </Badge>
                      ))}
                    </div>
                  </div>
                  <Button 
                    variant="outline" 
                    size="sm" 
                    onClick={handleLogout}
                    className="w-full"
                  >
                    <LogOut className="size-4 mr-2" />
                    Logout
                  </Button>
                </div>
              </div>
            )}
          </SidebarFooter>
        </Sidebar>
        
        <main className="flex-1 flex flex-col">
          {/* Header with sidebar trigger */}
          <header className="flex h-16 shrink-0 items-center gap-2 border-b px-4">
            <SidebarTrigger className="-ml-1" />
            <div className="flex-1" />
            {user ? (
              <span className="text-sm text-muted-foreground">
                Welcome, {user.username}
              </span>
            ) : (
              <Button variant="ghost" asChild>
                <Link to="/login">Login</Link>
              </Button>
            )}
          </header>
          
          {/* Main content area */}
          <div className="flex-1 p-6">
            {children}
          </div>
        </main>
      </div>
    </SidebarProvider>
  )
}