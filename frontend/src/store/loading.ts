import { create } from "zustand";

type LoadingState = {
  loadingCount: number;
  isGlobalLoading: boolean;
  increment: () => void;
  decrement: () => void;
  reset: () => void;
};

export const useLoadingStore = create<LoadingState>((set, get) => ({
  loadingCount: 0,
  isGlobalLoading: false,
  increment: () => {
    const next = Math.max(0, get().loadingCount + 1);
    set({ loadingCount: next, isGlobalLoading: next > 0 });
  },
  decrement: () => {
    const next = Math.max(0, get().loadingCount - 1);
    set({ loadingCount: next, isGlobalLoading: next > 0 });
  },
  reset: () => set({ loadingCount: 0, isGlobalLoading: false }),
}));

export default useLoadingStore;