import React, { createContext, useCallback, useState, type ReactNode } from 'react';
import type { BaseError, ErrorContextValue } from '@/types/errors';

export const ErrorContext = createContext<ErrorContextValue | null>(null);

interface ErrorProviderProps {
  children: ReactNode;
  maxErrors?: number;
}

export const ErrorProvider: React.FC<ErrorProviderProps> = ({ 
  children, 
  maxErrors = 10 
}) => {
  const [errors, setErrors] = useState<BaseError[]>([]);

  const addError = useCallback((error: Omit<BaseError, 'id' | 'timestamp'>) => {
    const fullError: BaseError = {
      ...error,
      id: `error-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
      timestamp: new Date()
    };

    setErrors(prevErrors => {
      // Remove duplicate errors (same message and type)
      const filteredErrors = prevErrors.filter(
        existingError => 
          existingError.message !== fullError.message || 
          existingError.type !== fullError.type
      );

      // Add new error and limit total count
      const newErrors = [fullError, ...filteredErrors];
      return newErrors.slice(0, maxErrors);
    });

    return fullError.id;
  }, [maxErrors]);

  const removeError = useCallback((errorId: string) => {
    setErrors(prevErrors => 
      prevErrors.filter(error => error.id !== errorId)
    );
  }, []);

  const clearErrors = useCallback(() => {
    setErrors([]);
  }, []);

  const clearErrorsByType = useCallback((type: BaseError['type']) => {
    setErrors(prevErrors => 
      prevErrors.filter(error => error.type !== type)
    );
  }, []);

  const getErrorsByType = useCallback((type: BaseError['type']) => {
    return errors.filter(error => error.type === type);
  }, [errors]);

  const getErrorsBySeverity = useCallback((severity: BaseError['severity']) => {
    return errors.filter(error => error.severity === severity);
  }, [errors]);

  const hasErrorsOfType = useCallback((type: BaseError['type']) => {
    return errors.some(error => error.type === type);
  }, [errors]);

  const hasErrorsOfSeverity = useCallback((severity: BaseError['severity']) => {
    return errors.some(error => error.severity === severity);
  }, [errors]);

  const contextValue: ErrorContextValue = {
    errors,
    addError,
    removeError,
    clearErrors,
    getErrorsByType,
    getErrorsBySeverity
  };

  return (
    <ErrorContext.Provider value={contextValue}>
      {children}
    </ErrorContext.Provider>
  );
};

export default ErrorProvider;