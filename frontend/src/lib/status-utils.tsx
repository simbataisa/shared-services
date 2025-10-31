/**
 * Unified Status Utilities
 *
 * This module provides a comprehensive status system that combines icons, colors,
 * and styling utilities for consistent visual representation across the application.
 */

import {
  CheckCircle,
  XCircle,
  Clock,
  AlertCircle,
  Info,
  CheckCircle2,
  Archive,
  Users,
  Shield,
  Building2,
  Activity,
  FileText,
  Loader,
  Ban,
  RotateCcw,
  DollarSign,
  Trash2,
} from "lucide-react";
import { type VariantProps } from "class-variance-authority";
import { badgeVariants } from "@/components/ui/badge";
import { type PaymentRequestStatus } from "@/types/payment";

// Comprehensive status types used across the application
export type StatusType =
  | "active"
  | "inactive"
  | "pending"
  | "suspended"
  | "success"
  | "error"
  | "warning"
  | "info"
  | "draft"
  | "published"
  | "archived"
  | "healthy"
  | "critical";

// Legacy status types for backward compatibility
export type LegacyStatusType = "ACTIVE" | "INACTIVE" | "SUSPENDED";

// Badge variant mapping for each status
export type BadgeVariant = VariantProps<typeof badgeVariants>["variant"];

/**
 * Comprehensive status configuration with icons, colors, and variants
 */
