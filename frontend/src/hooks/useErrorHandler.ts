import { useCallback, useContext } from 'react';
import { ErrorContext } from '@/contexts/ErrorContext';
import type { 
  BaseError, 
  ApiError, 
  ValidationError, 
  ClientError, 
  ErrorHandlerOptions 
} from '@/types/errors';

export const useErrorHandler = () => {
  const context = useContext(ErrorContext);
  
  if (!context) {
    throw new Error('useErrorHandler must be used within an ErrorProvider');
  }

  const { addError, removeError, clearErrors, errors, getErrorsByType, getErrorsBySeverity } = context;

  // Handle API errors
  const handleApiError = useCallback((error: any) => {
    const errorId = addError({
      type: 'network',
      severity: 'high',
      message: error.message || 'An API error occurred',
      code: error.response?.status,
      details: JSON.stringify({
        endpoint: error.config?.url,
        method: error.config?.method?.toUpperCase(),
        responseData: error.response?.data,
        requestData: error.config?.data
      })
    });

    return errorId;
  }, [addError]);

  // Handle validation errors
  const handleValidationError = useCallback((
    field: string, 
    message: string, 
    options?: { severity?: BaseError['severity']; value?: any }
  ) => {
    const errorId = addError({
      type: 'validation',
      severity: options?.severity || 'medium',
      message,
      details: JSON.stringify({ field, value: options?.value })
    });

    return errorId;
  }, [addError]);

  // Handle client errors
  const handleClientError = useCallback((
    message: string, 
    options?: { severity?: BaseError['severity']; code?: string; context?: any }
  ) => {
    const errorId = addError({
      type: 'client',
      severity: options?.severity || 'high',
      message,
      code: options?.code,
      details: options?.context ? JSON.stringify(options.context) : undefined
    });

    return errorId;
  }, [addError]);

  // Handle generic errors
  const handleError = useCallback((
    error: Error | string, 
    options?: { severity?: BaseError['severity'] }
  ) => {
    const message = typeof error === 'string' ? error : error.message;
    const errorId = addError({
      type: 'unknown',
      severity: options?.severity || 'high',
      message,
      stack: typeof error === 'object' ? error.stack : undefined
    });

    return errorId;
  }, [addError]);

  // Handle async operations with error catching
  const withErrorHandling = useCallback(<T>(
    asyncFn: () => Promise<T>
  ) => {
    return async (): Promise<T | null> => {
      try {
        return await asyncFn();
      } catch (error: any) {
        if (error.response) {
          // API error
          handleApiError(error);
        } else {
          // Client error
          handleError(error);
        }
        return null;
      }
    };
  }, [handleApiError, handleError]);

  // Retry mechanism for failed operations
  const retry = useCallback(async <T>(
    operation: () => Promise<T>,
    maxAttempts: number = 3,
    delay: number = 1000
  ): Promise<T | null> => {
    for (let attempt = 1; attempt <= maxAttempts; attempt++) {
      try {
        return await operation();
      } catch (error: any) {
        if (attempt === maxAttempts) {
          handleApiError(error);
          return null;
        }
        
        // Wait before retrying
        await new Promise(resolve => setTimeout(resolve, delay * attempt));
      }
    }
    return null;
  }, [handleApiError]);

  return {
    // Error handlers
    handleError,
    handleApiError,
    handleValidationError,
    handleClientError,
    
    // Utilities
    withErrorHandling,
    retry,
    
    // Context methods
    addError,
    removeError,
    clearErrors,
    getErrorsByType,
    getErrorsBySeverity,
    
    // State
    errors,
    hasErrors: errors.length > 0,
    errorCount: errors.length
  };
};

export default useErrorHandler;