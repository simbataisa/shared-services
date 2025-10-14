/**
 * Comprehensive Form Validator Utility
 * 
 * This utility provides a flexible and reusable form validation system
 * that supports various validation rules and can be easily extended.
 */

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

/**
 * Built-in validation patterns
 */
export const ValidationPatterns = {
  email: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
  phone: /^\+?[\d\s\-\(\)]+$/,
  url: /^https?:\/\/.+/,
  alphanumeric: /^[a-zA-Z0-9]+$/,
  alphabetic: /^[a-zA-Z\s]+$/,
  numeric: /^\d+$/,
  strongPassword: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/,
} as const;

/**
 * Form Validator Class
 */
export class FormValidator {
  private schema: FieldValidation;

  constructor(schema: FieldValidation) {
    this.schema = schema;
  }

  /**
   * Validate a single field
   */
  validateField(fieldName: string, value: any): string | null {
    const rules = this.schema[fieldName];
    if (!rules) return null;

    for (const rule of rules) {
      const error = this.applyRule(rule, value, fieldName);
      if (error) return error;
    }

    return null;
  }

  /**
   * Validate all fields in the form data
   */
  validateForm(formData: Record<string, any>): ValidationResult {
    const errors: ValidationErrors = {};
    let isValid = true;

    // Validate existing fields
    for (const [fieldName, value] of Object.entries(formData)) {
      const error = this.validateField(fieldName, value);
      if (error) {
        errors[fieldName] = error;
        isValid = false;
      }
    }

    // Check for required fields that might be missing
    for (const fieldName of Object.keys(this.schema)) {
      if (!(fieldName in formData)) {
        const error = this.validateField(fieldName, undefined);
        if (error) {
          errors[fieldName] = error;
          isValid = false;
        }
      }
    }

    return { isValid, errors };
  }

  /**
   * Validate form data and return only errors for changed fields
   */
  validateChangedFields(formData: Record<string, any>, changedFields: string[]): ValidationResult {
    const errors: ValidationErrors = {};
    let isValid = true;

    for (const fieldName of changedFields) {
      const error = this.validateField(fieldName, formData[fieldName]);
      if (error) {
        errors[fieldName] = error;
        isValid = false;
      }
    }

    return { isValid, errors };
  }

  /**
   * Apply a single validation rule
   */
  private applyRule(rule: ValidationRule, value: any, fieldName: string): string | null {
    const stringValue = String(value || '').trim();

    switch (rule.type) {
      case 'required':
        if (!value || stringValue === '') {
          return rule.message;
        }
        break;

      case 'minLength':
        if (stringValue.length < rule.value) {
          return rule.message;
        }
        break;

      case 'maxLength':
        if (stringValue.length > rule.value) {
          return rule.message;
        }
        break;

      case 'pattern':
        if (stringValue && !rule.value.test(stringValue)) {
          return rule.message;
        }
        break;

      case 'email':
        if (stringValue && !ValidationPatterns.email.test(stringValue)) {
          return rule.message;
        }
        break;

      case 'custom':
        if (rule.validator && !rule.validator(value)) {
          return rule.message;
        }
        break;

      default:
        console.warn(`Unknown validation rule type: ${rule.type}`);
    }

    return null;
  }

  /**
   * Update validation schema
   */
  updateSchema(newSchema: FieldValidation): void {
    this.schema = { ...this.schema, ...newSchema };
  }

  /**
   * Add validation rules for a specific field
   */
  addFieldRules(fieldName: string, rules: ValidationRule[]): void {
    this.schema[fieldName] = [...(this.schema[fieldName] || []), ...rules];
  }

  /**
   * Remove validation rules for a specific field
   */
  removeFieldRules(fieldName: string): void {
    delete this.schema[fieldName];
  }
}

/**
 * Utility function to create a validator instance
 */
export function createValidator(schema: FieldValidation): FormValidator {
  return new FormValidator(schema);
}

/**
 * Common validation rule builders
 */
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

/**
 * Hook for using form validation in React components
 */
export function useFormValidation(schema: FieldValidation) {
  const validator = new FormValidator(schema);

  const validateField = (fieldName: string, value: any): string | null => {
    return validator.validateField(fieldName, value);
  };

  const validateForm = (formData: Record<string, any>): ValidationResult => {
    return validator.validateForm(formData);
  };

  const validateChangedFields = (formData: Record<string, any>, changedFields: string[]): ValidationResult => {
    return validator.validateChangedFields(formData, changedFields);
  };

  return {
    validateField,
    validateForm,
    validateChangedFields,
    validator,
  };
}