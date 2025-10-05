import { type ReactNode } from 'react'
import { NavLink, useNavigate, Link } from 'react-router-dom'
import { LogOut, User, Settings, Shield, Users, Package, Layers, Key, Building, BarChart3 } from 'lucide-react'
import { useAuth } from '../store/auth'
import { useNavigationPermissions } from '../hooks/usePermissions'
import { PermissionGuard } from './PermissionGuard'
import { Button } from "@/components/ui/button"
import {
  NavigationMenu,
  NavigationMenuItem,
  NavigationMenuLink,
  NavigationMenuList,
} from "@/components/ui/navigation-menu"

interface LayoutProps {
  children: ReactNode
}

export default function Layout({ children }: LayoutProps) {
  const { user, tenant, logout } = useAuth()
  const navigate = useNavigate()
  const navPermissions = useNavigationPermissions()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const navigationItems = [
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
    <div className="flex flex-col min-h-screen">
      <header className="bg-primary text-primary-foreground p-4">
        <nav className="flex justify-between items-center">
          <NavigationMenu>
            <NavigationMenuList>
              <NavigationMenuItem>
                <NavigationMenuLink asChild>
                  <Link to="/">Dashboard</Link>
                </NavigationMenuLink>
              </NavigationMenuItem>
              {navPermissions.canAccessTenants && (
                <NavigationMenuItem>
                  <NavigationMenuLink asChild>
                    <Link to="/tenants">Tenants</Link>
                  </NavigationMenuLink>
                </NavigationMenuItem>
              )}
              {navPermissions.canAccessProducts && (
                <NavigationMenuItem>
                  <NavigationMenuLink asChild>
                    <Link to="/products">Products</Link>
                  </NavigationMenuLink>
                </NavigationMenuItem>
              )}
              {navPermissions.canAccessUsers && (
                <NavigationMenuItem>
                  <NavigationMenuLink asChild>
                    <Link to="/user-groups">User Groups</Link>
                  </NavigationMenuLink>
                </NavigationMenuItem>
              )}
            </NavigationMenuList>
          </NavigationMenu>
          <div>
            {user ? (
              <>
                <span className="mr-4">{user.username}</span>
                <Button variant="ghost" onClick={logout}>
                  Logout
                </Button>
              </>
            ) : (
              <Button variant="ghost" asChild>
                <Link to="/login">Login</Link>
              </Button>
            )}
          </div>
        </nav>
      </header>

      <div className="flex">
        {/* Sidebar Navigation */}
        <nav className="w-64 bg-white border-r border-gray-200 min-h-[calc(100vh-73px)]">
          <div className="p-4">
            <ul className="space-y-2">
              {navigationItems.map((item) => (
                <PermissionGuard key={item.to} fallback={null}>
                  {item.canAccess && (
                    <li>
                      <NavLink
                        to={item.to}
                        className={({ isActive }) =>
                          `flex items-center gap-3 px-3 py-2 rounded-md text-sm font-medium transition-colors ${
                            isActive
                              ? 'bg-blue-50 text-blue-700 border-r-2 border-blue-700'
                              : 'text-gray-700 hover:text-gray-900 hover:bg-gray-50'
                          }`
                        }
                      >
                        <item.icon className="h-4 w-4" />
                        {item.label}
                      </NavLink>
                    </li>
                  )}
                </PermissionGuard>
              ))}
            </ul>
          </div>
          
          {/* User Role Information */}
          {user && (
            <div className="absolute bottom-4 left-4 right-4">
              <div className="bg-gray-50 rounded-lg p-3">
                <p className="text-xs font-medium text-gray-700 mb-1">Your Roles:</p>
                <div className="flex flex-wrap gap-1">
                  {user.roles.map((role) => (
                    <span
                      key={role.roleId}
                      className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800"
                    >
                      {role.name}
                    </span>
                  ))}
                </div>
              </div>
            </div>
          )}
        </nav>

        {/* Main Content */}
        <main className="flex-1 p-6">
          {children}
        </main>
      </div>
    </div>
  )
}