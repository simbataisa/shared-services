/**
 * Validation Schemas for Forms
 * 
 * This file contains predefined validation schemas for various forms
 * used throughout the application.
 */

import { type FieldValidation, ValidationRules, ValidationPatterns } from './formValidator';

/**
 * Role Form Validation Schema
 */
export const roleValidationSchema: FieldValidation = {
  name: [
    ValidationRules.required('Role name is required'),
    ValidationRules.minLength(2, 'Role name must be at least 2 characters'),
    ValidationRules.maxLength(100, 'Role name must be less than 100 characters'),
    ValidationRules.pattern(
      /^[a-zA-Z0-9\s\-_]+$/,
      'Role name can only contain letters, numbers, spaces, hyphens, and underscores'
    ),
  ],
  description: [
    ValidationRules.maxLength(500, 'Description must be less than 500 characters'),
  ],
  permissions: [
    ValidationRules.custom(
      (permissions) => {
        // Optional field - if provided, should be an array
        if (permissions === undefined || permissions === null) {
          return true; // Optional field, no validation needed
        }
        return Array.isArray(permissions);
      },
      'Permissions must be an array'
    ),
    ValidationRules.custom(
      (permissions) => {
        // If permissions array is provided, validate each permission has required fields
        if (!Array.isArray(permissions) || permissions.length === 0) {
          return true; // Skip validation if not an array or empty
        }
        return permissions.every(
          (permission: any) => 
            permission && 
            typeof permission.id === 'number' && 
            typeof permission.name === 'string' &&
            typeof permission.resource === 'string' &&
            typeof permission.action === 'string'
        );
      },
      'Each permission must have valid id, name, resource, and action fields'
    ),
  ],
  permissionIds: [
    ValidationRules.custom(
      (permissionIds) => {
        // Optional field for permission IDs array
        if (permissionIds === undefined || permissionIds === null) {
          return true; // Optional field, no validation needed
        }
        return Array.isArray(permissionIds);
      },
      'Permission IDs must be an array'
    ),
    ValidationRules.custom(
      (permissionIds) => {
        // If permissionIds array is provided, validate each ID is a number
        if (!Array.isArray(permissionIds) || permissionIds.length === 0) {
          return true; // Skip validation if not an array or empty
        }
        return permissionIds.every((id: any) => typeof id === 'number' && id > 0);
      },
      'Each permission ID must be a positive number'
    ),
  ],
};

/**
 * User Form Validation Schema
 */
export const userValidationSchema: FieldValidation = {
  firstName: [
    ValidationRules.required('First name is required'),
    ValidationRules.minLength(2, 'First name must be at least 2 characters'),
    ValidationRules.maxLength(50, 'First name must be less than 50 characters'),
    ValidationRules.pattern(ValidationPatterns.alphabetic, 'First name can only contain letters and spaces'),
  ],
  lastName: [
    ValidationRules.required('Last name is required'),
    ValidationRules.minLength(2, 'Last name must be at least 2 characters'),
    ValidationRules.maxLength(50, 'Last name must be less than 50 characters'),
    ValidationRules.pattern(ValidationPatterns.alphabetic, 'Last name can only contain letters and spaces'),
  ],
  email: [
    ValidationRules.required('Email is required'),
    ValidationRules.email('Please enter a valid email address'),
    ValidationRules.maxLength(255, 'Email must be less than 255 characters'),
  ],
  username: [
    ValidationRules.required('Username is required'),
    ValidationRules.minLength(3, 'Username must be at least 3 characters'),
    ValidationRules.maxLength(30, 'Username must be less than 30 characters'),
    ValidationRules.pattern(
      /^[a-zA-Z0-9_]+$/,
      'Username can only contain letters, numbers, and underscores'
    ),
  ],
  phone: [
    ValidationRules.pattern(ValidationPatterns.phone, 'Please enter a valid phone number'),
  ],
};

/**
 * Password Validation Schema
 */
export const passwordValidationSchema: FieldValidation = {
  password: [
    ValidationRules.required('Password is required'),
    ValidationRules.minLength(8, 'Password must be at least 8 characters'),
    ValidationRules.pattern(
      ValidationPatterns.strongPassword,
      'Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character'
    ),
  ],
  confirmPassword: [
    ValidationRules.required('Please confirm your password'),
    // Note: Password matching validation should be handled at the form level
    // using the FormValidator's validateForm method with custom logic
  ],
};

/**
 * Login Form Validation Schema
 */
export const loginValidationSchema: FieldValidation = {
  email: [
    ValidationRules.required('Email is required'),
    ValidationRules.email('Please enter a valid email address'),
  ],
  password: [
    ValidationRules.required('Password is required'),
  ],
};

/**
 * Permission Form Validation Schema
 */
