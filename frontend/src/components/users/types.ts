// Shared types and interfaces for user components

export interface User {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  userStatus: "ACTIVE" | "INACTIVE";
  createdAt: string;
  updatedAt: string;
  roles: Role[];
  userGroups: UserGroup[];
  phoneNumber?: string;
  emailVerified?: boolean;
  lastLogin?: string;
  failedLoginAttempts?: number;
  createdBy?: string;
  updatedBy?: string;
}

export interface Role {
  id: number;
  name: string;
  description: string;
}

export interface UserGroup {
  userGroupId: number;
  name: string;
  description: string;
}

export interface UserStats {
  totalRoles: number;
  totalUserGroups: number;
  lastLogin: string | null;
  accountStatus: string;
}

export interface CreateUserForm {
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  password: string;
  roleIds: number[];
  userGroupIds: number[];
}

export interface PasswordChangeForm {
  newPassword: string;
  confirmPassword: string;
}

// Common props interfaces
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