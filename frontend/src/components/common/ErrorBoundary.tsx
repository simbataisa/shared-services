import React, { Component, type ReactNode } from "react";
import { AlertTriangle, RefreshCw, Home, Bug, Copy, Check } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Separator } from "@/components/ui/separator";
import {
  Collapsible,
  CollapsibleContent,
  CollapsibleTrigger,
} from "@/components/ui/collapsible";
import { cn } from "@/lib/utils";
import type {
  ErrorBoundaryProps,
  ErrorBoundaryState,
  ErrorFallbackProps,
} from "@/types/errors";

// Generate unique error ID
const generateErrorId = () => {
  return `error_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
};

// Default error fallback component
function DefaultErrorFallback({
  error,
  errorInfo,
  resetError,
  errorId,
}: ErrorFallbackProps) {
  const [showDetails, setShowDetails] = React.useState(false);
  const [copiedField, setCopiedField] = React.useState<string | null>(null);

  const handleCopy = async (text: string, field: string) => {
    try {
      if (typeof navigator !== "undefined" && navigator.clipboard) {
        await navigator.clipboard.writeText(text);
        setCopiedField(field);
        setTimeout(() => setCopiedField(null), 2000);
      }
    } catch (err) {
      console.error("Failed to copy to clipboard:", err);
    }
  };

  const handleReportError = () => {
    // In a real application, you would send this to your error reporting service
    const errorDetails = `
Error: ${error.message || "Unknown error"}
Stack: ${error.stack || "No stack trace available"}
Component Stack: ${errorInfo?.componentStack || "No component stack available"}
Timestamp: ${new Date().toISOString()}
User Agent: ${
      typeof navigator !== "undefined" ? navigator.userAgent : "Unknown"
    }
URL: ${typeof window !== "undefined" ? window.location.href : "Unknown"}
Environment: development
    `.trim();

    const errorReport = {
      errorId,
      message: error.message || "Unknown error",
      stack: error.stack || "No stack trace available",
      componentStack:
        errorInfo?.componentStack || "No component stack available",
      timestamp: new Date().toISOString(),
      userAgent:
        typeof navigator !== "undefined" ? navigator.userAgent : "Unknown",
      url: typeof window !== "undefined" ? window.location.href : "Unknown",
      details: errorDetails,
    };

    console.error("Error Report:", errorReport);
    // Example: sendErrorReport(errorReport);
  };

  const handleGoHome = () => {
    window.location.href = "/";
  };

  return (
    <div className="min-h-[400px] flex items-center justify-center p-4">
      <Card className="w-full max-w-2xl border-red-200 bg-red-50">
        <CardHeader>
          <div className="flex items-center gap-3">
            <AlertTriangle className="h-6 w-6 text-red-600" />
            <div className="flex-1">
              <CardTitle className="text-red-900">
                Something went wrong
              </CardTitle>
              <div className="flex items-center gap-2 mt-1">
                <Badge variant="destructive" className="text-xs">
                  CRITICAL
                </Badge>
                <Badge variant="outline" className="text-xs font-mono">
                  {errorId}
                </Badge>
              </div>
            </div>
          </div>
        </CardHeader>

        <CardContent className="space-y-4">
          <p className="text-red-800">
            An unexpected error occurred while rendering this component. The
            error has been logged and our team has been notified.
          </p>

          <div className="p-3 bg-red-100 rounded-md">
            <p className="text-sm font-medium text-red-900 mb-1">
              Error Message:
            </p>
            <p className="text-sm text-red-800 font-mono break-words">
              {error.message}
            </p>
          </div>

          {/* Error Details */}
          <Collapsible open={showDetails} onOpenChange={setShowDetails}>
            <CollapsibleTrigger asChild>
              <Button variant="outline" size="sm" className="w-full">
                <Bug className="h-4 w-4 mr-2" />
                {showDetails ? "Hide" : "Show"} Technical Details
              </Button>
            </CollapsibleTrigger>
            <CollapsibleContent className="mt-3 space-y-3">
              <div className="p-3 bg-muted/50 rounded-md">
                <div className="flex items-center justify-between mb-2">
                  <h4 className="text-sm font-medium">Stack Trace</h4>
                  <Button
                    variant="ghost"
                    size="sm"
                    className="h-6 w-6 p-0"
                    onClick={() => handleCopy(error.stack || "", "Stack Trace")}
                  >
                    {copiedField === "Stack Trace" ? (
                      <Check className="h-3 w-3 text-green-600" />
                    ) : (
                      <Copy className="h-3 w-3" />
                    )}
                  </Button>
                </div>
                <pre className="text-xs font-mono whitespace-pre-wrap text-muted-foreground overflow-x-auto">
                  {error.stack || "No stack trace available"}
                </pre>
              </div>

              <div className="p-3 bg-muted/50 rounded-md">
                <div className="flex items-center justify-between mb-2">
                  <h4 className="text-sm font-medium">Component Stack</h4>
                  <Button
                    variant="ghost"
                    size="sm"
                    className="h-6 w-6 p-0"
                    onClick={() =>
                      handleCopy(
                        errorInfo?.componentStack || "",
                        "Component Stack"
                      )
                    }
                  >
                    {copiedField === "Component Stack" ? (
                      <Check className="h-3 w-3 text-green-600" />
                    ) : (
                      <Copy className="h-3 w-3" />
                    )}
                  </Button>
                </div>
                <pre className="text-xs font-mono whitespace-pre-wrap text-muted-foreground overflow-x-auto">
                  {errorInfo.componentStack}
                </pre>
              </div>

              <div className="grid grid-cols-2 gap-4 text-xs">
                <div>
                  <span className="font-medium">Error ID:</span>
                  <p className="font-mono text-muted-foreground">{errorId}</p>
                </div>
                <div>
                  <span className="font-medium">Timestamp:</span>
                  <p className="font-mono text-muted-foreground">
                    {new Date().toISOString()}
                  </p>
                </div>
                <div>
                  <span className="font-medium">User Agent:</span>
                  <p className="font-mono text-muted-foreground truncate">
                    {navigator.userAgent}
                  </p>
                </div>
                <div>
                  <span className="font-medium">URL:</span>
                  <p className="font-mono text-muted-foreground truncate">
                    {window.location.href}
                  </p>
                </div>
              </div>
            </CollapsibleContent>
          </Collapsible>

          <Separator />

          {/* Action Buttons */}
          <div className="flex flex-wrap gap-2">
            <Button onClick={resetError} className="flex-1 sm:flex-none">
              <RefreshCw className="h-4 w-4 mr-2" />
              Try Again
            </Button>
            <Button
              variant="outline"
              onClick={handleGoHome}
              className="flex-1 sm:flex-none"
            >
              <Home className="h-4 w-4 mr-2" />
              Go Home
            </Button>
            <Button
              variant="outline"
              onClick={handleReportError}
              className="flex-1 sm:flex-none"
            >
              <Bug className="h-4 w-4 mr-2" />
              Report Issue
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}

// Error Boundary Class Component
export class ErrorBoundary extends Component<
  ErrorBoundaryProps,
  ErrorBoundaryState
> {
  private resetTimeoutId: number | null = null;

  constructor(props: ErrorBoundaryProps) {
    super(props);
    this.state = {
      hasError: false,
      error: null,
      errorInfo: null,
      errorId: "",
    };
  }

  static getDerivedStateFromError(error: Error): Partial<ErrorBoundaryState> {
    return {
      hasError: true,
      error,
      errorId: generateErrorId(),
    };
  }

  componentDidCatch(error: Error, errorInfo: React.ErrorInfo) {
    this.setState({
      errorInfo,
    });

    // Call the onError callback if provided
    this.props.onError?.(error, errorInfo);

    // Log error to console in development
    // Log error details in development mode
    if (import.meta.env.DEV) {
      console.group("🚨 Error Boundary Caught an Error");
      console.error("Error:", error);
      console.error("Error Info:", errorInfo);
      console.error("Component Stack:", errorInfo.componentStack);
      console.groupEnd();
    }
  }

  componentDidUpdate(prevProps: ErrorBoundaryProps) {
    const { resetKeys, resetOnPropsChange } = this.props;
    const { hasError } = this.state;

    // Reset error state if resetOnPropsChange is true and props changed
    if (hasError && resetOnPropsChange && prevProps !== this.props) {
      this.resetErrorBoundary();
      return;
    }

    // Reset error state if resetKeys changed
    if (hasError && resetKeys && prevProps.resetKeys) {
      const hasResetKeyChanged = resetKeys.some(
        (key, index) => key !== prevProps.resetKeys?.[index]
      );

      if (hasResetKeyChanged) {
        this.resetErrorBoundary();
      }
    }
  }

  componentWillUnmount() {
    if (this.resetTimeoutId) {
      clearTimeout(this.resetTimeoutId);
    }
  }

  resetErrorBoundary = () => {
    if (this.resetTimeoutId) {
      clearTimeout(this.resetTimeoutId);
    }

    this.resetTimeoutId = window.setTimeout(() => {
      this.setState({
        hasError: false,
        error: null,
        errorInfo: null,
        errorId: "",
      });
    }, 0);
  };

  render() {
    const { hasError, error, errorInfo, errorId } = this.state;
    const { children, fallback: FallbackComponent } = this.props;

    if (hasError && error && errorInfo) {
      if (FallbackComponent) {
        return (
          <FallbackComponent
            error={error}
            errorInfo={errorInfo}
            resetError={this.resetErrorBoundary}
            errorId={errorId}
          />
        );
      }

      return (
        <DefaultErrorFallback
          error={error}
          errorInfo={errorInfo}
          resetError={this.resetErrorBoundary}
          errorId={errorId}
        />
      );
    }

    return children;
  }
}

// Higher-order component for easier usage
export function withErrorBoundary<P extends object>(
  Component: React.ComponentType<P>,
  errorBoundaryProps?: Omit<ErrorBoundaryProps, "children">
) {
  const WrappedComponent = (props: P) => (
    <ErrorBoundary {...errorBoundaryProps}>
      <Component {...props} />
    </ErrorBoundary>
  );

  WrappedComponent.displayName = `withErrorBoundary(${
    Component.displayName || Component.name
  })`;

  return WrappedComponent;
}

export default ErrorBoundary;
