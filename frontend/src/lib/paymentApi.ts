import api from "./api";
import type {
  PaymentRequest,
  PaymentTransaction,
  PaymentRefund,
  PaymentAuditLog,
  CreatePaymentRequestDto,
  ProcessPaymentDto,
  CreateRefundDto,
  PaymentRequestStatus,
  PaymentTransactionStatus} from "@/types/payment";
import type { ApiResponse } from "@/types/api";

// Payment Requests API
export const paymentRequestApi = {
  // Get all payment requests with pagination
  getAll: async (page = 0, size = 10) => {
    const response = await api.get<ApiResponse<{ content: PaymentRequest[]; totalElements: number; totalPages: number }>>(`/payments/requests?page=${page}&size=${size}`);
    return response.data;
  },

  // Get payment request by ID
  getById: async (id: string) => {
    const response = await api.get<ApiResponse<PaymentRequest>>(`/payments/requests/${id}`);
    return response.data;
  },

  // Get payment request by code
  getByCode: async (code: string) => {
    const response = await api.get<ApiResponse<PaymentRequest>>(`/payments/requests/code/${code}`);
    return response.data;
  },

  // Get payment requests by tenant
  getByTenant: async (tenantId: number, page = 0, size = 10) => {
    const response = await api.get<ApiResponse<{ content: PaymentRequest[]; totalElements: number; totalPages: number }>>(`/payments/requests/tenant/${tenantId}?page=${page}&size=${size}`);
    return response.data;
  },

  // Get payment requests by status
  getByStatus: async (status: PaymentRequestStatus, page = 0, size = 10) => {
    const response = await api.get<ApiResponse<{ content: PaymentRequest[]; totalElements: number; totalPages: number }>>(`/payments/requests/status/${status}?page=${page}&size=${size}`);
    return response.data;
  },

  // Create new payment request
  create: async (data: CreatePaymentRequestDto) => {
    const response = await api.post<ApiResponse<PaymentRequest>>("/payments/requests", data);
    return response.data;
  },

  // Update payment request
  update: async (id: string, data: CreatePaymentRequestDto) => {
    const response = await api.put<ApiResponse<PaymentRequest>>(`/payments/requests/${id}`, data);
    return response.data;
  },

  // Cancel payment request
  cancel: async (id: string) => {
    const response = await api.patch<ApiResponse<void>>(`/payments/requests/${id}/cancel`);
    return response.data;
  },

  // Approve payment request
  approve: async (id: string) => {
    const response = await api.patch<ApiResponse<void>>(`/payments/requests/${id}/approve`);
    return response.data;
  },

  // Reject payment request
  reject: async (id: string) => {
    const response = await api.patch<ApiResponse<void>>(`/payments/requests/${id}/reject`);
    return response.data;
  }
};

// Payment Transactions API
export const paymentTransactionApi = {
  // Get all transactions with pagination
  getAll: async (page = 0, size = 10) => {
    const response = await api.get<ApiResponse<{ content: PaymentTransaction[]; totalElements: number; totalPages: number }>>(`/payments/transactions?page=${page}&size=${size}`);
    return response.data;
  },

  // Get transaction by ID
  getById: async (id: string) => {
    const response = await api.get<ApiResponse<PaymentTransaction>>(`/payments/transactions/${id}`);
    return response.data;
  },

  // Get transaction by code
  getByCode: async (code: string) => {
    const response = await api.get<ApiResponse<PaymentTransaction>>(`/payments/transactions/code/${code}`);
    return response.data;
  },

  // Get transactions by request
  getByRequest: async (requestId: string, page = 0, size = 10) => {
    const response = await api.get<ApiResponse<{ content: PaymentTransaction[]; totalElements: number; totalPages: number }>>(`/payments/transactions/request/${requestId}?page=${page}&size=${size}`);
    return response.data;
  },

  // Get transactions by status
  getByStatus: async (status: PaymentTransactionStatus, page = 0, size = 10) => {
    const response = await api.get<ApiResponse<{ content: PaymentTransaction[]; totalElements: number; totalPages: number }>>(`/payments/transactions/status/${status}?page=${page}&size=${size}`);
    return response.data;
  },

  // Process payment
  process: async (data: ProcessPaymentDto) => {
    const response = await api.post<ApiResponse<PaymentTransaction>>("/payments/transactions/process", data);
    return response.data;
  },

  // Retry transaction
  retry: async (id: string) => {
    const response = await api.patch<ApiResponse<void>>(`/payments/transactions/${id}/retry`);
    return response.data;
  },

  // Cancel transaction
  cancel: async (id: string) => {
    const response = await api.patch<ApiResponse<void>>(`/payments/transactions/${id}/cancel`);
    return response.data;
  }
};

