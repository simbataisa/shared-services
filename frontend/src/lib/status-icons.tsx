import { CheckCircle, XCircle, Clock } from "lucide-react";

export type StatusType = "ACTIVE" | "INACTIVE" | "SUSPENDED";

/**
 * Status Icon Utilities
 *
 * This module provides standardized status icons for consistent
 * visual representation across the application.
 */

/**
 * Get status icon component based on status string
 * @param status - The status string (ACTIVE, INACTIVE, SUSPENDED, etc.)
 * @returns JSX element with appropriate icon and styling
 */
export function getStatusIcon(status: string) {
  switch (status) {
    case "ACTIVE":
      return <CheckCircle className="h-4 w-4 text-green-600" />;
    case "INACTIVE":
      return <XCircle className="h-4 w-4 text-red-600" />;
    case "SUSPENDED":
      return <Clock className="h-4 w-4 text-yellow-600" />;
    default:
      return <XCircle className="h-4 w-4 text-gray-600" />;
  }
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
  switch (status) {
    case "ACTIVE":
      return <CheckCircle className={`${size} text-green-600`} />;
    case "INACTIVE":
      return <XCircle className={`${size} text-red-600`} />;
    case "SUSPENDED":
      return <Clock className={`${size} text-yellow-600`} />;
    default:
      return <XCircle className={`${size} text-gray-600`} />;
  }
}

/**
 * Get status color classes for styling
 * @param status - The status type (ACTIVE, INACTIVE, SUSPENDED)
 * @returns String with Tailwind CSS classes for text, background, and border colors
 */
export const getStatusColor = (status: string) => {
  switch (status) {
    case "ACTIVE":
      return "text-green-600 bg-green-50 border-green-200";
    case "INACTIVE":
      return "text-gray-600 bg-gray-50 border-gray-200";
    case "SUSPENDED":
      return "text-red-600 bg-red-50 border-red-200";
    default:
      return "text-gray-600 bg-gray-50 border-gray-200";
  }
};

/**
 * Map role status to StatusDisplayCard status type
 * @param roleStatus - The role status string (ACTIVE, INACTIVE, DRAFT, DEPRECATED, etc.)
 * @returns StatusType that can be used with status display components
 */
export const mapRoleStatusToStatusType = (roleStatus: string): StatusType => {
  switch (roleStatus) {
    case "ACTIVE":
      return "ACTIVE";
    case "INACTIVE":
      return "INACTIVE";
    case "DRAFT":
      return "SUSPENDED"; // Map DRAFT to SUSPENDED as closest match
    case "DEPRECATED":
      return "INACTIVE";
    default:
      return "INACTIVE";
  }
};
