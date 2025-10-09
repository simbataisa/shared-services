import React from "react";
import { Skeleton } from "@/components/ui/skeleton";
import { Spinner } from "@/components/ui/spinner";

interface LoadingSpinnerProps {
  variant?: "page" | "overlay";
  message?: string;
}

export const LoadingSpinner: React.FC<LoadingSpinnerProps> = ({ variant = "page", message }) => {
  if (variant === "overlay") {
    return (
      <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/20">
        <div className="flex items-center gap-3 rounded-md bg-white px-4 py-3 shadow">
          <Spinner className="size-5 text-muted-foreground" />
          <span className="text-sm text-muted-foreground">{message || "Loading..."}</span>
        </div>
      </div>
    );
  }

  // Default page skeleton layout (detail page style)
  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="space-y-6">
          <Skeleton className="h-8 w-64" />
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <Skeleton className="h-32" />
            <Skeleton className="h-32" />
            <Skeleton className="h-32" />
          </div>
          <Skeleton className="h-64" />
        </div>
      </div>
    </div>
  );
};

export default LoadingSpinner;