// Payment Refunds API
export const paymentRefundApi = {
  // Get all refunds with pagination
  getAll: async (page = 0, size = 10) => {
    const response = await api.get<ApiResponse<{ content: PaymentRefund[]; totalElements: number; totalPages: number }>>(`/payments/refunds?page=${page}&size=${size}`);
    return response.data;
  },

  // Get refund by ID
  getById: async (id: string) => {
    const response = await api.get<ApiResponse<PaymentRefund>>(`/payments/refunds/${id}`);
    return response.data;
  },

  // Get refund by code
  getByCode: async (code: string) => {
    const response = await api.get<ApiResponse<PaymentRefund>>(`/payments/refunds/code/${code}`);
    return response.data;
  },

  // Get refunds by transaction
  getByTransaction: async (transactionId: string, page = 0, size = 10) => {
    const response = await api.get<ApiResponse<{ content: PaymentRefund[]; totalElements: number; totalPages: number }>>(`/payments/refunds/transaction/${transactionId}?page=${page}&size=${size}`);
    return response.data;
  },

  // Get refunds by status
  getByStatus: async (status: PaymentTransactionStatus, page = 0, size = 10) => {
    const response = await api.get<ApiResponse<{ content: PaymentRefund[]; totalElements: number; totalPages: number }>>(`/payments/refunds/status/${status}?page=${page}&size=${size}`);
    return response.data;
  },

  // Create refund
  create: async (data: CreateRefundDto) => {
    const response = await api.post<ApiResponse<PaymentRefund>>("/payments/refunds", data);
    return response.data;
  },

  // Cancel refund
  cancel: async (id: string) => {
    const response = await api.patch<ApiResponse<void>>(`/payments/refunds/${id}/cancel`);
    return response.data;
  }
};

// Payment Audit Logs API
export const paymentAuditLogApi = {
  // Get all audit logs with pagination
  getAll: async (page = 0, size = 10) => {
    const response = await api.get<ApiResponse<{ content: PaymentAuditLog[]; totalElements: number; totalPages: number }>>(`/payments/audit-logs?page=${page}&size=${size}`);
    return response.data;
  },

  // Get audit log by ID
  getById: async (id: string) => {
    const response = await api.get<ApiResponse<PaymentAuditLog>>(`/payments/audit-logs/${id}`);
    return response.data;
  },

  // Get audit logs by payment request
  getByPaymentRequest: async (requestId: string, page = 0, size = 10) => {
    const response = await api.get<ApiResponse<{ content: PaymentAuditLog[]; totalElements: number; totalPages: number }>>(`/payments/audit-logs/request/${requestId}?page=${page}&size=${size}`);
    return response.data;
  },

  // Get audit logs by transaction
  getByTransaction: async (transactionId: string, page = 0, size = 10) => {
    const response = await api.get<ApiResponse<{ content: PaymentAuditLog[]; totalElements: number; totalPages: number }>>(`/payments/audit-logs/transaction/${transactionId}?page=${page}&size=${size}`);
    return response.data;
  },

  // Get audit logs by refund
  getByRefund: async (refundId: string, page = 0, size = 10) => {
    const response = await api.get<ApiResponse<{ content: PaymentAuditLog[]; totalElements: number; totalPages: number }>>(`/payments/audit-logs/refund/${refundId}?page=${page}&size=${size}`);
    return response.data;
  },

  // Get audit logs by user
  getByUser: async (userId: number, page = 0, size = 10) => {
    const response = await api.get<ApiResponse<{ content: PaymentAuditLog[]; totalElements: number; totalPages: number }>>(`/payments/audit-logs/user/${userId}?page=${page}&size=${size}`);
    return response.data;
  },

  // Search audit logs
  search: async (searchTerm: string, page = 0, size = 10) => {
    const response = await api.get<ApiResponse<{ content: PaymentAuditLog[]; totalElements: number; totalPages: number }>>(`/payments/audit-logs/search?searchTerm=${encodeURIComponent(searchTerm)}&page=${page}&size=${size}`);
    return response.data;
  }
};

// Payment Statistics API
export const paymentStatsApi = {
  // Get payment request statistics
  getRequestStats: async () => {
    const response = await api.get<ApiResponse<Record<string, number>>>("/payments/stats/requests");
    return response.data;
  },

  // Get transaction statistics
  getTransactionStats: async () => {
    const response = await api.get<ApiResponse<Record<string, number>>>("/payments/stats/transactions");
    return response.data;
  },

  // Get refund statistics
  getRefundStats: async () => {
    const response = await api.get<ApiResponse<Record<string, number>>>("/payments/stats/refunds");
    return response.data;
  },

  // Get audit log statistics
  getAuditLogStats: async () => {
    const response = await api.get<ApiResponse<Record<string, any>>>("/payments/stats/audit-logs");
    return response.data;
  }
};

// Combined payment API object
export const paymentApi = {
  requests: paymentRequestApi,
  transactions: paymentTransactionApi,
  refunds: paymentRefundApi,
  auditLogs: paymentAuditLogApi,
  stats: paymentStatsApi
};

export default paymentApi;