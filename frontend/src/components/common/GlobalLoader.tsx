import React from "react";
import { useLoadingStore } from "@/store/loading";
import LoadingSpinner from "./LoadingSpinner";

const GlobalLoader: React.FC = () => {
  const isGlobalLoading = useLoadingStore((s) => s.isGlobalLoading);
  if (!isGlobalLoading) return null;
  return <LoadingSpinner variant="overlay" />;
};

export default GlobalLoader;