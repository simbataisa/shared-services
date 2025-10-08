# Error Handling System Documentation

## Overview

This document provides comprehensive documentation for the error handling system implemented in the shared-services frontend application. The system includes error boundaries, context management, common error components, and a robust error handling mechanism.

## Architecture

The error handling system is built around three main components:

1. **Error Context** - Centralized error state management
2. **Error Boundary** - React error boundary for catching JavaScript errors
3. **Error Components** - Reusable UI components for displaying errors

## Error Types and Interfaces

### Base Error Types

```typescript
export type ErrorSeverity = 'low' | 'medium' | 'high' | 'critical';
export type ErrorType = 'validation' | 'network' | 'authentication' | 'authorization' | 'server' | 'client' | 'unknown';
export type ErrorAction = 'retry' | 'refresh' | 'login' | 'contact_support' | 'dismiss' | 'navigate';
```

### Core Error Interfaces

#### BaseError
The foundation interface for all errors in the system:

```typescript
interface BaseError {
  id: string;
  message: string;
  type: ErrorType;
  severity: ErrorSeverity;
  timestamp: Date;
  code?: string | number;
  details?: string;
  stack?: string;
}
```

#### Specialized Error Types

- **ApiError**: For network and server-related errors
- **ValidationError**: For form validation errors
- **ClientError**: For client-side JavaScript errors
- **ErrorWithActions**: Errors with suggested user actions

## Error Context

### Location
`src/contexts/ErrorContext.tsx`

### Purpose
Provides centralized error state management across the application using React Context API.

### Key Features

- **Error Collection**: Maintains a list of active errors
- **Duplicate Prevention**: Filters out duplicate errors based on message and type
- **Error Limits**: Configurable maximum number of errors (default: 10)
- **Type-based Filtering**: Methods to retrieve errors by type or severity

### API Methods

```typescript
interface ErrorContextValue {
  errors: BaseError[];
  addError: (error: Omit<BaseError, 'id' | 'timestamp'>) => string;
  removeError: (id: string) => void;
  clearErrors: () => void;
  getErrorsByType: (type: ErrorType) => BaseError[];
  getErrorsBySeverity: (severity: ErrorSeverity) => BaseError[];
}
```

### Usage Example

```typescript
import { useErrorContext } from '@/hooks/useErrorContext';

function MyComponent() {
  const { addError, removeError, errors } = useErrorContext();
  
  const handleError = () => {
    const errorId = addError({
      message: 'Something went wrong',
      type: 'client',
      severity: 'medium'
    });
  };
  
  return (
    <div>
      {errors.map(error => (
        <ErrorCard key={error.id} error={error} onDismiss={() => removeError(error.id)} />
      ))}
    </div>
  );
}
```

## Error Boundary

### Location
`src/components/common/ErrorBoundary.tsx`

### Purpose
Catches JavaScript errors anywhere in the component tree and displays a fallback UI instead of crashing the entire application.

### Key Features

- **Error Catching**: Catches unhandled JavaScript errors in React components
- **Fallback UI**: Displays user-friendly error messages with recovery options
- **Error Reporting**: Logs errors and provides error reporting capabilities
- **Recovery Mechanisms**: Allows users to retry, refresh, or navigate away
- **Error Details**: Collapsible error details for debugging

### Props Interface

```typescript
interface ErrorBoundaryProps {
  children: React.ReactNode;
  fallback?: React.ComponentType<ErrorFallbackProps>;
  onError?: (error: Error, errorInfo: React.ErrorInfo) => void;
  resetOnPropsChange?: boolean;
  resetKeys?: Array<string | number>;
}
```

### Default Fallback Component Features

- **Error Information Display**: Shows error message and ID
- **Action Buttons**: Retry, Go Home, Report Error
- **Collapsible Details**: Stack trace and error information
- **Copy to Clipboard**: Easy copying of error details
- **Responsive Design**: Works on all screen sizes

### Usage Examples

#### Basic Usage
```typescript
<ErrorBoundary>
  <MyComponent />
</ErrorBoundary>
```

