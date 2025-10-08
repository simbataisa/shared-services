// Error handling types and interfaces

export type ErrorSeverity = 'low' | 'medium' | 'high' | 'critical';
export type ErrorType = 'validation' | 'network' | 'authentication' | 'authorization' | 'server' | 'client' | 'unknown';
export type ErrorAction = 'retry' | 'refresh' | 'login' | 'contact_support' | 'dismiss' | 'navigate';

// Base error interface
export interface BaseError {
  id: string;
  message: string;
  type: ErrorType;
  severity: ErrorSeverity;
  timestamp: Date;
  code?: string | number;
  details?: string;
  stack?: string;
}

// API Error interface
export interface ApiError extends BaseError {
  type: 'network' | 'server' | 'authentication' | 'authorization';
  status?: number;
  statusText?: string;
  endpoint?: string;
  method?: string;
  requestId?: string;
}

// Validation Error interface
export interface ValidationError extends BaseError {
  type: 'validation';
  field?: string;
  value?: any;
  constraints?: string[];
}

// Client Error interface
export interface ClientError extends BaseError {
  type: 'client';
  component?: string;
  props?: Record<string, any>;
  userAgent?: string;
}

// Error with suggested actions
export interface ErrorWithActions extends BaseError {
  actions: ErrorActionItem[];
  primaryAction?: string;
}

// Error action item
export interface ErrorActionItem {
  id: string;
  label: string;
  action: ErrorAction;
  variant?: 'default' | 'destructive' | 'outline' | 'secondary' | 'ghost' | 'link';
  handler?: () => void | Promise<void>;
  href?: string;
  disabled?: boolean;
}

// Error context for error boundaries
export interface ErrorBoundaryState {
  hasError: boolean;
  error: Error | null;
  errorInfo: React.ErrorInfo | null;
  errorId: string;
}

// Error card props
export interface ErrorCardProps {
  error: BaseError | ErrorWithActions;
  onDismiss?: () => void;
  onAction?: (actionId: string) => void;
  dismissible?: boolean;
  className?: string;
  variant?: 'default' | 'destructive' | 'warning';
  showTimestamp?: boolean;
  showDetails?: boolean;
  collapsible?: boolean;
}

// Error dialog props
export interface ErrorDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  error: BaseError | ErrorWithActions;
  onAction?: (actionId: string) => void;
  title?: string;
  showDetails?: boolean;
  showTimestamp?: boolean;
}

// Error boundary props
export interface ErrorBoundaryProps {
  children: React.ReactNode;
  fallback?: React.ComponentType<ErrorFallbackProps>;
  onError?: (error: Error, errorInfo: React.ErrorInfo) => void;
  resetOnPropsChange?: boolean;
  resetKeys?: Array<string | number>;
}

// Error fallback component props
export interface ErrorFallbackProps {
  error: Error;
  errorInfo: React.ErrorInfo;
  resetError: () => void;
  errorId: string;
}

// Global error context
export interface ErrorContextValue {
  errors: BaseError[];
  addError: (error: Omit<BaseError, 'id' | 'timestamp'>) => string;
  removeError: (id: string) => void;
  clearErrors: () => void;
  getErrorsByType: (type: ErrorType) => BaseError[];
  getErrorsBySeverity: (severity: ErrorSeverity) => BaseError[];
}

// Error toast props
export interface ErrorToastProps {
  error: BaseError;
  onDismiss?: () => void;
  duration?: number;
  position?: 'top-left' | 'top-right' | 'bottom-left' | 'bottom-right' | 'top-center' | 'bottom-center';
}

// Error handler options
export interface ErrorHandlerOptions {
  showToast?: boolean;
  showDialog?: boolean;
  logError?: boolean;
  reportError?: boolean;
  fallbackMessage?: string;
  retryable?: boolean;
  maxRetries?: number;
}

// HTTP Error details
export interface HttpErrorDetails {
  url: string;
  method: string;
  status: number;
  statusText: string;
  headers?: Record<string, string>;
  body?: any;
  requestId?: string;
}

// Form error state
export interface FormErrorState {
  [fieldName: string]: string | string[] | undefined;
}

// Error reporting payload
export interface ErrorReportPayload {
  error: BaseError;
  userAgent: string;
  url: string;
  userId?: string;
  sessionId?: string;
  additionalContext?: Record<string, any>;
}