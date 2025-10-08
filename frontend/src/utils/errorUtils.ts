import type { BaseError, ErrorSeverity, ErrorType } from '@/types/errors';

// Error classification utilities
export const classifyError = (error: any): { type: ErrorType; severity: ErrorSeverity } => {
  // Network/API errors
  if (error.response) {
    const status = error.response.status;
    
    if (status === 401) {
      return { type: 'authentication', severity: 'high' };
    }
    
    if (status === 403) {
      return { type: 'authorization', severity: 'high' };
    }
    
    if (status >= 400 && status < 500) {
      return { type: 'client', severity: 'medium' };
    }
    
    if (status >= 500) {
      return { type: 'server', severity: 'high' };
    }
    
    return { type: 'network', severity: 'medium' };
  }
  
  // Network connection errors
  if (error.code === 'NETWORK_ERROR' || error.message?.includes('Network Error')) {
    return { type: 'network', severity: 'high' };
  }
  
  // Validation errors
  if (error.name === 'ValidationError' || error.type === 'validation') {
    return { type: 'validation', severity: 'medium' };
  }
  
  // Default to client error
  return { type: 'client', severity: 'medium' };
};

// Error message formatting
export const formatErrorMessage = (error: any): string => {
  if (typeof error === 'string') {
    return error;
  }
  
  if (error.message) {
    return error.message;
  }
  
  if (error.response?.data?.message) {
    return error.response.data.message;
  }
  
  if (error.response?.statusText) {
    return `${error.response.status}: ${error.response.statusText}`;
  }
  
  return 'An unexpected error occurred';
};

// Get user-friendly error messages
export const getUserFriendlyMessage = (error: BaseError): string => {
  const messageMap: Record<ErrorType, string> = {
    authentication: 'Please log in to continue',
    authorization: 'You do not have permission to perform this action',
    network: 'Please check your internet connection and try again',
    server: 'Our servers are experiencing issues. Please try again later',
    client: 'Something went wrong. Please try again',
    validation: 'Please check your input and try again',
    unknown: 'An unexpected error occurred'
  };
  
  return messageMap[error.type] || error.message;
};

// Error severity helpers
export const getSeverityColor = (severity: ErrorSeverity): string => {
  const colorMap: Record<ErrorSeverity, string> = {
    low: 'text-blue-600 bg-blue-50 border-blue-200',
    medium: 'text-yellow-600 bg-yellow-50 border-yellow-200',
    high: 'text-red-600 bg-red-50 border-red-200',
    critical: 'text-red-800 bg-red-100 border-red-300'
  };
  
  return colorMap[severity];
};

export const getSeverityIcon = (severity: ErrorSeverity): string => {
  const iconMap: Record<ErrorSeverity, string> = {
    low: 'info',
    medium: 'alert-triangle',
    high: 'alert-circle',
    critical: 'x-circle'
  };
  
  return iconMap[severity];
};

// Error deduplication
export const isDuplicateError = (error1: BaseError, error2: BaseError): boolean => {
  return (
    error1.message === error2.message &&
    error1.type === error2.type &&
    Math.abs(error1.timestamp.getTime() - error2.timestamp.getTime()) < 5000 // 5 seconds
  );
};

// Error grouping
export const groupErrorsByType = (errors: BaseError[]): Record<ErrorType, BaseError[]> => {
  return errors.reduce((groups, error) => {
    const type = error.type;
    if (!groups[type]) {
      groups[type] = [];
    }
    groups[type].push(error);
    return groups;
  }, {} as Record<ErrorType, BaseError[]>);
};

export const groupErrorsBySeverity = (errors: BaseError[]): Record<ErrorSeverity, BaseError[]> => {
  return errors.reduce((groups, error) => {
    const severity = error.severity;
    if (!groups[severity]) {
      groups[severity] = [];
    }
    groups[severity].push(error);
    return groups;
  }, {} as Record<ErrorSeverity, BaseError[]>);
};

// Error filtering
export const filterErrorsByAge = (errors: BaseError[], maxAgeMs: number): BaseError[] => {
  const now = new Date().getTime();
  return errors.filter(error => 
    now - error.timestamp.getTime() <= maxAgeMs
  );
};