export const STATUS_CONFIG = {
  active: {
    variant: "default" as BadgeVariant,
    className:
      "bg-green-100 text-green-800 hover:bg-green-100 border-green-200",
    iconColor: "text-green-600",
    bgColor: "bg-green-100",
    textColor: "text-green-800",
    label: "ACTIVE",
    icon: CheckCircle,
    legacyColorClasses: "text-green-600 bg-green-50 border-green-200",
  },
  inactive: {
    variant: "secondary" as BadgeVariant,
    className: "bg-gray-100 text-gray-800 hover:bg-gray-100 border-gray-200",
    iconColor: "text-gray-600",
    bgColor: "bg-gray-100",
    textColor: "text-gray-800",
    label: "INACTIVE",
    icon: XCircle,
    legacyColorClasses: "text-gray-600 bg-gray-50 border-gray-200",
  },
  pending: {
    variant: "outline" as BadgeVariant,
    className:
      "bg-yellow-100 text-yellow-800 hover:bg-yellow-100 border-yellow-200",
    iconColor: "text-yellow-600",
    bgColor: "bg-yellow-100",
    textColor: "text-yellow-800",
    label: "PENDING",
    icon: Clock,
    legacyColorClasses: "text-yellow-600 bg-yellow-50 border-yellow-200",
  },
  suspended: {
    variant: "destructive" as BadgeVariant,
    className:
      "bg-orange-100 text-orange-800 hover:bg-orange-100 border-orange-200",
    iconColor: "text-orange-600",
    bgColor: "bg-orange-100",
    textColor: "text-orange-800",
    label: "Suspended",
    icon: Clock,
    legacyColorClasses: "text-red-600 bg-red-50 border-red-200",
  },
  success: {
    variant: "default" as BadgeVariant,
    className:
      "bg-green-100 text-green-800 hover:bg-green-100 border-green-200",
    iconColor: "text-green-600",
    bgColor: "bg-green-100",
    textColor: "text-green-800",
    label: "Success",
    icon: CheckCircle2,
    legacyColorClasses: "text-green-600 bg-green-50 border-green-200",
  },
  error: {
    variant: "destructive" as BadgeVariant,
    className: "bg-red-100 text-red-800 hover:bg-red-100 border-red-200",
    iconColor: "text-red-600",
    bgColor: "bg-red-100",
    textColor: "text-red-800",
    label: "Error",
    icon: XCircle,
    legacyColorClasses: "text-red-600 bg-red-50 border-red-200",
  },
  warning: {
    variant: "outline" as BadgeVariant,
    className:
      "bg-yellow-100 text-yellow-800 hover:bg-yellow-100 border-yellow-200",
    iconColor: "text-yellow-600",
    bgColor: "bg-yellow-100",
    textColor: "text-yellow-800",
    label: "Warning",
    icon: AlertCircle,
    legacyColorClasses: "text-yellow-600 bg-yellow-50 border-yellow-200",
  },
  info: {
    variant: "outline" as BadgeVariant,
    className: "bg-blue-100 text-blue-800 hover:bg-blue-100 border-blue-200",
    iconColor: "text-blue-600",
    bgColor: "bg-blue-100",
    textColor: "text-blue-800",
    label: "Info",
    icon: Info,
    legacyColorClasses: "text-blue-600 bg-blue-50 border-blue-200",
  },
  draft: {
    variant: "secondary" as BadgeVariant,
    className: "bg-gray-100 text-gray-800 hover:bg-gray-100 border-gray-200",
    iconColor: "text-gray-600",
    bgColor: "bg-gray-100",
    textColor: "text-gray-800",
    label: "Draft",
    icon: Clock,
    legacyColorClasses: "text-gray-600 bg-gray-50 border-gray-200",
  },
  published: {
    variant: "default" as BadgeVariant,
    className:
      "bg-green-100 text-green-800 hover:bg-green-100 border-green-200",
    iconColor: "text-green-600",
    bgColor: "bg-green-100",
    textColor: "text-green-800",
    label: "Published",
    icon: CheckCircle2,
    legacyColorClasses: "text-green-600 bg-green-50 border-green-200",
  },
  archived: {
    variant: "secondary" as BadgeVariant,
    className: "bg-gray-100 text-gray-800 hover:bg-gray-100 border-gray-200",
    iconColor: "text-gray-600",
    bgColor: "bg-gray-100",
    textColor: "text-gray-800",
    label: "Archived",
    icon: Archive,
    legacyColorClasses: "text-gray-600 bg-gray-50 border-gray-200",
  },
  healthy: {
    variant: "default" as BadgeVariant,
    className:
      "bg-green-100 text-green-800 hover:bg-green-100 border-green-200",
    iconColor: "text-green-600",
    bgColor: "bg-green-100",
    textColor: "text-green-800",
    label: "Healthy",
    icon: CheckCircle,
    legacyColorClasses: "text-green-600 bg-green-50 border-green-200",
  },
  critical: {
    variant: "destructive" as BadgeVariant,
    className: "bg-red-100 text-red-800 hover:bg-red-100 border-red-200",
    iconColor: "text-red-600",
    bgColor: "bg-red-100",
    textColor: "text-red-800",
    label: "Critical",
    icon: AlertCircle,
    legacyColorClasses: "text-red-600 bg-red-50 border-red-200",
  },
} as const;

/**
 * Common status mappings for different entities
 */
export const ENTITY_STATUS_MAPPINGS = {
  user: {
    ACTIVE: "active",
    INACTIVE: "inactive",
    SUSPENDED: "suspended",
    PENDING: "pending",
  },
  tenant: {
    ACTIVE: "active",
    INACTIVE: "inactive",
    SUSPENDED: "suspended",
    PENDING: "pending",
  },
  product: {
    ACTIVE: "active",
    INACTIVE: "inactive",
    DRAFT: "draft",
    PUBLISHED: "published",
    ARCHIVED: "archived",
  },
  module: {
    ACTIVE: "active",
    INACTIVE: "inactive",
    DRAFT: "draft",
    PUBLISHED: "published",
  },
  role: {
    ACTIVE: "active",
    INACTIVE: "inactive",
    DRAFT: "draft",
    DEPRECATED: "archived",
  },
} as const;

// =============================================================================
// ACTIVITY UTILITIES
// =============================================================================

/**
 * Get activity icon component based on activity type
 * @param type - The activity type string (user_login, role_assigned, tenant_created, etc.)
 * @returns JSX element with appropriate icon
 */
