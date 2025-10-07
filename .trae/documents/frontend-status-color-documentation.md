# Frontend Status Color Code Documentation

## Overview

This document outlines the standardized status color system implemented across the frontend application. The system ensures consistent visual representation of different status types throughout all components and pages.

## Architecture

### Core Files

- **`/src/lib/status-colors.ts`** - Central configuration for all status colors and utilities
- **`/src/lib/status-icons.tsx`** - Standardized status icon utilities
- **`/src/components/StatusBadge.tsx`** - Reusable status badge component

### Design Principles

1. **Consistency** - All status indicators use the same color scheme
2. **Accessibility** - Colors meet WCAG contrast requirements
3. **Maintainability** - Centralized configuration for easy updates
4. **Flexibility** - Support for various status formats and display options

## Status Color Mapping

### Complete Status Types

| Status | Badge Variant | Background | Text Color | Icon Color | Label | Use Case |
|--------|---------------|------------|------------|------------|-------|----------|
| **Active** | `default` | `bg-green-100` | `text-green-800` | `text-green-600` | Active | Active entities (users, tenants, modules) |
| **Inactive** | `secondary` | `bg-gray-100` | `text-gray-800` | `text-gray-600` | Inactive | Inactive/disabled entities |
| **Pending** | `outline` | `bg-yellow-100` | `text-yellow-800` | `text-yellow-600` | Pending | Awaiting approval/processing |
| **Suspended** | `destructive` | `bg-orange-100` | `text-orange-800` | `text-orange-600` | Suspended | Temporarily suspended entities |
| **Success** | `default` | `bg-green-100` | `text-green-800` | `text-green-600` | Success | Successful operations |
| **Error** | `destructive` | `bg-red-100` | `text-red-800` | `text-red-600` | Error | Error states |
| **Warning** | `outline` | `bg-yellow-100` | `text-yellow-800` | `text-yellow-600` | Warning | Warning states |
| **Info** | `outline` | `bg-blue-100` | `text-blue-800` | `text-blue-600` | Info | Informational states |
| **Draft** | `secondary` | `bg-gray-100` | `text-gray-800` | `text-gray-600` | Draft | Draft/work-in-progress items |
| **Published** | `default` | `bg-green-100` | `text-green-800` | `text-green-600` | Published | Published content |
| **Archived** | `secondary` | `bg-gray-100` | `text-gray-800` | `text-gray-600` | Archived | Archived/historical items |

### Status Icons

The application provides standardized status icons through `/src/lib/status-icons.tsx`:

| Status | Icon | Color | Description |
|--------|------|-------|-------------|
| **ACTIVE** | `CheckCircle` | `text-green-600` | Indicates active/enabled state |
| **INACTIVE** | `XCircle` | `text-red-600` | Indicates inactive/disabled state |
| **SUSPENDED** | `Clock` | `text-yellow-600` | Indicates suspended/pending state |
| **Default** | `XCircle` | `text-gray-600` | Fallback for unknown statuses |

#### Status Icon Usage

```tsx
import { getStatusIcon, getStatusIconWithSize } from '@/lib/status-icons'

// Default size (h-4 w-4)
{getStatusIcon('ACTIVE')}

// Custom size
{getStatusIconWithSize('INACTIVE', 'h-6 w-6')}
```

## Implementation Guide

### Using StatusBadge Component

#### Basic Usage
```tsx
import { StatusBadge } from '@/components/StatusBadge'
import { getStatusIcon } from '@/lib/status-icons'
import { getStatusConfig, isValidStatus } from '@/lib/status-colors'

// Simple status badge
<StatusBadge status="active" />

// Status badge with validation
const userStatus = isValidStatus(user.status) ? user.status : 'inactive'
<StatusBadge status={userStatus} />

// Status badge with icon
<div className="flex items-center gap-2">
  {getStatusIcon(user.status)}
  <StatusBadge status={user.status} />
</div>

// Custom styling with status config
const config = getStatusConfig(tenant.status)
<Badge variant={config.variant} className={config.colors.background}>
  {config.label}
</Badge>
```

