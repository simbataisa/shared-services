import React, { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Separator } from '@/components/ui/separator';
import { ErrorCard, ErrorDialog, ErrorBoundary } from '@/components/common';
import { useErrorHandler } from '@/hooks/useErrorHandler';
import type { BaseError } from '@/types/errors';

// Component that throws an error for testing ErrorBoundary
const ErrorThrowingComponent: React.FC<{ shouldThrow: boolean }> = ({ shouldThrow }) => {
  if (shouldThrow) {
    throw new Error('This is a test error thrown by ErrorThrowingComponent');
  }
  
  return (
    <div className="p-4 bg-green-50 border border-green-200 rounded-lg">
      <p className="text-green-800">Component is working normally!</p>
    </div>
  );
};

const ErrorDemo: React.FC = () => {
  const [dialogOpen, setDialogOpen] = useState(false);
  const [shouldThrowError, setShouldThrowError] = useState(false);
  const [apiUrl, setApiUrl] = useState('https://jsonplaceholder.typicode.com/posts/999');
  const [validationField, setValidationField] = useState('');
  
  const {
    handleError,
    handleApiError,
    handleValidationError,
    handleClientError,
    withErrorHandling,
    retry,
    errors,
    clearErrors
  } = useErrorHandler();

  // Sample errors for demonstration
  const sampleErrors: BaseError[] = [
    {
      id: 'demo-1',
      type: 'validation',
      severity: 'medium',
      message: 'Email address is required',
      timestamp: new Date(),
      details: 'The email field cannot be empty'
    },
    {
      id: 'demo-2',
      type: 'network',
      severity: 'high',
      message: 'Failed to connect to server',
      timestamp: new Date(),
      code: 'NETWORK_ERROR',
      details: 'Please check your internet connection'
    },
    {
      id: 'demo-3',
      type: 'authentication',
      severity: 'high',
      message: 'Session expired',
      timestamp: new Date(),
      code: 401,
      details: 'Please log in again to continue'
    }
  ];

  const [currentError, setCurrentError] = useState<BaseError>(sampleErrors[0]);

  // Demo functions
  const simulateApiError = async () => {
    try {
      const response = await fetch(apiUrl);
      if (!response.ok) {
        throw {
          response: {
            status: response.status,
            statusText: response.statusText,
            data: { message: 'Resource not found' }
          },
          config: {
            url: apiUrl,
            method: 'GET'
          },
          message: `HTTP ${response.status}: ${response.statusText}`
        };
      }
    } catch (error) {
      handleApiError(error);
    }
  };

  const simulateValidationError = () => {
    if (!validationField.trim()) {
      handleValidationError('testField', 'This field is required', {
        severity: 'medium',
        value: validationField
      });
    } else if (validationField.length < 3) {
      handleValidationError('testField', 'Must be at least 3 characters long', {
        severity: 'medium',
        value: validationField
      });
    } else {
      handleClientError('Validation passed!', { severity: 'low' });
    }
  };

  const simulateClientError = () => {
    handleClientError('Something went wrong in the client', {
      severity: 'high',
      code: 'CLIENT_ERROR',
      context: { component: 'ErrorDemo', action: 'simulateClientError' }
    });
  };

  const simulateGenericError = () => {
    handleError(new Error('This is a generic error for testing'));
  };

  const testWithErrorHandling = withErrorHandling(async () => {
    // Simulate an async operation that might fail
    await new Promise((_, reject) => {
      setTimeout(() => reject(new Error('Async operation failed')), 1000);
    });
  });

  const testRetry = () => {
    let attempts = 0;
    retry(async () => {
      attempts++;
      if (attempts < 3) {
        throw new Error(`Attempt ${attempts} failed`);
      }
      return 'Success!';
    }, 3, 500);
  };

  return (
    <div className="container mx-auto p-6 space-y-6">
      <Card>
        <CardHeader>
          <CardTitle>Error Handling Components Demo</CardTitle>
        </CardHeader>
        <CardContent className="space-y-6">
          
          {/* Error Cards Demo */}
          <div>
            <h3 className="text-lg font-semibold mb-4">Error Cards</h3>
            <div className="space-y-4">
              {sampleErrors.map((error) => (
                <ErrorCard
                  key={error.id}
                  error={error}
                  onDismiss={() => console.log('Dismissed:', error.id)}
                  dismissible
                  showTimestamp
                  showDetails
                />
              ))}
            </div>
          </div>

          <Separator />

          {/* Error Dialog Demo */}
          <div>
            <h3 className="text-lg font-semibold mb-4">Error Dialog</h3>
            <div className="flex gap-4 mb-4">
              {sampleErrors.map((error, index) => (
                <Button
                  key={error.id}
                  variant="outline"
                  onClick={() => {
                    setCurrentError(error);
                    setDialogOpen(true);
                  }}
                >
                  Show {error.type} Error
                </Button>
              ))}
            </div>
            
            <ErrorDialog
              open={dialogOpen}
              onOpenChange={setDialogOpen}
              error={currentError}
              showDetails
              showTimestamp
            />
          </div>

          <Separator />

          {/* Error Boundary Demo */}
          <div>
            <h3 className="text-lg font-semibold mb-4">Error Boundary</h3>
            <div className="space-y-4">
              <div className="flex items-center gap-4">
                <Button
                  variant={shouldThrowError ? "destructive" : "default"}
                  onClick={() => setShouldThrowError(!shouldThrowError)}
                >
                  {shouldThrowError ? 'Fix Component' : 'Break Component'}
                </Button>
              </div>
              
              <ErrorBoundary>
                <ErrorThrowingComponent shouldThrow={shouldThrowError} />
              </ErrorBoundary>
            </div>
          </div>

          <Separator />

          {/* Error Handler Hook Demo */}
          <div>
            <h3 className="text-lg font-semibold mb-4">Error Handler Hook</h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              
              {/* API Error Test */}
              <Card>
                <CardHeader>
                  <CardTitle className="text-base">API Error Test</CardTitle>
                </CardHeader>
                <CardContent className="space-y-3">
                  <div>
                    <Label htmlFor="api-url">API URL</Label>
                    <Input
                      id="api-url"
                      value={apiUrl}
                      onChange={(e) => setApiUrl(e.target.value)}
                      placeholder="Enter API URL"
                    />
                  </div>
                  <Button onClick={simulateApiError} className="w-full">
                    Test API Error
                  </Button>
                </CardContent>
              </Card>

              {/* Validation Error Test */}
              <Card>
                <CardHeader>
                  <CardTitle className="text-base">Validation Error Test</CardTitle>
                </CardHeader>
                <CardContent className="space-y-3">
                  <div>
                    <Label htmlFor="validation-field">Test Field</Label>
                    <Input
                      id="validation-field"
                      value={validationField}
                      onChange={(e) => setValidationField(e.target.value)}
                      placeholder="Enter at least 3 characters"
                    />
                  </div>
                  <Button onClick={simulateValidationError} className="w-full">
                    Test Validation
                  </Button>
                </CardContent>
              </Card>

              {/* Other Error Tests */}
              <Card>
                <CardHeader>
                  <CardTitle className="text-base">Other Error Tests</CardTitle>
                </CardHeader>
                <CardContent className="space-y-3">
                  <Button onClick={simulateClientError} variant="outline" className="w-full">
                    Client Error
                  </Button>
                  <Button onClick={simulateGenericError} variant="outline" className="w-full">
                    Generic Error
                  </Button>
                  <Button onClick={testWithErrorHandling} variant="outline" className="w-full">
                    With Error Handling
                  </Button>
                  <Button onClick={testRetry} variant="outline" className="w-full">
                    Test Retry Logic
                  </Button>
                </CardContent>
              </Card>

              {/* Error Management */}
              <Card>
                <CardHeader>
                  <CardTitle className="text-base">Error Management</CardTitle>
                </CardHeader>
                <CardContent className="space-y-3">
                  <div className="text-sm text-muted-foreground">
                    Current Errors: {errors.length}
                  </div>
                  <Button onClick={clearErrors} variant="destructive" className="w-full">
                    Clear All Errors
                  </Button>
                </CardContent>
              </Card>
            </div>
          </div>

          {/* Current Errors Display */}
          {errors.length > 0 && (
            <>
              <Separator />
              <div>
                <h3 className="text-lg font-semibold mb-4">Current Errors ({errors.length})</h3>
                <div className="space-y-2">
                  {errors.map((error) => (
                    <ErrorCard
                      key={error.id}
                      error={error}
                      dismissible
                      showTimestamp
                    />
                  ))}
                </div>
              </div>
            </>
          )}
        </CardContent>
      </Card>
    </div>
  );
};

export default ErrorDemo;