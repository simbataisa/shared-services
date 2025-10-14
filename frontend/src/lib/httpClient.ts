import api from "./api";
import type { AxiosResponse } from "axios";
import type {
  ApiResponse,
  Page,
  User,
  Role,
  RoleDetails,
  Permission,
  UserGroup,
  RoleAssignment,
  Tenant,
  Module,
  Product,
  DashboardStats,
  Activity,
  CreateUserRequest,
  UpdateUserRequest,
  CreateRoleRequest,
  UpdateRoleRequest,
  CreateUserGroupRequest,
  UpdateUserGroupRequest,
  CreateTenantRequest,
  CreateModuleRequest,
  CreateProductRequest,
  LoginRequest,
  LoginResponse,
} from "@/types";

class HttpClient {
  // User API methods
  async getUsers(): Promise<User[]> {
    const response: AxiosResponse<ApiResponse<User[]>> = await api.get(
      "/v1/users"
    );
    return response.data.data;
  }

  async getUserById(id: number): Promise<User> {
    const response: AxiosResponse<ApiResponse<User>> = await api.get(
      `/v1/users/${id}`
    );
    return response.data.data;
  }

  async getUserByUsername(username: string): Promise<User> {
    const response: AxiosResponse<ApiResponse<User>> = await api.get(
      `/v1/users/username/${username}`
    );
    return response.data.data;
  }

  async getUserByEmail(email: string): Promise<User> {
    const response: AxiosResponse<ApiResponse<User>> = await api.get(
      `/v1/users/email/${email}`
    );
    return response.data.data;
  }

  async searchUsers(query: string): Promise<User[]> {
    const response: AxiosResponse<ApiResponse<User[]>> = await api.get(
      `/v1/users/search?query=${query}`
    );
    return response.data.data;
  }

  async createUser(userData: CreateUserRequest): Promise<User> {
    const response: AxiosResponse<ApiResponse<User>> = await api.post(
      "/v1/users",
      userData
    );
    return response.data.data;
  }

  async updateUser(id: number, userData: UpdateUserRequest): Promise<User> {
    const response: AxiosResponse<ApiResponse<User>> = await api.put(
      `/v1/users/${id}`,
      userData
    );
    return response.data.data;
  }

  async updateUserStatus(id: number, status: string): Promise<User> {
    const response: AxiosResponse<ApiResponse<User>> = await api.put(
      `/v1/users/${id}/status`,
      { userStatus: status }
    );
    return response.data.data;
  }

  async deleteUser(id: number): Promise<void> {
    await api.delete(`/v1/users/${id}`);
  }

  async getUsersByStatus(status: string): Promise<User[]> {
    const response: AxiosResponse<ApiResponse<User[]>> = await api.get(
      `/v1/users/status/${status}`
    );
    return response.data.data;
  }

  async getLockedUsers(): Promise<User[]> {
    const response: AxiosResponse<ApiResponse<User[]>> = await api.get(
      "/v1/users/locked"
    );
    return response.data.data;
  }

  async getInactiveUsers(cutoffDate?: string): Promise<User[]> {
    const url = cutoffDate
      ? `/v1/users/inactive?cutoffDate=${cutoffDate}`
      : "/v1/users/inactive";
    const response: AxiosResponse<ApiResponse<User[]>> = await api.get(url);
    return response.data.data;
  }

  // Role API methods
  async getRoles(): Promise<Role[]> {
    const response: AxiosResponse<Role[]> = await api.get("/v1/roles");
    return response.data;
  }

  async getRoleById(id: number): Promise<Role> {
    const response: AxiosResponse<ApiResponse<Role>> = await api.get(
      `/v1/roles/${id}`
    );
    return response.data.data;
  }

  async getRoleDetails(id: number): Promise<RoleDetails> {
    const response: AxiosResponse<RoleDetails> = await api.get(
      `/v1/roles/${id}`
    );
    return response.data;
  }

  async createRole(roleData: {
    name: string;
    description: string;
    permissionIds: number[];
  }): Promise<Role> {
    const response: AxiosResponse<ApiResponse<Role>> = await api.post(
      "/v1/roles",
      roleData
    );
    return response.data.data;
  }

  async updateRole(
    id: number,
    roleData: { name?: string; description?: string; permissionIds?: number[] }
  ): Promise<Role> {
    const response: AxiosResponse<ApiResponse<Role>> = await api.put(
      `/v1/roles/${id}`,
      roleData
    );
    return response.data.data;
  }

  async updateRoleStatus(id: number, status: string): Promise<RoleDetails> {
    const response: AxiosResponse<ApiResponse<RoleDetails>> = await api.put(
      `/v1/roles/${id}`,
      { roleStatus: status }
    );
    return response.data.data;
  }

