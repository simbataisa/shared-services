import { create } from "zustand";
import { persist } from "zustand/middleware";
import { decodeJwt, isTokenExpired } from "@/lib/jwt";
import type { UserProfile, Tenant } from "@/types/entities";

interface AuthState {
  token: string | null;
  user: UserProfile | null;
  tenant: Tenant | null;
  permissions: string[];
  isAuthenticated: boolean;
  setToken: (token: string | null) => void;
  setUser: (user: UserProfile | null) => void;
  setTenant: (tenant: Tenant | null) => void;
  hasPermission: (permission: string) => boolean;
  hasRole: (roleName: string) => boolean;
  hasAnyRole: (roleNames: string[]) => boolean;
  canAccessResource: (resource: string, action: string) => boolean;
  logout: () => void;
}

export const useAuth = create<AuthState>((set, get) => {
  // Initialize state from localStorage
  const storedToken = localStorage.getItem("token");
  let initialState: Partial<AuthState> = {
    token: null,
    user: null,
    tenant: null,
    permissions: [],
    isAuthenticated: false,
  };

  // If there's a stored token, try to decode it
  if (storedToken && !isTokenExpired(storedToken)) {
    const payload = decodeJwt(storedToken);
    if (payload && payload.userId) {
      const user: UserProfile = {
        id: payload.userId.toString(),
        email: payload.sub,
        name: `${payload.firstName || ""} ${payload.lastName || ""}`.trim(),
        firstName: payload.firstName,
        lastName: payload.lastName,
        username: payload.username,
        roles: (payload.roles || []).map((roleName, index) => ({
          id: index + 1, // Use numeric ID instead of string
          name: roleName,
          permissions: [],
        })),
        permissions: (payload.permissions || []).map((permName, index) => ({
          id: index + 1, // Use numeric ID instead of string
          name: permName,
          resource: permName.split(":")[0] || "",
          action: permName.split(":")[1] || "",
        })),
        createdAt: new Date().toISOString(),
      };

      initialState = {
        token: storedToken,
        user,
        tenant: null,
        permissions: payload.permissions || [],
        isAuthenticated: true,
      };
    }
  } else if (storedToken) {
    // Token exists but is expired, remove it
    localStorage.removeItem("token");
  }

  return {
    token: initialState.token || null,
    user: initialState.user || null,
    tenant: initialState.tenant || null,
    permissions: initialState.permissions || [],
    isAuthenticated: initialState.isAuthenticated || false,

    setToken: (token) => {
      if (token) {
        // Check if token is expired
        if (isTokenExpired(token)) {
          console.warn("Token is expired");
          localStorage.removeItem("token");
          set({
            token: null,
            isAuthenticated: false,
            user: null,
            tenant: null,
            permissions: [],
          });
          return;
        }

        localStorage.setItem("token", token);

        // Decode JWT and extract user information
        const payload = decodeJwt(token);
        if (payload) {
          const user: UserProfile = {
            id: payload.userId.toString(),
            email: payload.sub,
            name: `${payload.firstName} ${payload.lastName}`.trim(),
            firstName: payload.firstName,
            lastName: payload.lastName,
            username: payload.username,
            roles: payload.roles.map((roleName, index) => ({
              id: index + 1,
              name: roleName,
              permissions: [],
            })),
            permissions: payload.permissions.map((permName, index) => ({
              id: index + 1,
              name: permName,
              resource: permName.split(":")[0] || "",
              action: permName.split(":")[1] || "",
            })),
            createdAt: new Date().toISOString(),
          };

          const permissions = payload.permissions || [];

          set({
            token,
            isAuthenticated: true,
            user,
            permissions,
          });
        } else {
          set({ token, isAuthenticated: true });
        }
      } else {
        localStorage.removeItem("token");
        set({
          token,
          isAuthenticated: false,
          user: null,
          tenant: null,
          permissions: [],
        });
      }
    },

    setUser: (user) => {
      const permissions = user?.permissions.map((p) => p.name) || [];
      set({ user, permissions });
    },

    setTenant: (tenant) => {
      set({ tenant });
    },

    hasPermission: (permission: string) => {
      const { permissions } = get();
      return permissions.includes(permission);
    },

    hasRole: (roleName: string) => {
      const { user } = get();
      return user?.roles.some((role) => role.name === roleName) || false;
    },

    hasAnyRole: (roleNames: string[]) => {
      const { user } = get();
      return user?.roles.some((role) => roleNames.includes(role.name)) || false;
    },

    canAccessResource: (resource: string, action: string) => {
      const { user } = get();
      return (
        user?.permissions.some(
          (p) => p.resource === resource && p.action === action
        ) || false
      );
    },

    logout: () => {
      localStorage.removeItem("token");
      set({
        token: null,
        user: null,
        tenant: null,
        permissions: [],
        isAuthenticated: false,
      });
    },
  };
});
