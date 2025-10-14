# Validation Utilities Guide

## Overview

This guide covers the comprehensive form validation system implemented in the shared services application. The validation utilities provide a flexible, reusable, and type-safe approach to form validation across the entire application.

## Architecture

The validation system consists of two main components:

1. **`formValidator.ts`** - Core validation engine with rules and patterns
2. **`validationSchemas.ts`** - Predefined validation schemas for different forms

## Core Components

### FormValidator (`formValidator.ts`)

The `FormValidator` class provides the core validation functionality with built-in rules and custom validation support.

#### Key Interfaces

```typescript
export interface ValidationRule {
  type: 'required' | 'minLength' | 'maxLength' | 'pattern' | 'email' | 'custom';
  value?: any;
  message: string;
  validator?: (value: any) => boolean;
}

export interface FieldValidation {
  [fieldName: string]: ValidationRule[];
}

export interface ValidationErrors {
  [fieldName: string]: string;
}

export interface ValidationResult {
  isValid: boolean;
  errors: ValidationErrors;
}
```

#### Built-in Validation Patterns

```typescript
export const ValidationPatterns = {
  email: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
  phone: /^\+?[\d\s\-\(\)]+$/,
  url: /^https?:\/\/.+/,
  alphanumeric: /^[a-zA-Z0-9]+$/,
  alphabetic: /^[a-zA-Z\s]+$/,
  numeric: /^\d+$/,
  strongPassword: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/,
} as const;
```

#### FormValidator Class Methods

```typescript
export class FormValidator {
  constructor(schema: FieldValidation)

  // Validate a single field
  validateField(fieldName: string, value: any): string | null

  // Validate entire form
  validateForm(formData: Record<string, any>): ValidationResult

  // Validate only changed fields
  validateChangedFields(formData: Record<string, any>, changedFields: string[]): ValidationResult

  // Update validation schema
  updateSchema(newSchema: FieldValidation): void

  // Add rules to specific field
  addFieldRules(fieldName: string, rules: ValidationRule[]): void

  // Remove field rules
  removeFieldRules(fieldName: string): void
}
```

#### Validation Rules Factory

```typescript
export const ValidationRules = {
  required: (message = 'This field is required'): ValidationRule => ({
    type: 'required',
    message,
  }),

  minLength: (length: number, message?: string): ValidationRule => ({
    type: 'minLength',
    value: length,
    message: message || `Must be at least ${length} characters`,
  }),

  maxLength: (length: number, message?: string): ValidationRule => ({
    type: 'maxLength',
    value: length,
    message: message || `Must be no more than ${length} characters`,
  }),

  pattern: (pattern: RegExp, message: string): ValidationRule => ({
    type: 'pattern',
    value: pattern,
    message,
  }),

  email: (message = 'Please enter a valid email address'): ValidationRule => ({
    type: 'email',
    message,
  }),

  custom: (validator: (value: any) => boolean, message: string): ValidationRule => ({
    type: 'custom',
    validator,
    message,
  }),
};
```

#### React Hook Integration

```typescript
export function useFormValidation(schema: FieldValidation) {
  const [errors, setErrors] = useState<ValidationErrors>({});
  const validator = useMemo(() => new FormValidator(schema), [schema]);

  const validateField = useCallback((fieldName: string, value: any) => {
    const error = validator.validateField(fieldName, value);
    setErrors(prev => ({
      ...prev,
      [fieldName]: error || '',
    }));
    return !error;
  }, [validator]);

  const validateForm = useCallback((formData: Record<string, any>) => {
    const result = validator.validateForm(formData);
    setErrors(result.errors);
    return result.isValid;
  }, [validator]);

  const clearErrors = useCallback(() => {
    setErrors({});
  }, []);

  const clearFieldError = useCallback((fieldName: string) => {
    setErrors(prev => ({
      ...prev,
      [fieldName]: '',
    }));
  }, []);

  return {
    errors,
    validateField,
    validateForm,
    clearErrors,
    clearFieldError,
  };
}
```

### Validation Schemas (`validationSchemas.ts`)

Predefined validation schemas for common forms throughout the application.

#### Available Schemas

