# Component Reusability and HttpClient Usage Guide

## Table of Contents
1. [Overview](#overview)
2. [Component Reusability Principles](#component-reusability-principles)
3. [Card Components](#card-components)
4. [DetailHeaderCard Component](#detailheadercard-component)
5. [Edit Toggle Functionality in Detail Pages](#edit-toggle-functionality-in-detail-pages)
6. [HttpClient Architecture and Usage](#httpclient-architecture-and-usage)
7. [Best Practices](#best-practices)
8. [Implementation Examples](#implementation-examples)
9. [Maintenance Guidelines](#maintenance-guidelines)
10. [Common Patterns](#common-patterns)

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

### 5. Collapsible Card Components

Collapsible cards provide better user experience by allowing users to hide/show content sections, reducing visual clutter and improving page navigation.

#### Implementation Pattern

```typescript
import React, { useState } from "react";
import { ChevronDown, ChevronUp } from "lucide-react";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";

interface CollapsibleCardProps {
  title: string;
  children: React.ReactNode;
  defaultExpanded?: boolean;
  className?: string;
  icon?: React.ReactNode;
}

export const CollapsibleCard: React.FC<CollapsibleCardProps> = ({
  title,
  children,
  defaultExpanded = true,
  className = "",
  icon,
}) => {
  const [isExpanded, setIsExpanded] = useState(defaultExpanded);

  const toggleExpanded = () => {
    setIsExpanded(!isExpanded);
  };

  return (
    <Card className={className}>
      <CardHeader 
        className="cursor-pointer hover:bg-gray-50 transition-colors"
        onClick={toggleExpanded}
      >
        <CardTitle className="flex items-center justify-between">
          <div className="flex items-center">
            {icon && <span className="mr-2">{icon}</span>}
            {title}
          </div>
          {isExpanded ? (
            <ChevronUp className="h-4 w-4 text-gray-500" />
          ) : (
            <ChevronDown className="h-4 w-4 text-gray-500" />
          )}
        </CardTitle>
      </CardHeader>
      {isExpanded && (
        <CardContent>
          {children}
        </CardContent>
      )}
    </Card>
  );
};
```

#### Specific Implementation Example - PermissionsCard

```typescript
import React, { useState } from "react";
import { Activity, ChevronDown, ChevronUp } from "lucide-react";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import type { Permission } from "@/types";

export interface PermissionsCardProps {
  permissions?: Permission[];
  title?: string;
  emptyMessage?: string;
  className?: string;
  showIcon?: boolean;
  badgeVariant?: "default" | "secondary" | "destructive" | "outline";
  gridCols?: {
    base?: number;
    md?: number;
    lg?: number;
  };
  defaultExpanded?: boolean;
}

export const PermissionsCard: React.FC<PermissionsCardProps> = ({
  permissions = [],
  title = "Permissions",
  emptyMessage = "No permissions assigned.",
  className = "",
  showIcon = true,
  badgeVariant = "outline",
  gridCols = { base: 1, md: 2, lg: 3 },
  defaultExpanded = true,
}) => {
  const [isExpanded, setIsExpanded] = useState(defaultExpanded);
  
  const gridClasses = `grid grid-cols-${gridCols.base} ${
    gridCols.md ? `md:grid-cols-${gridCols.md}` : ""
  } ${gridCols.lg ? `lg:grid-cols-${gridCols.lg}` : ""} gap-2`;

  const toggleExpanded = () => {
    setIsExpanded(!isExpanded);
  };

  return (
    <Card className={className}>
      <CardHeader 
        className="cursor-pointer hover:bg-gray-50 transition-colors"
        onClick={toggleExpanded}
      >
        <CardTitle className="flex items-center justify-between">
          <div className="flex items-center">
            {showIcon && <Activity className="mr-2 h-5 w-5" />}
            {title} ({permissions.length})
          </div>
          {isExpanded ? (
            <ChevronUp className="h-4 w-4 text-gray-500" />
          ) : (
            <ChevronDown className="h-4 w-4 text-gray-500" />
          )}
        </CardTitle>
      </CardHeader>
      {isExpanded && (
        <CardContent>
          {permissions.length > 0 ? (
            <div className={gridClasses}>
              {permissions.map((permission) => (
                <Badge
                  key={permission.id}
                  variant={badgeVariant}
                  className="justify-start"
                  title={permission.description}
                >
                  {permission.name}
                </Badge>
              ))}
            </div>
          ) : (
            <p className="text-sm text-gray-500">{emptyMessage}</p>
          )}
        </CardContent>
      )}
    </Card>
  );
};
```

#### Collapsible Card Best Practices

1. **Visual Feedback**: 
   - Use hover effects on clickable headers
   - Clear chevron icons to indicate expand/collapse state
   - Smooth transitions for better user experience

2. **Default State Configuration**:
   - Provide `defaultExpanded` prop for initial state control
   - Consider context - frequently used sections should default to expanded

3. **Accessibility**:
   - Ensure keyboard navigation support
   - Use proper ARIA attributes for screen readers
   - Maintain focus management during state changes

4. **Performance Considerations**:
   - Use conditional rendering to avoid rendering hidden content
   - Consider lazy loading for expensive content in collapsed cards

5. **Consistent Interaction Patterns**:
   - Click entire header area, not just the chevron
   - Consistent animation timing across all collapsible components
   - Clear visual indicators for interactive elements

#### Usage Examples

```typescript
// Basic usage with default expanded state
<PermissionsCard 
  permissions={userPermissions}
  title="User Permissions"
  defaultExpanded={true}
/>

// Collapsed by default for less critical information
<PermissionsCard 
  permissions={additionalPermissions}
  title="Additional Permissions"
  defaultExpanded={false}
  className="mt-4"
/>

// Custom grid layout for different screen sizes
<PermissionsCard 
  permissions={rolePermissions}
  title="Role Permissions"
  gridCols={{ base: 1, md: 3, lg: 4 }}
  badgeVariant="secondary"
/>
```

## DetailHeaderCard Component

The `DetailHeaderCard` component provides a standardized header layout for detail pages across the application. It combines breadcrumb navigation, page title, description, and action buttons in a consistent format.

### 1. Component Overview

The `DetailHeaderCard` component is designed to replace custom header implementations in detail pages, ensuring visual consistency and reducing code duplication.

**Location:** `/src/components/common/DetailHeaderCard.tsx`

**Key Features:**
- Consistent breadcrumb navigation
- Flexible title and description display
- Configurable action buttons
- Responsive design
- TypeScript support with proper interfaces

### 2. Component Interface

```typescript
interface BreadcrumbItem {
  label: string;
  href?: string;
}

interface DetailHeaderCardProps {
  title: string;
  description?: string;
  breadcrumbs: BreadcrumbItem[];
  actions?: React.ReactNode;
  className?: string;
}
```

### 3. Implementation Structure

```typescript
import React from "react";
import { Link } from "react-router-dom";
import {
  Breadcrumb,
  BreadcrumbItem,
  BreadcrumbLink,
  BreadcrumbList,
  BreadcrumbPage,
  BreadcrumbSeparator,
} from "@/components/ui/breadcrumb";

export const DetailHeaderCard: React.FC<DetailHeaderCardProps> = ({
  title,
  description,
  breadcrumbs,
  actions,
  className = "",
}) => {
  return (
    <div className={`mb-8 ${className}`}>
      {/* Breadcrumb Navigation */}
      <Breadcrumb className="mb-4">
        <BreadcrumbList>
          {breadcrumbs.map((breadcrumb, index) => (
            <React.Fragment key={index}>
              <BreadcrumbItem>
                {breadcrumb.href ? (
                  <BreadcrumbLink asChild>
                    <Link to={breadcrumb.href}>{breadcrumb.label}</Link>
                  </BreadcrumbLink>
                ) : (
                  <BreadcrumbPage>{breadcrumb.label}</BreadcrumbPage>
                )}
              </BreadcrumbItem>
              {index < breadcrumbs.length - 1 && <BreadcrumbSeparator />}
            </React.Fragment>
          ))}
        </BreadcrumbList>
      </Breadcrumb>

      {/* Header Section */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">{title}</h1>
          {description && (
            <p className="mt-2 text-gray-600">{description}</p>
          )}
        </div>
        {actions && <div className="flex items-center space-x-3">{actions}</div>}
      </div>
    </div>
  );
};
```

### 4. Usage Patterns

#### Basic Usage
Simple header with title and breadcrumbs:

```typescript
<DetailHeaderCard
  title="User Details"
  description="Manage user information and permissions"
  breadcrumbs={[
    { label: "Users", href: "/users" },
    { label: "John Doe" }
  ]}
/>
```

#### With Actions
Header including action buttons:

```typescript
<DetailHeaderCard
  title="Product Details"
  description="View and manage product information"
  breadcrumbs={[
    { label: "Products", href: "/products" },
    { label: product.name }
  ]}
  actions={
    <PermissionGuard permission="products:update">
      <Button variant="outline" onClick={handleEdit}>
        <Edit className="h-4 w-4 mr-2" />
        Edit
      </Button>
    </PermissionGuard>
  }
/>
```

#### Complex Actions
Multiple actions with permission guards:

```typescript
<DetailHeaderCard
  title={product.name}
  description={product.description}
  breadcrumbs={[
    { label: "Products", href: "/products" },
    { label: product.name }
  ]}
  actions={
    <>
      <StatusBadge status={normalizeEntityStatus(product.status)} />
      <PermissionGuard permission="products:update">
        <Button
          variant={product.status === 'ACTIVE' ? 'destructive' : 'default'}
          onClick={handleToggleStatus}
          disabled={updating}
        >
          {product.status === 'ACTIVE' ? 'Deactivate' : 'Activate'}
        </Button>
      </PermissionGuard>
      <PermissionGuard permission="products:update">
        <Button variant="outline" asChild>
          <Link to={`/products/${product.id}/edit`}>
            <Edit className="h-4 w-4 mr-2" />
            Edit
          </Link>
        </Button>
      </PermissionGuard>
    </>
  }
/>
```

### 5. Best Practices

#### Breadcrumb Structure
1. **Consistent Navigation**: Always include the parent list page as the first breadcrumb
2. **Current Page**: The last breadcrumb should represent the current page without a link
3. **Meaningful Labels**: Use descriptive labels that help users understand their location

```typescript
// Good breadcrumb structure
breadcrumbs={[
  { label: "Users", href: "/users" },           // Parent list
  { label: "User Groups", href: "/user-groups" }, // Sub-category (if applicable)
  { label: userGroup.name }                     // Current page
]}
```

#### Title and Description
1. **Dynamic Titles**: Use actual entity names when available
2. **Descriptive Content**: Provide helpful descriptions that explain the page purpose
3. **Fallback Values**: Handle cases where data might not be available

```typescript
// Dynamic title with fallback
title={user ? getFullName(user) : 'Loading...'}
description={user ? `@${user.username} • ${user.email}` : undefined}
```

#### Action Integration
1. **Permission Guards**: Wrap actions with appropriate permission checks
2. **Loading States**: Disable actions during async operations
3. **Visual Hierarchy**: Use appropriate button variants to indicate action importance

```typescript
// Well-structured actions
actions={
  <PermissionGuard permission="users:update">
    <Button
      variant="outline"
      onClick={handleEdit}
      disabled={loading}
    >
      <Edit className="h-4 w-4 mr-2" />
      Edit
    </Button>
  </PermissionGuard>
}
```

### 6. Migration from Custom Headers

When migrating existing detail pages to use `DetailHeaderCard`:

1. **Identify Header Elements**: Locate existing breadcrumb, title, and action components
2. **Extract Data**: Gather the title, description, and breadcrumb information
3. **Consolidate Actions**: Combine action buttons into the actions prop
4. **Remove Old Code**: Delete the replaced header implementation
5. **Test Navigation**: Verify breadcrumb links work correctly

#### Before Migration
```typescript
// Old custom header implementation
<div className="mb-8">
  <Breadcrumb className="mb-4">
    <BreadcrumbList>
      <BreadcrumbItem>
        <BreadcrumbLink asChild>
          <Link to="/roles">Roles</Link>
        </BreadcrumbLink>
      </BreadcrumbItem>
      <BreadcrumbSeparator />
      <BreadcrumbItem>
        <BreadcrumbPage>{role.name}</BreadcrumbPage>
      </BreadcrumbItem>
    </BreadcrumbList>
  </Breadcrumb>
  
  <div className="flex items-center justify-between">
    <div>
      <h1 className="text-3xl font-bold text-gray-900">{role.name}</h1>
      <p className="mt-2 text-gray-600">{role.description}</p>
    </div>
  </div>
</div>
```

#### After Migration
```typescript
// New DetailHeaderCard implementation
<DetailHeaderCard
  title={role.name}
  description={role.description}
  breadcrumbs={[
    { label: "Roles", href: "/roles" },
    { label: role.name }
  ]}
/>
```

### 7. Responsive Design

The `DetailHeaderCard` component is designed to work across different screen sizes:

- **Mobile**: Actions stack vertically on smaller screens
- **Tablet**: Balanced layout with proper spacing
- **Desktop**: Full horizontal layout with optimal spacing

The component uses Tailwind CSS classes to ensure responsive behavior without additional configuration.

### 8. Accessibility Features

1. **Semantic HTML**: Uses proper heading hierarchy (h1 for main title)
2. **Navigation**: Breadcrumbs provide clear navigation context
3. **Focus Management**: Proper tab order for interactive elements
4. **Screen Readers**: Descriptive text and proper ARIA attributes

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

#### 4. Standardize Header Components
```typescript
// ✅ Good - Use DetailHeaderCard for all detail pages
const UserDetail: React.FC = () => {
  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <DetailHeaderCard
          title={getFullName(user)}
          description={`@${user.username} • ${user.email}`}
          breadcrumbs={[
            { label: "Users", href: "/users" },
            { label: getFullName(user) }
          ]}
        />
        {/* Page content */}
      </div>
    </div>
  );
};

// ❌ Bad - Custom header implementation
const UserDetail: React.FC = () => {
  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Custom breadcrumb and header implementation */}
        <nav className="flex" aria-label="Breadcrumb">
          <ol className="inline-flex items-center space-x-1 md:space-x-3">
            {/* Manual breadcrumb implementation */}
          </ol>
        </nav>
        <div className="mt-4">
          <h1 className="text-3xl font-bold">{getFullName(user)}</h1>
          <p className="text-gray-600">{user.email}</p>
        </div>
        {/* Inconsistent styling and structure */}
      </div>
    </div>
  );
};
```

#### 5. Header Component Guidelines
- **Always use `DetailHeaderCard`** for detail pages to ensure consistency
- **Include breadcrumbs** for navigation context using the `breadcrumbs` prop
- **Use descriptive titles** that clearly identify the entity or page purpose
- **Provide meaningful descriptions** that give additional context about the entity
- **Group related actions** in the `actions` prop with proper permission guards
- **Maintain responsive design** by leveraging the component's built-in responsive behavior
- **Follow accessibility standards** by using the component's ARIA-compliant structure

#### 6. Migration Strategy for Existing Headers
When updating existing detail pages to use `DetailHeaderCard`:

1. **Identify the current header structure** (breadcrumbs, title, description, actions)
2. **Extract the title and description** from existing markup
3. **Convert breadcrumb navigation** to the `breadcrumbs` prop format
4. **Move action buttons** to the `actions` prop
5. **Remove custom header markup** and replace with `DetailHeaderCard`
6. **Test responsive behavior** and accessibility compliance
7. **Verify permission guards** are properly applied to actions

```typescript
// Migration example - Before
<div className="bg-white shadow">
  <div className="px-4 py-6 sm:px-6">
    <nav className="flex" aria-label="Breadcrumb">
      {/* Custom breadcrumb */}
    </nav>
    <div className="mt-4 md:flex md:items-center md:justify-between">
      <div className="flex-1 min-w-0">
        <h2 className="text-2xl font-bold">{product.name}</h2>
        <p className="text-sm text-gray-500">{product.description}</p>
      </div>
      <div className="mt-4 flex md:mt-0 md:ml-4">
        {/* Action buttons */}
      </div>
    </div>
  </div>
</div>

// Migration example - After
<DetailHeaderCard
  title={product.name}
  description={product.description}
  breadcrumbs={[
    { label: "Products", href: "/products" },
    { label: product.name }
  ]}
  actions={
    <>
      <StatusBadge status={normalizeEntityStatus(product.status)} />
      <Button variant="outline" asChild>
        <Link to={`/products/${product.id}/edit`}>Edit</Link>
      </Button>
    </>
  }
/>
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

### 1. DetailHeaderCard Implementation Examples

The following examples demonstrate real-world usage of the `DetailHeaderCard` component across different detail pages in the application.

#### User Detail Page
```typescript
// UserDetail.tsx
import { DetailHeaderCard } from "@/components/common";

const UserDetail: React.FC = () => {
  const { user, loading } = useUserData();
  
  if (loading) return <Skeleton />;
  
  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <DetailHeaderCard
          title={getFullName(user)}
          description={`@${user.username} • ${user.email}`}
          breadcrumbs={[
            { label: "Users", href: "/users" },
            { label: getFullName(user) }
          ]}
        />
        
        {/* Rest of the page content */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* User information cards */}
        </div>
      </div>
    </div>
  );
};
```

#### Product Detail Page with Actions
```typescript
// ProductDetail.tsx
import { DetailHeaderCard } from "@/components/common";
import { StatusBadge } from "@/components/common/StatusBadge";
import { PermissionGuard } from "@/components/common/PermissionGuard";

const ProductDetail: React.FC = () => {
  const { product, updating, handleToggleStatus, handleDelete } = useProductData();
  
  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <DetailHeaderCard
          title={product.name}
          description={product.description}
          breadcrumbs={[
            { label: "Products", href: "/products" },
            { label: product.name }
          ]}
          actions={
            <>
              <StatusBadge status={normalizeEntityStatus(product.status)} />
              <PermissionGuard permission="products:update">
                <Button
                  variant={product.status === 'ACTIVE' ? 'destructive' : 'default'}
                  onClick={handleToggleStatus}
                  disabled={updating}
                >
                  {product.status === 'ACTIVE' ? 'Deactivate' : 'Activate'}
                </Button>
              </PermissionGuard>
              <PermissionGuard permission="products:update">
                <Button variant="outline" asChild>
                  <Link to={`/products/${product.id}/edit`}>
                    <Edit className="h-4 w-4 mr-2" />
                    Edit
                  </Link>
                </Button>
              </PermissionGuard>
              <PermissionGuard permission="products:delete">
                <AlertDialog>
                  <AlertDialogTrigger asChild>
                    <Button variant="destructive" size="sm">
                      <Trash2 className="h-4 w-4 mr-2" />
                      Delete
                    </Button>
                  </AlertDialogTrigger>
                  <AlertDialogContent>
                    <AlertDialogHeader>
                      <AlertDialogTitle>Delete Product</AlertDialogTitle>
                      <AlertDialogDescription>
                        Are you sure you want to delete this product? This action cannot be undone.
                      </AlertDialogDescription>
                    </AlertDialogHeader>
                    <AlertDialogFooter>
                      <AlertDialogCancel>Cancel</AlertDialogCancel>
                      <AlertDialogAction onClick={handleDelete}>
                        Delete
                      </AlertDialogAction>
                    </AlertDialogFooter>
                  </AlertDialogContent>
                </AlertDialog>
              </PermissionGuard>
            </>
          }
        />
        
        {/* Product details content */}
      </div>
    </div>
  );
};
```

#### Role Detail Page
```typescript
// RoleDetail.tsx
import { DetailHeaderCard } from "@/components/common";

const RoleDetail: React.FC = () => {
  const { role, loading } = useRoleData();
  
  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <DetailHeaderCard
          title={role.name}
          description={role.description}
          breadcrumbs={[
            { label: "Roles", href: "/roles" },
            { label: role.name }
          ]}
        />
        
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          <div className="lg:col-span-2 space-y-8">
            <RoleInfoCard role={role} />
            <PermissionsCard
              permissions={role.permissions}
              title="Permissions"
              defaultExpanded={false}
            />
          </div>
          <div className="space-y-6">
            <RoleStatusCard role={role} />
            <RoleStatsCard stats={stats} />
          </div>
        </div>
      </div>
    </div>
  );
};
```

#### User Group Detail Page with Delete Action
```typescript
// UserGroupDetail.tsx
import { DetailHeaderCard } from "@/components/common";

const UserGroupDetail: React.FC = () => {
  const { userGroup, handleDelete, deleting } = useUserGroupData();
  
  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <DetailHeaderCard
          title={userGroup.name}
          description={userGroup.description}
          breadcrumbs={[
            { label: "User Groups", href: "/user-groups" },
            { label: userGroup.name }
          ]}
          actions={
            <PermissionGuard permission="user-groups:delete">
              <AlertDialog>
                <AlertDialogTrigger asChild>
                  <Button variant="destructive" size="sm" disabled={deleting}>
                    <Trash2 className="h-4 w-4 mr-2" />
                    Delete Group
                  </Button>
                </AlertDialogTrigger>
                <AlertDialogContent>
                  <AlertDialogHeader>
                    <AlertDialogTitle>Delete User Group</AlertDialogTitle>
                    <AlertDialogDescription>
                      Are you sure you want to delete "{userGroup.name}"? 
                      This will remove all users from this group and cannot be undone.
                    </AlertDialogDescription>
                  </AlertDialogHeader>
                  <AlertDialogFooter>
                    <AlertDialogCancel>Cancel</AlertDialogCancel>
                    <AlertDialogAction
                      onClick={handleDelete}
                      className="bg-red-600 hover:bg-red-700"
                    >
                      Delete Group
                    </AlertDialogAction>
                  </AlertDialogFooter>
                </AlertDialogContent>
              </AlertDialog>
            </PermissionGuard>
          }
        />
        
        {/* User group content */}
      </div>
    </div>
  );
};
```

#### Edit Page Implementation
```typescript
// ProductEdit.tsx
import { DetailHeaderCard } from "@/components/common";

const ProductEdit: React.FC = () => {
  const { product } = useProductData();
  
  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <DetailHeaderCard
          title="Edit Product"
          description="Update product information and settings"
          breadcrumbs={[
            { label: "Products", href: "/products" },
            { label: product?.name || "Loading...", href: `/products/${product?.id}` },
            { label: "Edit" }
          ]}
        />
        
        {/* Edit form content */}
      </div>
    </div>
  );
};
```

### 2. Reusable Search Component
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