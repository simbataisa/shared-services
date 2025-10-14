// Form-related types and interfaces

// User forms
export interface CreateUserForm {
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  password: string;
  roleIds: number[];
  userGroupIds: number[];
}

export interface EditUserForm {
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  phoneNumber?: string;
  roleIds: number[];
  userGroupIds: number[];
}

export interface PasswordChangeForm {
  newPassword: string;
  confirmPassword: string;
}

// Role forms
export interface RoleFormData {
  name: string;
  description: string;
  permissionIds: number[];
  moduleId: number;
}

// User Group forms
export interface UserGroupFormData {
  name: string;
  description: string;
}

export interface CreateGroupForm {
  name: string;
  description: string;
}

// Tenant forms
export interface TenantFormData {
  name: string;
  description?: string;
}

// Module forms
export interface ModuleFormData {
  name: string;
  description?: string;
  productId?: number;
}

// Product forms
export interface ProductFormData {
  name: string;
  description?: string;
}

// Permission forms
export interface CreatePermissionForm {
  name: string;
  description: string;
  resource: string;
  action: string;
}

// User Group forms
export interface CreateGroupForm {
  name: string;
  description: string;
}

// Add more form types as needed