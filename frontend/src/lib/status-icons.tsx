import React from 'react'
import { CheckCircle, XCircle, Clock } from "lucide-react"

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
export function getStatusIconWithSize(status: string, size: string = "h-4 w-4") {
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