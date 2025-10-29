// Payment-related types and interfaces

export type PaymentRequestStatus = 
  | "DRAFT" 
  | "PENDING" 
  | "PROCESSING" 
  | "COMPLETED" 
  | "FAILED" 
  | "CANCELLED" 
  | "VOIDED" 
  | "REFUNDED" 
  | "PARTIAL_REFUND" 
  | "APPROVED" 
  | "REJECTED";
export type PaymentTransactionStatus = "PENDING" | "PROCESSING" | "SUCCESS" | "COMPLETED" | "FAILED" | "CANCELLED";
export type PaymentTransactionType = "PAYMENT" | "REFUND" | "CHARGEBACK" | "ADJUSTMENT";
export type PaymentMethodType = "CREDIT_CARD" | "DEBIT_CARD" | "BANK_TRANSFER" | "DIGITAL_WALLET" | "PAYPAL" | "STRIPE" | "MANUAL";

export interface PaymentRequest {
  id: string;
  requestCode: string;
  paymentToken: string;
  title: string;
  amount: number;
  currency: string;
  payerName: string;
  payerEmail: string;
  payerPhone: string;
  allowedPaymentMethods: PaymentMethodType[];
  preSelectedPaymentMethod: PaymentMethodType;
  status: PaymentRequestStatus;
  expiresAt: Date;
  paidAt: Date;
  tenantId: number;
  metadata: Record<string, any>;
  createdAt: Date;
  updatedAt: Date;
  createdBy: string;
  updatedBy: string;
}

export interface PaymentTransaction {
  id: string;
  transactionCode: string;
  externalTransactionId?: string;
  paymentRequestId: string;
  paymentRequest?: PaymentRequest;
  transactionType: PaymentTransactionType;
  transactionStatus: PaymentTransactionStatus;
  amount: number;
  currency: string;
  paymentMethod: PaymentMethodType;
  paymentMethodDetails?: Record<string, any>;
  gatewayName?: string;
  gatewayResponse?: Record<string, any>;
  processedAt?: string;
  errorCode?: string;
  errorMessage?: string;
  retryCount: number;
  maxRetries: number;
  metadata?: Record<string, any>;
  createdAt: string;
  updatedAt: string;
  createdBy?: string;
  updatedBy?: string;
  // Frontend-specific fields (not in backend DTO)
  gatewayTransactionId?: string;
  refunds?: PaymentRefund[];
}

export interface PaymentRefund {
  id: string;
  refundCode: string;
  paymentTransactionId: string;
  paymentTransaction?: PaymentTransaction;
  refundAmount: number;
  currency: string;
  reason: string;
  refundStatus: PaymentTransactionStatus;
  externalRefundId?: string;
  gatewayName?: string;
  gatewayResponse?: Record<string, any>;
  processedAt?: string;
  errorCode?: string;
  errorMessage?: string;
  metadata?: Record<string, any>;
  createdAt: string;
  updatedAt: string;
  createdBy?: string;
  updatedBy?: string;
}

export interface PaymentAuditLog {
  id: string;
  paymentRequestId?: string;
  paymentTransactionId?: string;
  paymentRefundId?: string;
  action: string;
  oldValues?: Record<string, any>;
  newValues?: Record<string, any>;
  userId?: number;
  username?: string;
  ipAddress?: string;
  userAgent?: string;
  createdAt: string;
}

// Request DTOs
export interface CreatePaymentRequestDto {
  tenantId: number;
  title: string;
  amount: number;
  currency: string;
  payerName: string;
  payerEmail: string;
  payerPhone?: string;
  allowedPaymentMethods: PaymentMethodType[];
  preSelectedPaymentMethod?: PaymentMethodType;
  expiresAt?: Date;
  metadata?: Record<string, any>;
}

export interface ProcessPaymentDto {
  paymentRequestId: string;
  paymentMethod: PaymentMethodType;
  gatewayName?: string;
  metadata?: Record<string, any>;
}

export interface CreateRefundDto {
  paymentTransactionId: string;
  refundAmount: number;
  reason: string;
  metadata?: Record<string, any>;
}