#### Entity Type Mapping

The system provides entity-specific status validation and mapping through `getEntityStatusMapping`:

```tsx
import { getEntityStatusMapping, isValidStatus } from '@/lib/status-colors'

// Get valid statuses for specific entity types
const userStatuses = getEntityStatusMapping('user')
// Returns: ['active', 'inactive', 'suspended', 'pending']

const tenantStatuses = getEntityStatusMapping('tenant')
// Returns: ['active', 'inactive', 'suspended']

const moduleStatuses = getEntityStatusMapping('module')
// Returns: ['active', 'inactive']

const auditStatuses = getEntityStatusMapping('audit')
// Returns: ['success', 'error', 'warning', 'info']

const contentStatuses = getEntityStatusMapping('content')
// Returns: ['draft', 'published', 'archived']

// Validate status for entity type
const isValidUserStatus = isValidStatus('pending') // true
const isValidModuleStatus = isValidStatus('suspended') // false (not valid for modules)
```

#### Legacy Status Normalization

For backward compatibility with existing APIs that use different status formats:

```tsx
// Handle legacy status formats
const normalizeStatus = (entityType: string, rawStatus: any) => {
  if (typeof rawStatus === 'boolean') {
    return rawStatus ? 'active' : 'inactive'
  }
  
  if (typeof rawStatus === 'string') {
    return rawStatus.toLowerCase()
  }
  
  return 'inactive' // fallback
}
```
normalizeEntityStatus('product', 'active') // → 'active'
normalizeEntityStatus('product', 'inactive') // → 'inactive'
```

### Utility Functions

The status color system provides several utility functions for consistent status handling:

#### Core Functions

```tsx
import { 
  getStatusConfig, 
  getStatusBadgeVariant, 
  getStatusColors,
  getEntityStatusMapping,
  isValidStatus 
} from '@/lib/status-colors'

// Get complete status configuration
const config = getStatusConfig('active')
// Returns: { variant: 'default', colors: {...}, label: 'Active' }

// Get badge variant for status
const variant = getStatusBadgeVariant('pending')
// Returns: 'outline'

// Get color classes for status
const colors = getStatusColors('error')
// Returns: { background: 'bg-red-100', text: 'text-red-800', icon: 'text-red-600' }

// Check if status is valid
const isValid = isValidStatus('active')
// Returns: true

// Get entity-specific status mapping
const userStatuses = getEntityStatusMapping('user')
// Returns: ['active', 'inactive', 'suspended', 'pending']
```

#### Entity Status Mappings

The system supports entity-specific status mappings through `ENTITY_STATUS_MAPPINGS`:

```tsx
// Available entity types and their valid statuses
const ENTITY_STATUS_MAPPINGS = {
  user: ['active', 'inactive', 'suspended', 'pending'],
  tenant: ['active', 'inactive', 'suspended'],
  module: ['active', 'inactive'],
  role: ['active', 'inactive'],
  permission: ['active', 'inactive'],
  audit: ['success', 'error', 'warning', 'info'],
  content: ['draft', 'published', 'archived'],
  system: ['active', 'inactive', 'error', 'warning']
}
```

## Component Integration

### Updated Components

The following components have been updated to use the standardized status system:

1. **ProductDetail.tsx** - Product status badges
2. **ProductList.tsx** - Product status in list view with StatusBadge
3. **ModuleDetail.tsx** - Module active/inactive status
4. **ModuleList.tsx** - Module status in list view
5. **UserList.tsx** - User status indicators
6. **UserEdit.tsx** - User status management
7. **UserGroupsTable.tsx** - User group status display with StatusBadge
8. **RoleList.tsx** - Role status column with StatusBadge
9. **TenantDetail.tsx** - Tenant status display
10. **TenantList.tsx** - Tenant status in list view

### Migration Pattern

When updating existing components to use the standardized status system:

```tsx
// Before (inconsistent)
<Badge variant={status === 'ACTIVE' ? 'default' : 'secondary'}>
  {status}
