import { type ReactNode, useState } from "react";
import { NavLink, useNavigate, Link, useLocation } from "react-router-dom";
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
  Receipt,
  Undo2,
  FileText,
  ChevronRight,
} from "lucide-react";
import { useAuth } from "@/store/auth";
import { useNavigationPermissions } from "@/hooks/usePermissions";
import { PermissionGuard } from "@/components/common/PermissionGuard";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import {
  Collapsible,
  CollapsibleContent,
  CollapsibleTrigger,
} from "@/components/ui/collapsible";
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
  SidebarMenuSub,
  SidebarMenuSubButton,
  SidebarMenuSubItem,
  SidebarRail,
  SidebarProvider,
  SidebarTrigger,
} from "@/components/ui/sidebar";

interface LayoutProps {
  children: ReactNode;
}

interface NavigationSubItem {
  to: string;
  label: string;
  icon: React.ComponentType<{ className?: string }>;
}

interface NavigationItem {
  to: string;
  label: string;
  icon: React.ComponentType<{ className?: string }>;
  canAccess: boolean;
  subItems?: NavigationSubItem[];
}

export default function Layout({ children }: LayoutProps) {
  const { user, tenant, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const navPermissions = useNavigationPermissions();
  const [openMenus, setOpenMenus] = useState<Record<string, boolean>>({});

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  const toggleMenu = (menuKey: string) => {
    setOpenMenus(prev => ({
      ...prev,
      [menuKey]: !prev[menuKey]
    }));
  };

  const isMenuActive = (item: NavigationItem) => {
    if (item.subItems) {
      return item.subItems.some(subItem => location.pathname === subItem.to);
    }
    return location.pathname === item.to;
  };

  const isSubMenuActive = (subItem: NavigationSubItem) => {
    return location.pathname === subItem.to;
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
      subItems: [
        {
          to: "/payments/transactions",
          label: "Transaction List",
          icon: Receipt,
        },
        {
          to: "/payments/refunds",
          label: "Refund List",
          icon: Undo2,
        },
        {
          to: "/payments/audit-logs",
          label: "Audit Log",
          icon: FileText,
        },
      ],
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
                          {item.subItems ? (
                            <Collapsible
                              open={openMenus[item.to] || isMenuActive(item)}
                              onOpenChange={() => toggleMenu(item.to)}
                            >
                              <CollapsibleTrigger asChild>
                                <SidebarMenuButton
                                  asChild
                                  tooltip={item.label}
                                  size="lg"
                                  className={`${
                                    isMenuActive(item)
                                      ? "bg-sidebar-accent text-sidebar-accent-foreground"
                                      : ""
                                  }`}
                                >
                                  <NavLink
                                    to={item.to}
                                    className="flex items-center gap-2 w-full"
                                  >
                                    <item.icon className="size-4" />
                                    <span className="group-data-[collapsible=icon]:hidden flex-1 text-left">
                                      {item.label}
                                    </span>
                                    <ChevronRight className="ml-auto size-4 transition-transform group-data-[collapsible=icon]:hidden data-[state=open]:rotate-90" />
                                  </NavLink>
                                </SidebarMenuButton>
                              </CollapsibleTrigger>
                              <CollapsibleContent>
                                <SidebarMenuSub>
                                  {item.subItems.map((subItem) => (
                                    <SidebarMenuSubItem key={subItem.to}>
                                      <SidebarMenuSubButton
                                        asChild
                                        isActive={isSubMenuActive(subItem)}
                                      >
                                        <NavLink to={subItem.to}>
                                          <subItem.icon className="size-4" />
                                          <span>{subItem.label}</span>
                                        </NavLink>
                                      </SidebarMenuSubButton>
                                    </SidebarMenuSubItem>
                                  ))}
                                </SidebarMenuSub>
                              </CollapsibleContent>
                            </Collapsible>
                          ) : (
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
                          )}
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