export const permissionValidationSchema: FieldValidation = {
  name: [
    ValidationRules.required('Permission name is required'),
    ValidationRules.minLength(3, 'Permission name must be at least 3 characters'),
    ValidationRules.maxLength(100, 'Permission name must be less than 100 characters'),
    ValidationRules.pattern(
      /^[a-zA-Z0-9:_\-]+$/,
      'Permission name can only contain letters, numbers, colons, underscores, and hyphens'
    ),
  ],
  description: [
    ValidationRules.maxLength(255, 'Description must be less than 255 characters'),
  ],
  resource: [
    ValidationRules.required('Resource is required'),
    ValidationRules.maxLength(50, 'Resource must be less than 50 characters'),
  ],
  action: [
    ValidationRules.required('Action is required'),
    ValidationRules.maxLength(50, 'Action must be less than 50 characters'),
  ],
};

/**
 * Organization Form Validation Schema
 */
export const organizationValidationSchema: FieldValidation = {
  name: [
    ValidationRules.required('Organization name is required'),
    ValidationRules.minLength(2, 'Organization name must be at least 2 characters'),
    ValidationRules.maxLength(100, 'Organization name must be less than 100 characters'),
  ],
  description: [
    ValidationRules.maxLength(500, 'Description must be less than 500 characters'),
  ],
  website: [
    ValidationRules.pattern(ValidationPatterns.url, 'Please enter a valid website URL'),
  ],
  email: [
    ValidationRules.email('Please enter a valid email address'),
  ],
  phone: [
    ValidationRules.pattern(ValidationPatterns.phone, 'Please enter a valid phone number'),
  ],
};

/**
 * Contact Form Validation Schema
 */
export const contactValidationSchema: FieldValidation = {
  name: [
    ValidationRules.required('Name is required'),
    ValidationRules.minLength(2, 'Name must be at least 2 characters'),
    ValidationRules.maxLength(100, 'Name must be less than 100 characters'),
  ],
  email: [
    ValidationRules.required('Email is required'),
    ValidationRules.email('Please enter a valid email address'),
  ],
  subject: [
    ValidationRules.required('Subject is required'),
    ValidationRules.minLength(5, 'Subject must be at least 5 characters'),
    ValidationRules.maxLength(200, 'Subject must be less than 200 characters'),
  ],
  message: [
    ValidationRules.required('Message is required'),
    ValidationRules.minLength(10, 'Message must be at least 10 characters'),
    ValidationRules.maxLength(1000, 'Message must be less than 1000 characters'),
  ],
};

/**
 * Settings Form Validation Schema
 */
export const settingsValidationSchema: FieldValidation = {
  companyName: [
    ValidationRules.required('Company name is required'),
    ValidationRules.maxLength(100, 'Company name must be less than 100 characters'),
  ],
  timezone: [
    ValidationRules.required('Timezone is required'),
  ],
  language: [
    ValidationRules.required('Language is required'),
  ],
  currency: [
    ValidationRules.required('Currency is required'),
  ],
};

/**
 * Search Form Validation Schema
 */
export const searchValidationSchema: FieldValidation = {
  query: [
    ValidationRules.required('Search query is required'),
    ValidationRules.minLength(2, 'Search query must be at least 2 characters'),
    ValidationRules.maxLength(100, 'Search query must be less than 100 characters'),
  ],
};

/**
 * Comment Form Validation Schema
 */
export const commentValidationSchema: FieldValidation = {
  content: [
    ValidationRules.required('Comment is required'),
    ValidationRules.minLength(5, 'Comment must be at least 5 characters'),
    ValidationRules.maxLength(500, 'Comment must be less than 500 characters'),
  ],
};

/**
 * File Upload Validation Schema
 */
export const fileUploadValidationSchema: FieldValidation = {
  file: [
    ValidationRules.required('Please select a file'),
    ValidationRules.custom(
      (file) => file && file.size <= 10 * 1024 * 1024, // 10MB
      'File size must be less than 10MB'
    ),
    ValidationRules.custom(
      (file) => {
        if (!file) return true;
        const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'application/pdf', 'text/plain'];
        return allowedTypes.includes(file.type);
      },
      'File type not supported. Please upload JPEG, PNG, GIF, PDF, or TXT files'
    ),
  ],
};

/**
 * Export all schemas for easy access
 */
export const validationSchemas = {
  role: roleValidationSchema,
  user: userValidationSchema,
  password: passwordValidationSchema,
  login: loginValidationSchema,
  permission: permissionValidationSchema,
  organization: organizationValidationSchema,
  contact: contactValidationSchema,
  settings: settingsValidationSchema,
  search: searchValidationSchema,
  comment: commentValidationSchema,
  fileUpload: fileUploadValidationSchema,
} as const;

/**
 * Helper function to get validation schema by name
 */
export function getValidationSchema(schemaName: keyof typeof validationSchemas): FieldValidation {
  return validationSchemas[schemaName];
}