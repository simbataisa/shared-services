import React from 'react';
import { ErrorProvider } from '@/contexts/ErrorContext';
import ErrorDemo from '@/components/demo/ErrorDemo';

const ErrorDemoPage: React.FC = () => {
  return (
    <ErrorProvider maxErrors={10}>
      <div className="min-h-screen bg-gray-50">
        <div className="py-8">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="text-center mb-8">
              <h1 className="text-3xl font-bold text-gray-900">
                Error Handling System Demo
              </h1>
              <p className="mt-2 text-lg text-gray-600">
                Comprehensive error handling components and utilities
              </p>
            </div>
            <ErrorDemo />
          </div>
        </div>
      </div>
    </ErrorProvider>
  );
};

export default ErrorDemoPage;