#### With Custom Fallback
```typescript
<ErrorBoundary 
  fallback={CustomErrorFallback}
  onError={(error, errorInfo) => {
    console.error('Error caught by boundary:', error);
    // Send to error reporting service
  }}
>
  <MyComponent />
</ErrorBoundary>
```

#### With HOC Pattern
```typescript
const MyComponentWithErrorBoundary = withErrorBoundary(MyComponent, {
  onError: (error, errorInfo) => {
    // Custom error handling
  }
});
```

## Common Error Components

### ErrorCard Component

#### Location
`src/components/common/ErrorCard.tsx`

#### Purpose
Displays individual errors in a card format with optional actions and details.

#### Features
- **Multiple Variants**: Default, destructive, warning styles
- **Dismissible**: Optional close button
- **Action Buttons**: Configurable action buttons
- **Collapsible Details**: Expandable error details
- **Timestamp Display**: Shows when the error occurred
- **Severity Indicators**: Visual indicators for error severity

#### Props
```typescript
interface ErrorCardProps {
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
```

### ErrorDialog Component

#### Location
`src/components/common/ErrorDialog.tsx`

#### Purpose
Displays errors in a modal dialog format for critical errors that require user attention.

#### Features
- **Modal Display**: Blocks user interaction until addressed
- **Action Buttons**: Primary and secondary actions
- **Error Details**: Expandable technical details
- **Responsive Design**: Adapts to different screen sizes

### ErrorToast Component

#### Location
`src/components/common/ErrorToast.tsx`

#### Purpose
Shows temporary error notifications that auto-dismiss after a specified duration.

#### Features
- **Auto-dismiss**: Configurable timeout duration
- **Positioning**: Multiple position options
- **Severity Styling**: Different styles based on error severity
- **Action Support**: Optional action buttons

## Error Handling Patterns

### 1. API Error Handling

```typescript
// In API service
try {
  const response = await httpClient.get('/api/data');
  return response.data;
} catch (error) {
  const apiError: ApiError = {
    message: error.message || 'Network request failed',
    type: 'network',
    severity: 'high',
    status: error.response?.status,
    endpoint: '/api/data',
    method: 'GET'
  };
  
  addError(apiError);
  throw error;
}
```

### 2. Form Validation Errors

```typescript
// In form component
const validateForm = (data: FormData) => {
  const errors: ValidationError[] = [];
  
  if (!data.email) {
    errors.push({
      message: 'Email is required',
      type: 'validation',
      severity: 'medium',
      field: 'email',
      constraints: ['required']
    });
  }
  
  errors.forEach(error => addError(error));
  return errors.length === 0;
};
```

### 3. Component Error Handling

```typescript
// In React component
const MyComponent = () => {
  const { addError } = useErrorContext();
  
  const handleAsyncOperation = async () => {
    try {
      await someAsyncOperation();
    } catch (error) {
      addError({
        message: 'Operation failed',
        type: 'client',
        severity: 'medium',
        details: error.message
      });
    }
  };
  
  return <button onClick={handleAsyncOperation}>Execute</button>;
};
```

## Error Severity Guidelines

### Critical (critical)
- Application crashes
- Data loss scenarios
- Security breaches
- Complete feature failures

**Actions**: Immediate attention, error reporting, fallback UI

### High (high)
- API failures
- Authentication errors
- Important feature malfunctions
- Data corruption

**Actions**: User notification, retry mechanisms, error logging

### Medium (medium)
- Validation errors
- Non-critical feature issues
- Recoverable errors
- Performance issues

**Actions**: User feedback, graceful degradation

### Low (low)
- Minor UI glitches
- Non-essential feature issues
- Informational errors
- Warning messages

**Actions**: Subtle notifications, optional user feedback

## Error Recovery Strategies

### 1. Automatic Recovery
- **Retry Logic**: Automatic retries for transient failures
- **Fallback Data**: Use cached or default data when primary source fails
- **Progressive Enhancement**: Graceful degradation of features

