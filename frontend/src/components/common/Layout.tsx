import { type ReactNode } from "react";
import { NavLink, useNavigate, Link } from "react-router-dom";
import {
  LogOut,
  User,
  Shield,
  Users,
  Package,
  Layers,
  Key,
  Building,
  BarChart3,
  CreditCard,
} from "lucide-react";
import { useAuth } from "@/store/auth";
import { useNavigationPermissions } from "@/hooks/usePermissions";
import { PermissionGuard } from "@/components/common/PermissionGuard";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
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
  SidebarRail,
  SidebarProvider,
  SidebarTrigger,
} from "@/components/ui/sidebar";

interface LayoutProps {
  children: ReactNode;
}

interface NavigationItem {
  to: string;
  label: string;
  icon: React.ComponentType<{ className?: string }>;
  canAccess: boolean;
}

export default function Layout({ children }: LayoutProps) {
  const { user, tenant, logout } = useAuth();
  const navigate = useNavigate();
  const navPermissions = useNavigationPermissions();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  const navigationItems: NavigationItem[] = [
    {
      to: "/dashboard",
      label: "Dashboard",
      icon: BarChart3,
      canAccess: navPermissions.canAccessDashboard,
    },
    {
      to: "/user-groups",
      label: "User Groups",
      icon: Users,
      canAccess: navPermissions.canAccessUsers,
    },
    {
      to: "/users",
      label: "Users",
      icon: User,
      canAccess: navPermissions.canAccessUsers,
    },
    {
      to: "/roles",
      label: "Roles",
      icon: Shield,
      canAccess: navPermissions.canAccessRoles,
    },
    {
      to: "/permissions",
      label: "Permissions",
      icon: Key,
      canAccess: navPermissions.canAccessRoles,
    },
    {
      to: "/products",
      label: "Products",
      icon: Package,
      canAccess: navPermissions.canAccessProducts,
    },
    {
      to: "/modules",
      label: "Modules",
      icon: Layers,
      canAccess: navPermissions.canAccessModules,
    },
    {
      to: "/tenants",
      label: "Tenants",
      icon: Building,
      canAccess: navPermissions.canAccessTenants,
    },
    {
      to: "/payments",
      label: "Payments",
      icon: CreditCard,
      canAccess: navPermissions.canAccessPayments,
    },
  ];

  return (
    <SidebarProvider>
      <div className="flex min-h-screen w-full">
        <Sidebar variant="inset" collapsible="icon">
          <SidebarHeader>
            <div className="flex items-center gap-2 px-4 py-2 group-data-[collapsible=icon]:justify-left group-data-[collapsible=icon]:px-0 group-data-[collapsible=icon]:gap-0">
              <Building className="size-4 shrink-0 text-sidebar-foreground" />
              <div className="grid flex-1 text-left text-sm leading-tight group-data-[collapsible=icon]:hidden">
                <span className="truncate font-semibold">Shared Services</span>
                {/* <span className="truncate text-xs">
                  {tenant?.name || "Platform"}
                </span> */}
              </div>
            </div>
          </SidebarHeader>

          <SidebarRail />

          <SidebarContent>
            <SidebarGroup>
              <SidebarGroupLabel className="group-data-[collapsible=icon]:hidden">
                Navigation
              </SidebarGroupLabel>
              <SidebarGroupContent>
                <SidebarMenu>
                  {navigationItems.map((item) => (
                    <PermissionGuard key={item.to} fallback={null}>
                      {item.canAccess && (
                        <SidebarMenuItem>
                          <SidebarMenuButton
                            asChild
                            tooltip={item.label}
                            size="lg"
                          >
                            <NavLink
                              to={item.to}
                              className={({ isActive }) =>
                                `flex items-center gap-2 ${
                                  isActive
                                    ? "bg-sidebar-accent text-sidebar-accent-foreground"
                                    : ""
                                }`
                              }
                            >
                              <item.icon className="size-4" />
                              <span className="group-data-[collapsible=icon]:hidden">
                                {item.label}
                              </span>
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
              <div className="p-4 group-data-[collapsible=icon]:hidden">
                <div className="rounded-lg border bg-sidebar-accent/50 p-3">
                  <div className="flex items-center gap-2 mb-2">
                    <User className="size-4" />
                    <span className="text-sm font-medium">{user.username}</span>
                  </div>
                  <div className="mb-3">
                    <p className="text-xs text-muted-foreground mb-1">
                      Your Roles:
                    </p>
                    <div className="flex flex-wrap gap-1">
                      {user.roles.map((role) => (
                        <Badge
                          key={role.id}
                          variant="secondary"
                          className="text-xs"
                        >
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
            {user && (
              <div className="p-2 hidden group-data-[collapsible=icon]:block">
                <SidebarMenu>
                  <SidebarMenuItem>
                    <SidebarMenuButton
                      size="lg"
                      tooltip="Logout"
                      onClick={handleLogout}
                      aria-label="Logout"
                    >
                      <LogOut className="size-4" />
                    </SidebarMenuButton>
                  </SidebarMenuItem>
                </SidebarMenu>
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
          <div className="flex-1 p-6">{children}</div>
        </main>
      </div>
    </SidebarProvider>
  );
}