  async deleteRole(id: number): Promise<void> {
    await api.delete(`/v1/roles/${id}`);
  }

  async assignPermissionsToRole(
    roleId: number,
    permissionIds: number[]
  ): Promise<void> {
    await api.put(`/v1/roles/${roleId}/permissions`, permissionIds);
  }

  async removePermissionsFromRole(
    roleId: number,
    permissionIds: number[]
  ): Promise<void> {
    await api.delete(`/v1/roles/${roleId}/permissions`, {
      data: permissionIds,
    });
  }

  async getPermissions(): Promise<Permission[]> {
    const response: AxiosResponse<Permission[]> = await api.get(
      "/v1/permissions"
    );
    return response.data;
  }

  async getPermissionById(id: number): Promise<Permission> {
    const response: AxiosResponse<Permission> = await api.get(
      `/v1/permissions/${id}`
    );
    console.log(response);
    return response.data;
  }

  async updatePermission(
    id: number,
    permissionData: { name?: string; description?: string }
  ): Promise<Permission> {
    const response: AxiosResponse<Permission> = await api.put(
      `/v1/permissions/${id}`,
      permissionData
    );
    return response.data;
  }

  async updatePermissionStatus(
    id: number,
    isActive: boolean
  ): Promise<Permission> {
    const response: AxiosResponse<Permission> = await api.put(
      `/v1/permissions/${id}`,
      { isActive }
    );
    return response.data;
  }

  async assignRoles(userId: number, roleIds: number[]): Promise<User> {
    const response: AxiosResponse<ApiResponse<User>> = await api.post(
      `/v1/users/${userId}/roles`,
      roleIds
    );
    return response.data.data;
  }

  async removeRoles(userId: number, roleIds: number[]): Promise<User> {
    const response: AxiosResponse<ApiResponse<User>> = await api.delete(
      `/v1/users/${userId}/roles`,
      { data: roleIds }
    );
    return response.data.data;
  }

  // User Group API methods
  async getUserGroups(): Promise<Page<UserGroup>> {
    const response: AxiosResponse<ApiResponse<Page<UserGroup>>> = await api.get(
      "/v1/user-groups"
    );
    return response.data.data;
  }

  async getUserGroupById(id: number): Promise<UserGroup> {
    const response: AxiosResponse<UserGroup> = await api.get(
      `/v1/user-groups/${id}`
    );
    return response.data;
  }

  async createUserGroup(groupData: {
    name: string;
    description?: string;
  }): Promise<UserGroup> {
    const response: AxiosResponse<ApiResponse<UserGroup>> = await api.post(
      "/v1/user-groups",
      groupData
    );
    return response.data.data;
  }

  async updateUserGroup(
    id: number,
    groupData: { name?: string; description?: string }
  ): Promise<UserGroup> {
    const response: AxiosResponse<ApiResponse<UserGroup>> = await api.put(
      `/v1/user-groups/${id}`,
      groupData
    );
    return response.data.data;
  }

  async deleteUserGroup(id: number): Promise<void> {
    await api.delete(`/v1/user-groups/${id}`);
  }

  async assignUserGroups(
    userId: number,
    userGroupIds: number[]
  ): Promise<User> {
    const response: AxiosResponse<ApiResponse<User>> = await api.post(
      `/v1/users/${userId}/user-groups`,
      userGroupIds
    );
    return response.data.data;
  }

  async removeUserGroups(
    userId: number,
    userGroupIds: number[]
  ): Promise<User> {
    const response: AxiosResponse<ApiResponse<User>> = await api.delete(
      `/v1/users/${userId}/user-groups`,
      { data: userGroupIds }
    );
    return response.data.data;
  }

  async assignRolesToUserGroup(
    userGroupId: number,
    roleIds: number[]
  ): Promise<void> {
    await api.post(`/v1/user-groups/${userGroupId}/roles`, { roleIds });
  }

  async removeRolesFromUserGroup(
    userGroupId: number,
    roleIds: number[]
  ): Promise<void> {
    await api.delete(`/v1/user-groups/${userGroupId}/roles`, {
      data: { roleIds },
    });
  }

  // Tenant API methods
  async getTenants(): Promise<Tenant[]> {
    const response: AxiosResponse<ApiResponse<Tenant[]>> = await api.get(
      "/v1/tenants"
    );
    return response.data.data;
  }

  async getTenantById(id: number): Promise<Tenant> {
    const response: AxiosResponse<ApiResponse<Tenant>> = await api.get(
      `/v1/tenants/${id}`
    );
    return response.data.data;
  }