export function getActivityIcon(type: string) {
  switch (type) {
    case "user_login":
      return <Users className="h-4 w-4" />;
    case "role_assigned":
      return <Shield className="h-4 w-4" />;
    case "tenant_created":
      return <Building2 className="h-4 w-4" />;
    case "permission_granted":
      return <CheckCircle className="h-4 w-4" />;
    default:
      return <Activity className="h-4 w-4" />;
  }
}

// =============================================================================
// ICON UTILITIES
// =============================================================================

/**
 * Get status icon component based on status string
 * @param status - The status string (ACTIVE, INACTIVE, SUSPENDED, etc.)
 * @returns JSX element with appropriate icon and styling
 */
export function getStatusIcon(status: string) {
  const normalizedStatus = normalizeStatus(status);
  const config = STATUS_CONFIG[normalizedStatus] || STATUS_CONFIG.inactive;
  const IconComponent = config.icon;
  return <IconComponent className={`h-4 w-4 ${config.iconColor}`} />;
}

/**
 * Get status icon with custom size
 * @param status - The status string (ACTIVE, INACTIVE, SUSPENDED, etc.)
 * @param size - Size class for the icon (default: "h-4 w-4")
 * @returns JSX element with appropriate icon and styling
 */
export function getStatusIconWithSize(
  status: string,
  size: string = "h-4 w-4"
) {
  const normalizedStatus = normalizeStatus(status);
  const config = STATUS_CONFIG[normalizedStatus] || STATUS_CONFIG.inactive;
  const IconComponent = config.icon;

  return <IconComponent className={`${size} ${config.iconColor}`} />;
}

// =============================================================================
// COLOR UTILITIES
// =============================================================================

/**
 * Utility function to get status configuration
 */
export function getStatusConfig(
  status: string
): (typeof STATUS_CONFIG)[StatusType] {
  const normalizedStatus = normalizeStatus(status);
  return STATUS_CONFIG[normalizedStatus] || STATUS_CONFIG.inactive;
}

/**
 * Utility function to get Badge variant for a status
 */
export function getStatusBadgeVariant(status: string): BadgeVariant {
  return getStatusConfig(status).variant;
}

/**
 * Utility function to get custom Badge className for a status
 */
export function getStatusBadgeClassName(status: string): string {
  return getStatusConfig(status).className;
}

/**
 * Utility function to get icon color class for a status
 */
export function getStatusIconColor(status: string): string {
  return getStatusConfig(status).iconColor;
}

/**
 * Utility function to get background color class for a status
 */
export function getStatusBgColor(status: string): string {
  return getStatusConfig(status).bgColor;
}

/**
 * Utility function to get text color class for a status
 */
export function getStatusTextColor(status: string): string {
  return getStatusConfig(status).textColor;
}

/**
 * Utility function to get status label for display
 */
export function getStatusLabel(status: string): string {
  return getStatusConfig(status).label;
}

/**
 * Get status color classes for styling (legacy compatibility)
 * @param status - The status type (ACTIVE, INACTIVE, SUSPENDED)
 * @returns String with Tailwind CSS classes for text, background, and border colors
 */
export const getStatusColor = (status: string): string => {
  const normalizedStatus = normalizeStatus(status);
  const config = STATUS_CONFIG[normalizedStatus] || STATUS_CONFIG.inactive;
  return config.legacyColorClasses;
};

/**
 * Legacy utility function to get Badge variant directly from raw status string
 * @deprecated Use getStatusBadgeVariant with normalizeEntityStatus instead
 */
export function getStatusVariant(status: string): BadgeVariant {
  switch (status) {
    case "ACTIVE":
      return "default";
    case "INACTIVE":
      return "destructive";
    case "SUSPENDED":
      return "secondary";
    default:
      return "outline";
  }
}

