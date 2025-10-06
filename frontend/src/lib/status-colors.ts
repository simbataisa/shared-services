/**
 * Centralized Status Color Configuration
 * 
 * This module provides a standardized color system for all status indicators
 * across the application, ensuring consistent visual representation.
 */

import { type VariantProps } from "class-variance-authority"
import { badgeVariants } from "@/components/ui/badge"

// Standard status types used across the application
export type StatusType = 
  | 'active' 
  | 'inactive' 
  | 'pending' 
  | 'suspended' 
  | 'success' 
  | 'error' 
  | 'warning' 
  | 'info'
  | 'draft'
  | 'published'
  | 'archived'

// Badge variant mapping for each status
export type BadgeVariant = VariantProps<typeof badgeVariants>['variant']

/**
 * Status color configuration with Badge variants and custom classes
 */
export const STATUS_CONFIG = {
  active: {
    variant: 'default' as BadgeVariant,
    className: 'bg-green-100 text-green-800 hover:bg-green-100 border-green-200',
    iconColor: 'text-green-600',
    bgColor: 'bg-green-100',
    textColor: 'text-green-800',
    label: 'Active'
  },
  inactive: {
    variant: 'secondary' as BadgeVariant,
    className: 'bg-gray-100 text-gray-800 hover:bg-gray-100 border-gray-200',
    iconColor: 'text-gray-600',
    bgColor: 'bg-gray-100',
    textColor: 'text-gray-800',
    label: 'Inactive'
  },
  pending: {
    variant: 'outline' as BadgeVariant,
    className: 'bg-yellow-100 text-yellow-800 hover:bg-yellow-100 border-yellow-200',
    iconColor: 'text-yellow-600',
    bgColor: 'bg-yellow-100',
    textColor: 'text-yellow-800',
    label: 'Pending'
  },
  suspended: {
    variant: 'destructive' as BadgeVariant,
    className: 'bg-orange-100 text-orange-800 hover:bg-orange-100 border-orange-200',
    iconColor: 'text-orange-600',
    bgColor: 'bg-orange-100',
    textColor: 'text-orange-800',
    label: 'Suspended'
  },
  success: {
    variant: 'default' as BadgeVariant,
    className: 'bg-green-100 text-green-800 hover:bg-green-100 border-green-200',
    iconColor: 'text-green-600',
    bgColor: 'bg-green-100',
    textColor: 'text-green-800',
    label: 'Success'
  },
  error: {
    variant: 'destructive' as BadgeVariant,
    className: 'bg-red-100 text-red-800 hover:bg-red-100 border-red-200',
    iconColor: 'text-red-600',
    bgColor: 'bg-red-100',
    textColor: 'text-red-800',
    label: 'Error'
  },
  warning: {
    variant: 'outline' as BadgeVariant,
    className: 'bg-yellow-100 text-yellow-800 hover:bg-yellow-100 border-yellow-200',
    iconColor: 'text-yellow-600',
    bgColor: 'bg-yellow-100',
    textColor: 'text-yellow-800',
    label: 'Warning'
  },
  info: {
    variant: 'outline' as BadgeVariant,
    className: 'bg-blue-100 text-blue-800 hover:bg-blue-100 border-blue-200',
    iconColor: 'text-blue-600',
    bgColor: 'bg-blue-100',
    textColor: 'text-blue-800',
    label: 'Info'
  },
  draft: {
    variant: 'secondary' as BadgeVariant,
    className: 'bg-gray-100 text-gray-800 hover:bg-gray-100 border-gray-200',
    iconColor: 'text-gray-600',
    bgColor: 'bg-gray-100',
    textColor: 'text-gray-800',
    label: 'Draft'
  },
  published: {
    variant: 'default' as BadgeVariant,
    className: 'bg-green-100 text-green-800 hover:bg-green-100 border-green-200',
    iconColor: 'text-green-600',
    bgColor: 'bg-green-100',
    textColor: 'text-green-800',
    label: 'Published'
  },
  archived: {
    variant: 'secondary' as BadgeVariant,
    className: 'bg-gray-100 text-gray-800 hover:bg-gray-100 border-gray-200',
    iconColor: 'text-gray-600',
    bgColor: 'bg-gray-100',
    textColor: 'text-gray-800',
    label: 'Archived'
  }
} as const

/**
 * Utility function to get status configuration
 */
export function getStatusConfig(status: string): typeof STATUS_CONFIG[StatusType] {
  const normalizedStatus = status.toLowerCase() as StatusType
  return STATUS_CONFIG[normalizedStatus] || STATUS_CONFIG.inactive
}

/**
 * Utility function to get Badge variant for a status
 */
export function getStatusBadgeVariant(status: string): BadgeVariant {
  return getStatusConfig(status).variant
}

/**
 * Utility function to get custom Badge className for a status
 */
export function getStatusBadgeClassName(status: string): string {
  return getStatusConfig(status).className
}

/**
 * Utility function to get icon color class for a status
 */
export function getStatusIconColor(status: string): string {
  return getStatusConfig(status).iconColor
}

/**
 * Utility function to get background color class for a status
 */
export function getStatusBgColor(status: string): string {
  return getStatusConfig(status).bgColor
}

/**
 * Utility function to get text color class for a status
 */
export function getStatusTextColor(status: string): string {
  return getStatusConfig(status).textColor
}

/**
 * Utility function to get display label for a status
 */
export function getStatusLabel(status: string): string {
  return getStatusConfig(status).label
}

/**
 * Common status mappings for different entities
 */
export const ENTITY_STATUS_MAPPINGS = {
  user: {
    ACTIVE: 'active',
    INACTIVE: 'inactive',
    SUSPENDED: 'suspended',
    PENDING: 'pending'
  },
  tenant: {
    ACTIVE: 'active',
    INACTIVE: 'inactive',
    SUSPENDED: 'suspended',
    PENDING: 'pending'
  },
  product: {
    ACTIVE: 'active',
    INACTIVE: 'inactive',
    DRAFT: 'draft',
    PUBLISHED: 'published',
    ARCHIVED: 'archived'
  },
  module: {
    ACTIVE: 'active',
    INACTIVE: 'inactive',
    DRAFT: 'draft',
    PUBLISHED: 'published'
  },
  role: {
    ACTIVE: 'active',
    INACTIVE: 'inactive'
  }
} as const

/**
 * Utility function to normalize entity status to standard status type
 */
export function normalizeEntityStatus(entityType: keyof typeof ENTITY_STATUS_MAPPINGS, status: string): StatusType {
  const mapping = ENTITY_STATUS_MAPPINGS[entityType]
  const normalizedStatus = mapping[status as keyof typeof mapping]
  return normalizedStatus || 'inactive'
}