### 2. User-Initiated Recovery
- **Retry Buttons**: Allow users to retry failed operations
- **Refresh Options**: Page or component refresh capabilities
- **Navigation**: Redirect to safe pages or previous states

### 3. Preventive Measures
- **Input Validation**: Client-side validation to prevent errors
- **Loading States**: Clear feedback during async operations
- **Error Boundaries**: Contain errors to specific components

## Best Practices

### 1. Error Messages
- **User-Friendly**: Write messages for end users, not developers
- **Actionable**: Provide clear next steps when possible
- **Contextual**: Include relevant context about what went wrong
- **Consistent**: Use consistent language and tone

### 2. Error Logging
- **Structured Logging**: Use consistent error formats
- **Context Information**: Include user ID, session ID, and relevant state
- **Privacy**: Avoid logging sensitive information
- **Error Tracking**: Integrate with error monitoring services

### 3. Performance Considerations
- **Error Limits**: Prevent error spam with maximum error counts
- **Debouncing**: Avoid duplicate error reports
- **Memory Management**: Clean up old errors automatically
- **Lazy Loading**: Load error components only when needed

### 4. Testing
- **Error Scenarios**: Test all error paths and edge cases
- **Boundary Testing**: Verify error boundaries catch all errors
- **Recovery Testing**: Test all recovery mechanisms
- **User Experience**: Test error UX with real users

## Integration with External Services

### Error Reporting
```typescript
// Example integration with error reporting service
const reportError = (error: BaseError) => {
  if (process.env.NODE_ENV === 'production') {
    errorReportingService.captureException(error, {
      tags: {
        severity: error.severity,
        type: error.type
      },
      extra: {
        timestamp: error.timestamp,
        errorId: error.id
      }
    });
  }
};
```

### Analytics
```typescript
// Track error metrics
const trackError = (error: BaseError) => {
  analytics.track('Error Occurred', {
    errorType: error.type,
    errorSeverity: error.severity,
    errorMessage: error.message,
    timestamp: error.timestamp
  });
};
```

## Configuration

### Environment Variables
```env
# Error handling configuration
REACT_APP_ERROR_REPORTING_ENABLED=true
REACT_APP_ERROR_REPORTING_DSN=your-dsn-here
REACT_APP_MAX_ERRORS_PER_SESSION=50
REACT_APP_ERROR_TOAST_DURATION=5000
```

### Error Handler Options
```typescript
interface ErrorHandlerOptions {
  showToast?: boolean;
  showDialog?: boolean;
  logError?: boolean;
  reportError?: boolean;
  fallbackMessage?: string;
  retryable?: boolean;
  maxRetries?: number;
}
```

## Troubleshooting

### Common Issues

1. **Error Boundary Not Catching Errors**
   - Ensure errors occur during render, not in event handlers
   - Use try-catch blocks for async operations
   - Check that error boundary is properly positioned in component tree

2. **Memory Leaks from Error Context**
   - Implement error cleanup mechanisms
   - Set appropriate error limits
   - Clear errors on component unmount

3. **Performance Issues**
   - Limit error collection size
   - Debounce error reporting
   - Use React.memo for error components

### Debugging Tips

1. **Enable Error Logging**: Set up comprehensive error logging
2. **Use React DevTools**: Inspect error boundary state
3. **Check Network Tab**: Verify API error responses
4. **Test Error Scenarios**: Manually trigger error conditions

## Future Enhancements

### Planned Features
- **Error Analytics Dashboard**: Visual error tracking and trends
- **Smart Error Grouping**: Automatic error categorization
- **User Feedback Integration**: Allow users to provide error context
- **Offline Error Handling**: Queue errors when offline
- **Error Recovery Suggestions**: AI-powered recovery recommendations

### Performance Optimizations
- **Error Batching**: Batch multiple errors for reporting
- **Lazy Error Components**: Load error UI components on demand
- **Error Caching**: Cache error states for better UX
- **Background Error Processing**: Handle errors without blocking UI

This documentation provides a comprehensive guide to the error handling system. For specific implementation details, refer to the individual component files and their inline documentation.