// =============================================================================
// MAPPING AND NORMALIZATION UTILITIES
// =============================================================================

/**
 * Utility function to get tenant type label for display
 * @param type - The tenant type string (BUSINESS_IN, BUSINESS_OUT, INDIVIDUAL)
 * @returns Human-readable label for the tenant type
 */
export function getTenantTypeLabel(type: string): string {
  switch (type) {
    case "BUSINESS_IN":
      return "Business Internal";
    case "BUSINESS_OUT":
      return "Business External";
    case "INDIVIDUAL":
      return "Individual";
    default:
      return type;
  }
}

/**
 * Utility function to normalize entity status to standard status type
 */
export function normalizeEntityStatus(
  entityType: keyof typeof ENTITY_STATUS_MAPPINGS,
  status: string
): StatusType {
  const mapping = ENTITY_STATUS_MAPPINGS[entityType];
  const normalizedStatus = mapping[status as keyof typeof mapping];
  return normalizedStatus || "inactive";
}

/**
 * Map role status to StatusDisplayCard status type (legacy compatibility)
 * @param roleStatus - The role status string (ACTIVE, INACTIVE, DRAFT, DEPRECATED, etc.)
 * @returns StatusType that can be used with status display components
 */
export const mapRoleStatusToStatusType = (
  roleStatus: string
): LegacyStatusType => {
  switch (roleStatus) {
    case "ACTIVE":
      return "ACTIVE";
    case "INACTIVE":
      return "INACTIVE";
    case "DRAFT":
      return "SUSPENDED"; // Map DRAFT to SUSPENDED as closest match
    case "DEPRECATED":
      return "INACTIVE"; // Map DEPRECATED to INACTIVE
    default:
      return "INACTIVE";
  }
};

/**
 * Internal utility to normalize status strings to StatusType
 */
function normalizeStatus(status: string): StatusType {
  const upperStatus = status.toUpperCase();

  // Handle legacy uppercase statuses
  switch (upperStatus) {
    case "ACTIVE":
      return "active";
    case "INACTIVE":
      return "inactive";
    case "SUSPENDED":
      return "suspended";
    case "PENDING":
      return "pending";
    case "DRAFT":
      return "draft";
    case "PUBLISHED":
      return "published";
    case "ARCHIVED":
      return "archived";
    case "SUCCESS":
      return "success";
    case "ERROR":
      return "error";
    case "WARNING":
      return "warning";
    case "INFO":
      return "info";
    case "HEALTHY":
      return "healthy";
    case "CRITICAL":
      return "critical";
    default:
      // Try lowercase
      const lowerStatus = status.toLowerCase() as StatusType;
      return STATUS_CONFIG[lowerStatus] ? lowerStatus : "inactive";
  }
}

/**
 * Get payment request status color classes for styling
 * @param status - The PaymentRequestStatus type
 * @returns String with Tailwind CSS classes for background, text, hover, and border colors
 */
export const getPaymentRequestStatusColor = (
  status: PaymentRequestStatus
): string => {
  switch (status) {
    case "DRAFT":
      return "bg-gray-100 text-gray-800 hover:bg-gray-100 border-gray-200";
    case "PENDING":
      return "bg-yellow-100 text-yellow-800 hover:bg-yellow-100 border-yellow-200";
    case "PROCESSING":
      return "bg-blue-100 text-blue-800 hover:bg-blue-100 border-blue-200";
    case "COMPLETED":
      return "bg-green-100 text-green-800 hover:bg-green-100 border-green-200";
    case "FAILED":
      return "bg-red-100 text-red-800 hover:bg-red-100 border-red-200";
    case "CANCELLED":
      return "bg-gray-100 text-gray-800 hover:bg-gray-100 border-gray-200";
    case "VOIDED":
      return "bg-purple-100 text-purple-800 hover:bg-purple-100 border-purple-200";
    case "REFUNDED":
      return "bg-orange-100 text-orange-800 hover:bg-orange-100 border-orange-200";
    case "PARTIAL_REFUND":
      return "bg-orange-100 text-orange-800 hover:bg-orange-100 border-orange-200";
    case "APPROVED":
      return "bg-green-100 text-green-800 hover:bg-green-100 border-green-200";
    case "REJECTED":
      return "bg-red-100 text-red-800 hover:bg-red-100 border-red-200";
    default:
      return "bg-gray-100 text-gray-800 hover:bg-gray-100 border-gray-200";
  }
};