  async searchTenants(query: string): Promise<Tenant[]> {
    const response: AxiosResponse<ApiResponse<Tenant[]>> = await api.get(
      `/v1/tenants/search?query=${query}`
    );
    return response.data.data;
  }

  async createTenant(tenantData: CreateTenantRequest): Promise<Tenant> {
    const response: AxiosResponse<ApiResponse<Tenant>> = await api.post(
      "/v1/tenants",
      tenantData
    );
    return response.data.data;
  }

  async updateTenant(
    id: number,
    tenantData: Partial<CreateTenantRequest>
  ): Promise<Tenant> {
    const response: AxiosResponse<ApiResponse<Tenant>> = await api.put(
      `/v1/tenants/${id}`,
      tenantData
    );
    return response.data.data;
  }

  async updateTenantStatus(id: number, status: string): Promise<Tenant> {
    const response: AxiosResponse<ApiResponse<Tenant>> = await api.patch(
      `/v1/tenants/${id}/status`,
      { status }
    );
    return response.data.data;
  }

  async deleteTenant(id: number): Promise<void> {
    await api.delete(`/v1/tenants/${id}`);
  }

  // Module API methods
  async getModules(): Promise<Module[]> {
    const response: AxiosResponse<Module[]> = await api.get("/v1/modules");
    return response.data;
  }

  async getModuleById(id: number): Promise<Module> {
    const response: AxiosResponse<Module> = await api.get(`/v1/modules/${id}`);
    return response.data;
  }

  async getModulesByProductId(productId: number): Promise<Module[]> {
    const response: AxiosResponse<Module[]> = await api.get(
      `/v1/modules/product/${productId}`
    );
    return response.data;
  }

  async createModule(moduleData: CreateModuleRequest): Promise<Module> {
    const response: AxiosResponse<Module> = await api.post(
      "/v1/modules",
      moduleData
    );
    return response.data;
  }

  async updateModule(
    id: number,
    moduleData: Partial<CreateModuleRequest & { isActive?: boolean }>
  ): Promise<Module> {
    const response: AxiosResponse<Module> = await api.put(
      `/v1/modules/${id}`,
      moduleData
    );
    return response.data;
  }

  async deleteModule(id: number): Promise<void> {
    await api.delete(`/v1/modules/${id}`);
  }

  // Product API methods
  async getProducts(): Promise<Product[]> {
    const response: AxiosResponse<Product[]> = await api.get("/products");
    return response.data;
  }

  async getProductById(id: number): Promise<Product> {
    const response: AxiosResponse<Product> = await api.get(`/products/${id}`);
    return response.data;
  }

  async createProduct(productData: CreateProductRequest): Promise<Product> {
    const response: AxiosResponse<Product> = await api.post(
      "/products",
      productData
    );
    return response.data;
  }

  async updateProduct(
    id: number,
    productData: Partial<CreateProductRequest>
  ): Promise<Product> {
    const response: AxiosResponse<Product> = await api.put(
      `/products/${id}`,
      productData
    );
    return response.data;
  }

  async deleteProduct(id: number): Promise<void> {
    await api.delete(`/products/${id}`);
  }

  // Dashboard API methods
  async getDashboardStats(): Promise<DashboardStats> {
    const response: AxiosResponse<ApiResponse<DashboardStats>> = await api.get(
      "/v1/dashboard/stats"
    );
    return response.data.data;
  }

  async getRecentActivities(): Promise<Activity[]> {
    const response: AxiosResponse<ApiResponse<Activity[]>> = await api.get(
      "/v1/dashboard/recent-activities"
    );
    return response.data.data;
  }

  // Auth API methods
  async login(credentials: LoginRequest): Promise<LoginResponse> {
    const response: AxiosResponse<LoginResponse> = await api.post(
      "/v1/auth/login",
      credentials
    );
    return response.data;
  }

  // Generic API methods for flexibility
  async get<T>(url: string): Promise<T> {
    const response: AxiosResponse<T> = await api.get(url);
    return response.data;
  }

  async post<T>(url: string, data?: any): Promise<T> {
    const response: AxiosResponse<T> = await api.post(url, data);
    return response.data;
  }

  async put<T>(url: string, data?: any): Promise<T> {
    const response: AxiosResponse<T> = await api.put(url, data);
    return response.data;
  }

  async patch<T>(url: string, data?: any): Promise<T> {
    const response: AxiosResponse<T> = await api.patch(url, data);
    return response.data;
  }

  async delete<T>(url: string, config?: any): Promise<T> {
    const response: AxiosResponse<T> = await api.delete(url, config);
    return response.data;
  }
}

// Export a singleton instance
export const httpClient = new HttpClient();
export default httpClient;