// Statistics interfaces
export interface PaymentStats {
  totalRequests: number;
  pendingRequests: number;
  approvedRequests: number;
  rejectedRequests: number;
  cancelledRequests: number;
  totalTransactions: number;
  pendingTransactions: number;
  successfulTransactions: number;
  failedTransactions: number;
  cancelledTransactions: number;
  totalRefunds: number;
  pendingRefunds: number;
  successfulRefunds: number;
  failedRefunds: number;
  cancelledRefunds: number;
}

// Filter and search interfaces
export interface PaymentRequestFilters {
  status?: PaymentRequestStatus;
  tenantId?: number;
  paymentMethod?: PaymentMethodType;
  dateFrom?: string;
  dateTo?: string;
  amountMin?: number;
  amountMax?: number;
  currency?: string;
  searchTerm?: string;
}

export interface PaymentTransactionFilters {
  status?: PaymentTransactionStatus;
  transactionType?: PaymentTransactionType;
  paymentMethod?: PaymentMethodType;
  gatewayName?: string;
  dateFrom?: string;
  dateTo?: string;
  amountMin?: number;
  amountMax?: number;
  currency?: string;
  searchTerm?: string;
}

export interface PaymentRefundFilters {
  status?: PaymentTransactionStatus;
  dateFrom?: string;
  dateTo?: string;
  amountMin?: number;
  amountMax?: number;
  currency?: string;
  reason?: string;
  searchTerm?: string;
}

// Form validation schemas
export interface PaymentRequestFormData {
  tenantId: string;
  title: string;
  amount: string;
  currency: string;
  payerName: string;
  payerEmail: string;
  payerPhone?: string;
  allowedPaymentMethods: PaymentMethodType[];
  preSelectedPaymentMethod?: PaymentMethodType;
  expiresAt?: string;
}

export interface ProcessPaymentFormData {
  paymentRequestId: string;
  paymentMethod: PaymentMethodType;
  gatewayName?: string;
}

export interface RefundFormData {
  paymentTransactionId: string;
  refundAmount: string;
  reason: string;
}

// Status mappings for display
export const PAYMENT_REQUEST_STATUS_MAPPINGS = {
  DRAFT: "Draft",
  PENDING: "Pending",
  PROCESSING: "Processing",
  COMPLETED: "Completed",
  FAILED: "Failed",
  CANCELLED: "Cancelled",
  VOIDED: "Voided",
  REFUNDED: "Refunded",
  PARTIAL_REFUND: "Partial Refund",
  APPROVED: "Approved", 
  REJECTED: "Rejected"
} as const;

export const PAYMENT_TRANSACTION_STATUS_MAPPINGS = {
  PENDING: "Pending",
  PROCESSING: "Processing",
  SUCCESS: "Success",
  COMPLETED: "Completed",
  FAILED: "Failed",
  CANCELLED: "Cancelled"
} as const;

export const PAYMENT_METHOD_TYPE_MAPPINGS = {
  CREDIT_CARD: "Credit Card",
  DEBIT_CARD: "Debit Card",
  BANK_TRANSFER: "Bank Transfer",
  DIGITAL_WALLET: "Digital Wallet",
  PAYPAL: "PayPal",
  STRIPE: "Stripe",
  MANUAL: "Manual Payment"
} as const;

export const PAYMENT_TRANSACTION_TYPE_MAPPINGS = {
  PAYMENT: "Payment",
  REFUND: "Refund",
  CHARGEBACK: "Chargeback",
  ADJUSTMENT: "Adjustment"
} as const;

// Status filter options for UI components
export const PAYMENT_REQUEST_STATUS_OPTIONS = [
  { value: "all", label: "All Statuses" },
  { value: "DRAFT", label: "Draft" },
  { value: "PENDING", label: "Pending" },
  { value: "PROCESSING", label: "Processing" },
  { value: "COMPLETED", label: "Completed" },
  { value: "FAILED", label: "Failed" },
  { value: "CANCELLED", label: "Cancelled" },
  { value: "VOIDED", label: "Voided" },
  { value: "REFUNDED", label: "Refunded" },
  { value: "PARTIAL_REFUND", label: "Partial Refund" },
  { value: "APPROVED", label: "Approved" },
  { value: "REJECTED", label: "Rejected" },
] as const;