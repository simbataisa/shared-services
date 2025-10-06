# Frontend Status Color Code Documentation

## Overview

This document outlines the standardized status color system implemented across the frontend application. The system ensures consistent visual representation of different status types throughout all components and pages.

## Architecture

### Core Files

- **`/src/lib/status-colors.ts`** - Central configuration for all status colors and utilities
- **`/src/components/StatusBadge.tsx`** - Reusable status badge component

### Design Principles

1. **Consistency** - All status indicators use the same color scheme
2. **Accessibility** - Colors meet WCAG contrast requirements
3. **Maintainability** - Centralized configuration for easy updates
4. **Flexibility** - Support for various status formats and display options

## Status Color Mapping

### Primary Status Types

| Status | Badge Variant | Background | Text Color | Icon Color | Use Case |
|--------|---------------|------------|------------|------------|----------|
| **Active** | `default` | `bg-green-100` | `text-green-800` | `text-green-600` | Active entities (users, tenants, modules) |
| **Inactive** | `secondary` | `bg-gray-100` | `text-gray-800` | `text-gray-600` | Inactive/disabled entities |
| **Pending** | `outline` | `bg-yellow-100` | `text-yellow-800` | `text-yellow-600` | Awaiting approval/processing |
| **Suspended** | `destructive` | `bg-orange-100` | `text-orange-800` | `text-orange-600` | Temporarily suspended entities |

### Extended Status Types

| Status | Badge Variant | Background | Text Color | Icon Color | Description |
|--------|---------------|------------|------------|------------|-------------|
| **Approved** | `default` | `bg-green-100` | `text-green-800` | `text-green-600` | Approved items/requests |
| **Rejected** | `destructive` | `bg-red-100` | `text-red-800` | `text-red-600` | Rejected items/requests |
| **Draft** | `outline` | `bg-yellow-100` | `text-yellow-800` | `text-yellow-600` | Draft/work-in-progress items |
| **Published** | `default` | `bg-blue-100` | `text-blue-800` | `text-blue-600` | Published content |
| **Archived** | `secondary` | `bg-gray-100` | `text-gray-800` | `text-gray-600` | Archived/historical items |
| **Enabled** | `default` | `bg-green-100` | `text-green-800` | `text-green-600` | Enabled features/settings |
| **Disabled** | `secondary` | `bg-gray-100` | `text-gray-800` | `text-gray-600` | Disabled features/settings |

## Implementation Guide

### Using StatusBadge Component

#### Basic Usage
```tsx
import { StatusBadge } from '@/components/StatusBadge'
import { normalizeEntityStatus } from '@/lib/status-colors'

// Simple status badge
<StatusBadge status={normalizeEntityStatus('user', user.status)} />

// Status badge with icon
<StatusBadgeWithIcon status={normalizeEntityStatus('module', module.isActive ? 'ACTIVE' : 'INACTIVE')} />
```

#### Entity Type Mapping
The `normalizeEntityStatus` function handles different entity types:

```tsx
// User statuses
normalizeEntityStatus('user', 'ACTIVE') // → 'active'
normalizeEntityStatus('user', 'INACTIVE') // → 'inactive'

// Tenant statuses
normalizeEntityStatus('tenant', 'ACTIVE') // → 'active'
normalizeEntityStatus('tenant', 'SUSPENDED') // → 'suspended'

// Module statuses (boolean to status)
normalizeEntityStatus('module', true) // → 'active'
normalizeEntityStatus('module', false) // → 'inactive'

// Product statuses
normalizeEntityStatus('product', 'active') // → 'active'
normalizeEntityStatus('product', 'inactive') // → 'inactive'
```

### Utility Functions

#### Get Status Configuration
```tsx
import { getStatusConfig } from '@/lib/status-colors'

const config = getStatusConfig('active')
// Returns: { variant: 'default', className: '...', iconColor: '...', ... }
```

#### Get Badge Variant
```tsx
import { getStatusBadgeVariant } from '@/lib/status-colors'

const variant = getStatusBadgeVariant('pending') // Returns: 'outline'
```

#### Get Status Colors
```tsx
import { getStatusColors } from '@/lib/status-colors'

const colors = getStatusColors('rejected')
// Returns: { bgColor: 'bg-red-100', textColor: 'text-red-800', iconColor: 'text-red-600' }
```

## Component Integration

### Updated Components

The following components have been updated to use the standardized status system:

1. **ProductDetail.tsx** - Product status badges
2. **ModuleDetail.tsx** - Module active/inactive status
3. **ModuleList.tsx** - Module status in list view
4. **UserList.tsx** - User status indicators
5. **UserEdit.tsx** - User status management
6. **TenantDetail.tsx** - Tenant status display
7. **TenantList.tsx** - Tenant status in list view

### Migration Pattern

When updating existing components:

```tsx
// Before (inconsistent)
<Badge variant={status === 'ACTIVE' ? 'default' : 'secondary'}>
  {status}
</Badge>

// After (standardized)
<StatusBadge 
  status={normalizeEntityStatus('user', status)}
/>
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

## Support

For questions or issues related to the status color system:

1. Check this documentation first
2. Review the implementation in `/src/lib/status-colors.ts`
3. Examine existing usage in updated components
4. Create an issue if problems persist

---

*This documentation is maintained as part of the frontend technical documentation suite.*