</Badge>

// After (standardized with validation)
import { getStatusConfig, isValidStatus } from '@/lib/status-colors'

const normalizedStatus = status.toLowerCase()
const validStatus = isValidStatus(normalizedStatus) ? normalizedStatus : 'inactive'
const config = getStatusConfig(validStatus)

<Badge variant={config.variant} className={config.colors.background}>
  {config.label}
</Badge>

// Or using StatusBadge component
<StatusBadge status={validStatus} />
```

#### Legacy API Migration

```tsx
// Before (old normalizeEntityStatus)
<StatusBadge status={normalizeEntityStatus('user', rawStatus)} />

// After (current API)
import { getEntityStatusMapping, isValidStatus } from '@/lib/status-colors'

const validStatuses = getEntityStatusMapping('user')
const normalizedStatus = typeof rawStatus === 'string' 
  ? rawStatus.toLowerCase() 
  : rawStatus ? 'active' : 'inactive'
const finalStatus = validStatuses.includes(normalizedStatus) 
  ? normalizedStatus 
  : 'inactive'

<StatusBadge status={finalStatus} />
```

## Customization

### Adding New Status Types

To add a new status type, update the `STATUS_CONFIG` object in `/src/lib/status-colors.ts`:

```tsx
export const STATUS_CONFIG: Record<StatusType, StatusConfig> = {
  // ... existing statuses
  'new-status': {
    variant: 'outline',
    className: 'bg-purple-100 text-purple-800 hover:bg-purple-100 border-purple-200',
    iconColor: 'text-purple-600',
    bgColor: 'bg-purple-100',
    textColor: 'text-purple-800',
  },
}
```

### Custom Status Colors

For special cases requiring custom colors, you can extend the component:

```tsx
<StatusBadge 
  status="custom"
  className="bg-custom-100 text-custom-800"
/>
```

## Best Practices

### Do's ✅

- Always use `StatusBadge` component for status indicators
- Use `normalizeEntityStatus` for consistent status formatting
- Leverage the centralized configuration system
- Test color combinations for accessibility
- Document any new status types added

### Don'ts ❌

- Don't use hardcoded Tailwind color classes for status
- Don't create custom badge variants outside the system
- Don't bypass the normalization function
- Don't use inconsistent status naming conventions

## Accessibility Considerations

### Color Contrast
All status colors meet WCAG AA standards:
- Minimum contrast ratio of 4.5:1 for normal text
- Minimum contrast ratio of 3:1 for large text

### Color Blindness Support
- Status information is not conveyed by color alone
- Icons and text labels provide additional context
- High contrast between background and text colors

## Testing

### Visual Testing Checklist

- [ ] All status badges display consistently across pages
- [ ] Colors are accessible and meet contrast requirements
- [ ] Status normalization works for all entity types
- [ ] Icons display correctly when using `StatusBadgeWithIcon`
- [ ] Hover states work properly
- [ ] Dark mode compatibility (if applicable)

### Browser Compatibility

The status color system is compatible with:
- Chrome 90+
- Firefox 88+
- Safari 14+
- Edge 90+

## Maintenance

### Regular Tasks

1. **Quarterly Review** - Assess if new status types are needed
2. **Accessibility Audit** - Verify color contrast compliance
3. **Performance Check** - Ensure no unused status configurations
4. **Documentation Updates** - Keep this document current with changes

### Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0.0 | 2024-01-XX | Initial standardized status color system |
| 1.1.0 | 2024-01-XX | Added StatusBadge integration to UserGroupsTable, ProductList, and RoleList components |

## Support

For questions or issues related to the status color system:

1. Check this documentation first
2. Review the implementation in `/src/lib/status-colors.ts`
3. Examine existing usage in updated components
4. Create an issue if problems persist

---

*This documentation is maintained as part of the frontend technical documentation suite.*