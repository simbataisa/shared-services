import React, { createContext, useContext, type ReactNode } from "react";
import { useLoadingStore } from "@/store/loading";

export interface LoadingContextValue {
  loadingCount: number;
  isGlobalLoading: boolean;
  increment: () => void;
  decrement: () => void;
  reset: () => void;
}

export const LoadingContext = createContext<LoadingContextValue | null>(null);

export const useGlobalLoading = () => {
  const ctx = useContext(LoadingContext);
  if (!ctx) throw new Error("useGlobalLoading must be used within LoadingProvider");
  return ctx;
};

export const LoadingProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const { loadingCount, isGlobalLoading, increment, decrement, reset } = useLoadingStore();

  const value: LoadingContextValue = {
    loadingCount,
    isGlobalLoading,
    increment,
    decrement,
    reset,
  };

  return <LoadingContext.Provider value={value}>{children}</LoadingContext.Provider>;
};

export default LoadingProvider;