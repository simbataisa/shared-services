import React from "react";
import { getStatusIcon, getStatusColor, getPaymentRequestStatusIcon, getPaymentRequestStatusColor } from "@/lib/status-utils";
import type { RoleStatus, ProductStatus } from "@/types";
import type { PaymentRequestStatus } from "@/types/payment";

interface StatusDisplayCardProps {
  title: string;
  description: string;
  status: RoleStatus | ProductStatus | PaymentRequestStatus;
  className?: string;
  actions?: React.ReactNode;
}

const StatusDisplayCard: React.FC<StatusDisplayCardProps> = ({
  title,
  description,
  status,
  className = "",
  actions,
}) => {
  // Determine if this is a PaymentRequestStatus by checking if it's one of the payment status values
  const isPaymentStatus = [
    "DRAFT", "PENDING", "PROCESSING", "COMPLETED", "FAILED", 
    "CANCELLED", "VOIDED", "REFUNDED", "PARTIAL_REFUND", "APPROVED", "REJECTED"
  ].includes(status as string);

  // Use appropriate status functions based on status type
  const statusIcon = isPaymentStatus 
    ? getPaymentRequestStatusIcon(status as PaymentRequestStatus)
    : getStatusIcon(status as string);
  
  const statusColor = isPaymentStatus
    ? getPaymentRequestStatusColor(status as PaymentRequestStatus)
    : getStatusColor(status as string);

  return (
    <div className={`border border-gray-200 rounded-lg p-4 ${className}`}>
      <div className="flex items-center justify-between">
        <div>
          <h4 className="font-medium text-gray-900">{title}</h4>
          <p className="text-sm text-gray-600">{description}</p>
        </div>
        <div className="flex items-center gap-3">
          <div
            className={`flex items-center gap-2 px-3 py-1 rounded-full border text-sm font-medium ${statusColor}`}
          >
            {statusIcon}
            {status}
          </div>
          {actions && <div className="flex items-center gap-2">{actions}</div>}
        </div>
      </div>
    </div>
  );
};

export default StatusDisplayCard;
