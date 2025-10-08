// Store and state management types
import type { User } from './entities';

// Auth store types
export type AuthState = {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
  clearError: () => void;
  checkAuth: () => Promise<void>;
};

// Other store types can be added here as the application grows
export interface AppState {
  // Global application state
}

export interface UIState {
  // UI-specific state (modals, notifications, etc.)
}