/**
 * Get payment request status icon component based on status
 * @param status - The PaymentRequestStatus type
 * @returns JSX element with appropriate icon and styling
 */
export const getPaymentRequestStatusIcon = (status: PaymentRequestStatus) => {
  const getIconColor = (status: PaymentRequestStatus): string => {
    switch (status) {
      case "DRAFT":
        return "text-gray-600";
      case "PENDING":
        return "text-yellow-600";
      case "PROCESSING":
        return "text-blue-600";
      case "COMPLETED":
        return "text-green-600";
      case "FAILED":
        return "text-red-600";
      case "CANCELLED":
        return "text-gray-600";
      case "VOIDED":
        return "text-purple-600";
      case "REFUNDED":
        return "text-orange-600";
      case "PARTIAL_REFUND":
        return "text-orange-600";
      case "APPROVED":
        return "text-green-600";
      case "REJECTED":
        return "text-red-600";
      default:
        return "text-gray-600";
    }
  };

  const iconColor = getIconColor(status);

  switch (status) {
    case "DRAFT":
      return <FileText className={`h-4 w-4 ${iconColor}`} />;
    case "PENDING":
      return <Clock className={`h-4 w-4 ${iconColor}`} />;
    case "PROCESSING":
      return <Loader className={`h-4 w-4 ${iconColor}`} />;
    case "COMPLETED":
      return <CheckCircle className={`h-4 w-4 ${iconColor}`} />;
    case "FAILED":
      return <XCircle className={`h-4 w-4 ${iconColor}`} />;
    case "CANCELLED":
      return <Ban className={`h-4 w-4 ${iconColor}`} />;
    case "VOIDED":
      return <Trash2 className={`h-4 w-4 ${iconColor}`} />;
    case "REFUNDED":
      return <RotateCcw className={`h-4 w-4 ${iconColor}`} />;
    case "PARTIAL_REFUND":
      return <RotateCcw className={`h-4 w-4 ${iconColor}`} />;
    case "APPROVED":
      return <CheckCircle2 className={`h-4 w-4 ${iconColor}`} />;
    case "REJECTED":
      return <XCircle className={`h-4 w-4 ${iconColor}`} />;
    default:
      return <AlertCircle className={`h-4 w-4 ${iconColor}`} />;
  }
};

/**
 * Get badge properties for payment transaction status
 * Returns variant and className for consistent transaction status styling
 */
export const getTransactionStatusBadgeProps = (status: string) => {
  switch (status) {
    case "SUCCESS":
    case "COMPLETED":
      return {
        variant: "default" as const,
        className: "bg-green-100 text-green-800 hover:bg-green-200",
      };
    case "PENDING":
      return {
        variant: "secondary" as const,
        className: "bg-yellow-100 text-yellow-800 hover:bg-yellow-200",
      };
    case "PROCESSING":
      return {
        variant: "outline" as const,
        className: "bg-blue-100 text-blue-800 hover:bg-blue-200",
      };
    case "FAILED":
      return {
        variant: "destructive" as const,
        className: "bg-red-100 text-red-800 hover:bg-red-200",
      };
    case "CANCELLED":
      return {
        variant: "secondary" as const,
        className: "bg-gray-100 text-gray-800 hover:bg-gray-200",
      };
    default:
      return {
        variant: "secondary" as const,
        className: "bg-gray-100 text-gray-800 hover:bg-gray-200",
      };
  }
};