export const filterErrorsByType = (errors: BaseError[], types: ErrorType[]): BaseError[] => {
  return errors.filter(error => types.includes(error.type));
};

export const filterErrorsBySeverity = (errors: BaseError[], severities: ErrorSeverity[]): BaseError[] => {
  return errors.filter(error => severities.includes(error.severity));
};

// Error statistics
export const getErrorStats = (errors: BaseError[]) => {
  const total = errors.length;
  const byType = groupErrorsByType(errors);
  const bySeverity = groupErrorsBySeverity(errors);
  
  return {
    total,
    byType: Object.entries(byType).map(([type, errs]) => ({
      type: type as ErrorType,
      count: errs.length,
      percentage: total > 0 ? Math.round((errs.length / total) * 100) : 0
    })),
    bySeverity: Object.entries(bySeverity).map(([severity, errs]) => ({
      severity: severity as ErrorSeverity,
      count: errs.length,
      percentage: total > 0 ? Math.round((errs.length / total) * 100) : 0
    })),
    mostRecent: errors.length > 0 ? errors[0] : null,
    oldestUnresolved: errors.length > 0 ? errors[errors.length - 1] : null
  };
};

// Error serialization for logging/reporting
export const serializeError = (error: BaseError): string => {
  return JSON.stringify({
    id: error.id,
    type: error.type,
    severity: error.severity,
    message: error.message,
    timestamp: error.timestamp.toISOString(),
    code: error.code,
    details: error.details,
    stack: error.stack
  }, null, 2);
};

export const deserializeError = (serialized: string): BaseError | null => {
  try {
    const parsed = JSON.parse(serialized);
    return {
      ...parsed,
      timestamp: new Date(parsed.timestamp)
    };
  } catch {
    return null;
  }
};

// Retry logic helpers
export const shouldRetry = (error: any, attempt: number, maxAttempts: number): boolean => {
  if (attempt >= maxAttempts) {
    return false;
  }
  
  // Don't retry client errors (4xx)
  if (error.response?.status >= 400 && error.response?.status < 500) {
    return false;
  }
  
  // Retry server errors (5xx) and network errors
  return (
    error.response?.status >= 500 ||
    error.code === 'NETWORK_ERROR' ||
    error.message?.includes('Network Error')
  );
};

export const getRetryDelay = (attempt: number, baseDelay: number = 1000): number => {
  // Exponential backoff with jitter
  const exponentialDelay = baseDelay * Math.pow(2, attempt - 1);
  const jitter = Math.random() * 0.1 * exponentialDelay;
  return Math.min(exponentialDelay + jitter, 30000); // Max 30 seconds
};

// Error boundary helpers
export const createErrorId = (): string => {
  return `error-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
};

export const formatStackTrace = (stack?: string): string => {
  if (!stack) return 'No stack trace available';
  
  return stack
    .split('\n')
    .filter(line => line.trim())
    .map(line => line.trim())
    .join('\n');
};

// Development helpers
export const logError = (error: BaseError, context?: string): void => {
  const prefix = context ? `[${context}]` : '[Error]';
  
  console.group(`${prefix} ${error.type.toUpperCase()} - ${error.severity.toUpperCase()}`);
  console.error('Message:', error.message);
  console.error('ID:', error.id);
  console.error('Timestamp:', error.timestamp.toISOString());
  
  if (error.code) {
    console.error('Code:', error.code);
  }
  
  if (error.details) {
    console.error('Details:', error.details);
  }
  
  if (error.stack) {
    console.error('Stack:', error.stack);
  }
  
  console.groupEnd();
};

export default {
  classifyError,
  formatErrorMessage,
  getUserFriendlyMessage,
  getSeverityColor,
  getSeverityIcon,
  isDuplicateError,
  groupErrorsByType,
  groupErrorsBySeverity,
  filterErrorsByAge,
  filterErrorsByType,
  filterErrorsBySeverity,
  getErrorStats,
  serializeError,
  deserializeError,
  shouldRetry,
  getRetryDelay,
  createErrorId,
  formatStackTrace,
  logError
};