1. **Role Validation Schema**
   - `name`: Required, 2-100 characters, alphanumeric with spaces/hyphens/underscores
   - `description`: Optional, max 500 characters
   - `permissions`: Optional array with validation for permission objects
   - `permissionIds`: Optional array of positive numbers

2. **User Validation Schema**
   - `firstName`: Required, 2-50 characters, alphabetic only
   - `lastName`: Required, 2-50 characters, alphabetic only
   - `email`: Required, valid email format, max 255 characters
   - `username`: Required, 3-30 characters, alphanumeric with underscores
   - `phone`: Optional, valid phone number format

3. **Password Validation Schema**
   - `password`: Required, min 8 characters, strong password pattern
   - `confirmPassword`: Required, must match password

4. **Login Validation Schema**
   - `email`: Required, valid email format
   - `password`: Required

5. **Permission Validation Schema**
   - `name`: Required, 3-100 characters, alphanumeric with colons/underscores/hyphens
   - `description`: Optional, max 255 characters
   - `resource`: Required, max 50 characters
   - `action`: Required, max 50 characters

6. **Organization Validation Schema**
   - `name`: Required, 2-100 characters
   - `description`: Optional, max 500 characters
   - `website`: Optional, valid URL format
   - `email`: Optional, valid email format
   - `phone`: Optional, valid phone format

7. **Contact Validation Schema**
   - `name`: Required, 2-100 characters
   - `email`: Required, valid email format
   - `subject`: Required, 5-200 characters
   - `message`: Required, 10-1000 characters

8. **Settings Validation Schema**
   - `companyName`: Required, max 100 characters
   - `timezone`: Required
   - `language`: Required
   - `currency`: Required

9. **Search Validation Schema**
   - `query`: Required, 2-100 characters

10. **Comment Validation Schema**
    - `content`: Required, 5-500 characters

11. **File Upload Validation Schema**
    - `file`: Required, max 10MB, specific file types (JPEG, PNG, GIF, PDF, TXT)

#### Schema Access

```typescript
// Direct access to individual schemas
import { roleValidationSchema, userValidationSchema } from './validationSchemas';

// Access via centralized object
import { validationSchemas } from './validationSchemas';
const roleSchema = validationSchemas.role;

// Helper function for dynamic access
import { getValidationSchema } from './validationSchemas';
const schema = getValidationSchema('user');
```

## Usage Examples

### Basic Form Validation

```typescript
import { useFormValidation } from '../utils/formValidator';
import { userValidationSchema } from '../utils/validationSchemas';

const UserForm: React.FC = () => {
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    username: '',
  });

  const { errors, validateField, validateForm, clearFieldError } = useFormValidation(userValidationSchema);

  const handleFieldChange = (field: string, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }));
    clearFieldError(field);
    validateField(field, value);
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (validateForm(formData)) {
      // Form is valid, proceed with submission
      console.log('Form submitted:', formData);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <div>
        <input
          type="text"
          placeholder="First Name"
          value={formData.firstName}
          onChange={(e) => handleFieldChange('firstName', e.target.value)}
        />
        {errors.firstName && <span className="error">{errors.firstName}</span>}
      </div>
      
      <div>
        <input
          type="text"
          placeholder="Last Name"
          value={formData.lastName}
          onChange={(e) => handleFieldChange('lastName', e.target.value)}
        />
        {errors.lastName && <span className="error">{errors.lastName}</span>}
      </div>
      
      <div>
        <input
          type="email"
          placeholder="Email"
          value={formData.email}
          onChange={(e) => handleFieldChange('email', e.target.value)}
        />
        {errors.email && <span className="error">{errors.email}</span>}
      </div>
      
      <div>
        <input
          type="text"
          placeholder="Username"
          value={formData.username}
          onChange={(e) => handleFieldChange('username', e.target.value)}
        />
        {errors.username && <span className="error">{errors.username}</span>}
      </div>
      
      <button type="submit">Submit</button>
    </form>
  );
};
```

### Custom Validation Rules

