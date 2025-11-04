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
  RecentActivity,
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
      "/users"
    );
    return response.data.data;
  }

  async getUserById(id: number): Promise<User> {
    const response: AxiosResponse<ApiResponse<User>> = await api.get(
      `/users/${id}`
    );
    return response.data.data;
  }

  async getUserByUsername(username: string): Promise<User> {
    const response: AxiosResponse<ApiResponse<User>> = await api.get(
      `/users/username/${username}`
    );
    return response.data.data;
  }

  async getUserByEmail(email: string): Promise<User> {
    const response: AxiosResponse<ApiResponse<User>> = await api.get(
      `/users/email/${email}`
    );
    return response.data.data;
  }

  async searchUsers(query: string): Promise<User[]> {
    const response: AxiosResponse<ApiResponse<User[]>> = await api.get(
      `/users/search?query=${query}`
    );
    return response.data.data;
  }

  async createUser(userData: CreateUserRequest): Promise<User> {
    const response: AxiosResponse<ApiResponse<User>> = await api.post(
      "/users",
      userData
    );
    return response.data.data;
  }

  async updateUser(id: number, userData: UpdateUserRequest): Promise<User> {
    const response: AxiosResponse<ApiResponse<User>> = await api.put(
      `/users/${id}`,
      userData
    );
    return response.data.data;
  }

  async updateUserStatus(id: number, status: string): Promise<User> {
    const response: AxiosResponse<ApiResponse<User>> = await api.put(
      `/users/${id}/status`,
      { userStatus: status }
    );
    return response.data.data;
  }

  async deleteUser(id: number): Promise<void> {
    await api.delete(`/users/${id}`);
  }

  async getUsersByStatus(status: string): Promise<User[]> {
    const response: AxiosResponse<ApiResponse<User[]>> = await api.get(
      `/users/status/${status}`
    );
    return response.data.data;
  }

  async getLockedUsers(): Promise<User[]> {
    const response: AxiosResponse<ApiResponse<User[]>> = await api.get(
      "/users/locked"
    );
    return response.data.data;
  }

  async getInactiveUsers(cutoffDate?: string): Promise<User[]> {
    const url = cutoffDate
      ? `/users/inactive?cutoffDate=${cutoffDate}`
      : "/users/inactive";
    const response: AxiosResponse<ApiResponse<User[]>> = await api.get(url);
    return response.data.data;
  }

  // Role API methods
  async getRoles(): Promise<Role[]> {
    const response: AxiosResponse<ApiResponse<Role[]>> = await api.get("/roles");
    return response.data.data;
  }

  async getRoleById(id: number): Promise<Role> {
    const response: AxiosResponse<ApiResponse<Role>> = await api.get(
      `/roles/${id}`
    );
    return response.data.data;
  }

  async getRoleDetails(id: number): Promise<RoleDetails> {
    const response: AxiosResponse<ApiResponse<RoleDetails>> = await api.get(
      `/roles/${id}`
    );
    return response.data.data;
  }

  async createRole(roleData: {
    name: string;
    description: string;
    permissionIds: number[];
  }): Promise<Role> {
    const response: AxiosResponse<ApiResponse<Role>> = await api.post(
      "/roles",
      roleData
    );
    return response.data.data;
  }

  async updateRole(
    id: number,
    roleData: { name?: string; description?: string; permissionIds?: number[] }
  ): Promise<Role> {
    const response: AxiosResponse<ApiResponse<Role>> = await api.put(
      `/roles/${id}`,
      roleData
    );
    return response.data.data;
  }

  async updateRoleStatus(id: number, status: string): Promise<RoleDetails> {
    const response: AxiosResponse<ApiResponse<RoleDetails>> = await api.put(
      `/roles/${id}`,
      { roleStatus: status }
    );
    return response.data.data;
  }

  async deleteRole(id: number): Promise<void> {
    await api.delete(`/roles/${id}`);
  }

  async assignPermissionsToRole(
    roleId: number,
    permissionIds: number[]
  ): Promise<void> {
    await api.put(`/roles/${roleId}/permissions`, permissionIds);
  }

  async removePermissionsFromRole(
    roleId: number,
    permissionIds: number[]
  ): Promise<void> {
    await api.delete(`/roles/${roleId}/permissions`, {
      data: permissionIds,
    });
  }

  async getPermissions(): Promise<Permission[]> {
    const response: AxiosResponse<ApiResponse<Permission[]>> = await api.get(
      "/permissions"
    );
    return response.data.data;
  }

  async getPermissionById(id: number): Promise<Permission> {
    const response: AxiosResponse<ApiResponse<Permission>> = await api.get(
      `/permissions/${id}`
    );
    console.log(response);
    return response.data.data;
  }

  async updatePermission(
    id: number,
    permissionData: { name?: string; description?: string }
  ): Promise<Permission> {
    const response: AxiosResponse<ApiResponse<Permission>> = await api.put(
      `/permissions/${id}`,
      permissionData
    );
    return response.data.data;
  }

  async updatePermissionStatus(
    id: number,
    isActive: boolean
  ): Promise<Permission> {
    const response: AxiosResponse<ApiResponse<Permission>> = await api.put(
      `/permissions/${id}`,
      { isActive }
    );
    return response.data.data;
  }

  async assignRoles(userId: number, roleIds: number[]): Promise<User> {
    const response: AxiosResponse<ApiResponse<User>> = await api.post(
      `/users/${userId}/roles`,
      roleIds
    );
    return response.data.data;
  }

  async removeRoles(userId: number, roleIds: number[]): Promise<User> {
    const response: AxiosResponse<ApiResponse<User>> = await api.delete(
      `/users/${userId}/roles`,
      { data: roleIds }
    );
    return response.data.data;
  }

  // User Group API methods
  async getUserGroups(): Promise<Page<UserGroup>> {
    const response: AxiosResponse<ApiResponse<Page<UserGroup>>> = await api.get(
      "/user-groups"
    );
    return response.data.data;
  }

  async getUserGroupById(id: number): Promise<UserGroup> {
    const response: AxiosResponse<ApiResponse<UserGroup>> = await api.get(
      `/user-groups/${id}`
    );
    return response.data.data;
  }

  async createUserGroup(groupData: {
    name: string;
    description?: string;
  }): Promise<UserGroup> {
    const response: AxiosResponse<ApiResponse<UserGroup>> = await api.post(
      "/user-groups",
      groupData
    );
    return response.data.data;
  }

  async updateUserGroup(
    id: number,
    groupData: { name?: string; description?: string }
  ): Promise<UserGroup> {
    const response: AxiosResponse<ApiResponse<UserGroup>> = await api.put(
      `/user-groups/${id}`,
      groupData
    );
    return response.data.data;
  }

  async deleteUserGroup(id: number): Promise<void> {
    await api.delete(`/user-groups/${id}`);
  }

  async assignUserGroups(
    userId: number,
    userGroupIds: number[]
  ): Promise<User> {
    const response: AxiosResponse<ApiResponse<User>> = await api.post(
      `/users/${userId}/user-groups`,
      userGroupIds
    );
    return response.data.data;
  }

  async removeUserGroups(
    userId: number,
    userGroupIds: number[]
  ): Promise<User> {
    const response: AxiosResponse<ApiResponse<User>> = await api.delete(
      `/users/${userId}/user-groups`,
      { data: userGroupIds }
    );
    return response.data.data;
  }

  async assignRolesToUserGroup(
    userGroupId: number,
    roleIds: number[]
  ): Promise<void> {
    await api.post(`/user-groups/${userGroupId}/roles`, { roleIds });
  }

  async removeRolesFromUserGroup(
    userGroupId: number,
    roleIds: number[]
  ): Promise<void> {
    await api.delete(`/user-groups/${userGroupId}/roles`, {
      data: { roleIds },
    });
  }

  // Tenant API methods
  async getTenants(): Promise<Tenant[]> {
    const response: AxiosResponse<ApiResponse<Tenant[]>> = await api.get(
      "/tenants"
    );
    return response.data.data;
  }

  async getTenantById(id: number): Promise<Tenant> {
    const response: AxiosResponse<ApiResponse<Tenant>> = await api.get(
      `/tenants/${id}`
    );
    return response.data.data;
  }

  async searchTenants(query: string): Promise<Tenant[]> {
    const response: AxiosResponse<ApiResponse<Tenant[]>> = await api.get(
      `/tenants/search?query=${query}`
    );
    return response.data.data;
  }

  async createTenant(tenantData: CreateTenantRequest): Promise<Tenant> {
    const response: AxiosResponse<ApiResponse<Tenant>> = await api.post(
      "/tenants",
      tenantData
    );
    return response.data.data;
  }

  async updateTenant(
    id: number,
    tenantData: Partial<CreateTenantRequest>
  ): Promise<Tenant> {
    const response: AxiosResponse<ApiResponse<Tenant>> = await api.put(
      `/tenants/${id}`,
      tenantData
    );
    return response.data.data;
  }

  async updateTenantStatus(id: number, status: string): Promise<Tenant> {
    const response: AxiosResponse<ApiResponse<Tenant>> = await api.patch(
      `/tenants/${id}/status`,
      { status }
    );
    return response.data.data;
  }

  async deleteTenant(id: number): Promise<void> {
    await api.delete(`/tenants/${id}`);
  }

  // Module API methods
  async getModules(): Promise<Module[]> {
    const response: AxiosResponse<ApiResponse<Module[]>> = await api.get("/modules");
    return response.data.data;
  }

  async getModuleById(id: number): Promise<Module> {
    const response: AxiosResponse<ApiResponse<Module>> = await api.get(`/modules/${id}`);
    return response.data.data;
  }

  async getModulesByProductId(productId: number): Promise<Module[]> {
    const response: AxiosResponse<ApiResponse<Module[]>> = await api.get(
      `/modules/product/${productId}`
    );
    return response.data.data;
  }

  async createModule(moduleData: CreateModuleRequest): Promise<Module> {
    const response: AxiosResponse<ApiResponse<Module>> = await api.post(
      "/modules",
      moduleData
    );
    return response.data.data;
  }

  async updateModule(
    id: number,
    moduleData: Partial<CreateModuleRequest & { isActive?: boolean }>
  ): Promise<Module> {
    const response: AxiosResponse<ApiResponse<Module>> = await api.put(
      `/modules/${id}`,
      moduleData
    );
    return response.data.data;
  }

  async deleteModule(id: number): Promise<void> {
    await api.delete(`/modules/${id}`);
  }

  // Product API methods
  async getProducts(): Promise<Product[]> {
    const response: AxiosResponse<ApiResponse<Product[]>> = await api.get("/products");
    return response.data.data;
  }

  async getProductById(id: number): Promise<Product> {
    const response: AxiosResponse<ApiResponse<Product>> = await api.get(`/products/${id}`);
    return response.data.data;
  }

  async createProduct(productData: CreateProductRequest): Promise<Product> {
    const response: AxiosResponse<ApiResponse<Product>> = await api.post(
      "/products",
      productData
    );
    return response.data.data;
  }

  async updateProduct(
    id: number,
    productData: Partial<CreateProductRequest>
  ): Promise<Product> {
    const response: AxiosResponse<ApiResponse<Product>> = await api.put(
      `/products/${id}`,
      productData
    );
    return response.data.data;
  }

  async deleteProduct(id: number): Promise<void> {
    await api.delete(`/products/${id}`);
  }

  // Dashboard API methods
  async getDashboardStats(): Promise<DashboardStats> {
    const response: AxiosResponse<ApiResponse<DashboardStats>> = await api.get(
      "/dashboard/stats"
    );
    return response.data.data;
  }

  async getRecentActivities(): Promise<Activity[]> {
    const response: AxiosResponse<ApiResponse<Activity[]>> = await api.get(
      "/dashboard/recent-activities"
    );
    return response.data.data;
  }

  async getDashboardData(): Promise<{
    stats: DashboardStats;
    activities: RecentActivity[];
  }> {
    const [statsResponse, activitiesResponse] = await Promise.all([
      api.get<ApiResponse<DashboardStats>>("/dashboard/stats"),
      api.get<ApiResponse<RecentActivity[]>>("/dashboard/recent-activities"),
    ]);
    
    return {
      stats: statsResponse.data.data,
      activities: activitiesResponse.data.data,
    };
  }

  // Auth API methods
  async login(credentials: LoginRequest): Promise<LoginResponse> {
    const response: AxiosResponse<ApiResponse<{ token: string }>> = await api.post(
      "/auth/login",
      credentials
    );
    
    return {
      token: response.data.data.token,
      user: null as any // User info will be extracted from JWT token in the auth store
    };
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
