// Payment-related types and interfaces

export type PaymentRequestStatus = "PENDING" | "APPROVED" | "REJECTED" | "CANCELLED";
export type PaymentTransactionStatus = "PENDING" | "PROCESSING" | "COMPLETED" | "FAILED" | "CANCELLED";
export type PaymentTransactionType = "PAYMENT" | "REFUND" | "CHARGEBACK" | "ADJUSTMENT";
export type PaymentMethodType = "CREDIT_CARD" | "DEBIT_CARD" | "BANK_TRANSFER" | "DIGITAL_WALLET" | "CASH" | "CHECK";

export interface PaymentRequest {
  id: string;
  requestCode: string;
  tenantId: number;
  tenantName?: string;
  amount: number;
  currency: string;
  description: string;
  requestorName: string;
  requestorEmail: string;
  paymentMethod: PaymentMethodType;
  status: PaymentRequestStatus;
  dueDate?: string;
  approvedBy?: string;
  approvedAt?: string;
  rejectedBy?: string;
  rejectedAt?: string;
  rejectionReason?: string;
  metadata?: Record<string, any>;
  createdAt: string;
  updatedAt: string;
  createdBy?: string;
  updatedBy?: string;
  transactions?: PaymentTransaction[];
}

export interface PaymentTransaction {
  id: number;
  transactionCode: string;
  paymentRequestId: string;
  paymentRequest?: PaymentRequest;
  amount: number;
  currency: string;
  transactionType: PaymentTransactionType;
  paymentMethod: PaymentMethodType;
  status: PaymentTransactionStatus;
  gatewayName?: string;
  gatewayTransactionId?: string;
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
  refunds?: PaymentRefund[];
}

export interface PaymentRefund {
  id: number;
  refundCode: string;
  paymentTransactionId: number;
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
  id: number;
  paymentRequestId?: string;
  paymentTransactionId?: number;
  paymentRefundId?: number;
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
  amount: number;
  currency: string;
  description: string;
  requestorName: string;
  requestorEmail: string;
  paymentMethod: PaymentMethodType;
  dueDate?: string;
  metadata?: Record<string, any>;
}

export interface ProcessPaymentDto {
  paymentRequestId: string;
  paymentMethod: PaymentMethodType;
  gatewayName?: string;
  metadata?: Record<string, any>;
}

export interface CreateRefundDto {
  paymentTransactionId: number;
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
  processingTransactions: number;
  completedTransactions: number;
  failedTransactions: number;
  cancelledTransactions: number;
  totalRefunds: number;
  pendingRefunds: number;
  processingRefunds: number;
  completedRefunds: number;
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
  amount: string;
  currency: string;
  description: string;
  requestorName: string;
  requestorEmail: string;
  paymentMethod: PaymentMethodType;
  dueDate?: string;
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
  PENDING: "Pending",
  APPROVED: "Approved", 
  REJECTED: "Rejected",
  CANCELLED: "Cancelled"
} as const;

export const PAYMENT_TRANSACTION_STATUS_MAPPINGS = {
  PENDING: "Pending",
  PROCESSING: "Processing",
  COMPLETED: "Completed",
  FAILED: "Failed",
  CANCELLED: "Cancelled"
} as const;

export const PAYMENT_METHOD_TYPE_MAPPINGS = {
  CREDIT_CARD: "Credit Card",
  DEBIT_CARD: "Debit Card",
  BANK_TRANSFER: "Bank Transfer",
  DIGITAL_WALLET: "Digital Wallet",
  CASH: "Cash",
  CHECK: "Check"
} as const;

export const PAYMENT_TRANSACTION_TYPE_MAPPINGS = {
  PAYMENT: "Payment",
  REFUND: "Refund",
  CHARGEBACK: "Chargeback",
  ADJUSTMENT: "Adjustment"
} as const;