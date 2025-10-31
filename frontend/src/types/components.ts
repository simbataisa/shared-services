// Component props interfaces
import type { 
  User, 
  Role, 
  UserGroup, 
  UserGroupDetails, 
  RoleAssignment, 
  Module, 
  Permission 
} from './entities';
import type { PaymentRefund } from './payment';

// Layout and navigation
export interface LayoutProps {
  children: React.ReactNode;
}

export interface NavigationItem {
  name: string;
  href: string;
  icon?: React.ComponentType<{ className?: string }>;
  permission?: string;
}

// Authentication and authorization
export interface ProtectedRouteProps {
  children: React.ReactNode;
  permission?: string;
}

export interface PermissionGuardProps {
  children: React.ReactNode;
  permission: string;
  fallback?: React.ReactNode;
}

export interface RequireAuthProps {
  children: React.ReactNode;
  fallback?: React.ReactNode;
}

export interface ConditionalRenderProps {
  condition: boolean;
  children: React.ReactNode;
  fallback?: React.ReactNode;
}

// User component props
export interface UserFormProps {
  roles: Role[];
  userGroups: UserGroup[];
  onUserCreated?: () => void;
  onUserUpdated?: () => void;
  onError: (error: string) => void;
}

export interface UserEditProps {
  userId: number | null;
  onUserUpdated?: () => void;
}

export interface UserDetailProps {
  userId: string;
}

export interface UserInfoCardProps {
  user: User;
  onUpdate?: (data: { firstName: string; lastName: string; email: string; phoneNumber?: string }) => Promise<void>;
  updating?: boolean;
}

export interface UserPasswordCardProps {
  userId: number;
  onPasswordChange?: () => void;
}

export interface UserStatusCardProps {
  user: User;
  onStatusChange?: (status: string) => Promise<void>;
  updating?: boolean;
}

export interface UserRoleGroupCardProps {
  user: User;
  roles: Role[];
  userGroups: UserGroup[];
  onUpdate?: () => void;
}

export interface UserInfoFormCardProps {
  user: User;
  onUpdate: (data: { firstName: string; lastName: string; email: string; phoneNumber?: string }) => Promise<void>;
  updating: boolean;
}

export interface UserPasswordFormCardProps {
  userId: number;
  onPasswordChange: () => void;
}

export interface UserRoleGroupFormCardProps {
  user: User;
  roles: Role[];
  userGroups: UserGroup[];
  onUpdate: () => void;
}

// Role component props
export interface RoleDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  role?: Role | null;
  permissions: Permission[];
  onSave: (form: { name: string; description: string; permissionIds: number[] }) => Promise<void>;
  loading?: boolean;
}

export interface RoleListProps {
  roles: Role[];
  permissions: Permission[];
  loading?: boolean;
  onRoleSelect?: (role: Role) => void;
  selectedRoleId?: number;
  showActions?: boolean;
  className?: string;
}

export interface RoleDetailProps {
  roleId?: string;
}

// User Group component props
export interface BasicInformationCardProps {
  userGroup: UserGroup;
  onUpdate?: (data: { name: string; description: string }) => Promise<void>;
  updating?: boolean;
}

export interface RoleAssignmentsCardProps {
  userGroup: UserGroupDetails;
  onRoleAssignmentUpdate?: () => void;
  loading?: boolean;
}

export interface UserGroupCreateProps {
  onUserGroupCreated?: (userGroup: UserGroup) => void;
  onError?: (error: string) => void;
}

export interface UserGroupDetailProps {
  userGroupId: string;
}

export interface UserGroupsGridProps {
  userGroups: UserGroup[];
  loading?: boolean;
  onUserGroupSelect?: (userGroup: UserGroup) => void;
  onUserGroupEdit?: (userGroup: UserGroup) => void;
  onUserGroupDelete?: (userGroupId: number) => void;
}

export interface UserGroupsTableProps {
  userGroups: UserGroup[];
  loading?: boolean;
  onUserGroupSelect?: (userGroup: UserGroup) => void;
  onUserGroupEdit?: (userGroup: UserGroup) => void;
  onUserGroupDelete?: (userGroupId: number) => void;
}

export interface UserGroupRolesManagerProps {
  userGroupId: number;
  modules: Module[];
  roles: Role[];
  currentAssignments: RoleAssignment[];
  onAssignmentChange?: (assignments: RoleAssignment[]) => void;
  loading?: boolean;
}

// UI component props
export interface StatusBadgeProps {
  status: string;
  variant?: 'default' | 'secondary' | 'destructive' | 'outline';
  size?: 'sm' | 'default' | 'lg';
}

export interface StatusBadgeWithIconProps extends StatusBadgeProps {
  showIcon?: boolean;
}

// Common table interfaces
export interface TableFilterOption {
  value: string;
  label: string;
}

export interface TableFilter {
  label: string;
  value: string;
  onChange: (value: string) => void;
  options: TableFilterOption[];
  placeholder?: string;
  width?: string;
}

// Base table props interface that can be extended by specific table components
export interface BaseTableProps<T = any> {
  data: T[];
  loading?: boolean;
  searchTerm?: string;
  onSearchChange?: (value: string) => void;
  searchPlaceholder?: string;
  filters?: TableFilter[];
  actions?: React.ReactNode;
}

export interface SearchAndFilterProps {
  searchTerm: string;
  onSearchChange: (term: string) => void;
  searchPlaceholder?: string;
  filters?: TableFilter[];
  actions?: React.ReactNode;
  className?: string;
}

// Legacy interface for backward compatibility
export interface FilterOption {
  label: string;
  value: string;
  onChange: (value: string) => void;
  options: { value: string; label: string }[];
  width?: string;
}

// Search and filter types
export interface RoleSearchFilters {
  searchTerm: string;
  status?: string;
  hasPermissions?: boolean;
}

// Table row types
export interface RoleTableRow {
  id: number;
  name: string;
  description: string;
  status: "ACTIVE" | "INACTIVE";
  permissions: Permission[];
  createdAt: string;
  updatedAt: string;
}

export interface RefundListProps {
  data: PaymentRefund[];
  permissions: Permission[];
  loading?: boolean;
  onRefundSelect?: (refund: PaymentRefund) => void;
  selectedRefundId?: string;
  showActions?: boolean;
  className?: string;
}