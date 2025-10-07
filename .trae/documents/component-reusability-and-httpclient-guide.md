# Component Reusability and HttpClient Usage Guide

## Table of Contents
1. [Overview](#overview)
2. [Component Reusability Principles](#component-reusability-principles)
3. [HttpClient Architecture and Usage](#httpclient-architecture-and-usage)
4. [Best Practices](#best-practices)
5. [Implementation Examples](#implementation-examples)
6. [Maintenance Guidelines](#maintenance-guidelines)
7. [Common Patterns](#common-patterns)

## Overview

This document outlines the best practices for implementing reusable components and utilizing the httpClient for better maintainability in the Shared Services frontend application. The goal is to reduce code duplication, improve consistency, and enhance developer productivity.

## Component Reusability Principles

### 1. Single Responsibility Principle
Each component should have a single, well-defined purpose. Components should be focused on one specific functionality or UI pattern.

**Example:**
- `UserInfoFormCard` - Handles only user information input
- `UserPasswordFormCard` - Handles only password creation/validation
- `UserRoleGroupFormCard` - Handles only role and group selection

### 2. Composition Over Inheritance
Build complex components by composing smaller, reusable components rather than creating monolithic components.

**Benefits:**
- Easier testing and debugging
- Better code organization
- Improved maintainability
- Enhanced reusability across different contexts

### 3. Props-Based Configuration
Design components to be configurable through props, making them adaptable to different use cases.

**Key Props Patterns:**
```typescript
interface ComponentProps {
  // Data props
  data: T[];
  selectedItems: string[];
  
  // Event handlers
  onItemSelect: (item: T) => void;
  onItemRemove: (id: string) => void;
  
  // UI configuration
  loading?: boolean;
  disabled?: boolean;
  className?: string;
  
  // Feature flags
  showSearch?: boolean;
  allowMultiSelect?: boolean;
}
```

### 4. Consistent Interface Design
Maintain consistent prop naming and patterns across similar components.

**Standard Patterns:**
- `loading` for loading states
- `disabled` for disabled states
- `className` for custom styling
- `onXxxChange` for event handlers
- `selectedXxx` for selected state

## HttpClient Architecture and Usage

### 1. Centralized HTTP Client
The application uses a centralized `HttpClient` class that provides a consistent interface for all API interactions.

**Location:** `/src/lib/httpClient.ts`

**Key Features:**
- Type-safe API methods
- Consistent error handling
- Automatic token management
- Response data transformation
- Generic methods for flexibility

### 2. HttpClient Structure

```typescript
class HttpClient {
  // Entity-specific methods
  async getUsers(): Promise<User[]>
  async createUser(userData: CreateUserRequest): Promise<User>
  async updateUser(id: number, userData: UpdateUserRequest): Promise<User>
  
  // Generic methods for flexibility
  async get<T>(url: string): Promise<T>
  async post<T>(url: string, data?: any): Promise<T>
  async put<T>(url: string, data?: any): Promise<T>
  async delete<T>(url: string, config?: any): Promise<T>
}
```

### 3. Type Safety
All API methods are fully typed with TypeScript interfaces, ensuring compile-time safety and better developer experience.

**Type Definitions:**
```typescript
export interface User {
  id: number;
  username: string;
  email: string;
  // ... other properties
}

export interface CreateUserRequest {
  username: string;
  email: string;
  password: string;
  roleIds: number[];
  userGroupIds: number[];
}
```

## Best Practices

### Component Design

#### 1. Create Focused, Single-Purpose Components
```typescript
// ✅ Good - Focused component
const UserSearchInput: React.FC<UserSearchInputProps> = ({
  searchTerm,
  onSearchChange,
  placeholder = "Search users...",
  disabled = false
}) => {
  return (
    <div className="relative">
      <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
      <Input
        placeholder={placeholder}
        value={searchTerm}
        onChange={(e) => onSearchChange(e.target.value)}
        className="pl-10"
        disabled={disabled}
      />
    </div>
  );
};

// ❌ Bad - Monolithic component handling multiple concerns
const UserManagementPanel = () => {
  // Handles search, filtering, user list, user creation, role assignment, etc.
  // Too many responsibilities in one component
};
```

#### 2. Use Composition for Complex UI
```typescript
// ✅ Good - Composed from smaller components
const UserCreateForm: React.FC = () => {
  return (
    <div className="space-y-6">
      <UserInfoFormCard {...userInfoProps} />
      <UserPasswordFormCard {...passwordProps} />
      <UserRoleGroupFormCard {...roleGroupProps} />
    </div>
  );
};
```

#### 3. Implement Consistent State Management
```typescript
// ✅ Good - Consistent state patterns
const [searchTerm, setSearchTerm] = useState("");
const [selectedItems, setSelectedItems] = useState<string[]>([]);
const [loading, setLoading] = useState(false);

// Use useMemo for expensive computations
const filteredItems = useMemo(() => {
  return items.filter(item => 
    item.name.toLowerCase().includes(searchTerm.toLowerCase())
  );
}, [items, searchTerm]);
```

### HttpClient Usage

#### 1. Use Specific Methods When Available
```typescript
// ✅ Good - Use specific methods
const users = await httpClient.getUsers();
const user = await httpClient.createUser(userData);

// ❌ Avoid - Generic methods when specific ones exist
const users = await httpClient.get<User[]>('/v1/users');
```

#### 2. Handle Errors Consistently
```typescript
// ✅ Good - Consistent error handling
const fetchUsers = async () => {
  try {
    setLoading(true);
    const users = await httpClient.getUsers();
    setUsers(users);
  } catch (error) {
    console.error("Error fetching users:", error);
    setError("Failed to load users");
  } finally {
    setLoading(false);
  }
};
```

#### 3. Transform Data When Necessary
```typescript
// ✅ Good - Transform API data to match UI requirements
const transformedUser: User = {
  id: userData.id,
  username: userData.username,
  email: userData.email,
  userStatus: userData.userStatus as "ACTIVE" | "INACTIVE",
  roles: userData.roles?.map(role => ({
    id: role.id,
    name: role.name,
    description: role.description || ""
  })) || [],
};
```

## Implementation Examples

### 1. Reusable Search Component
```typescript
interface SearchAndFilterProps<T> {
  items: T[];
  onFilteredItemsChange: (items: T[]) => void;
  searchFields: (keyof T)[];
  placeholder?: string;
  className?: string;
}

const SearchAndFilter = <T extends Record<string, any>>({
  items,
  onFilteredItemsChange,
  searchFields,
  placeholder = "Search...",
  className = ""
}: SearchAndFilterProps<T>) => {
  const [searchTerm, setSearchTerm] = useState("");

  const filteredItems = useMemo(() => {
    if (!searchTerm.trim()) return items;
    
    return items.filter(item =>
      searchFields.some(field =>
        String(item[field]).toLowerCase().includes(searchTerm.toLowerCase())
      )
    );
  }, [items, searchTerm, searchFields]);

  useEffect(() => {
    onFilteredItemsChange(filteredItems);
  }, [filteredItems, onFilteredItemsChange]);

  return (
    <div className={`relative ${className}`}>
      <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
      <Input
        placeholder={placeholder}
        value={searchTerm}
        onChange={(e) => setSearchTerm(e.target.value)}
        className="pl-10"
      />
    </div>
  );
};
```

### 2. Reusable Form Card Pattern
```typescript
interface FormCardProps {
  title: string;
  description?: string;
  icon?: React.ReactNode;
  children: React.ReactNode;
  className?: string;
}

const FormCard: React.FC<FormCardProps> = ({
  title,
  description,
  icon,
  children,
  className = ""
}) => {
  return (
    <Card className={className}>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          {icon}
          {title}
        </CardTitle>
        {description && (
          <CardDescription>{description}</CardDescription>
        )}
      </CardHeader>
      <CardContent>
        {children}
      </CardContent>
    </Card>
  );
};
```

### 3. HttpClient Service Layer
```typescript
// Create service layer for complex business logic
class UserService {
  private httpClient = httpClient;

  async createUserWithValidation(userData: CreateUserRequest): Promise<User> {
    // Pre-validation
    await this.validateUserData(userData);
    
    // Create user
    const user = await this.httpClient.createUser(userData);
    
    // Post-processing
    await this.sendWelcomeEmail(user.email);
    
    return user;
  }

  private async validateUserData(userData: CreateUserRequest): Promise<void> {
    // Check if username exists
    try {
      await this.httpClient.getUserByUsername(userData.username);
      throw new Error("Username already exists");
    } catch (error) {
      // Username doesn't exist, which is good
    }

    // Check if email exists
    try {
      await this.httpClient.getUserByEmail(userData.email);
      throw new Error("Email already exists");
    } catch (error) {
      // Email doesn't exist, which is good
    }
  }
}
```

## Maintenance Guidelines

### 1. Regular Refactoring
- **Identify Duplication:** Regularly review code for repeated patterns that can be extracted into reusable components
- **Extract Common Logic:** Move shared business logic into custom hooks or service classes
- **Update Dependencies:** Keep component dependencies up to date and remove unused imports

### 2. Component Documentation
```typescript
/**
 * UserRoleGroupFormCard - A reusable component for selecting roles and user groups
 * 
 * @param availableRoles - List of available roles to choose from
 * @param availableGroups - List of available user groups to choose from
 * @param selectedRoleIds - Currently selected role IDs
 * @param selectedGroupIds - Currently selected group IDs
 * @param onRoleToggle - Callback when a role is selected/deselected
 * @param onGroupToggle - Callback when a group is selected/deselected
 * @param loading - Whether the component is in loading state
 * @param className - Additional CSS classes
 */
```

### 3. Testing Strategy
- **Unit Tests:** Test individual components in isolation
- **Integration Tests:** Test component interactions and data flow
- **API Tests:** Test httpClient methods with mock responses

### 4. Performance Optimization
- Use `useMemo` for expensive computations
- Use `useCallback` for event handlers passed to child components
- Implement proper loading states and error boundaries
- Optimize re-renders with `React.memo` when appropriate

## Common Patterns

### 1. Loading States
```typescript
const [loading, setLoading] = useState(false);
const [error, setError] = useState<string | null>(null);

const handleAsyncOperation = async () => {
  try {
    setLoading(true);
    setError(null);
    await someAsyncOperation();
  } catch (err) {
    setError(err instanceof Error ? err.message : 'An error occurred');
  } finally {
    setLoading(false);
  }
};
```

### 2. Search and Filter
```typescript
const [searchTerm, setSearchTerm] = useState("");
const [filteredItems, setFilteredItems] = useState(items);

const handleSearch = useCallback((term: string) => {
  setSearchTerm(term);
  const filtered = items.filter(item =>
    item.name.toLowerCase().includes(term.toLowerCase())
  );
  setFilteredItems(filtered);
}, [items]);
```

### 3. Form State Management
```typescript
const [formData, setFormData] = useState<FormType>(initialState);
const [errors, setErrors] = useState<Record<string, string>>({});

const updateField = (field: keyof FormType, value: any) => {
  setFormData(prev => ({ ...prev, [field]: value }));
  // Clear error when field is updated
  if (errors[field]) {
    setErrors(prev => ({ ...prev, [field]: '' }));
  }
};
```

## Conclusion

Following these reusability principles and httpClient patterns will lead to:

- **Reduced Code Duplication:** Reusable components eliminate repeated code
- **Improved Maintainability:** Centralized logic is easier to update and debug
- **Enhanced Developer Experience:** Consistent patterns make development faster
- **Better Testing:** Smaller, focused components are easier to test
- **Scalability:** Well-structured code can grow with the application

Remember to regularly review and refactor code to maintain these standards as the application evolves.