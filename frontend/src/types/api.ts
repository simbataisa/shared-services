// API-related types and interfaces

export interface ApiResponse<T> {
  data: T;
  message: string;
  path: string;
  success?: boolean;
}

// Authentication types
export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  user: User;
}

// Request types for creating/updating entities
export interface CreateUserRequest {
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  password: string;
  roleIds: number[];
  userGroupIds: number[];
}

export interface UpdateUserRequest {
  username?: string;
  email?: string;
  firstName?: string;
  lastName?: string;
  phoneNumber?: string;
  roleIds?: number[];
  userGroupIds?: number[];
}

export interface CreateRoleRequest {
  name: string;
  description: string;
  permissionIds: number[];
}

export interface UpdateRoleRequest {
  name?: string;
  description?: string;
  permissionIds?: number[];
}

export interface CreateUserGroupRequest {
  name: string;
  description?: string;
}

export interface UpdateUserGroupRequest {
  name?: string;
  description?: string;
}

export interface CreateTenantRequest {
  name: string;
  description?: string;
}

export interface CreateModuleRequest {
  name: string;
  description?: string;
  productId?: number;
}

export interface CreateProductRequest {
  name: string;
  description?: string;
}

// Import core entity types
import type { User } from "./entities";
