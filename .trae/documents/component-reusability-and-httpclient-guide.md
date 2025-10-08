# Component Reusability and HttpClient Usage Guide

## Table of Contents
1. [Overview](#overview)
2. [Component Reusability Principles](#component-reusability-principles)
3. [Card Components](#card-components)
4. [Edit Toggle Functionality in Detail Pages](#edit-toggle-functionality-in-detail-pages)
5. [HttpClient Architecture and Usage](#httpclient-architecture-and-usage)
6. [Best Practices](#best-practices)
7. [Implementation Examples](#implementation-examples)
8. [Maintenance Guidelines](#maintenance-guidelines)
9. [Common Patterns](#common-patterns)

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
```
+ 
+ ## Edit Toggle Functionality in Detail Pages
+ 
+ Edit toggle functionality allows users to switch between view and edit modes within detail pages, providing a seamless user experience for data modification.
+ 
+ ### 1. Edit Toggle Pattern
+ 
+ The edit toggle pattern consists of three main states:
+ - **View Mode**: Display data in read-only format
+ - **Edit Mode**: Show form inputs for data modification
+ - **Saving State**: Indicate ongoing save operations
+ 
+ ### 2. Implementation Structure
+ 
+ #### Basic Edit Toggle Hook
+ ```typescript
+ interface UseEditToggleProps<T> {
+   initialData: T;
+   onSave: (data: T) => Promise<void>;
+   onCancel?: () => void;
+ }
+ 
+ export function useEditToggle<T>({ initialData, onSave, onCancel }: UseEditToggleProps<T>) {
+   const [isEditing, setIsEditing] = useState(false);
+   const [editData, setEditData] = useState<T>(initialData);
+   const [isSaving, setIsSaving] = useState(false);
+ 
+   const handleEditToggle = () => {
+     if (isEditing) {
+       // Cancel editing - reset data
+       setEditData(initialData);
+       onCancel?.();
+     }
+     setIsEditing(!isEditing);
+   };
+ 
+   const handleSave = async () => {
+     setIsSaving(true);
+     try {
+       await onSave(editData);
+       setIsEditing(false);
+     } catch (error) {
+       console.error('Save failed:', error);
+     } finally {
+       setIsSaving(false);
+     }
+   };
+ 
+   return {
+     isEditing,
+     editData,
+     isSaving,
+     setEditData,
+     handleEditToggle,
+     handleSave,
+   };
+ }
+ ```
+ 
+ #### Component Implementation
+ ```typescript
+ export const BasicInformationCard: React.FC<BasicInformationCardProps> = ({
+   userGroup,
+   onUpdate,
+   updating = false,
+ }) => {
+   const [isEditing, setIsEditing] = useState(false);
+   const [editForm, setEditForm] = useState({
+     name: userGroup.name,
+     description: userGroup.description || '',
+   });
+ 
+   const handleEditToggle = () => {
+     if (isEditing) {
+       // Cancel editing - reset form
+       setEditForm({
+         name: userGroup.name,
+         description: userGroup.description || '',
+       });
+     }
+     setIsEditing(!isEditing);
+   };
+ 
+   const handleSaveChanges = async () => {
+     if (!editForm.name.trim()) return;
+     
+     try {
+       if (onUpdate) {
+         await onUpdate({
+           name: editForm.name.trim(),
+           description: editForm.description.trim(),
+         });
+       }
+       setIsEditing(false);
+     } catch (error) {
+       console.error('Failed to update:', error);
+     }
+   };
+ 
+   return (
+     <Card>
+       <CardHeader>
+         <div className="flex items-center justify-between">
+           <CardTitle>Basic Information</CardTitle>
+           <PermissionGuard permission="user-groups:update">
+             {isEditing ? (
+               <div className="flex gap-2">
+                 <Button
+                   variant="outline"
+                   size="sm"
+                   onClick={handleSaveChanges}
+                   disabled={!editForm.name.trim() || updating}
+                 >
+                   <Save className="h-4 w-4 mr-2" />
+                   Save
+                 </Button>
+                 <Button
+                   variant="ghost"
+                   size="sm"
+                   onClick={handleEditToggle}
+                   disabled={updating}
+                 >
+                   <X className="h-4 w-4 mr-2" />
+                   Cancel
+                 </Button>
+               </div>
+             ) : (
+               <Button
+                 variant="outline"
+                 size="sm"
+                 onClick={handleEditToggle}
+               >
+                 <Edit className="h-4 w-4 mr-2" />
+                 Edit
+               </Button>
+             )}
+           </PermissionGuard>
+         </div>
+       </CardHeader>
+       <CardContent>
+         <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
+           <div className="space-y-2">
+             <Label>Group Name</Label>
+             {isEditing ? (
+               <Input
+                 value={editForm.name}
+                 onChange={(e) =>
+                   setEditForm({ ...editForm, name: e.target.value })
+                 }
+                 placeholder="Enter group name"
+                 disabled={updating}
+               />
+             ) : (
+               <div className="text-sm">{userGroup.name}</div>
+             )}
+           </div>
+           
+           <div className="space-y-2 md:col-span-2">
+             <Label>Description</Label>
+             {isEditing ? (
+               <Textarea
+                 value={editForm.description}
+                 onChange={(e) =>
+                   setEditForm({ ...editForm, description: e.target.value })
+                 }
+                 placeholder="Enter description"
+                 rows={3}
+                 disabled={updating}
+               />
+             ) : (
+               <div className="text-sm text-muted-foreground">
+                 {userGroup.description || "No description provided"}
+               </div>
+             )}
+           </div>
+         </div>
+       </CardContent>
+     </Card>
+   );
+ };
+ ```
+ 
+ ### 3. Edit Toggle Best Practices
+ 
+ #### State Management
+ 1. **Reset on Cancel**: Always reset form data when canceling edits
+ 2. **Validation**: Implement client-side validation before saving
+ 3. **Loading States**: Show loading indicators during save operations
+ 4. **Error Handling**: Display error messages and maintain edit state on failure
+ 
+ #### User Experience
+ 1. **Clear Visual Indicators**: Use distinct styling for edit vs view modes
+ 2. **Keyboard Navigation**: Support Enter to save, Escape to cancel
+ 3. **Confirmation**: Ask for confirmation before discarding unsaved changes
+ 4. **Auto-save**: Consider implementing auto-save for long forms
+ 
+ #### Permission Integration
+ ```typescript
+ <PermissionGuard permission="resource:update">
+   {isEditing ? (
+     // Edit mode buttons
+   ) : (
+     // Edit button
+   )}
+ </PermissionGuard>
+ ```
+ 
+ #### Form Validation
+ ```typescript
+ const [errors, setErrors] = useState<Record<string, string>>({});
+ 
+ const validateForm = () => {
+   const newErrors: Record<string, string> = {};
+   
+   if (!editForm.name.trim()) {
+     newErrors.name = 'Name is required';
+   }
+   
+   if (editForm.name.length > 100) {
+     newErrors.name = 'Name must be less than 100 characters';
+   }
+   
+   setErrors(newErrors);
+   return Object.keys(newErrors).length === 0;
+ };
+ 
+ const handleSaveChanges = async () => {
+   if (!validateForm()) return;
+   
+   // Proceed with save
+ };
+ ```
+ 
+ ### 4. Advanced Edit Toggle Patterns
+ 
+ #### Inline Editing
+ For simple fields that can be edited individually:
+ 
+ ```typescript
+ const [editingField, setEditingField] = useState<string | null>(null);
+ 
+ const handleFieldEdit = (fieldName: string) => {
+   setEditingField(fieldName);
+ };
+ 
+ const handleFieldSave = async (fieldName: string, value: any) => {
+   await onUpdate({ [fieldName]: value });
+   setEditingField(null);
+ };
+ ```
+ 
+ #### Bulk Edit Mode
+ For editing multiple items simultaneously:
+ 
+ ```typescript
+ const [bulkEditMode, setBulkEditMode] = useState(false);
+ const [selectedItems, setSelectedItems] = useState<string[]>([]);
+ 
+ const handleBulkEdit = async (updates: Partial<T>) => {
+   await Promise.all(
+     selectedItems.map(id => onUpdate(id, updates))
+   );
+   setBulkEditMode(false);
+   setSelectedItems([]);
+ };
+ ```
+ 
+ ### 5. Testing Edit Toggle Components
+ 
+ ```typescript
+ describe('BasicInformationCard', () => {
+   it('should toggle between view and edit modes', () => {
+     render(<BasicInformationCard {...props} />);
+     
+     // Initially in view mode
+     expect(screen.getByText('Edit')).toBeInTheDocument();
+     
+     // Click edit button
+     fireEvent.click(screen.getByText('Edit'));
+     
+     // Should be in edit mode
+     expect(screen.getByText('Save')).toBeInTheDocument();
+     expect(screen.getByText('Cancel')).toBeInTheDocument();
+   });
+ 
+   it('should reset form data on cancel', () => {
+     render(<BasicInformationCard {...props} />);
+     
+     // Enter edit mode and modify data
+     fireEvent.click(screen.getByText('Edit'));
+     fireEvent.change(screen.getByDisplayValue('Original Name'), {
+       target: { value: 'Modified Name' }
+     });
+     
+     // Cancel editing
+     fireEvent.click(screen.getByText('Cancel'));
+     
+     // Should reset to original value
+     fireEvent.click(screen.getByText('Edit'));
+     expect(screen.getByDisplayValue('Original Name')).toBeInTheDocument();
+   });
+ });
+ ```
 
  ## HttpClient Architecture and Usagetypescript
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

## Card Components

Card components are fundamental building blocks for organizing content in detail pages and forms. They provide a consistent visual structure and encapsulate related functionality.

### 1. Card Component Structure

Card components follow a consistent structure using shadcn/ui components:

```typescript
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';

interface CardProps {
  title: string;
  data: T;
  onUpdate?: (data: Partial<T>) => Promise<void>;
  updating?: boolean;
  className?: string;
}

export const ExampleCard: React.FC<CardProps> = ({
  title,
  data,
  onUpdate,
  updating = false,
  className
}) => {
  return (
    <Card className={className}>
      <CardHeader>
        <CardTitle>{title}</CardTitle>
      </CardHeader>
      <CardContent>
        {/* Card content */}
      </CardContent>
    </Card>
  );
};
```

### 2. Card Component Patterns

#### Information Display Cards
Used for displaying read-only or editable information:

```typescript
// BasicInformationCard example
export const BasicInformationCard: React.FC<BasicInformationCardProps> = ({
  userGroup,
  onUpdate,
  updating = false,
}) => {
  const [isEditing, setIsEditing] = useState(false);
  
  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle>Basic Information</CardTitle>
          {/* Edit toggle button */}
        </div>
      </CardHeader>
      <CardContent>
        {/* Form fields or display content */}
      </CardContent>
    </Card>
  );
};
```

#### Action Cards
Cards that contain interactive elements and actions:

```typescript
// RoleAssignmentsCard example
export const RoleAssignmentsCard: React.FC<RoleAssignmentsCardProps> = ({
  roleAssignments,
  availableModules,
  availableRoles,
  onAssignRoles,
  onRemoveRoleAssignment,
  updating,
}) => {
  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle>Role Assignments</CardTitle>
          <Button onClick={handleAddRole}>
            <Plus className="h-4 w-4 mr-2" />
            Add Role
          </Button>
        </div>
      </CardHeader>
      <CardContent>
        {/* Role management interface */}
      </CardContent>
    </Card>
  );
};
```

### 3. Card Layout Patterns

#### Two-Column Layout
Common pattern for detail pages with main content and sidebar:

```typescript
<div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
  {/* Left Column - Main Information */}
  <div className="lg:col-span-2 space-y-6">
    <BasicInformationCard {...props} />
    <RoleAssignmentsCard {...props} />
  </div>
  
  {/* Right Column - Actions & Statistics */}
  <div className="space-y-6">
    <StatusManagementCard {...props} />
    <StatisticsCard {...props} />
  </div>
</div>
```

#### Single Column Layout
For simpler interfaces or mobile-first designs:

```typescript
<div className="space-y-6">
  <BasicInformationCard {...props} />
  <RoleAssignmentsCard {...props} />
  <ActivityLogCard {...props} />
</div>
```

### 4. Card Component Best Practices

1. **Consistent Header Structure**: Always include a clear title and relevant actions
2. **Loading States**: Show loading indicators during async operations
3. **Error Handling**: Display error messages within the card context
4. **Responsive Design**: Use responsive grid layouts for different screen sizes
5. **Permission Guards**: Wrap actions with permission checks
6. **Accessibility**: Include proper ARIA labels and keyboard navigation

```typescript
<Card>
  <CardHeader>
    <div className="flex items-center justify-between">
      <CardTitle>Card Title</CardTitle>
      <PermissionGuard permission="resource:action">
        <Button variant="outline" size="sm">
          Action
        </Button>
      </PermissionGuard>
    </div>
  </CardHeader>
  <CardContent>
    {loading ? (
      <Skeleton className="h-20 w-full" />
    ) : error ? (
      <div className="text-red-500">Error: {error}</div>
    ) : (
      // Card content
    )}
  </CardContent>
</Card>
```

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