```typescript
import { ValidationRules, createValidator } from '../utils/formValidator';

// Create custom validation schema
const customSchema = {
  customField: [
    ValidationRules.required('Custom field is required'),
    ValidationRules.custom(
      (value) => value && value.includes('@company.com'),
      'Must be a company email address'
    ),
  ],
  conditionalField: [
    ValidationRules.custom(
      (value, formData) => {
        // Access to entire form data for conditional validation
        if (formData.requiresConditional) {
          return value && value.length > 0;
        }
        return true;
      },
      'This field is required when conditional is checked'
    ),
  ],
};

const validator = createValidator(customSchema);
```

### Dynamic Schema Updates

```typescript
import { FormValidator, ValidationRules } from '../utils/formValidator';
import { userValidationSchema } from '../utils/validationSchemas';

const DynamicForm: React.FC = () => {
  const [validator] = useState(() => new FormValidator(userValidationSchema));
  const [isAdminMode, setIsAdminMode] = useState(false);

  useEffect(() => {
    if (isAdminMode) {
      // Add additional validation rules for admin mode
      validator.addFieldRules('adminCode', [
        ValidationRules.required('Admin code is required'),
        ValidationRules.pattern(/^ADMIN_\d{4}$/, 'Admin code must be in format ADMIN_XXXX'),
      ]);
    } else {
      // Remove admin-specific rules
      validator.removeFieldRules('adminCode');
    }
  }, [isAdminMode, validator]);

  // Rest of component logic...
};
```

### File Upload Validation

```typescript
import { fileUploadValidationSchema } from '../utils/validationSchemas';
import { useFormValidation } from '../utils/formValidator';

const FileUploadForm: React.FC = () => {
  const [file, setFile] = useState<File | null>(null);
  const { errors, validateField } = useFormValidation(fileUploadValidationSchema);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const selectedFile = e.target.files?.[0] || null;
    setFile(selectedFile);
    validateField('file', selectedFile);
  };

  return (
    <div>
      <input
        type="file"
        onChange={handleFileChange}
        accept=".jpg,.jpeg,.png,.gif,.pdf,.txt"
      />
      {errors.file && <span className="error">{errors.file}</span>}
      
      {file && (
        <div>
          <p>Selected file: {file.name}</p>
          <p>Size: {(file.size / 1024 / 1024).toFixed(2)} MB</p>
        </div>
      )}
    </div>
  );
};
```

## Advanced Features

### Conditional Validation

```typescript
const conditionalSchema = {
  userType: [ValidationRules.required('User type is required')],
  companyName: [
    ValidationRules.custom(
      (value, formData) => {
        if (formData.userType === 'business') {
          return value && value.length > 0;
        }
        return true;
      },
      'Company name is required for business users'
    ),
  ],
};
```

### Async Validation

```typescript
const asyncValidationSchema = {
  username: [
    ValidationRules.required('Username is required'),
    ValidationRules.custom(
      async (value) => {
        if (!value) return true;
        
        try {
          const response = await fetch(`/api/check-username/${value}`);
          const data = await response.json();
          return data.available;
        } catch {
          return false;
        }
      },
      'Username is already taken'
    ),
  ],
};
```

### Cross-Field Validation

```typescript
const crossFieldSchema = {
  password: [
    ValidationRules.required('Password is required'),
    ValidationRules.minLength(8, 'Password must be at least 8 characters'),
  ],
  confirmPassword: [
    ValidationRules.required('Please confirm your password'),
    ValidationRules.custom(
      (value, formData) => value === formData.password,
      'Passwords do not match'
    ),
  ],
};
```

## Integration with UI Components

### With Shadcn/UI Components

```typescript
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Button } from '@/components/ui/button';
import { useFormValidation } from '../utils/formValidator';

const FormWithShadcn: React.FC = () => {
  const { errors, validateField } = useFormValidation(userValidationSchema);

  return (
    <div className="space-y-4">
      <div>
        <Label htmlFor="firstName">First Name</Label>
        <Input
          id="firstName"
          className={errors.firstName ? 'border-red-500' : ''}
          onChange={(e) => validateField('firstName', e.target.value)}
        />
        {errors.firstName && (
          <p className="text-sm text-red-500 mt-1">{errors.firstName}</p>
        )}
      </div>
    </div>
  );
};
```

### With React Hook Form

```typescript
import { useForm } from 'react-hook-form';
import { FormValidator } from '../utils/formValidator';
import { userValidationSchema } from '../utils/validationSchemas';

const ReactHookFormIntegration: React.FC = () => {
  const validator = new FormValidator(userValidationSchema);
  
  const {
    register,
    handleSubmit,
    formState: { errors },
    setError,
    clearErrors,
  } = useForm();

  const onSubmit = (data: any) => {
    const validationResult = validator.validateForm(data);
    
    if (!validationResult.isValid) {
      // Set errors from our validator
      Object.entries(validationResult.errors).forEach(([field, message]) => {
        setError(field, { message });
      });
      return;
    }

    // Form is valid, proceed
    console.log('Form submitted:', data);
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <input
        {...register('firstName')}
        placeholder="First Name"
      />
      {errors.firstName && <span>{errors.firstName.message}</span>}
      
      <button type="submit">Submit</button>
    </form>
  );
};
```

## Best Practices

### 1. Schema Organization
- Keep schemas in separate files for large applications
- Use consistent naming conventions
- Group related schemas together
- Document complex validation rules

### 2. Error Messages
- Provide clear, actionable error messages
- Use consistent language and tone
- Consider internationalization for multi-language apps
- Avoid technical jargon in user-facing messages

### 3. Performance Optimization
- Use `useMemo` for expensive validation schemas
- Implement debounced validation for real-time feedback
- Validate only changed fields when possible
- Cache validation results for repeated validations

### 4. Testing
- Write unit tests for custom validation rules
- Test edge cases and boundary conditions
- Mock async validations in tests
- Test form submission with various data combinations

### 5. Accessibility
- Associate error messages with form fields using `aria-describedby`
- Use appropriate ARIA attributes for validation states
- Ensure error messages are announced by screen readers
- Provide clear focus management for validation errors

## Common Patterns

### Real-time Validation with Debouncing

```typescript
import { useDebouncedCallback } from 'use-debounce';

const useRealtimeValidation = (schema: FieldValidation) => {
  const { validateField } = useFormValidation(schema);
  
  const debouncedValidate = useDebouncedCallback(
    (fieldName: string, value: any) => {
      validateField(fieldName, value);
    },
    300
  );

  return { debouncedValidate };
};
```

### Validation State Management

```typescript
const useValidationState = () => {
  const [validationState, setValidationState] = useState<{
    [key: string]: {
      isValid: boolean;
      isDirty: boolean;
      isTouched: boolean;
      error?: string;
    };
  }>({});

  const updateFieldState = (
    fieldName: string,
    updates: Partial<typeof validationState[string]>
  ) => {
    setValidationState(prev => ({
      ...prev,
      [fieldName]: { ...prev[fieldName], ...updates },
    }));
  };

  return { validationState, updateFieldState };
};
```

## Troubleshooting

### Common Issues

1. **Validation not triggering**
   - Ensure the field name matches the schema key exactly
   - Check that the validation hook is properly initialized
   - Verify that the form data is being passed correctly

2. **Custom validation not working**
   - Ensure the custom validator function returns a boolean
   - Check for async validation issues
   - Verify that the validator has access to required data

3. **Performance issues**
   - Implement debouncing for real-time validation
   - Use `useCallback` for validation functions
   - Consider memoizing expensive validation logic

4. **Type errors**
   - Ensure proper TypeScript types for form data
   - Use generic types for reusable validation components
   - Check that validation schemas match form data structure

## Conclusion

The validation utilities provide a comprehensive, flexible, and type-safe approach to form validation. By leveraging the predefined schemas and extending them with custom rules, you can handle complex validation scenarios while maintaining code reusability and consistency across your application.

Key benefits:
- **Type Safety**: Full TypeScript support with proper type inference
- **Reusability**: Predefined schemas for common use cases
- **Flexibility**: Easy to extend with custom validation rules
- **Performance**: Optimized for real-time validation scenarios
- **Integration**: Works seamlessly with popular form libraries
- **Accessibility**: Built with accessibility best practices in mind

For additional examples and advanced use cases, refer to the implementation files and the component reusability guide.